#![cfg_attr(
  all(not(debug_assertions), target_os = "windows"),
  windows_subsystem = "windows"
)]
use tauri::{Manager, Runtime, Wry, Window};
use tauri_plugin_log::{LogTarget, LoggerBuilder};
use tauri_plugin_store::PluginBuilder;
use tokio::{net::TcpStream, sync::Mutex};
use tokio_tungstenite::{
  connect_async_with_config,
  tungstenite::{
    protocol::{CloseFrame as ProtocolCloseFrame, WebSocketConfig},
    Message,
  },
  MaybeTlsStream, WebSocketStream,
};
use window_shadows::set_shadow;
use window_vibrancy::{
  apply_acrylic, apply_blur, apply_mica, clear_acrylic, clear_blur, clear_mica,
};
use winreg::{enums::HKEY_CURRENT_USER, RegKey};

use flate2::read::ZlibDecoder;
use tauri_plugin_websocket::{ConnectionConfig, ConnectionManager, Id, WebSocketMessage, Error};
fn main() {
  // Logging targets
  let targets = [LogTarget::LogDir, LogTarget::Stdout, LogTarget::Webview];
  // Start building our tauri app
  tauri::Builder::default()
    .setup(|app| {
      // Loading mechanics
      let window = app.get_window("main").unwrap();
      let splashscreen = app.get_window("splashscreen").unwrap();
      #[cfg(any(target_os = "windows", target_os = "macos"))]
      set_shadow(&window, true).unwrap(); // Set shadows
      #[cfg(any(target_os = "windows", target_os = "macos"))]
      set_shadow(&splashscreen, true).unwrap();
      splashscreen.center().unwrap(); // Center splashscreen window
      Ok(())
    })
    .plugin(PluginBuilder::default().build::<Wry>()) // Store plugin
    .plugin(LoggerBuilder::new().targets(targets).build()) // Log Plugin
    .invoke_handler(tauri::generate_handler![
      set_mica,
      close_splashscreen,
      set_blur,
      set_acrylic,
      get_accent_color,
      open_dev_tools
    ])
    .run(tauri::generate_context!())
    .expect("error while running tauri application");
}

fn connect_to_gateway<R: Runtime>(
  window: Window<R>,
  url: String,
  config: Option<ConnectionConfig>,
) -> tauri_plugin_websocket::Result<Id> {
  let zlib = url.contains("?compression=zlib");
  let id = rand::random();
  let (ws_stream, _) =
    tauri::async_runtime::block_on(connect_async_with_config(url, config.map(Into::into)))?;

  tauri::async_runtime::spawn(async move {
    let (write, read) = ws_stream.split();
    let manager = window::state::<ConnectionManager>();
    manager.0.lock().await.insert(id, write);
    read
      .for_each(move |message| {
        let window_ = window.clone();
        async move {
          if let Ok(Message::Close(_)) = message {
            let manager = window_.state::<ConnectionManager>();
            manager.0.lock().await.remove(&id);
          }

          let response = match message {
            Ok(Message::Text(t)) => serde_json::to_value(WebSocketMessage::Text(t)).unwrap(),
            Ok(Message::Binary(t)) => {
              if zlib {
                let mut decoder = ZlibDecoder::new(&t[..]);
                let mut decoded_string = decoder.read_to_string(&mut s);
                serde_json::to_value(WebSocketMessage::Text(decoded_string)).unwrap()
              } else {
                serde_json::to_value(WebSocketMessage::Binary(t)).unwrap()
              }
            }
            Ok(Message::Ping(t)) => serde_json::to_value(WebSocketMessage::Ping(t)).unwrap(),
            Ok(Message::Pong(t)) => serde_json::to_value(WebSocketMessage::Pong(t)).unwrap(),
            Ok(Message::Close(t)) => {
              serde_json::to_value(WebSocketMessage::Close(t.map(|v| CloseFrame {
                code: v.code.into(),
                reason: v.reason.into_owned(),
              })))
              .unwrap()
            }
            Ok(Message::Frame(_)) => serde_json::Value::Null, // This value can't be recieved.
            Err(e) => serde_json::to_value(Error::from(e)).unwrap(),
          };
          let js = format_callback(callback_function, &response)
            .expect("unable to serialize websocket message");
          let _ = window_.eval(js.as_str());
        }
      })
      .await;
  });

  Ok(id)
}
// #[tauri::command]
// fn get_battery_percentage() -> Result<i32, String> {
//   return Ok(30);
// }

// #[tauri::command]
// fn chatty_notification(title: String, body: String, icon: String) {
//   Notification::new("org.feuer.chatty")
//     .title(title)
//     .body(body)
//     .icon(icon)
//     .show();
// }
#[tauri::command]
async fn open_dev_tools(window: tauri::Window) {
  window.get_window("main").unwrap().open_devtools();
}
#[tauri::command]
async fn close_splashscreen(window: tauri::Window) {
  // Close splashscreen
  if let Some(splashscreen) = window.get_window("splashscreen") {
    splashscreen.close().unwrap();
  }
  // Show main window
  window.get_window("main").unwrap().show().unwrap();
}

/**
 * Set mica (Win 11 > *)
 */
#[tauri::command]
async fn set_mica(window: tauri::Window, mica: bool) {
  if mica {
    #[cfg(target_os = "windows")]
    apply_mica(&window).unwrap();
  } else {
    #[cfg(target_os = "windows")]
    clear_mica(&window).unwrap();
  }

  return;
}
/**
 * Set blur (Win 11 < *)
 */
#[tauri::command]
async fn set_blur(window: tauri::Window, blur: bool) {
  if blur {
    #[cfg(target_os = "windows")]
    apply_blur(&window, Some((18, 18, 18, 125)))
      .expect("Unsupported platform! 'apply_blur' is only supported on Windows");
  } else {
    #[cfg(target_os = "windows")]
    clear_blur(&window).unwrap();
  }
}
/**
 * Set acrylic (Win 10+)
 */
#[tauri::command]
async fn set_acrylic(window: tauri::Window, acrylic: bool, r: u8, g: u8, b: u8, opacity: u8) {
  if acrylic {
    #[cfg(target_os = "windows")]
    println!("Applying acrylic effect...");
    apply_acrylic(&window, Some((r, g, b, opacity)))
      .expect("Unsupported platform! 'apply_acrylic' is only supported on Windows");
    return;
  } else {
    println!("Clearing acrylic effect...");
    clear_acrylic(&window).unwrap();
    return;
  }
}
/**
 * Get accent color for system (Win 10+)
 */
#[tauri::command]
fn get_accent_color() -> Result<u32, String> {
  let hklm = RegKey::predef(HKEY_CURRENT_USER);
  let cur_dwm = hklm
    .open_subkey("Software\\Microsoft\\Windows\\DWM")
    .unwrap(); // DWM contains all compositor configs
  let ac: u32 = cur_dwm
    .get_value("ColorizationColor")
    .expect("Could not find accent"); // Source of the accent color for windows wrapped in a u32 encoded hex code
  return Ok(ac);
}

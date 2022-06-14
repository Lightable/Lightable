#![cfg_attr(
  all(not(debug_assertions), target_os = "windows"),
  windows_subsystem = "windows"
)]

// use notify_rust::Notification;

// fn main() {
//   Notification::new()
//       .app_id(&"org.feuer.chatty".into())
//       .summary("Firefox News")
//       .body("This will almost look like a real firefox notification.")
//       .icon("firefox")
//       .show().expect("Fail");
// }
//
use tauri::{Manager, Wry};
use tauri_plugin_store::PluginBuilder;
use window_shadows::set_shadow;
use window_vibrancy::{
  apply_acrylic, apply_blur, apply_mica, apply_vibrancy, clear_acrylic, clear_blur, clear_mica,
};
use winreg::RegKey;
use winreg::enums::HKEY_CURRENT_USER;
fn main() {
  tauri::Builder::default()
    .setup(|app| {
      let window = app.get_window("main").unwrap();
      let splashscreen = app.get_window("splashscreen").unwrap();
      #[cfg(any(target_os = "windows", target_os = "macos"))]
      set_shadow(&window, true).unwrap();
      #[cfg(any(target_os = "windows", target_os = "macos"))]
      set_shadow(&splashscreen, true).unwrap();
      splashscreen.center().unwrap();
      Ok(())
    })
    .plugin(PluginBuilder::default().build::<Wry>())
    .invoke_handler(tauri::generate_handler![
      set_mica,
      close_splashscreen,
      set_blur,
      set_acrylic,
      get_accent_color
    ])
    .run(tauri::generate_context!())
    .expect("error while running tauri application")
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
// #[tauri::command]
// async fn open_dev_tools(window: tauri::Window) {
//   window.get_window("main").unwrap().open_devtools();
// }
#[tauri::command]
async fn close_splashscreen(window: tauri::Window) {
  // Close splashscreen
  if let Some(splashscreen) = window.get_window("splashscreen") {
    splashscreen.close().unwrap();
  }
  // Show main window
  window.get_window("main").unwrap().show().unwrap();
}

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
#[tauri::command]
async fn set_blur(window: tauri::Window, blur: bool) {
  if blur {
    #[cfg(target_os = "windows")]
apply_blur(&window, Some((18, 18, 18, 125))).expect("Unsupported platform! 'apply_blur' is only supported on Windows");
  } else {
    #[cfg(target_os = "windows")]
    clear_blur(&window).unwrap();
  }
}
#[tauri::command]
async fn set_acrylic(window: tauri::Window, acrylic: bool) {
  if acrylic {
    #[cfg(target_os = "windows")]
    apply_acrylic(&window, Some((18, 18, 18, 125)))
      .expect("Unsupported platform! 'apply_acrylic' is only supported on Windows");
  } else {
    clear_acrylic(&window).unwrap();
  }
}
#[tauri::command]
fn get_accent_color(window: tauri::Window) -> Result<u32, String> {
  println!("Getting sys info...");
  let hklm = RegKey::predef(HKEY_CURRENT_USER);
  let cur_dwm = hklm.open_subkey("Software\\Microsoft\\Windows\\DWM").unwrap();
  let ac: u32 = cur_dwm.get_value("AccentColor").expect("Could not find accent");
  return Ok(ac)
}

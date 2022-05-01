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
use tauri::api::notification::Notification;
use tauri::{Manager, Wry};
use tauri_plugin_store::PluginBuilder;
use window_shadows::set_shadow;
use window_vibrancy::{apply_mica, apply_vibrancy, NSVisualEffectMaterial};

fn main() {
  tauri::Builder::default()
    .setup(|app| {
      let window = app.get_window("main").unwrap();
      #[cfg(any(target_os = "windows", target_os = "macos"))]
      set_shadow(&window, true).unwrap();
      Ok(())
    })
    .plugin(PluginBuilder::default().build::<Wry>())
    .invoke_handler(tauri::generate_handler!(chatty_notification))
    .invoke_handler(tauri::generate_handler!(get_battery_percentage))
    .invoke_handler(tauri::generate_handler!(open_dev_tools))
    .run(tauri::generate_context!())
    .expect("error while running tauri application")
}

#[tauri::command]
fn get_battery_percentage() -> Result<i32, String> {
  return Ok(30);
}

#[tauri::command]
fn chatty_notification(title: String, body: String, icon: String) {
  Notification::new("org.feuer.chatty")
    .title(title)
    .body(body)
    .icon(icon)
    .show();
}
#[tauri::command]
async fn open_dev_tools(window: tauri::Window) {
  window.get_window("main").unwrap().open_devtools();
}

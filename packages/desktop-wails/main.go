package main

import (
	"embed"

	"github.com/wailsapp/wails/v2"
	"github.com/wailsapp/wails/v2/pkg/options"
	"github.com/wailsapp/wails/v2/pkg/options/windows"
)

//go:embed frontend/dist
var assets embed.FS

func main() {

	app := NewApp()
	app.PreInit()
	// Create application with options
	err := wails.Run(&options.App{
		Title:     "Lightable Green",
		Width:     1220,
		Height:    700,
		MinWidth:  940,
		MinHeight: 500,
		Frameless: true,
		Assets:    assets,
		OnStartup: app.startup,
		Windows: &windows.Options{
			WebviewIsTransparent: true,
			WindowIsTranslucent:  true,
			TranslucencyType:     windows.TabbedWindow,
		},
		DisableResize: false,
		Bind: []interface{}{
			app,
		},
	})

	if err != nil {
		println("Error:", err)
	}
}

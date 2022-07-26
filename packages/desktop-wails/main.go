package main

import (
	"embed"
	"fmt"
	"red/backend/app"
	"red/backend/client"
	"time"

	"github.com/wailsapp/wails/v2"
	"github.com/wailsapp/wails/v2/pkg/options"
	"github.com/wailsapp/wails/v2/pkg/options/windows"
)

//go:embed frontend/dist
var assets embed.FS
var start = time.Now()
var currentVersion = "0.0.5"

func main() {
	
	app := app.NewApp(currentVersion)
	app.PreInit()
	client := client.NewClient(&app.Ctx, &app.Config, app)
	fmt.Printf("Preinit took approx %v\n", time.Since(start))

	// Create application with options
	err := wails.Run(&options.App{
		Title:     "Lightable Red",
		Width:     1220,
		Height:    700,
		MinWidth:  940,
		MinHeight: 500,
		Frameless: true,
		Assets:    assets,
		OnStartup: app.Startup,
		Windows: &windows.Options{
			WebviewIsTransparent: true,
			WindowIsTranslucent:  true,
			TranslucencyType:     windows.TabbedWindow,
		},
		DisableResize: false,
		Bind: []interface{}{
			app,
			client,
			client.Http,

		},
	})

	if err != nil {
		println("Error:", err)
	}

}

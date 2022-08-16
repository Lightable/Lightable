package main

import (
	"embed"
	"fmt"
	"io"
	"os"
	"path"
	"red/backend/app"
	"red/backend/client"
	"time"

	"github.com/rs/zerolog"
	"github.com/wailsapp/wails/v2"
	"github.com/wailsapp/wails/v2/pkg/options"
	"github.com/wailsapp/wails/v2/pkg/options/windows"
	"gopkg.in/natefinch/lumberjack.v2"
)

//go:embed frontend/dist
var assets embed.FS
var start = time.Now()
var currentVersion = "0.0.25-ALPHA"

func main() {
	logger := Configure()
	app := app.NewApp(logger, currentVersion)
	app.PreInit()
	client := client.NewClient(&app.Ctx, logger, &app.Config, app)
	client.CurrentUser = app.Config.LoggedInUser
	logger.Info().Msg(fmt.Sprintf("Preinit took approx %v", time.Since(start)))
	// Create application with options
	err := wails.Run(&options.App{
		Title:      "Lightable Red",
		Width:      1220,
		Height:     700,
		MinWidth:   940,
		MinHeight:  500,
		Frameless:  true,
		Assets:     assets,
		OnStartup:  app.Startup,
		OnShutdown: app.Shutdown,
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
			client.RelationshipManager,
		},
	})

	if err != nil {
		println("Error:", err)
	}
}

func Configure() *zerolog.Logger {
	dCfgDir, _ := os.UserConfigDir()
	cfgDir := fmt.Sprintf("%v/com.lightable.red/logs", dCfgDir)
	var writers []io.Writer
	writers = append(writers, zerolog.ConsoleWriter{Out: os.Stderr})
	writers = append(writers, newRollingFile(cfgDir, "latest.log"))
	mw := io.MultiWriter(writers...)

	// zerolog.SetGlobalLevel(zerolog.DebugLevel)
	logger := zerolog.New(mw).With().Timestamp().Logger()

	logger.Info().
		Bool("fileLogging", true).
		Bool("jsonLogOutput", true).
		Str("logDirectory", cfgDir).
		Str("fileName", "latest").
		Int("maxSizeMB", 15).
		Int("maxBackups", 0).
		Int("maxAgeInDays", 3000).
		Msg("logging configured")

	return &logger
}

func newRollingFile(dir string, file string) io.Writer {
	return &lumberjack.Logger{
		Filename: path.Join(dir, file),
		MaxSize:  15,
		MaxAge: 1,
	}
}

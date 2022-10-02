package main

import (
	"embed"
	"fmt"
	"io"
	"net/http"
	_ "net/http/pprof"
	"os"
	"path"
	"red/backend/app"
	"red/backend/client"
	"runtime/debug"
	"strings"
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
var currentVersion = "0.3.0"

type FileLoader struct {
	http.Handler
}

func NewFileLoader() *FileLoader {
	return &FileLoader{}
}

func (h *FileLoader) ServeHTTP(res http.ResponseWriter, req *http.Request) {
	var err error
	if req.Method == "POST" {
		body, _ := io.ReadAll(req.Body)
		fmt.Printf("Got body: %v\n", string(body))
	}
	requestedFilename := strings.TrimPrefix(req.URL.Path, "/")
	println("Requesting file:", requestedFilename)
	wd, _ := os.Getwd()
	fileData, err := os.ReadFile(wd + "/" + requestedFilename)
	if err != nil {
		res.WriteHeader(http.StatusBadRequest)
		res.Write([]byte(fmt.Sprintf("Could not load file %s", requestedFilename)))
	}

	res.Write(fileData)
}

func main() {
	debug.SetGCPercent(80)
	go func() {
        fmt.Println(http.ListenAndServe("localhost:6060", nil))
    }()
	logger := Configure()
	app := app.NewApp(logger, currentVersion)
	app.PreInit()
	client := client.NewClient(&app.Ctx, logger, &app.Config, app)
	client.CurrentUser = app.Config.LoggedInUser
	logger.Info().Msg(fmt.Sprintf("Preinit took approx %v", time.Since(start)))
	// Create application with options
	err := wails.Run(&options.App{
		Title:            "Lightable Red",
		Width:            1220,
		Height:           700,
		MinWidth:         940,
		MinHeight:        500,
		Frameless:        false,
		Assets:           assets,
		OnStartup:        app.Startup,
		OnShutdown:       app.Shutdown,
		BackgroundColour: options.NewRGB(16, 16, 20),
		DisableResize:    false,
		Windows: &windows.Options{
			CustomTheme: &windows.ThemeSettings{
				DarkModeTitleBar: 0xD0D0D,
				DarkModeTitleText: 0xFFFFFF,
				DarkModeTitleBarInactive: 0x101014,
				DarkModeTitleTextInactive: 0x8c8c8c,
			},
		},
		Bind: []interface{}{
			app,
			client,
			client.Http,
			client.RelationshipManager,
			client.DeveloperClient,
		},
		AssetsHandler: NewFileLoader(),
		Experimental: &options.Experimental{
			UseCSSDrag: true,
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
		MaxAge:   1,
	}
}

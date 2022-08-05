package app

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"io/ioutil"
	"net/http"
	"os"
	"red/mocks"
	"strconv"
	"time"

	"github.com/rs/zerolog"
	"github.com/wailsapp/wails/v2/pkg/runtime"
)

type App struct {
	Ctx     context.Context
	Logger  *zerolog.Logger
	Config  mocks.AppConfig
	Update  mocks.Update
	Version string
	Dir     string
	start   time.Time
}

// Write counter to override default writer
type WriteCounter struct {
	Total    uint64
	Size     uint64
	Previous uint64
	Start    time.Time
	Logger   *zerolog.Logger
	Ctx      context.Context
}

// NewApp creates a new App application struct
func NewApp(logger *zerolog.Logger, version string) *App {
	dCfgDir, _ := os.UserConfigDir()
	cfgDir := fmt.Sprintf("%v/com.lightable.red", dCfgDir)
	os.MkdirAll(cfgDir, 0755)
	logger.Debug().Str("config", cfgDir).Msg("Created App")
	return &App{
		Version: version,
		Logger:  logger,
		Dir:     cfgDir,
		start:   time.Now(),
	}
}

// Write valid app config
func WriteAppConfig(logger *zerolog.Logger, dir string, config mocks.AppConfig) {
	jsonFile, jsonWriteErr := json.Marshal(config)
	logger.Debug().Str("dir", dir).RawJSON("config", jsonFile).Msg("Writing to config")
	if jsonWriteErr != nil {
		fmt.Printf("Something went wrong when trying to marshal AppConfig: %v\n", jsonWriteErr)
	} else {
		writeErr := ioutil.WriteFile(fmt.Sprintf("%v/.app", dir), jsonFile, 0664)
		if writeErr != nil {
			fmt.Printf("Something went wrong when trying to write AppConfig : %v\n", writeErr)
		}
	}
}

// Run on preinit of wails app to make sure files and config is in order.
func (a *App) PreInit() {
	file, err := ioutil.ReadFile(fmt.Sprintf("%v/.app", a.Dir))
	wd, _ := os.Getwd()
	a.Logger.Info().Str("working", wd).Msg("Running preinit on app...")
	fmt.Printf("Working -> %v\n", wd)
	if err != nil {
		a.Logger.Error().Str("type", "FileNotFound").Msg(fmt.Sprintf("Something went wrong when trying to read `.app` config file.. Making a new one. (Error: %v)", err))
		newAppConfig := mocks.AppConfig{Theme: "Dark", Users: &map[string]*mocks.PrivateUser{}}
		WriteAppConfig(a.Logger, a.Dir, newAppConfig)
		a.Config = newAppConfig
		return
	}
	appConfig := mocks.AppConfig{}
	jsonErr := json.Unmarshal([]byte(file), &appConfig)
	if jsonErr != nil {
		a.Logger.Error().Str("type", "JSONRead").Msg(fmt.Sprintf("%v", jsonErr))
	}
	a.Config = appConfig
}

// startup is called when the app starts. The context is saved
// so we can call the runtime methods
func (a *App) Startup(ctx context.Context) {
	a.Ctx = ctx
	a.Logger.Info().Str("type", "Startup").Msg(fmt.Sprintf("Startup in: %v", time.Since(a.start)))
}

// Shutdown log
func (a *App) Shutdown(ctx context.Context) {
	a.Logger.Info().Str("type", "Shutdown").Msg(fmt.Sprintf("Shutdown was called after app being active for: %v", time.Since(a.start)))
}

// Get the current config on the process
func (a *App) GetConfig() mocks.AppConfig {
	a.Logger.Debug().Str("type", "GetConfig").Msg("A function asked for config")
	return a.Config
}

// Change appconfig theme and then write the updated file
func (a *App) ChangeTheme(theme string) {
	a.Logger.Debug().Str("type", "ChangeTheme").Msg(fmt.Sprintf("Changing theme from: %s, to: %s", a.Config.Theme, theme))
	a.Config.Theme = theme
	WriteAppConfig(a.Logger, a.Dir, a.Config)
}

func (a *App) SetCurrentUser(user mocks.PrivateUser) {
	a.Logger.Debug().Str("type", "SetCurrentUser").Msg(fmt.Sprintf("Current user was changed from: %v, to: %v", a.Config.LoggedInUser, user))
	a.Config.LoggedInUser = &user
	WriteAppConfig(a.Logger, a.Dir, a.Config)
}

func (a *App) HasUser(statement bool) {
	a.Logger.Debug().Str("type", "SetHasUser").Msg(fmt.Sprintf("Has user was changed from: %v, to: %v", a.Config.HasUser, statement))
	a.Config.HasUser = statement
	WriteAppConfig(a.Logger, a.Dir, a.Config)
}

// Set a user in the map
func (a *App) SetUser(id string, user mocks.PrivateUser) {
	(*a.Config.Users)[id] = &user
	WriteAppConfig(a.Logger, a.Dir, a.Config)
}

// Make an empty method to test ipc ms delay
func (c *App) PingDelay() {
	c.Logger.Debug().Str("type", "PingDelayRequest").Msg("Client requested ping delay from IPC")
	return
}

// Get app version
func (a *App) GetVersion() string {
	return a.Version
}

// Restart via starting a new process, releasing to system, and then closing this process
func (a *App) Restart() {
	a.Logger.Debug().Str("type", "RestartRequest").Msg("Client requested application restart")
	procAttr := new(os.ProcAttr)
	procAttr.Files = []*os.File{os.Stdin, os.Stdout, os.Stderr}
	proc, _ := os.StartProcess(os.Args[0], []string{"", ""}, procAttr)
	proc.Release()
	os.Exit(0)
}

// Download update with given URL (Insecure, allows any type/url to be downloaded)
func (c *App) DownloadUpdate(url string) string {
	path, _ := os.Getwd()
	os.Rename(path+"/Lightable Red.exe", path+"/Old.exe")
	out, err := os.Create(path + "/Lightable Red.exe")
	if err != nil {
		out.Close()
		return fmt.Sprintf("%v", err)
	}
	fmt.Printf("New path %v", path+"Lightable Red.exe\n")
	start := time.Now()
	resp, err := http.Get(url)
	if err != nil {
		out.Close()
		return fmt.Sprintf("%v", err)
	}
	defer resp.Body.Close()
	length := resp.Header.Get("Content-Length")
	parsedUint, _ := strconv.ParseUint(length, 10, 64)
	runtime.EventsEmit(c.Ctx, "download:total", length)
	counter := &WriteCounter{
		Ctx:      c.Ctx,
		Size:     parsedUint,
		Start:    start,
		Logger:   c.Logger,
		Previous: 0,
	}
	if _, err = io.Copy(out, io.TeeReader(resp.Body, counter)); err != nil {
		out.Close()
		return fmt.Sprintf("%v", err)
	}

	out.Close()
	return ""
}

// Write controller for percentage tracking
func (wc *WriteCounter) Write(p []byte) (int, error) {
	n := len(p)
	wc.Total += uint64(n)
	if wc.Total > wc.Previous || wc.Total == wc.Size {
		runtime.EventsEmit(wc.Ctx, "download:progress", wc.Total)
		if wc.Total == wc.Size {
			wc.Logger.Info().Str("type", "WriteFinished").Msg(fmt.Sprintf("Write finished in %v", time.Since(wc.Start)))
		}
		wc.Previous = wc.Total + 10000
	}
	return n, nil
}

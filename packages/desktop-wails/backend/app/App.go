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

	"github.com/wailsapp/wails/v2/pkg/runtime"
)

type App struct {
	Ctx     context.Context
	Config  mocks.AppConfig
	Update  mocks.Update
	Version string
	Dir     string
}
// Write counter to override default writer
type WriteCounter struct {
	Total    uint64
	Size     uint64
	Previous uint64
	Ctx      context.Context
}

// NewApp creates a new App application struct
func NewApp(version string) *App {
	dCfgDir, _ := os.UserConfigDir()
	cfgDir := fmt.Sprintf("%v/com.lightable.red", dCfgDir)
	os.MkdirAll(cfgDir, 0755)
	return &App{
		Version: version,
		Dir:     cfgDir,
	}
}

// Write valid app config
func WriteAppConfig(dir string, config mocks.AppConfig) {
	jsonFile, jsonWriteErr := json.Marshal(config)
	if jsonWriteErr != nil {
		fmt.Errorf("\nSomething went wrong when trying to marshal AppConfig: %v", jsonWriteErr)
	} else {
		writeErr := ioutil.WriteFile(fmt.Sprintf("%v/.app", dir), jsonFile, 0664)
		if writeErr != nil {
			fmt.Errorf("\nSomething went wrong when trying to write AppConfig : %v", writeErr)
		}
	}
}

// Run on preinit of wails app to make sure files and config is in order.
func (a *App) PreInit() {
	file, err := ioutil.ReadFile(fmt.Sprintf("%v/.app", a.Dir))
	wd, _ := os.Getwd()
	fmt.Printf("Working -> %v\n", wd)
	if err != nil {
		fmt.Printf("\nSomething went wrong when trying to read `.app` config file.. Making a new one. (Error: %v)\n", err)
		newAppConfig := mocks.AppConfig{Theme: "Dark"}
		WriteAppConfig(a.Dir, newAppConfig)
		a.Config = newAppConfig
		return
	}
	appConfig := mocks.AppConfig{}
	jsonErr := json.Unmarshal([]byte(file), &appConfig)
	if jsonErr != nil {
		fmt.Printf("\nSomething went wrong when trying to parse config: %v", jsonErr)
	}
	a.Config = appConfig
}

// startup is called when the app starts. The context is saved
// so we can call the runtime methods
func (a *App) Startup(ctx context.Context) {
	a.Ctx = ctx
}

// Get the current config on the process
func (a *App) GetConfig() mocks.AppConfig {
	return a.Config
}

// Change appconfig theme and then write the updated file
func (a *App) ChangeTheme(theme string) {
	a.Config.Theme = theme
	WriteAppConfig(a.Dir, a.Config)
}

func (a *App) SetCurrentUser(user mocks.PrivateUser) {
	a.Config.LoggedInUser = user
	WriteAppConfig(a.Dir, a.Config)
}

// Make an empty method to test ipc ms delay
func (c *App) PingDelay() {
	return
}

// Restart via starting a new process, releasing to system, and then closing this process
func (a *App) Restart() {
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
	fmt.Printf("New path %v", path+"Lightable Red.exe\n")
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
		wc.Previous = wc.Total + 10000
	}
	return n, nil
}

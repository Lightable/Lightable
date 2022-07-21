package main

import (
	"context"
	"encoding/json"
	"fmt"
	"io/ioutil"
)

// App struct
type App struct {
	ctx    context.Context
	config AppConfig
}

// NewApp creates a new App application struct
func NewApp() *App {
	return &App{}
}

func (a *App) PreInit() {
	file, err := ioutil.ReadFile(".app")
	if err != nil {
		fmt.Printf("Something went wrong when trying to read `.app` config file.. Making a new one.")
		newAppConfig := AppConfig{Theme: "Dark"}
		WriteAppConfig(newAppConfig)
		a.config = newAppConfig
		return
	}
	appConfig := AppConfig{}
	jsonErr := json.Unmarshal([]byte(file), &appConfig)
	if jsonErr != nil {
		fmt.Printf("\nSomething went wrong when trying to parse config: %v", jsonErr)
	}
	a.config = appConfig
}

func (a *App) ChangeTheme(theme string) {
	a.config.Theme = theme
	WriteAppConfig(a.config)
}

func (a *App) GetConfig() AppConfig {
	return a.config
}

// startup is called when the app starts. The context is saved
// so we can call the runtime methods
func (a *App) startup(ctx context.Context) {
	a.ctx = ctx
}

// Greet returns a greeting for the given name
func (a *App) Greet(name string) string {
	return fmt.Sprintf("Hello %s, It's show time!", name)
}

type AppConfig struct {
	Theme string
}

// Write valid app config
func WriteAppConfig(config AppConfig) {
	jsonFile, jsonWriteErr := json.Marshal(config)
	if jsonWriteErr != nil {
		fmt.Errorf("\nSomething went wrong when trying to marshal AppConfig: %v", jsonWriteErr)
	} else {
		writeErr := ioutil.WriteFile(".app", jsonFile, 0664)
		if writeErr != nil {
			fmt.Errorf("\nSomething went wrong when trying to write AppConfig : %v", writeErr)
		}
	}
}

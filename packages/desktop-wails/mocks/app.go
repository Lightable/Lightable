package mocks

// Config to use
type AppConfig struct {
	Theme string `json:"Theme"`
}

// The (Future) style of an update
type Update struct {
	Author  string `json:"Author"`
	Title   string `json:"Title"`
	URL     string `json:"URL"`
	Version int    `json:"Version"`
}


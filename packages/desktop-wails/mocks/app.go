package mocks

// Config to use
type AppConfig struct {
	Theme        string                   `json:"theme"`
	Responder    *AppResponderConfig      `json:"responder"`
	LoggedInUser *PrivateUser             `json:"currentUser"`
	HasUser      bool                     `json:"hasUser"`
	Users        *map[string]*PrivateUser `json:"users"`
}

type AppResponderConfig struct {
	Gateway string `json:"gateway"`
	API     string `json:"api"`
	Secure  bool   `json:"secure"`
}

// The (Future) style of an update
type Update struct {
	Version string    `json:"version"`
	Title   string `json:"title"`
	Notes   string `json:"notes"`
	Author  string `json:"signature"`
	URL     string `json:"url"`
}

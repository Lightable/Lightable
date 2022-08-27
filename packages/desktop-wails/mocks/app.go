package mocks

// Config to use
type AppConfig struct {
	Theme string `json:"theme"`
	Responder *AppResponderConfig `json:"responder"`
	LoggedInUser *PrivateUser `json:"currentUser"`
	HasUser bool `json:"hasUser"`
	Users *map[string]*PrivateUser `json:"users"`
}

type AppResponderConfig struct {
	Gateway string `json:"gateway"`
	API string `json:"api"`
	Secure bool `json:"secure"`
}

// The (Future) style of an update
type Update struct {
	Author  string `json:"author"`
	Title   string `json:"title"`
	URL     string `json:"url"`
	Version int    `json:"version"`
}


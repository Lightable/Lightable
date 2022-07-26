package mocks

// Config to use
type AppConfig struct {
	Theme string `json:"theme"`
	LoggedInUser PrivateUser `json:"currentUser"`
}

// The (Future) style of an update
type Update struct {
	Author  string `json:"author"`
	Title   string `json:"title"`
	URL     string `json:"url"`
	Version int    `json:"version"`
}


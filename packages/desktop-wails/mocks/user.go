package mocks

type PrivateUser struct {
	Name           string          `json:"name"`
	Id             string          `json:"id"`
	Email          string          `json:"email"`
	Status         *UserStatus     `json:"status"`
	Token          StandardToken   `json:"token"`
	Admin          bool            `json:"admin"`
	Avatar         *UserAvatar     `json:"avatar"`
	ProfileOptions map[string]bool `json:"profileOptions"`
}

type PublicUser struct {
	Name   string      `json:"name"`
	Id     string      `json:"id"`
	Status *UserStatus `json:"status"`
	Admin  bool        `json:"admin"`
	Avatar *UserAvatar `json:"avatar"`
	State  *int16      `json:"state"`
}

type UserStatus struct {
	Icon Icon   `json:"icon"`
	Text string `json:"text"`
}

type Icon struct {
	Cdn      string `json:"cdn"`
	Animated string `json:"animated"`
	Id       string `json:"id"`
}

type StandardToken struct {
	Permissions []string `json:"permissions"`
	Token       string   `json:"token"`
}

type UserAvatar struct {
	Animated bool   `json:"animated"`
	Id       string `json:"id"`
}

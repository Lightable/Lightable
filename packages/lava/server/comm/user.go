package comm

type MinimalUser struct {
	Identifier int64  `json:"identifier"`
	Name       string `json:"name"`
	Token      string `json:"token"`
}
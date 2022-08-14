package mocks

type RelationshipStruct struct {
	Pending  []PublicUser `json:"pending"`
	Requests []PublicUser `json:"requests"`
	Friends  []PublicUser `json:"friends"`
	Empty    bool         `json:"empty"`
}
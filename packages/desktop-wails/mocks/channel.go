package mocks

const (
	// Channel
	DM    = _channelType(0)
	GROUP = _channelType(1)
	SPACE = _channelType(2)
	// Message
	TEXT       = _messageType(0)
	ATTACHMENT = _messageType(1)
	GameStart  = _messageType(2)
	GameEnd    = _messageType(3)
	// Any
	UNKNOWN = -1
)

type _channelType int
type _messageType int

type Channel struct {
	Id       string       `json:"id"`
	Type     _channelType `json:"type"`
	Users    []int64      `json:"users"`
	Owner    *PublicUser  `json:"owner"`
	Messages []Message    `json:"messages"`
}

type Message struct {
	Content *string  `json:"content"`
	System  bool     `json:"system"`
	Type    int      `json:"type"`
	Channel int64    `json:"channel"`
	Author  int64    `json:"author"`
	Created float64  `json:"created"`
	Edited  *float64 `json:"edited"`
}
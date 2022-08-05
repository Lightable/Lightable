package mocks

import (
	"fmt"

	"github.com/asaskevich/EventBus"
	"github.com/gorilla/websocket"
)

/* <------------------ Socket ------------------> */

type Connection struct {
	// WS connection
	Ws *websocket.Conn
	// Buff channel outbound
	Send chan []byte
	// Bus to comm between structs
	Bus EventBus.Bus

	Closed bool
}

func (c *Connection) ReadPump() {
	defer func() {
		c.Bus.Publish("ws:close")
		c.Ws.Close()
	}()

	for {
		_, message, err := c.Ws.ReadMessage()
		if err != nil {
			if websocket.IsUnexpectedCloseError(err, websocket.CloseGoingAway) {
				c.Closed = true
				fmt.Printf("\nUnexpected errror occurred in socket: %v", err)
				c.Bus.Publish("ws:read:close:error", message)
			}
			break
		}
		c.Bus.Publish("ws:read:message", message)
	}
}

func (c *Connection) Write(mt int, payload []byte) error {
	if c.Closed {
		return nil
	}
	return c.Ws.WriteMessage(mt, payload)
}

func (c *Connection) Ping() {
	c.Ws.WriteMessage(websocket.TextMessage, []byte("{\"t\": 2}"))
}

func NewPing() GenericSocketMessage {
	return GenericSocketMessage{T: 2}
}

/* <------------ Message Structs ------------> */

type ClientReadyMessage struct {
	T int
	D ClientReadyPayload
}

type ClientReadyPayload struct {
	Os      string
	Browser string
	Build   string
}

type GenericSocketMessage struct {
	T int `json:"t"`
}

type ServerStartMessage struct {
	T int                `json:"t"`
	D ServerStartPayload `json:"d"`
}

type ServerStartPayload struct {
	User          PublicUser                 `json:"user"`
	Status        UserStatus                 `json:"status"`
	Admin         bool                       `json:"admin"`
	Enabled       bool                       `json:"enabled"`
	State         int                        `json:"state"`
	Avatar        UserAvatar                 `json:"avatar"`
	Id            string                     `json:"id"`
	Relationships ServerRelationshipsPayload `json:"relationships"`
	Meta          ServerMetaPayload          `json:"meta"`
}

type ServerRelationshipsPayload struct {
	Friends  []PublicUser `json:"friends"`
	Pending  []PublicUser `json:"pending"`
	Requests []PublicUser `json:"requests"`
	Empty    bool         `json:"empty"`
}

type ServerMetaPayload struct {
	Production bool `json:"prod"`
}

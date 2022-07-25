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
				fmt.Printf("\nUnexpected errror occured in socket: %v", err)
				c.Bus.Publish("ws:read:close:error", message)
			}
			break
		}
		c.Bus.Publish("ws:read:message", message)
	}
}

func (c *Connection) Write(mt int, payload []byte) error {
	return c.Ws.WriteMessage(mt, payload)
}

func (c *Connection) Ping() {
	c.Ws.WriteMessage(websocket.TextMessage, []byte("{\"t\": 2}"))
}

func NewPing() GenericSocketMessage {
	return GenericSocketMessage{t: 2}
}

	/* <------------ Message Structs ------------> */

	type GenericSocketMessage struct {
		t int
	}

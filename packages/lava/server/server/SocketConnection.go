package server

import (
	"log"
	"time"

	"github.com/gorilla/websocket"
)

type LavaWebSocketConnection struct {
	server *LavaWebSocketServer
	Conn   *websocket.Conn
	send   chan []byte
}

func NewLavaSocketConnection(src *LavaWebSocketServer, conn *websocket.Conn) *LavaWebSocketConnection {
	return &LavaWebSocketConnection{
		server: src,
		Conn:   conn,
		send:   make(chan []byte, 256),
	}
}

func (a *LavaWebSocketConnection) read() {
	defer func() {
		a.server.Unregister <- a
		a.Conn.Close()
	}()

	for {
		_, message, err := a.Conn.ReadMessage()
		if err != nil {
			if websocket.IsUnexpectedCloseError(err, websocket.CloseGoingAway, websocket.CloseAbnormalClosure) {
				log.Printf("RTCP stream panic! Oh noes :( %v\n", err)
			}
			log.Printf("RTCP stream panic! Oh noes :( %v\n", err)
			break
		}
		a.server.Broadcast <- RawLavaClientMessage{Sender: *a, Message: message}
	}
}
func (c *LavaWebSocketConnection) write() {
	for {
		select {
		case message, ok := <-c.send:
			c.Conn.SetWriteDeadline(time.Now().Add(writeWait))
			if !ok {
				// The hub closed the channel.
				c.Conn.WriteMessage(websocket.CloseMessage, []byte{})
				return
			}

			w, err := c.Conn.NextWriter(websocket.TextMessage)
			if err != nil {
				return
			}
			w.Write(message)

			if err := w.Close(); err != nil {
				return
			}
		}
	}
}

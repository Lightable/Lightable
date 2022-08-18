package server

import (
	"bytes"
	"encoding/gob"
	"fmt"
	"log"
	"net/http"
	"time"

	"github.com/brys0/arc/cmd/rtcp"
	"github.com/brys0/arc/cmd/rtp"
	"github.com/brys0/arc/server/comm"
	"github.com/gorilla/websocket"
)

const (
	// Time allowed to write a message to the peer.
	writeWait = 10 * time.Second
)

var (
	newline = []byte{'\n'}
	space   = []byte{' '}
)

type LavaServer struct {
	Version string
}

type IncomingLavaConnection struct {
	RTCP *LavaWebSocketConnection
}

type OutgoingLavaConnection struct {
	RTCP *LavaWebSocketConnection
}



type LavaWebSocketServer struct {
	//@go-ignore
	Clients    []*LavaWebSocketConnection
	Broadcast  chan RawLavaClientMessage
	Register   chan *LavaWebSocketConnection
	Unregister chan *LavaWebSocketConnection
}

type RawLavaClientMessage struct {
	Sender  LavaWebSocketConnection
	Message []byte
}


var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool {
		return true
	},
	ReadBufferSize:  4096,
	WriteBufferSize: 4096,
}

func WS(s LavaWebSocketServer, w http.ResponseWriter, r *http.Request) {
	c, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Printf("Upgrade : %v", err)
		return
	}
	client := NewLavaSocketConnection(&s, c)
	s.Clients = append(s.Clients, client)
	s.Register <- client
	go client.read()
	go client.write()
}
type TestMessageMeme struct {
	Type int
	Data rtcp.RTCPPacket
}
func StartServer() LavaWebSocketServer {
	commClient := comm.NewCommunicationClient("http://192.168.50.111:8081")
	commClient.GetAllMinimalUsers()
	server := LavaWebSocketServer{
		Clients:    make([]*LavaWebSocketConnection, 0),
		Register:   make(chan *LavaWebSocketConnection),
		Unregister: make(chan *LavaWebSocketConnection),
		Broadcast:  make(chan RawLavaClientMessage),
	}
	go func() {
		for {
			select {
			case client := <-server.Register:
				log.Printf("Got client : %v\n", client)
				packet := CreateValidPayload(0, nil)
				bytes, err := packet.EncodeToJSON()
				if err != nil {
					log.Panic(err)
					return
				}
				client.send <- bytes
			case msg := <-server.Broadcast:
				var buf bytes.Buffer
				enGob := gob.NewEncoder(&buf)
				enGob.Encode(TestMessageMeme{Type: 0, Data: rtcp.RTCPPacket{
					Version: 0,
					ID:      `urmum`,
					Stats: rtp.RTPStats{
						RTT: 120,
					},
				}})
				converted := TestMessageMeme{}
				dGob := gob.NewDecoder(bytes.NewReader(buf.Bytes()))
				dGob.Decode(&converted)
				fmt.Printf("Cringe %v", converted.Data)
				msg.Sender.send <- buf.Bytes()
			case unReg := <-server.Unregister:
				log.Printf("Client unreg : %v\n", unReg)
			}
		}

	}()
	return server
}


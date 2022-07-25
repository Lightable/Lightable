package client

import (
	"bytes"
	"compress/zlib"
	"context"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"red/mocks"
	"time"

	"github.com/asaskevich/EventBus"
	"github.com/gorilla/websocket"
	"github.com/wailsapp/wails/v2/pkg/runtime"
)

type Client struct {
	Config        *mocks.AppConfig
	Api           string
	Connection    *mocks.Connection
	Ctx           *context.Context
	Http          *HttpClient
	SocketHistory []string
}

/* Client */
func NewClient(ctx *context.Context, config *mocks.AppConfig) *Client {
	client := Client{
		Config: config,
		Ctx:    ctx,
	}
	client.Http = CreateHTTP(&client)
	return &client
}

/* Socket */
func (c *Client) DialSocket() string {
	bus := EventBus.New()
	if c.Api == "" {
		fmt.Printf("\nInvalid Host\n")
		return "Invalid Host"
	}
	u := url.URL{Scheme: "wss", Host: c.Api, Path: "/ws", RawQuery: "compression=zlib"}

	wsHeaders := http.Header{
		"Origin": {u.Host},
	}
	cha, _, err := websocket.DefaultDialer.Dial(u.String(), wsHeaders)
	if err != nil {
		runtime.LogErrorf(*c.Ctx, "Dial failed: %v\n", err)
		return fmt.Sprintf("Dial Failed: %v\n", err)
	}
	runtime.LogInfof(*c.Ctx, "Connecting to %s\n", u.String())

	c.Connection = &mocks.Connection{
		Send: make(chan []byte, 256),
		Ws:   cha,
		Bus:  bus,
	}

	go c.Connection.ReadPump()
	ticker := time.NewTicker(30000 * time.Millisecond)
	go func() {
		for range ticker.C {
			c.Connection.Ping()
		}
	}()
	bus.Subscribe("ws:read:message", c.ReadAndRespond)
	runtime.LogInfof(*c.Ctx, "New connection established LOCAL=%v REMOTE=%v\n", c.Connection.Ws.LocalAddr().String(), u.String())
	return "All is good :)"
}

func (c *Client) ReadAndRespond(m []byte) {
	decoded := DecodeMessage(m)
	if len(c.SocketHistory) > 50 {
		c.SocketHistory = c.SocketHistory[:len(c.SocketHistory)-1]
	}
	c.SocketHistory = append(c.SocketHistory, decoded)

	fmt.Printf("\nDecoded message: %v\n", decoded)
	runtime.EventsEmit(*c.Ctx, "ws:read:decode", decoded)

}
func (c *Client) GetSocketHistory() []string {
	return c.SocketHistory
}

func DecodeMessage(m []byte) string {
	var out bytes.Buffer
	r, err := zlib.NewReader(bytes.NewBuffer(m))
	if err != nil {
		fmt.Printf("\nSomething went wrong when trying to decode ZLIB stream: %v\n", err)
	}
	io.Copy(&out, r)
	return string(out.Bytes()[:])
}

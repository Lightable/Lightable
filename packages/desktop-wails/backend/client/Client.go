package client

import (
	"bytes"
	"compress/zlib"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"red/backend/app"
	"red/mocks"
	r "runtime"
	"time"

	"github.com/asaskevich/EventBus"
	"github.com/gorilla/websocket"
	"github.com/rs/zerolog"
	"github.com/wailsapp/wails/v2/pkg/runtime"
)

type Client struct {
	Config        *mocks.AppConfig
	Logger        *zerolog.Logger
	CurrentUser   *mocks.PrivateUser
	Api           string
	Connection    *mocks.Connection
	Ctx           *context.Context
	Http          *HttpClient
	SocketHistory []string
}

/* Client */
func NewClient(ctx *context.Context, logger *zerolog.Logger, config *mocks.AppConfig, a *app.App) *Client {
	client := Client{
		Config: config,
		Logger: logger,
		Ctx:    ctx,
	}
	client.Http = CreateHTTP(&client, a)
	return &client
}

func (c *Client) SetSocket(socket string) {
	c.Api = socket
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
		fmt.Printf("Dial failed: %v\n", err)
		return fmt.Sprintf("Dial Failed: %v\n", err)
	}
	fmt.Printf("Connecting to %s\n", u.String())

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
	fmt.Printf("New connection established LOCAL=%v REMOTE=%v\n", c.Connection.Ws.LocalAddr().String(), u.String())
	return "All is good :)"
}

func (c *Client) ReadAndRespond(m []byte) {
	tParse := mocks.GenericSocketMessage{}
	err := json.Unmarshal(m, &tParse)
	if err != nil {
		c.Logger.Info().Str("err", fmt.Sprint(err)).Msg("Error occurred when trying to read socket generic message")
		runtime.EventsEmit(*c.Ctx, "ws:read:error", err)
		return
	}
	switch tParse.T {
	case 11:
		cData := mocks.ServerStartMessage{}
		err := json.Unmarshal(m, &tParse)
		if err != nil {
			c.Logger.Info().Str("err", fmt.Sprint(err)).Msg("Error occurred when trying to read server ready message")
			runtime.EventsEmit(*c.Ctx, "ws:read:error", err)
			return
		}
		runtime.EventsEmit(*c.Ctx, "ws:read:server|ready", cData.D)
		c.Logger.Info().Msg(fmt.Sprintf("Server ready with client: %v", cData.D.Id))
	}
	decoded := DecodeMessage(m)
	if len(c.SocketHistory) > 50 {
		c.SocketHistory = c.SocketHistory[:len(c.SocketHistory)-1]
	}
	c.SocketHistory = append(c.SocketHistory, decoded)

	fmt.Printf("Decoded message: %v\n", decoded)
	runtime.EventsEmit(*c.Ctx, "ws:read:decode", decoded)
}

func (c *Client) LoginToSocket() {
	SendMessage(c, mocks.ClientReadyMessage{
		T: 0,
		D: mocks.ClientReadyPayload{
			Os: r.GOOS,
			Browser: "Desktop",
			Build: "1.9.2",
		},
	})
}

func SendMessage[T any](c *Client, u T) {
	b, err := json.Marshal(u)
	if err != nil {
		c.Logger.Err(err)
	}
	c.Connection.Write(websocket.TextMessage, b)
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

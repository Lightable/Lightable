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
	Config              *mocks.AppConfig
	Logger              *zerolog.Logger
	CurrentUser         *mocks.PrivateUser
	Api                 string
	Connection          *mocks.Connection
	Ctx                 *context.Context
	Http                *HttpClient
	RelationshipManager *RelationshipManager
	SocketHistory       []string
	SocketTicker        *time.Ticker
}

/* Client */
func NewClient(ctx *context.Context, logger *zerolog.Logger, config *mocks.AppConfig, a *app.App) *Client {
	client := Client{
		Config: config,
		Logger: logger,
		Ctx:    ctx,
	}
	client.Http = CreateHTTP(&client, a)
	client.RelationshipManager = NewRelationshipManager(client.Http, &client)
	return &client
}

func (c *Client) SetSocket(socket string) {
	c.Api = socket
}

/* Socket */
func (c *Client) DialSocket() (*string, error) {
	start := time.Now()
	bus := EventBus.New()
	if c.Connection != nil {
		c.Logger.Info().Msg("Socket was already connected. Closing current and connecting a new one.")
		c.Connection.Ws.Close()
		c.Connection = nil
	}
	if c.Api == "" {
		fmt.Printf("\nInvalid Host\n")
		return nil, fmt.Errorf("Invalid Host")
	}
	u := url.URL{Scheme: "ws", Host: c.Api, Path: "/ws", RawQuery: "compression=zlib"}
	if c.Http.Secure {
		u.Scheme = "wss"
	}

	wsHeaders := http.Header{
		"Origin": {u.Host},
	}
	cha, _, err := websocket.DefaultDialer.Dial(u.String(), wsHeaders)
	if err != nil {
		fmt.Printf("Dial failed: %v\n", err)
		return nil, fmt.Errorf("Dial Failed: %v\n", err)
	}
	fmt.Printf("Connecting to %s\n", u.String())

	c.Connection = &mocks.Connection{
		Send: make(chan []byte, 256),
		Ws:   cha,
		Bus:  bus,
	}

	go c.Connection.ReadPump()
	if c.SocketTicker == nil {
		c.SocketTicker = time.NewTicker(30000 * time.Millisecond)
		go func() {
			for range c.SocketTicker.C {
				if !c.Connection.Closed {
					c.Connection.Ping()
				}
			}
		}()
	}
	bus.Subscribe("ws:read:message", c.ReadAndRespond)
	fmt.Printf("New connection established LOCAL=%v REMOTE=%v\n", c.Connection.Ws.LocalAddr().String(), u.String())
	status := fmt.Sprintf("Connected, took %v to connect", time.Since(start))
	return &status, nil
}

func (c *Client) ReadAndRespond(m []byte) {
	decoded := DecodeMessage(m)
	tParse := mocks.GenericSocketMessage{}
	err := json.Unmarshal(decoded, &tParse)
	if err != nil {
		c.Logger.Info().Str("err", fmt.Sprint(err)).Msg("Error occurred when trying to read socket generic message")
		runtime.EventsEmit(*c.Ctx, "ws:read:error", err)
		return
	}
	switch tParse.T {
	case 1:
		cData := mocks.ServerReadyMessage{}
		err := json.Unmarshal(decoded, &cData)
		if err != nil {
			c.Logger.Info().Str("err", fmt.Sprint(err)).Msg("Error occured when trying to read server ready message")
			runtime.EventsEmit(*c.Ctx, "ws:read:error", err)
			return
		}
		runtime.EventsEmit(*c.Ctx, "ws:read:server|ready", cData.D)
		c.Logger.Info().Msg(fmt.Sprintf("Server ready with heartbeat: %v", cData.D.Interval))
	case 11:
		cData := mocks.ServerStartMessage{}
		err := json.Unmarshal(decoded, &cData)
		if err != nil {
			c.Logger.Info().Str("err", fmt.Sprint(err)).Msg("Error occurred when trying to read server start message")
			runtime.EventsEmit(*c.Ctx, "ws:read:error", err)
			return
		}
		d := cData.D
		fmt.Printf("Req: %v\n", d)
		if !d.Relationships.Empty {
			relation := d.Relationships
			for f := 0; f < len(relation.Friends); f++ {
				c.RelationshipManager.InternalAddRelation("friend", relation.Friends[f])
			}
			for p := 0; p < len(relation.Pending); p++ {
				c.RelationshipManager.InternalAddRelation("pending", relation.Pending[p])
			}
			for r := 0; r < len(relation.Requests); r++ {
				c.RelationshipManager.InternalAddRelation("request", relation.Requests[r])
			}
		}
		runtime.EventsEmit(*c.Ctx, "ws:read:server|start", cData.D)
		c.Logger.Info().Msg(fmt.Sprintf("Server start with client: %v", cData.D.User.Id))
	}

	if len(c.SocketHistory) > 50 {
		c.SocketHistory = c.SocketHistory[:len(c.SocketHistory)-1]
	}
	c.SocketHistory = append(c.SocketHistory, string(decoded))

	fmt.Printf("Decoded message: %v\n", string(decoded))
	runtime.EventsEmit(*c.Ctx, "ws:read:decode", string(decoded))
}

func (c *Client) LoginToSocket() {
	fmt.Printf("Curr User %v", c.CurrentUser)
	SendMessage(c, mocks.ClientReadyMessage{
		T: 0,
		D: mocks.ClientReadyPayload{
			Auth: c.CurrentUser.Token.Token,
			Properties: mocks.ClientReadyPropertiesPayload{
				Os:      r.GOOS,
				Browser: "Desktop",
				Build:   "1.9.2",
			},
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

func DecodeMessage(m []byte) []byte {
	var out bytes.Buffer
	r, err := zlib.NewReader(bytes.NewBuffer(m))
	if err != nil {
		fmt.Printf("\nSomething went wrong when trying to decode ZLIB stream: %v\n", err)
	}
	io.Copy(&out, r)
	return out.Bytes()[:]
}

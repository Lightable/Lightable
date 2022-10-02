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
	"os"
	p "path/filepath"
	"red/backend"
	"red/backend/app"
	"red/mocks"
	r "runtime"
	"strconv"
	"time"

	"github.com/asaskevich/EventBus"
	"github.com/gorilla/websocket"
	"github.com/rs/zerolog"
	"github.com/wailsapp/wails/v2/pkg/runtime"
	"golang.org/x/exp/maps"
)

type Client struct {
	Config              *mocks.AppConfig
	Logger              *zerolog.Logger
	CurrentUser         *mocks.PrivateUser
	Api                 string
	Connection          *mocks.Connection
	Location            *mocks.GeoLocation
	Ctx                 *context.Context
	Update              *mocks.Update
	Http                *HttpClient
	App                 *app.App
	RelationshipManager *RelationshipManager
	ChannelManager      *ChannelManager
	DeveloperClient     *DeveloperClient
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
	if config.Responder != nil {
		client.Http.Api = config.Responder.API
		client.Api = config.Responder.Gateway
		client.Http.Secure = config.Responder.Secure
	}
	client.RelationshipManager = NewRelationshipManager(client.Http, &client)
	client.DeveloperClient = NewDeveloperClient(&a.Ctx, *client.Http)
	client.App = a
	client.ChannelManager = NewChannelManager(&client, client.Http, client.RelationshipManager)
	go client.getLocation()
	return &client
}

func (c *Client) SetSocket(socket string) {
	c.Api = socket
	if c.Config.Responder != nil {
		config := *c.Config
		config.Responder.Gateway = socket
		c.Http.App.SaveConfig()
	}
}

func (c *Client) GetUser() *mocks.PrivateUser {
	return c.CurrentUser
}

/* Socket */
func (c *Client) DialSocket() (*string, error) {
	start := time.Now()
	bus := EventBus.New()
	if c.Connection != nil && !c.Connection.Closed {
		c.Logger.Info().Msg("Socket was already connected. Closing current and connecting a new one.")
		c.Connection.GracefulClose()
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
				if c.Connection != nil && !c.Connection.Closed {
					c.Connection.Ping()
				}
			}
		}()
	}
	bus.Subscribe("ws:read:close:error", func(message []byte) {
		runtime.EventsEmit(*c.Ctx, "ws:close", string(message))
	})
	bus.Subscribe("ws:read:message", c.ReadAndRespond)
	fmt.Printf("New connection established LOCAL=%v REMOTE=%v\n", c.Connection.Ws.LocalAddr().String(), u.String())
	status := fmt.Sprintf("Connected, took %v to connect", time.Since(start))
	return &status, nil
}

func (c *Client) ReadAndRespond(m []byte) {
	decoded := decodeMessage(m)
	fmt.Printf("Decoded message: %v\n", string(decoded))
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
		c.CurrentUser = &d.User
		c.RelationshipManager.ClearRelations()
		if !d.Relationships.Empty {
			relation := d.Relationships
			for f := 0; f < len(relation.Friends); f++ {
				c.RelationshipManager.internalAddRelation("friend", relation.Friends[f])
			}
			for p := 0; p < len(relation.Pending); p++ {
				c.RelationshipManager.internalAddRelation("pending", relation.Pending[p])
			}
			for r := 0; r < len(relation.Requests); r++ {
				c.RelationshipManager.internalAddRelation("request", relation.Requests[r])
			}
		}
		runtime.EventsEmit(*c.Ctx, "ws:read:server|start", cData.D)
		c.Logger.Info().Msg(fmt.Sprintf("Server start with client: %v", cData.D.User.Id))
	case 16:
		cData := mocks.FriendRequestAddMessage{}
		err := json.Unmarshal(decoded, &cData)
		if err != nil {
			c.Logger.Info().Str("err", fmt.Sprint(err)).Msg("Error occurred when trying to read new pending request")
			runtime.EventsEmit(*c.Ctx, "ws:read:error", err)
			return
		}
		c.RelationshipManager.internalAddRelation("pending", cData.D)
		runtime.EventsEmit(*c.Ctx, "ws:read:server|pending", cData.D)
	case 24:
		uData := mocks.UserStatusUpdateMessage{}
		err := json.Unmarshal(decoded, &uData)
		if err != nil {
			c.Logger.Info().Str("err", fmt.Sprint(err)).Msg("Error occurred when trying to read user status update message")
			runtime.EventsEmit(*c.Ctx, "ws:read:error", err)
			return
		}
		d := uData.D
		m, u, i, err := c.RelationshipManager.findRelationWithId(d.Id)
		nU := c.RelationshipManager.updateUserWithMessageData(*u, d)
		if err != nil {
			c.Logger.Info().Str("err", fmt.Sprint(err)).Msg("Error occurred when trying to find user for update message")
			runtime.EventsEmit(*c.Ctx, "ws:read:error", err)
			return
		}
		(*m)[*i] = nU
		runtime.EventsEmit(*c.Ctx, "ws:read:user|status", d)
	case 25:
		uData := mocks.ServerMessage{}
		err := json.Unmarshal(decoded, &uData)
		if err != nil {
			c.Logger.Info().Str("err", fmt.Sprint(err)).Msg("Error occurred when trying to read server dm message")
			runtime.EventsEmit(*c.Ctx, "ws:read:error", err)
			return
		}
		c.ChannelManager.addMessageToChannel(uData.D.Channel, &uData.D.Message)
		runtime.EventsEmit(*c.Ctx, "ws:read:server|dm-message", uData.D)
	case 35:
		uData := mocks.ServerUpdateMessage{}
		err := json.Unmarshal(decoded, &uData)
		if err != nil {
			c.Logger.Info().Str("err", fmt.Sprint(err)).Msg("Error occurred when trying to read server update message")
			runtime.EventsEmit(*c.Ctx, "ws:read:error", err)
			return
		}
		d := uData.D
		c.Update = &d
		runtime.EventsEmit(*c.Ctx, "ws:read:server|update", d)
	}

	if len(c.SocketHistory) > 50 {
		c.SocketHistory = c.SocketHistory[:len(c.SocketHistory)-1]
	}
	c.SocketHistory = append(c.SocketHistory, string(decoded))

	runtime.EventsEmit(*c.Ctx, "ws:read:decode", string(decoded))
}

func (c *Client) GetUpdate() *mocks.Update {
	return c.Update
}

func (c *Client) GetAvatar(id string, size int) string {
	parse, err := strconv.ParseInt(id, 10, 64)
	if err != nil {
		panic(err)
	}
	u := c.RelationshipManager.FindRelation(parse)
	if u == nil {
		return ""
	}
	if u.Avatar != nil {
		return c.GetHttpURL() + "/cdn/user/" + u.Id + "/avatars/avatar_" + u.Avatar.Id + "?size=" + fmt.Sprint(size)
	}
	return ""
}
func (c *Client) GetSelfAvatar(size int) string {
	if c.CurrentUser == nil {
		return ""
	} 
	if c.CurrentUser.Avatar != nil {
		return c.GetHttpURL() + "/cdn/user/" + c.CurrentUser.Id + "/avatars/avatar_" + c.CurrentUser.Avatar.Id + "?size=" + fmt.Sprint(size)
	}
	return ""
}
func (c *Client) GetAvatarRaw(uid string, aid string, size int) string {
	return c.GetHttpURL() + "/cdn/user/" + uid + "/avatars/avatar_" + aid + "?size=" + fmt.Sprint(size)
}

func (c *Client) OpenAvatarPickDialog() *string {
	c.Logger.Info().Msg("Opened avatar picker")
	path, err := runtime.OpenFileDialog(*c.Ctx, runtime.OpenDialogOptions{
		Title: "Select your avatar",
		Filters: []runtime.FileFilter{
			{
				DisplayName: "Avatar Formats",
				Pattern:     "*.jpg;*.jpeg;*.png;*.webp;*.gif",
			},
		},
	})
	if err != nil {
		c.Logger.Err(err)
		return nil
	}
	if path == "" {
		return nil
	}
	wd, _ := os.Getwd()
	basePath := p.Base(path)
	backend.CopyFile(path, wd+"/"+basePath)
	return &basePath
}
func (c *Client) GetHttpURL() string {
	url := url.URL{Host: c.Api}
	if c.Http.Secure {
		url.Scheme = "https"
	} else {
		url.Scheme = "http"
	}
	return url.String()
}

func (c *Client) GetLocation() *mocks.GeoLocation {
	return c.Location
}

func (c *Client) GetUsers() []*mocks.PrivateUser {
	mape := *c.App.GetConfig().Users
	v := maps.Values(mape)
	return v
}

func (client *Client) getLocation() {
		location, err := client.Http.GetLocation()
		if err != nil {
			fmt.Printf("Something went wrong when trying to get location: %v\n", err)
		}
		fmt.Printf("Got location: %v", location)
		client.Location = location
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
				Geo:     *c.Location,
			},
		},
	})
}

func (c *Client) ReconnectToSocket() {
	c.DialSocket()
}

func (c *Client) SendTyping(channel string) {
	SendMessage(c, mocks.ClientTypingMessage{
		T: 4,
		D: channel,
	})
}

func (c *Client) ChangeUser(u mocks.PrivateUser) {
	c.CurrentUser = &u
	c.App.Config.LoggedInUser = c.CurrentUser
	c.RelationshipManager.clearRelations()
	c.DialSocket()
	c.getLocation()
	c.App.SaveConfig()
	runtime.EventsEmit(*c.Ctx, "ws:reconnect", c.CurrentUser)
}

func (c *Client) RemoveUser(u mocks.PrivateUser) {
	if (u.Id == c.App.GetConfig().LoggedInUser.Id) {
		c.App.Config.LoggedInUser = nil
		c.CurrentUser = nil;
	}
	delete(*c.App.GetConfig().Users, u.Id)
	c.App.SaveConfig()
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

func decodeMessage(m []byte) []byte {
	var out bytes.Buffer
	r, err := zlib.NewReader(bytes.NewBuffer(m))
	if err != nil {
		fmt.Printf("\nSomething went wrong when trying to decode ZLIB stream: %v\n", err)
	}
	io.Copy(&out, r)
	return out.Bytes()[:]
}

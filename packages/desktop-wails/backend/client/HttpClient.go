package client

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"red/backend/app"
	"time"

	"github.com/wailsapp/wails/v2/pkg/runtime"
)

type HttpClient struct {
	Client *Client
	App    *app.App
	Http   http.Client
	Secure bool
	Api    string
}

func CreateHTTP(client *Client, app *app.App) *HttpClient {
	return &HttpClient{Client: client, App: app, Http: http.Client{Timeout: time.Duration(25) * time.Second}}
}

/* Methods */

func (h *HttpClient) SetAPI(api string) {
	h.Api = api
}

func (h *HttpClient) SetSecure(secure bool) {
	h.Secure = secure
}

/* Commands */

func (h *HttpClient) RegisterEmail(email string) HttpResponse {
	u := h.CreateURL("/invite/register")
	json := bytes.NewBuffer([]byte(fmt.Sprintf(`{"email": "%s"}`, email)))
	resp, err := h.Http.Post(u.String(), "application/json", json)
	if err != nil {
		runtime.LogErrorf(*h.Client.Ctx, "Error occured when attempting to register email: %s\n", err)
		return HttpResponse{
			Err: fmt.Sprintf("%v", err),
		}
	}
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)

	if err != nil {
		runtime.LogErrorf(*h.Client.Ctx, "Error occured when trying to read body: %s\n", err)
		return HttpResponse{
			Err: fmt.Sprintf("%v", err),
		}
	}
	return HttpResponse{
		Status: resp.StatusCode,
		Json:   string(body),
		Err:    "",
	}
}

func (h *HttpClient) RegisterUser(username string, email string, password string, code string) HttpResponse {
	u := h.CreateURL("/user")
	resJson := bytes.NewBuffer([]byte(fmt.Sprintf(`{"username": "%s", "email": "%s", "password": "%s", "code": "%s"}`, username, email, password, code)))
	resp, err := h.Http.Post(u.String(), "application/json", resJson)
	if err != nil {
		runtime.LogErrorf(*h.Client.Ctx, "Error occured when attempting to register user: %s\n", err)
		return HttpResponse{
			Err: fmt.Sprintf("%v", err),
		}
	}
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		runtime.LogErrorf(*h.Client.Ctx, "Error occured when trying to read body: %s\n", err)
		return HttpResponse{
			Err: fmt.Sprintf("%v", err),
		}
	}
	if resp.StatusCode != 201 {
		return HttpResponse{
			Status: resp.StatusCode,
			Json:   string(body),
			Err:    "Error occured when registering user",
		}
	}
	userRegister := UserRegister{}
	jsonErr := json.Unmarshal(body, &userRegister)
	if jsonErr != nil {
		runtime.LogErrorf(*h.Client.Ctx, "Error occured when trying to parse body for registering user: %s\n", err)
		return HttpResponse{
			Status: 520,
			Err:    "JSON parse error",
		}
	}
	return HttpResponse{
		Status: resp.StatusCode,
		Json:   string(body),
	}
}

/* Utils */

func (h *HttpClient) CreateURL(path string) url.URL {
	u := url.URL{Scheme: "http", Host: h.Api, Path: path}
	if h.Secure {
		u.Scheme = "https"
	}
	return u
}

type HttpResponse struct {
	Status int    `json:"status"`
	Json   string `json:"Json"`
	Err    string `json:"Err"`
}

type UserRegister struct {
	Token string `json:"token"`
}

type User struct {
}

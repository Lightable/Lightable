package client

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"net/http"
	"net/url"
	"red/backend/app"
	user "red/mocks"
	"time"
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
		fmt.Printf("Error occurred when attempting to register email: %s\n", err)
		return HttpResponse{
			Err: fmt.Sprintf("%v", err),
		}
	}
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)

	if err != nil {
		fmt.Printf("Error occurred when trying to read body: %s\n", err)
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
		fmt.Printf("Error occurred when attempting to register user: %s\n", err)
		return HttpResponse{
			Err: fmt.Sprintf("%v", err),
		}
	}
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		fmt.Printf("Error occurred when trying to read body: %s\n", err)
		return HttpResponse{
			Err: fmt.Sprintf("%v", err),
		}
	}
	if resp.StatusCode != 201 {
		return HttpResponse{
			Status: resp.StatusCode,
			Json:   string(body),
			Err:    "Error occurred when registering user",
		}
	}
	userRegister := UserRegister{}
	jsonErr := json.Unmarshal(body, &userRegister)
	if jsonErr != nil {
		fmt.Printf("Error occurred when trying to parse body for registering user: %s\n", err)
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

func (h *HttpClient) LoginWithToken(token string) (*user.PrivateUser, error) {
	u := h.CreateURL("/user/@me")
	req, err := h.CreateGetRequestWithAuthorization(u, token)
	if err != nil {
		return nil, err
	}
	resp, err := h.Http.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if resp.StatusCode == 200 {
		if err != nil {
			return nil, err
		}
		privUser := &user.PrivateUser{}
		jsonErr := json.Unmarshal(body, privUser)
		if jsonErr != nil {
			return nil, err
		}
		h.RegisterLoginWithClient(privUser)
		return privUser, nil
	}
	return nil, fmt.Errorf("%v (%v)", string(body), resp.StatusCode)
}

func (h *HttpClient) LoginWithEmailAndPassword(email string, password string) (*user.PrivateUser, error) {
	u := h.CreateURL("/user/@me/login")
	resJson := bytes.NewBuffer([]byte(fmt.Sprintf(`{"email": "%v", "password": "%v"}`, email, password)))
	resp, err := h.Http.Post(u.String(), "application/json", resJson)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if resp.StatusCode == 200 {
		privUser := &user.PrivateUser{}
		jsonErr := json.Unmarshal(body, privUser)
		if jsonErr != nil {
			return nil, err
		}
		h.RegisterLoginWithClient(privUser)
		return privUser, nil
	}

	return nil, fmt.Errorf("%v (%v)", string(body), resp.StatusCode)
}

func (h *HttpClient) AddFriend(name string) (*user.PublicUser, error) {
	u := h.CreateURL(fmt.Sprintf("/user/@me/relationships/%s", name))
	post, err := h.CreatePostRequestWithAuthorization(u)
	if err != nil {
		h.Client.Logger.Error().Str("err", fmt.Sprint(err)).Msg("Something went wrong when trying to create post request for add friend")
		return nil, err
	}
	resp, err := h.Http.Do(post)
	if err != nil {
		h.Client.Logger.Error().Str("err", fmt.Sprint(err)).Msg("Something went wrong when trying to run post request for add friend")
		return nil, err
	}
	defer dclose(resp.Body)
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		h.Client.Logger.Error().Str("err", fmt.Sprint(err)).Msg("Something went wrong when serializing body in addFriend")
		return nil, err
	}
	code := resp.StatusCode
	if code == 201 {
		pubUser := &user.PublicUser{}
		jsonErr := json.Unmarshal(body, pubUser)
		if jsonErr != nil {
			return nil, jsonErr
		}
		return pubUser, nil
	} else if code != 201 {
		failImpl := &GenericFail{}
		jsonErr := json.Unmarshal(body, failImpl)
		if jsonErr != nil {
			return nil, jsonErr
		}
		return nil, fmt.Errorf("%s (%s)", failImpl.Message, failImpl.Code)
	}
	return nil, nil
}

func (h *HttpClient) RegisterLoginWithClient(u *user.PrivateUser) {
	h.Client.CurrentUser = u
}

/* Utils */

func (h *HttpClient) CreateURL(path string) url.URL {
	u := url.URL{Scheme: "http", Host: h.Api, Path: path}
	if h.Secure {
		u.Scheme = "https"
	}
	return u
}

func (h *HttpClient) CreateGetRequestWithAuthorization(url url.URL, token string) (*http.Request, error) {
	req, err := http.NewRequest("GET", url.String(), nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("Authorization", h.Client.CurrentUser.Token.Token)
	return req, nil
}
func (h *HttpClient) CreatePostRequestWithAuthorization(url url.URL) (*http.Request, error) {
	req, err := http.NewRequest("POST", url.String(), nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("Authorization", h.Client.CurrentUser.Token.Token)
	return req, nil
}

type HttpResponse struct {
	Status int    `json:"status"`
	Json   string `json:"Json"`
	Err    string `json:"Err"`
}

type UserRegister struct {
	Token string `json:"token"`
}

type GenericFail struct {
	Code    string `json:"code"`
	Message string `json:"message"`
}

func dclose(c io.Closer) {
    if err := c.Close(); err != nil {
        log.Fatal(err)
    }
}
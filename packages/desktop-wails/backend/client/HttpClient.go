package client

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"mime/multipart"
	"net/http"
	"net/textproto"
	"net/url"
	"os"
	"red/backend/app"
	"red/mocks"
	user "red/mocks"
	gr "runtime"
	"runtime/debug"
	"strings"
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
type PassThru struct {
	io.Reader
	ctx      context.Context
	total    int64 // Total # of bytes transferred
	length   int64 // Expected length
	progress float64
}

func (pt *PassThru) Read(p []byte) (int, error) {
	n, err := pt.Reader.Read(p)
	if n > 0 {
		pt.total += int64(n)
		percentage := float64(pt.total) / float64(pt.length) * float64(100)
		if percentage-pt.progress > 2 {
			pt.progress = percentage
			runtime.EventsEmit(pt.ctx, "upload:progress", pt.progress)
		}
		if pt.total == pt.length {
			runtime.EventsEmit(pt.ctx, "upload:finished", pt.length)
		}
	}

	return n, err
}
func CreateHTTP(client *Client, app *app.App) *HttpClient {
	return &HttpClient{Client: client, App: app, Http: http.Client{Timeout: time.Duration(25) * time.Second}}
}

/* Methods */

func (h *HttpClient) SetAPI(api string) {
	h.Api = api
	if h.App.Config.Responder != nil {
		h.App.Config.Responder.API = api
		h.App.SaveConfig()
	}
}

func (h *HttpClient) SetSecure(secure bool) {
	h.Secure = secure
	if h.App.Config.Responder != nil {
		h.App.Config.Responder.Secure = secure
		h.App.SaveConfig()
	}
}

/* Commands */

func (h *HttpClient) RegisterEmail(email string) HttpResponse {
	u := h.createURL("/invite/register")
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
	u := h.createURL("/user")
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
	u := h.createURL("/user/@me")
	req, err := h.createGetRequestWithAuthorization(u, token)
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

func (h *HttpClient) GetLatestRelease() (*mocks.Update, error) {
	u := h.createURL("/cdn/releases/" + h.App.Version)
	resp, err := h.Http.Get(u.String())
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if resp.StatusCode == 204 {
		if err != nil {
			return nil, err
		}
		return nil, nil
	} else if resp.StatusCode == 200 {
		update := mocks.Update{}
		jsonErr := json.Unmarshal(body, &update)
		if jsonErr != nil {
			return nil, err
		} else {
			return &update, nil
		}
	}
	return nil, nil
}

func (h *HttpClient) PublishRelease(r mocks.Update) (*mocks.Update, error) {
	if !h.Client.CurrentUser.Admin {
		return nil, fmt.Errorf("Missing correct user permissions")
	}
	u := h.createURL("/admin/release")
	resJson := []byte(fmt.Sprintf(`{"version": "%s", "title": "%s", "notes": "%s", "signature": "%s", "url": "%s"}`, r.Version, r.Title, r.Notes, r.Author, r.URL))
	req, err := http.NewRequest("POST", u.String(), bytes.NewReader(resJson))
	if err != nil {
		return nil, err
	}
	req.Header.Set("Authorization", h.Client.CurrentUser.Token.Token)
	resp, err := h.Http.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}
	if resp.StatusCode == 201 {
		update := mocks.Update{}
		err := json.Unmarshal(body, &update)
		if err != nil {
			return nil, err
		}
		return &update, nil
	} else {
		return nil, fmt.Errorf("Something didn't quite go right: %s", string(body))
	}

}
func (h *HttpClient) LoginWithEmailAndPassword(email string, password string) (*user.PrivateUser, error) {
	u := h.createURL("/user/@me/login")
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
	u := h.createURL(fmt.Sprintf("/user/@me/relationships/%s", name))
	post, err := h.createPostRequestWithAuthorization(u)
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
func (h *HttpClient) UploadAvatar(file string) (*user.PublicUser, error) {
	u := h.createURL("/user/@me/avatar")
	b := &bytes.Buffer{}
	writer := multipart.NewWriter(b)
	fw, err := CreateFormImageFile(writer, "avatar", "avatar")
	if err != nil {
		fmt.Printf("Got error : %v", err)
		return nil, err
	}

	wd, err := os.Getwd()
	if err != nil {
		fmt.Printf("Got error : %v", err)
		return nil, err
	}
	f, err := os.Open(wd + "/" + file)
	if err != nil {
		return nil, err
	}
	by, _ := io.ReadAll(f)
	fw.Write(by)
	writer.Close()
	byytes := b.Bytes()
	passT := &PassThru{Reader: bytes.NewReader(byytes), ctx: h.App.Ctx, length: int64(len(byytes))}
	post, err := h.createPostRequestWithBodyAndAuthorization(u, passT)
	post.Header.Set("Content-Type", fmt.Sprintf("multipart/form-data; boundary=%v", writer.Boundary()))
	if err != nil {
		return nil, err
	}
	resp, err := h.Http.Do(post)
	if err != nil {
		return nil, err
	}
	defer dclose(resp.Body)
	body, err := ioutil.ReadAll(resp.Body)

	if err != nil {
		h.Client.Logger.Error().Str("err", fmt.Sprint(err)).Msg("Something went wrong when serializing body in set avatar")
		return nil, err
	}

	// Free memory (GC is extremely aggresive)
	gr.GC()
	debug.FreeOSMemory() // Free more memory
	code := resp.StatusCode
	if code == 200 || code == 201 {
		pubUser := &user.PublicUser{}
		jsonErr := json.Unmarshal(body, pubUser)
		if jsonErr != nil {
			return nil, jsonErr
		}
		h.Client.CurrentUser.Avatar = pubUser.Avatar
		os.Remove(wd + "/" + file)
		return pubUser, nil
	} else {
		failImpl := &GenericFail{}
		jsonErr := json.Unmarshal(body, failImpl)
		if jsonErr != nil {
			return nil, jsonErr
		}
		os.Remove(wd + "/" + file)
		return nil, fmt.Errorf("%s (%s)", failImpl.Message, failImpl.Code)
	}
}

func (h *HttpClient) GetLocation() (*mocks.GeoLocation, error) {
	u := url.URL{Scheme: "https", Path: "/cookieconsentpub/v1/geo/location", Host: "geolocation.onetrust.com"}
	str := u.String()
	req, err := http.NewRequest("GET", str, nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("Accept", "application/json")
	resp, err := h.Http.Do(req)
	if err != nil {
		return nil, err
	}
	defer dclose(resp.Body)
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}
	if resp.StatusCode == 200 {
		location := mocks.GeoLocation{}
		err := json.Unmarshal(body, &location)
		if err != nil {
			return nil, err
		}
		return &location, err
	}
	return nil, nil
}

func (h *HttpClient) GetDevices(auth string) (*[]mocks.Device, error) {
	u := h.createURL("/user/@me/devices")
	req, err := h.createGetRequestWithAuthorization(u, auth)
	if err != nil {
		return nil, err
	}
	resp, err := h.Http.Do(req)
	if err != nil {
		return nil, err
	}
	defer dclose(resp.Body)
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}
	if resp.StatusCode == 200 {
		data := []mocks.Device{}
		err := json.Unmarshal(body, &data)
		if err != nil {
			return nil, err
		}
		return &data, nil
	}
	return nil, fmt.Errorf("Error with: %v", string(body))
}

func (h *HttpClient) GetMessagesFromDM(id string, before *string, after *string) (*[]mocks.Message, error) {
	var u url.URL
	str := "/user/@me/" + id + "/messages"+ "?type=FRIEND"
	if after == nil && before == nil {
		u = h.createURL(str)
	} else if after == nil && before != nil {
		u = h.createURL(str + "&before=" + *before)
	} else if after != nil && before == nil {
		u = h.createURL(str + "&after=" + *after)
	} else if after != nil && before != nil {
		u = h.createURL(str + "&before=" + *before + "&after=" + *after)
	}
	req, err := h.createGetRequestWithAuthorization(u, h.Client.CurrentUser.Token.Token)
	if err != nil {
		return nil, err
	}
	resp, err := h.Http.Do(req)
	if err != nil {
		return nil, err
	}
	defer dclose(resp.Body)
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}
	if resp.StatusCode == 200 {
		data := []mocks.Message{}
		err := json.Unmarshal(body, &data)
		if err != nil {
			return nil, err
		}
		return &data, nil
	} else if resp.StatusCode == 204 {
		return nil, nil
	}
	return nil, fmt.Errorf("Error with: %v", string(body))
}

func (h *HttpClient) RegisterLoginWithClient(u *user.PrivateUser) {
	h.Client.CurrentUser = u
}

/* Utils */

func (h *HttpClient) createURL(path string) url.URL {
	u := url.URL{Scheme: "http", Host: h.Api, Path: path}
	if h.Secure {
		u.Scheme = "https"
	}
	return u
}

func (h *HttpClient) createGetRequestWithAuthorization(url url.URL, token string) (*http.Request, error) {
	req, err := http.NewRequest("GET", url.String(), nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("Authorization", token)
	return req, nil
}
func (h *HttpClient) createPostRequestWithAuthorization(url url.URL) (*http.Request, error) {
	req, err := http.NewRequest("POST", url.String(), nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("Authorization", h.Client.CurrentUser.Token.Token)
	return req, nil
}
func (h *HttpClient) createPostRequestWithBodyAndAuthorization(url url.URL, body *PassThru) (*http.Request, error) {
	req, err := http.NewRequest("POST", url.String(), body)
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
func CreateFormImageFile(w *multipart.Writer, fieldname, filename string) (io.Writer, error) {
	h := make(textproto.MIMEHeader)
	h.Set("Content-Disposition",
		fmt.Sprintf(`form-data; name="%s"; filename="%s"`,
			escapeQuotes(fieldname), escapeQuotes(filename)))
	h.Set("Content-Type", "image/png")
	return w.CreatePart(h)
}

var quoteEscaper = strings.NewReplacer("\\", "\\\\", `"`, "\\\"")

func escapeQuotes(s string) string {
	return quoteEscaper.Replace(s)
}

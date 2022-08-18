package comm

import (
	"encoding/json"
	"io/ioutil"
	"log"
	"net/http"
	"time"
)

type CommunicationClient struct {
	h http.Client
	gateway string
}

func NewCommunicationClient(gateway string) *CommunicationClient{
	return &CommunicationClient{
		h: http.Client{
			Timeout: 10 * time.Second,
		},
		gateway: gateway,
	}
}

func (c *CommunicationClient) GetAllMinimalUsers() (*[]MinimalUser, error){
	resp, err := c.h.Get(c.gateway + "/internal/users/minimal/all")
	if err != nil {
		log.Printf("error occured when trying to fetch users : %v", err)
		return nil, err
	}
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		log.Printf("error occured wehn trying to read body : %v", err)
		return nil, err
	}
	log.Printf("Raw : %v", string(body))
	arr := make([]MinimalUser, 0)
	err = json.Unmarshal(body, &arr)
	if err != nil {
		log.Printf("error occured when trying to unmarshal : %v", err)
		return nil, err
	}
	return &arr, nil
}
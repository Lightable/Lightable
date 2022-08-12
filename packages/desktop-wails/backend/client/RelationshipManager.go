package client

import (
	"fmt"
	"red/mocks"
	"strconv"

	maps "golang.org/x/exp/maps"
)

type RelationshipManager struct {
	httpC    *HttpClient
	c        *Client
	Pending  map[int64]mocks.PublicUser `json:"pending"`
	Requests map[int64]mocks.PublicUser `json:"requests"`
	Friends  map[int64]mocks.PublicUser `json:"friends"`
}

func NewRelationshipManager(h *HttpClient, c *Client) *RelationshipManager {

	return &RelationshipManager{
		httpC:    h,
		c:        c,
		Pending:  make(map[int64]mocks.PublicUser),
		Requests: make(map[int64]mocks.PublicUser),
		Friends:  make(map[int64]mocks.PublicUser),
	}
}

func (rm *RelationshipManager) RequestFriend(name string) (*mocks.PublicUser, error) {
	httpC := rm.httpC
	pubUser, err := httpC.AddFriend(name)
	if err != nil {
		return nil, err
	}
	intID, err := strconv.ParseInt(pubUser.Id, 10, 64)
	if err != nil {
		rm.c.Logger.Error().Str("err", fmt.Sprint(err)).Msg("Could not convert string to int64")
		return nil, fmt.Errorf("could not convert string to int64 (%v)", err)
	}
	rm.Pending[intID] = *pubUser
	return pubUser, nil
}

func (rm *RelationshipManager) GetRelations() *mocks.RelationshipStruct {
	return &mocks.RelationshipStruct{
		Pending:  maps.Values(rm.Pending),
		Requests: maps.Values(rm.Requests),
		Friends:  maps.Values(rm.Friends),
	}
}

func (rm *RelationshipManager) InternalAddRelation(Type string, u mocks.PublicUser) error {
	intID, err := strconv.ParseInt(u.Id, 10, 64)
	if err != nil {
		return err
	}
	fmt.Printf("Adding internal %v\n", u)
	switch Type {
	case "pending":
		rm.Pending[intID] = u
	case "friend":
		rm.Friends[intID] = u
	case "request":
		rm.Requests[intID] = u
	}
	return nil
}

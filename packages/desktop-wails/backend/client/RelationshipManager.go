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

func (rm *RelationshipManager) FindRelations(pubUser *mocks.PublicUser) (*map[int64]mocks.PublicUser, *int64, error) {
	intID, err := strconv.ParseInt(pubUser.Id, 10, 64)
	if err != nil {
		rm.c.Logger.Error().Str("err", fmt.Sprint(err)).Msg("Could not convert string to int64")
		return nil, nil, fmt.Errorf("could not convert string to int64 (%v)", err)
	}
	_, ok := rm.Pending[intID]
	if ok {
		return &rm.Pending, &intID, nil
	}
	_, ok = rm.Requests[intID]
	if ok {
		return &rm.Requests, &intID, nil
	}
	_, ok = rm.Friends[intID]
	if ok {
		return &rm.Friends, &intID, nil
	}
	return nil, nil, fmt.Errorf("Could not find user")
} 
func (rm *RelationshipManager) FindRelation(id int64) *mocks.PublicUser {
	u, ok := rm.Pending[id]
	if ok {
		return &u
	}
	u, ok = rm.Requests[id]
	if ok {
		return &u
	}
	u, ok = rm.Friends[id]
	if ok {
		return &u
	}
	return nil
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
	rm.Requests[intID] = *pubUser
	return pubUser, nil
}
func (rm *RelationshipManager) AcceptFriend(name string) (*mocks.PublicUser, error) {
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
	rm.Friends[intID] = *pubUser
	return pubUser, nil
}

func (rm *RelationshipManager) GetRelations() *mocks.RelationshipStruct {
	return &mocks.RelationshipStruct{
		Pending:  maps.Values(rm.Pending),
		Requests: maps.Values(rm.Requests),
		Friends:  maps.Values(rm.Friends),
		Empty: len(rm.Pending) == 0 && len(rm.Requests) == 0 && len(rm.Friends) == 0,
	}
}

func (rm *RelationshipManager) ClearRelations() {
	for k := range rm.Pending {
		delete(rm.Pending, k)
	}
	for k := range rm.Requests {
		delete(rm.Requests, k)
	}
	for k := range rm.Friends {
		delete(rm.Friends, k)
	}
}

func (rm *RelationshipManager) InternalAddRelation(Type string, u mocks.PublicUser) error {
	intID, err := strconv.ParseInt(u.Id, 10, 64)
	if err != nil {
		return err
	}
	users := make([]int64, 0)
	users = append(users, intID)
	selfID, err := strconv.ParseInt(rm.c.CurrentUser.Id, 10, 64)
	if err != nil {
		return err
	}
	users = append(users, selfID)
	users = append(users, selfID)
	u.Channel = &mocks.Channel{
		Id: u.Id,
		Type: 0,
		Users: users,
		Owner: rm.c.CurrentUser.ToPublic(),
		Messages: make([]mocks.Message, 0),
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

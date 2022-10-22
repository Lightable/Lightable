package client

import (
	"fmt"
	"red/mocks"
	"strconv"
)

type ChannelManager struct {
	c        *Client
	h        *HttpClient
	r        *RelationshipManager
	Channels map[int64]*mocks.Channel
}

func NewChannelManager(c *Client, h *HttpClient, r *RelationshipManager) *ChannelManager {
	return &ChannelManager{
		c: c,
		h: h,
		r: r,
		Channels: make(map[int64]*mocks.Channel),
	}
}

func (cm *ChannelManager) GetMessagesFromDM(id string, before *string, after *string) ([]mocks.Message, error) {
	channel, err := cm.GetChannel(id)
	if err != nil {
		return nil, err
	}
	data, err := cm.h.GetMessagesFromDM(id, before, after)
	if err != nil {
		return nil, err
	}
	messages := *data
	for i := 0; i < len(messages); i++ {
		msg := messages[i]
		channel.Messages[msg.Id] = &msg
	}
	return messages, nil
}


func (cm *ChannelManager) addMessageToChannel(id string, msg *mocks.Message) error {
	channel, err := cm.GetChannel(id)
	if err != nil {
		return err
	}
	channel.Messages[msg.Id] = msg 
	return nil
}

func (cm *ChannelManager) GetChannel(id string) (*mocks.Channel, error) {
	intID, err := strconv.ParseInt(id, 10, 64)
	if err != nil {
		return nil, err
	}
	channel, ok := cm.Channels[intID]
	if !ok {
		return nil, fmt.Errorf("channel not found")
	}
	return channel, nil
}
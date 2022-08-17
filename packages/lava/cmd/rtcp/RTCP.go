package rtcp

import (
	"net"

	"github.com/brys0/arc/cmd/rtp"
)

const (
	ReceiverReport    = 0
	SenderReport      = 1
	SourceDescription = 2
)

type RTCPPacket struct {
	Version int
	ID      string
	Stats   rtp.RTPStats
}

type RTCPConnection struct {
	Connection *net.TCPListener
}

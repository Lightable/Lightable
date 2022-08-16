package rtp

type RTPStats struct {
	RTT              int32
	Jitter           int32
	BitRate          int64
	BytesSent        int64
	BytesReceived    int64
	PacketsLost      int64
	NackCount        int64
	FirCount         int64
	PilCount         int64
	LimitationReason *string
	Limitation       *string
}
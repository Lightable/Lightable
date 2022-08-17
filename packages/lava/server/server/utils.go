package server

import (
	"bytes"
	"encoding/base64"
	"encoding/gob"
	"encoding/json"
)

type RTCPPayload struct {
	Type int     `json:"type"`
	Data *string `json:"data"`
}

func GobEncode(d any) []byte {
	var buf bytes.Buffer
	enc := gob.NewEncoder(&buf)
	enc.Encode(d)
	return buf.Bytes()
}

func JSONToPayload(src string) *gob.Decoder {
	arr, err := base64.URLEncoding.DecodeString(src)
	println(string(arr))
	if err != nil {
		panic(err)
	}
	buf := bytes.NewBuffer(arr)
	dec := gob.NewDecoder(buf)
	return dec
}
func CreateValidPayload(t int, d *[]byte) *RTCPPayload {
	if d == nil {
		return &RTCPPayload{Type: t, Data: nil}
	}
	b64 := base64.URLEncoding.EncodeToString(*d)
	return &RTCPPayload{Type: t, Data: &b64}
}

func (r *RTCPPayload) EncodeToJSON() ([]byte, error) {
	return json.Marshal(r)
}

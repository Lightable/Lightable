package main

import (
	"net/http"

	"github.com/brys0/arc/server/server"
)
type RandomBS struct {
	Hi int
    Brrr string
}
func main() {
	s := server.StartServer()
	http.HandleFunc("/ws", func(w http.ResponseWriter, r *http.Request) {
		server.WS(s, w, r)
	})
	err := http.ListenAndServe(":8085", nil)
	println(err)

  
}
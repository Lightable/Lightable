module pkg

require (
	github.com/wailsapp/wails/v2 v2.0.0-beta.40
	red/mocks v0.0.1
)

require (
	github.com/asaskevich/EventBus v0.0.0-20200907212545-49d423059eef // indirect
	github.com/gorilla/websocket v1.5.0 // indirect
	github.com/imdario/mergo v0.3.12 // indirect
	github.com/leaanthony/slicer v1.5.0 // indirect
)

replace red/mocks => ../mocks

go 1.18

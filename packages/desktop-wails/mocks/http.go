package mocks

/* <------------------ Http ------------------> */

type GeoLocation struct {
	Country   string `json:"country"`
	State     string `json:"state"`
	StateName string `json:"stateName"`
	ZipCode   string `json:"zipcode"`
	Timezone  string `json:"timezone"`
	Latitude  string `json:"latitude"`
	Longitude string `json:"longitude"`
	City      string `json:"city"`
	Continent string `json:"continent"`
}

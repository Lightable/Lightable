# Changelog
<h2 align="center">August 23 2022</h2>
<h3 align="center">1.18.1-ALPHA</h3>

#### Changes:
* Properly send update event to clients that are outdated


<h2 align="center">August 23 2022</h2>
<h3 align="center">1.18.0-ALPHA</h3>

#### Changes:
* Add simplified user analytics

<h2 align="center">August 17 2022</h2>
<h3 align="center">1.17.0-ALPHA</h3>

#### Changes:
* Add new path '/internal/users/minimal/all' to all minimal users on node

##### Minimal User:
- identifier = int64
- name = string
- token = string

<h2 align="center">August 17 2022</h2>
<h3 align="center">1.16.0-ALPHA</h3>

#### Changes:
* Add new controller 'InternalsController' that allows valid IP's to use and control internal data of the given API
* Add new path '/internal' with current GET route `/internal/users/all`
##### Syntax: 
- internals=127.0.0.1,192.168.0.1

<h2 align="center">August 14 2022</h2>
<h3 align="center">1.15.5-ALPHA</h3>

#### Changes:
* Add semantic versioning 
* Simplify Event driven architecture of server
* Update to Kotlin 1.7.20-BETA
* Remove all "self" socket events. 
* Add user generic for simplified finding of user sockets
* Removal of updating user "state".. (Rethinking implmentation)
* Removal of Message create event. (Rethinking implmentation)
* Set ENV `verbose=true` for custom flow event logs (Debugging)

#### Patches
* Fix events not firing 

#### Extras
* Removed "custom" named classes (Everything is generic)



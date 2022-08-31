package rebase.interfaces

import rebase.events.EventHandler

interface EventController {
    val handler: EventHandler
}
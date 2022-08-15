package rebase.interfaces

import rebase.events.EventBus

interface EventController {
    val eventBus: EventBus
}
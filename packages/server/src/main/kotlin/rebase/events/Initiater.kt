package rebase.events

import rebase.GetCPUUsage
import rebase.controllers.*

class EventHandler {
    private val listeners: MutableList<EventListener> = mutableListOf()
    fun add(listener: EventListener) {
        listeners.add(listener)
    }
    fun broadcastUserUpdate(event: UserUpdateEvent) {
        for (listener in listeners) {
            listener.onUserUpdate(event)
        }
    }
    fun broadcastTyping(event: TypingEvent) {
        for (listener in listeners) {
            listener.onTyping(event)
        }
    }
    fun broadcastPendingFriend(event: PendingFriendEvent) {
        for (listener in listeners) {
            listener.onPendingFriend(event)
        }
    }
    fun broadcastRemoveFriendRequest(event: RemoveFriendRequestEvent) {
        for (listener in listeners) {
            listener.onRemoveFriendRequest(event)
        }
    }
    fun broadcastAcceptFriendRequest(event: AcceptFriendRequestEvent) {
        for (listener in listeners) {
            listener.onAcceptFriendRequest(event)
        }
    }
    fun broadcastUpdate(event: UpdateEvent) {
        for (listener in listeners) {
            listener.onUpdate(event)
        }
    }
    fun broadcastCPUUsage(event: GetCPUUsage.CPUUsageUpdate) {
        for (listener in listeners) {
            listener.onCPUUsage(event)
        }
    }
}
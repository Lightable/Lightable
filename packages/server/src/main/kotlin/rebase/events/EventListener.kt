package rebase.events

import com.typesafe.config.Optional
import rebase.GetCPUUsage
import rebase.controllers.*

@FunctionalInterface
interface EventListener {
    fun onUserUpdate(payload: UserUpdateEvent)
    fun onTyping(payload: TypingEvent)
    fun onPendingFriend(payload: PendingFriendEvent)
    fun onRemoveFriendRequest(payload: RemoveFriendRequestEvent)
    fun onAcceptFriendRequest(payload: AcceptFriendRequestEvent)
    fun onUpdate(payload: UpdateEvent)
    fun onCPUUsage(payload: GetCPUUsage.CPUUsageUpdate)
    fun onDMMessageCreate(payload: DMMessageCreateEvent)
}
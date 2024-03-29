package rebase.controllers

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.javalin.websocket.WsCloseContext
import io.javalin.websocket.WsConnectContext
import io.javalin.websocket.WsContext
import io.javalin.websocket.WsMessageContext
import kotlinx.coroutines.DelicateCoroutinesApi
import org.slf4j.Logger
import rebase.GetCPUUsage
import rebase.Utils
import rebase.cache.UserCache
import rebase.compression.CompressionUtil
import rebase.events.EventHandler
import rebase.events.EventListener
import rebase.interfaces.GenericUser
import rebase.messages.Message
import rebase.schema.*
import java.nio.ByteBuffer
import java.time.Instant

class WebSocketController(
    private val logger: Logger,
    private val ehandler: EventHandler,
    private val userCache: UserCache,
    private val isProd: Boolean
) : EventListener {
    private val rawConnections = mutableMapOf<String, SessionWithCompression>()
    private val connections = mutableMapOf<String, SocketSession>()
    private val jsonWrap: ObjectMapper = jacksonObjectMapper().findAndRegisterModules()
    private val compressionEngine = CompressionUtil
    fun connection(handler: WsConnectContext) {
        val compressionType = handler.queryParam("compression") ?: "none"
        rawConnections[handler.sessionId] = SessionWithCompression(handler, compressionType)
        send(handler, compressionType, ReadyPayload(30000))
    }

    fun message(handler: WsMessageContext) {
        logger.info("Socket Message: ${handler.message()}")
        try {
            val session = rawConnections[handler.sessionId]!!
            val rawMessage = jsonWrap.readTree(handler.message())
            val message = ReceivedMessage(rawMessage["t"].asInt())
            if (message.t == SocketMessageType.ClientPing.ordinal) {
                send(session.session, session.type, PongPayload)
                logger.debug("Sent 💓 to client ${session.session.sessionId}")
                return
            } else if (message.t == SocketMessageType.ClientStart.ordinal) {
                val properties = jsonWrap.convertValue(rawMessage["d"], SessionProperties::class.java)
                val device = Device(
                    Instant.now(),
                    handler.session.remoteAddress.address.hostAddress,
                    properties.properties.browser,
                    properties.properties.build,
                    properties.properties.os,
                    properties.properties.geo
                )
                val user = userCache.users.values.find { u -> u.token.token == properties.auth }
                    ?: return session.session.closeSession(
                        1010,
                        "Authentication matching ${properties.auth} doesn't exist"
                    )
                val existingDevice = user.devices.find { d -> d == device }
                if (existingDevice == null) {
                    user.devices.add(device)
                    user.save(true)
                } else {
                    user.devices.remove(existingDevice)
                    user.devices.add(device)
                }
                val existingConnection = connections.values.find { u -> u.user.token.token == properties.auth }
                if (existingConnection != null) {
                    send(
                        existingConnection.ws.session,
                        existingConnection.ws.type,
                        ServerDropGateway(
                            device
                        )
                    )
                    existingConnection.ws.session.closeSession(
                        1008,
                        "Can't have 2 connections at once! Security Risk ⚠"
                    )
                }
                if (!user.enabled) {
                    send(session.session, session.type, DisabledUser)
                    session.session.closeSession(1008, "Your account has been disabled")
                    return
                }
                connections[session.session.sessionId] = SocketSession(session, true, user, properties)
                val latest = userCache.latestRelease
                if (userCache.releases.size >= 1 && Utils.versionCompare(latest!!.tag, properties.properties.build) < 0) {
                    send(session.session, session.type, UpdateEvent(userCache.releases.values.last()))
                }

                send(
                    session.session,
                    session.type,
                    ServerReadyPayload(user.toPrivate(), user.getFriends(), MetaInfoPayload(isProd))
                )
                user.state = UserState.ONLINE.ordinal
                user.save()
                return
            } else if (connections[session.session.sessionId]?.authenticated == false) {

                return session.session.closeSession(
                    1008,
                    "That is a privileged endpoint and can't be accessed without first authenticating"
                )
            } else {
                val userSession = connections[handler.sessionId]!!
                when (message.t) {
                    SocketMessageType.ClientTyping.ordinal -> {
                        val typingTo = userCache.users.values.find { f ->
                            f.identifier == jsonWrap.convertValue(
                                rawMessage["d"],
                                String::class.java
                            ).toLong()
                        }
                        if (typingTo != null) {
                            ehandler.broadcastTyping(TypingEvent(userSession.user, typingTo))
                            return
                        } else {
                            send(userSession.ws.session, userSession.ws.type, object {
                                val t = "DoesNotExist"
                            })
                        }
                    }
                }
            }
        } catch (e: Exception) {
            if (!isProd) e.printStackTrace()
            if (e == JsonParseException::class.java) {
                logger.error("Failed to parse JSON ${e.message}")
                handler.closeSession(1007, "Could not parse JSON ${e.message}")
            } else {
                handler.closeSession(1006, e.message)
                logger.error("The controller encountered an error it couldn't recover from (Check err.clog) (Session = ${handler.sessionId})")
            }
        }
    }

    fun close(handler: WsCloseContext) {
        val connection = connections[handler.sessionId] ?: return

        val friends = getSockets(connection.user.getFriends().allAsOne())
        for (friend in friends) {
            send(
                friend.ws.session,
                friend.ws.type,
                FriendUpdatePayload(connection.user, connection.user.identifier, "state", UserState.OFFLINE)
            )
        }
        connections.remove(handler.sessionId)
        rawConnections.remove(handler.sessionId)
        connection.user.analytics.activeTime =
            connection.user.analytics.activeTime?.plus((Instant.now().toEpochMilli() - connection.start) / 1000)
        connection.user.save()
    }


    /**
     * Get sockets based on a [Collection] of [GenericUser]
     * @param users Collection of [GenericUser]
     * @author Brys
     * @since 0.0.5-ALPHA
     * @see GenericUser
     * @see SocketSession
     * @see Collection
     * @return A [List] of [SocketSession]s
     */
    private fun getSockets(users: Collection<GenericUser>): List<SocketSession> {
        val sockets = mutableListOf<SocketSession>()
        for (user in users) {
            connections.values.find { u -> u.user.identifier == user.identifier }?.let { sockets.add(it) }
        }
        return sockets.toList()
    }

    /*
     * Broadcast to many based on collection of users
     *
     *                  ->      User1
     *                  ->      User2
     *  Client          ->      User3
     *                  ->      User4
     *                  ->      User5
     *
    */

    /**
     * Broadcast to many sockets
     *
     * This function takes a collection of generic users and attempts to get every socket to send your given payload
     * @param payload The payload you wish to broadcast
     * @param users A collection of generic users
     * @author Brys
     * @since 1.12.5-ALPHA
     * @see GenericUser
     * @see getSockets
     */
    private fun oneToMany(payload: Any, users: Collection<GenericUser>) {
        val sockets = getSockets(users)
        for (socket in sockets) {
            send(socket.ws.session, socket.ws.type, payload)
        }
    }

    /**
     * Broadcast to all sockets of the same user.
     * @param payload The payload you wish to broadcast
     * @param user The user you wish to gather all the sockets for
     * @since 1.12.5-ALPHA
     * @see GenericUser
     * @see getSockets
     */
    private fun oneToPotentialMany(payload: Any, user: GenericUser) {
        val sockets = getSockets(mutableListOf(user))
        for (socket in sockets) {
            send(socket.ws.session, socket.ws.type, payload)
        }
    }

    /**
     * ⚠️ This function is deprecated and shouldn't be used! ⚠️
     *
     * Function doesn't account for multiple sockets of the same user being connected
     *
     * Which has been supported since 0.1.6-ALPHA
     * @see GenericUser
     * @see SocketSession
     * @see oneToPotentialMany
     * @author Brys
     * @since 0.0.3-ALPHA
     * @return The [SocketSession] or nothing
     */
    @Deprecated("Deprecated, use oneToPotentialMany instead")
    private fun getSelfSocket(self: GenericUser): SocketSession? {
        return connections.values.find { u -> u.user.identifier == self.identifier }
    }

    private fun send(session: WsContext, type: String, data: Any? = null) {
        val json = if (data is String) {
            data
        } else {
            jsonWrap.writeValueAsString(data)
        }
        when (type) {
            "none" -> session.send(json)
            "zlib" -> session.send(ByteBuffer.wrap(compressionEngine.compress(json)))
        }
    }

    override fun onUserUpdate(payload: UserUpdateEvent) {
        oneToPotentialMany(payload.toSelf(), payload.self)
        oneToMany(payload.toExternal(), payload.self.getFriends().allAsOne())
    }

    override fun onTyping(payload: TypingEvent) {
        oneToPotentialMany(payload.toExternal(), payload.to)
        oneToPotentialMany(payload.toSelf(), payload.self)
    }

    override fun onPendingFriend(payload: PendingFriendEvent) {
        oneToPotentialMany(payload.toExternal(), payload.friend as GenericUser)
        oneToPotentialMany(payload.toSelf(), payload.self as GenericUser)
    }

    override fun onRemoveFriendRequest(payload: RemoveFriendRequestEvent) {
        oneToPotentialMany(payload.toSelf(), payload.self as GenericUser)
        oneToPotentialMany(payload.toExternal(), payload.pendingFriend as GenericUser)
    }

    override fun onAcceptFriendRequest(payload: AcceptFriendRequestEvent) {
        oneToPotentialMany(payload.toSelf(), payload.self as GenericUser)
        oneToPotentialMany(payload.toExternal(), payload.pendingFriend as GenericUser)
    }

    override fun onUpdate(payload: UpdateEvent) {
        connections.values.forEach { u ->
            println(u.session.properties.build.replace(".", "").toInt())
            println(payload.d.tag.replace(".", "").toInt())
        }
        println()
        val sockets = connections.values.filter { u ->
            u.session.properties.build.replace(".", "").toInt() <= payload.d.tag.replace(".", "").toInt()

        }
        for (socket in sockets) {
            send(socket.ws.session, socket.ws.type, payload)
        }
    }

    override fun onCPUUsage(payload: GetCPUUsage.CPUUsageUpdate) {}
    override fun onDMMessageCreate(payload: DMMessageCreateEvent) {
        oneToPotentialMany(payload.toSelf(), payload.self as GenericUser)
        oneToPotentialMany(payload.toExternal(), payload.to as GenericUser)
    }
}

data class ReceivedMessage(
    val t: Int,
)

data class DMMessageCreateEvent(@JsonIgnore val self: User, @JsonIgnore val to: User, val message: Message) {
    fun toExternal(): DMMessageCreateEventExternal {
        return DMMessageCreateEventExternal()
    }
    fun toSelf(): DMMessageCreateEventSelf {
        return DMMessageCreateEventSelf()
    }
    inner class DMMessageCreateEventExternal {
        val d = DMMessageWrapper(self.identifier.toString(), message)
        val t = SocketMessageType.ServerMessageCreate.ordinal
    }
    inner class DMMessageCreateEventSelf {
        val d = DMMessageWrapper(to.identifier.toString(), message)
        val t = SocketMessageType.ServerSelfMessageCreate.ordinal
    }

    data class DMMessageWrapper(val channel: String, val message: Message)
}
data class ServerMessageCreate(
    @JsonIgnore var to: Long,
    var d: Message
) {
    val t = SocketMessageType.ServerMessageCreate.ordinal
}

data class ReadyPayload(@JsonIgnore val heartbeat: Int) {
    val t = SocketMessageType.ServerStart.ordinal
    val d = object {
        val interval = heartbeat
    }
}

/**
 * Note: Execute toSelf() FIRST!
 * @see toSelf
 * @see toExternal
 */
data class UserUpdateEvent(
    @JsonIgnore val self: User,
    @JsonIgnore val difference: MutableMap<String, Any?>
) {

    fun toExternal(): UserUpdateExternal {
        difference.censorSensitiveData()
        return UserUpdateExternal(difference)
    }


    fun toSelf(): UserUpdateSelf {
        return UserUpdateSelf(difference)
    }

    data class UserUpdateExternal(val d: MutableMap<String, Any?>) {
        val t = SocketMessageType.ServerUserUpdate.ordinal
    }

    data class UserUpdateSelf(val d: MutableMap<String, Any?>) {
        val t = SocketMessageType.ServerSelfUpdate.ordinal
    }

    companion object {
        fun MutableMap<String, Any?>.censorSensitiveData() {
            this.remove("email")
            this.remove("relationships")
            this.remove("analytics")
        }
    }
}

data class FriendUpdatePayload(
    @JsonIgnore val self: User,
    val id: Long,
    val name: String,
    val value: Any,
    var t: Int = SocketMessageType.ServerUserUpdate.ordinal,
) {
    fun toJSON(): String {
        val json = jacksonObjectMapper().createObjectNode()
        val dataJson = jacksonObjectMapper().createObjectNode()
        val valueJson = jacksonObjectMapper().convertValue(value, JsonNode::class.java)
        json.put("t", t)
        dataJson.set<JsonNode>(name, valueJson)
        dataJson.put("id", id.toString())
        json.set<JsonNode>("d", dataJson)
        return json.toString()
    }
}

data class SelfUpdatePayload(
    @JsonIgnore val self: User,
    val name: String,
    val value: Any,
    val t: Int = SocketMessageType.ServerSelfUpdate.ordinal
) {
    fun toJSON(): String {
        val json = jacksonObjectMapper().createObjectNode()
        val dataJson = jacksonObjectMapper().createObjectNode()
        val valueJson = jacksonObjectMapper().convertValue(value, JsonNode::class.java)
        json.put("t", t)
        json.set<JsonNode>("d", dataJson)
        dataJson.set<JsonNode>(name, valueJson)
        return json.toString()
    }
}

data class TypingEvent(
    @JsonIgnore val self: User,
    @JsonIgnore val to: User,
) {
    fun toExternal(): ClientTypingExternal {
        return ClientTypingExternal(self)
    }

    fun toSelf(): ClientTypingSelf {
        return ClientTypingSelf(to)
    }

    data class ClientTypingExternal(@JsonIgnore val user: User) {
        val t = SocketMessageType.ServerTyping.ordinal
        val d = user.toPublic()
    }

    data class ClientTypingSelf(@JsonIgnore val user: User?) {
        val t = SocketMessageType.ServerSelfTyping.ordinal
        val d = user?.toPublic()
    }
}

data class ServerReadyPayload(
    @JsonIgnore val userRaw: PrivateUser,
    @JsonIgnore val relationshipsRaw: FriendsPublic,
    @JsonIgnore val metaRaw: MetaInfoPayload
) {
    val t = SocketMessageType.ServerReady.ordinal
    val d = object {
        val user = userRaw
        val relationships = relationshipsRaw
        val meta = metaRaw
    }
}

data class MetaInfoPayload(
    val prod: Boolean
)

object PongPayload {
    val t = SocketMessageType.ServerPong.ordinal
}

data class SessionWithCompression(
    val session: WsContext,
    val type: String
)

data class SocketSession(
    val ws: SessionWithCompression,
    var authenticated: Boolean = false,
    var user: User,
    var session: SessionProperties,
    var start: Long = Instant.now().toEpochMilli()
)

data class SessionProperties(
    val auth: String,
    var properties: WebSocketDevice
)

data class WebSocketDevice(
    val browser: String,
    val build: String,
    val os: String,
    val geo: GeoLocation
)

data class PendingFriendEvent(
    @JsonIgnore val self: User,
    @JsonIgnore val friend: User
) {
    fun toExternal(): ServerPendingExternal {
        return ServerPendingExternal(self)
    }

    fun toSelf(): ServerPendingSelf {
        return ServerPendingSelf(friend)
    }

    data class ServerPendingExternal(@JsonIgnore val user: User) {
        val t = SocketMessageType.ServerPendingFriend.ordinal
        val d = user.toPublic()
    }

    data class ServerPendingSelf(@JsonIgnore val user: User) {
        val t = SocketMessageType.ServerSelfRequestFriend.ordinal
        val d = user.toPublic()
    }
}


data class RemoveFriendRequestEvent(
    @JsonIgnore val self: User,
    @JsonIgnore val pendingFriend: User
) {
    fun toExternal(): ServerRequestRemoveExternal {
        return ServerRequestRemoveExternal(self)
    }

    fun toSelf(): ServerRequestRemoveSelf {
        return ServerRequestRemoveSelf(pendingFriend)
    }

    data class ServerRequestRemoveExternal(@JsonIgnore val user: User) {
        val t = SocketMessageType.ServerRequestRemoved.ordinal
        val d = user.toPublic()
    }

    data class ServerRequestRemoveSelf(@JsonIgnore val user: User) {
        val t = SocketMessageType.ServerSelfRequestRemove.ordinal
        val d = user.toPublic()
    }
}

data class AcceptFriendRequestEvent(
    @JsonIgnore val self: User,
    @JsonIgnore val pendingFriend: User
) {
    fun toExternal(): FriendRequestAcceptExternal {
        return FriendRequestAcceptExternal(self)
    }

    fun toSelf(): FriendRequestAcceptSelf {
        return FriendRequestAcceptSelf(pendingFriend)
    }

    data class FriendRequestAcceptExternal(@JsonIgnore val user: User) {
        val t = SocketMessageType.ServerRequestAccepted.ordinal
        val d = user.toPublic()
    }

    data class FriendRequestAcceptSelf(@JsonIgnore val user: User) {
        val t = SocketMessageType.ServerSelfRequestAccepted.ordinal
        val d = user.toPublic()
    }
}


data class ServerDropGateway(val d: Device) {
    val t = SocketMessageType.ServerDropGateway.ordinal
}

data class UpdateEvent(val d: ChattyRelease) {
    val t = SocketMessageType.ServerUpdate.ordinal
}

object DisabledUser {
    val t = SocketMessageType.ServerSelfDisabledUser.ordinal
    val d = null
}

interface WebsocketPayload {
    val t: SocketMessageType
    var d: Any
    fun toSelf(): Any
    fun toServer(): Any
}

enum class SocketMessageType {
    ClientStart,
    ServerStart,
    ClientPing,
    ServerPong,
    ClientTyping,
    ClientCallStart,
    ClientCallEnd,
    ClientStatusAway,
    ClientStatusDND,
    ClientStatusOnline,
    ServerReset,
    ServerReady,
    ServerSelfUpdate,
    ServerSessionCreate,
    ServerSessionUpdate,
    ServerSessionDelete,
    ServerPendingFriend,
    ServerFriendRemove,
    ServerSelfFriendRemove,
    ServerSelfRequestFriend,
    ServerSelfRequestRemove,
    ServerRequestRemoved,
    ServerRequestAccepted,
    ServerSelfRequestAccepted,
    ServerUserUpdate,
    ServerMessageCreate,
    ServerSelfMessageCreate,
    ServerMessageDelete,
    ServerSelfMessageDelete,
    ServerMessageUpdate,
    ServerSelfMessageUpdate,
    ServerTyping,
    ServerSelfTyping,
    ServerDropGateway,
    ServerSelfDisabledUser,
    ServerUpdate
}

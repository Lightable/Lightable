package rebase.controllers

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.javalin.websocket.WsConnectContext
import io.javalin.websocket.WsContext
import io.javalin.websocket.WsMessageContext
import me.kosert.flowbus.EventsReceiver
import me.kosert.flowbus.GlobalBus
import me.kosert.flowbus.subscribe
import org.slf4j.Logger
import rebase.*
import rebase.compression.CompressionUtil
import java.nio.ByteBuffer
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.Public

class WebSocketController(private val logger: Logger, private val cache: Cache, private val isProd: Boolean) {
    private val rawConnections = mutableMapOf<String, SessionWithCompression>()
    private val connections = mutableMapOf<String, ChattySession>()
    private val jsonWrap: ObjectMapper = jacksonObjectMapper().findAndRegisterModules()
    private val compressionEngine = CompressionUtil
    private val events = EventsReceiver()
    fun connection(handler: WsConnectContext) {
        val compressionType = handler.queryParam("compression") ?: "none"
        rawConnections[handler.sessionId] = SessionWithCompression(handler, compressionType)
        logger.info("Connection established \"/ws\" ${handler.sessionId}")
        send(handler, compressionType, ReadyPayload(30000))
    }

    fun message(handler: WsMessageContext) {
        try {
            val session = rawConnections[handler.sessionId]!!
            val rawMessage = jsonWrap.readTree(handler.message())
            val message = ReceivedMessage(rawMessage["t"].asInt())
            if (message.t == SocketMessageType.ClientPing.ordinal) {
                send(session.session, session.type, PongPayload)
                logger.info("Sent 💓 to client ${session.session.sessionId}")
                return
            } else if (message.t == SocketMessageType.ClientStart.ordinal) {
                val properties = jsonWrap.convertValue(rawMessage["d"], SessionProperties::class.java)
                val user = cache.users.values.find { u -> u.token.token == properties.auth }
                    ?: return session.session.closeSession(
                        4004,
                        "Authentication matching ${properties.auth} doesn't exist"
                    )
                val existingConnection = connections.values.find { u -> u.user.token.token == properties.auth}
                if (existingConnection != null) {
                    send(existingConnection.ws.session, existingConnection.ws.type, ServerDropGateway)
                    existingConnection.ws.session.closeSession(5000, "Can't have 2 connections at once! Security Risk ⚠")
                }
                if (!user.enabled) {
                    send(session.session, session.type, DisabledUser)
                    session.session.closeSession(4004, "Your account has been disabled")
                    return
                }
                connections[session.session.sessionId] = ChattySession(session, true, user, properties)
                logger.info("New session authenticated $session $properties")
                send(
                    session.session,
                    session.type,
                    ServerReadyPayload(user.toPublic(), user.getFriends(), MetaInfoPayload(isProd))
                )
                return
            } else if (connections[session.session.sessionId]?.authenticated == false) {
                return session.session.closeSession(
                    4003,
                    "That is a privileged endpoint and can't be accessed without first authenticating"
                )
            } else {
                val userSession = connections[handler.sessionId]!!
                when (message.t) {
                    SocketMessageType.ClientStatusOnline.ordinal -> {
                        userSession.user.state = UserState.ONLINE.ordinal
                        userSession.user.save()
                        GlobalBus.post(FriendUpdatePayload(userSession.user.toPublic(), userSession.user.identifier, "state", UserState.ONLINE.ordinal))
                        GlobalBus.post(SelfUpdatePayload(userSession.user.toPublic(), "state", UserState.ONLINE.ordinal))
                    }
                    SocketMessageType.ClientStatusAway.ordinal -> {
                        userSession.user.state = UserState.AWAY.ordinal
                        userSession.user.save()
                        GlobalBus.post(
                            FriendUpdatePayload(userSession.user.toPublic(), userSession.user.identifier, "state", UserState.AWAY.ordinal))
                        GlobalBus.post(SelfUpdatePayload(userSession.user.toPublic(), "state", UserState.AWAY.ordinal))
                    }
                    SocketMessageType.ClientStatusDND.ordinal -> {
                        userSession.user.state = UserState.DND.ordinal
                        userSession.user.save()
                        GlobalBus.post(FriendUpdatePayload(userSession.user.toPublic(), userSession.user.identifier, "state", UserState.DND.ordinal))
                        GlobalBus.post(SelfUpdatePayload(userSession.user.toPublic(), "state", UserState.DND.ordinal))
                    }
                    SocketMessageType.ClientTyping.ordinal -> {
                        val typingTo = connections.values.find{ f -> f.user.identifier == jsonWrap.convertValue(rawMessage["d"], Long::class.java) }
                        if (typingTo != null) {
                            GlobalBus.post(ClientTyping(userSession, typingTo))
                        } else {
                            send(userSession.ws.session, userSession.ws.type, object { val t = "DoesNotExist"})
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to parse JSON ${e.message}")
            handler.closeSession(4000, "Could not parse JSON ${e.message}")
        }
    }

    private fun send(session: WsContext, type: String, obj: Any? = null, jsonStr: String? = null) {
        val json = jsonStr ?: jsonWrap.writeValueAsString(obj!!)
        when (type) {
            "none" -> session.send(json)
            "zlib" -> session.send(ByteBuffer.wrap(compressionEngine.compress(json)))
        }
    }

    init {
        events.subscribe<FriendUpdatePayload> { payload ->
            logger.info("Update Payload -> ${payload.id} ${payload.name} = ${payload.value}")
            val friends = connections.values.find { u -> u.user.identifier == payload.id }!!.user.getFriends()
            for (friend in friends.friends) {
                val friendSession = connections.values.find { f -> f.user.identifier == friend.id }!!
                send(friendSession.ws.session, friendSession.ws.type, jsonStr = payload.toJSON())
                return@subscribe
            }
        }
        events.subscribe<SelfUpdatePayload> { payload ->
            logger.info("Self Update -> ${payload.self.id} ${payload.name} = ${payload.value}")
            val selfSocket = connections.values.find { u -> u.user.identifier == payload.self.id }
            if (selfSocket != null) {
                send(selfSocket.ws.session, selfSocket.ws.type, jsonStr = payload.toJSON())
            }
        }
        events.subscribe<ClientTyping> { payload ->
            logger.info("Client Typing -> To = ${payload.to.user.identifier} From = ${payload.self.user.identifier}")
            payload.t = SocketMessageType.ServerTyping.ordinal
            send(payload.to.ws.session, payload.to.ws.type, payload)
            payload.t = SocketMessageType.ServerSelfTyping.ordinal
            send(payload.self.ws.session, payload.self.ws.type, payload)
        }

        // Relationships

        // External pending
        events.subscribe<ServerPendingFriend> { payload ->
            logger.info("New Pending Friend -> To = ${payload.friend.id} From = ${payload.self.id}")
            val friendSocket = connections.values.find { u -> u.user.identifier == payload.friend.id }
            if (friendSocket != null) {
                send(friendSocket.ws.session, friendSocket.ws.type, payload)
            }
        }
        // Self Friend Request
        events.subscribe<ServerSelfPending> { payload ->
            logger.info("New Request To = ${payload.friend.id} From = ${payload.self.id}")
            val selfSocket = connections.values.find { u -> u.user.identifier == payload.self.id }
            if (selfSocket != null) {
                send(selfSocket.ws.session, selfSocket.ws.type, payload)
            }
        }

        // Self -> You've removed a
        events.subscribe<ServerRequestRemove> { payload ->
            logger.info("Remove Pending Friend -> To = ${payload.friend.id} From = ${payload.self.id}")
            val friendSocket = connections.values.find { u -> u.user.identifier == payload.friend.id }
            val selfSocket = connections.values.find { u -> u.user.identifier == payload.self.id }
            if (friendSocket != null) {
                send(friendSocket.ws.session, friendSocket.ws.type, payload)
            }
            if (selfSocket != null) {
                payload.t = SocketMessageType.ServerSelfRequestRemove
                send(selfSocket.ws.session, selfSocket.ws.type, payload)
            }
        }
    }
}

data class ReceivedMessage(
    val t: Int,
)

data class ReadyPayload(@JsonIgnore val heartbeat: Int) {
    val t = SocketMessageType.ServerReady.ordinal
    val d = object {
        val interval = heartbeat
    }
}

data class FriendUpdatePayload(
    @JsonIgnore val self: PublicUser,
    val id: Long,
    val name: String,
    val value: Any,
    var t: Int = SocketMessageType.ServerFriendUpdate.ordinal
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
data class SelfUpdatePayload(
    @JsonIgnore val self: PublicUser,
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
data class ClientTyping(
    @JsonIgnore val self: ChattySession,
    @JsonIgnore val to: ChattySession,
    var t: Int = 0
) {
    val d = object {
        val id = self.user.identifier
    }
}
data class ServerReadyPayload(
    val user: PublicUser,
    val relationships: FriendsPublic,
    val meta: MetaInfoPayload
)

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

data class ChattySession(
    val ws: SessionWithCompression,
    var authenticated: Boolean = false,
    var user: User,
    var session: SessionProperties
)

data class SessionProperties(
    val auth: String,
    var browser: String,
    var ip: String,
    var build: String
)
data class ServerPendingFriend(
    @JsonIgnore val self: PublicUser,
    @JsonIgnore val friend: PublicUser
) {
    var t = SocketMessageType.ServerPendingFriend.ordinal
    val d = self
}
data class ServerSelfPending(
    @JsonIgnore val self: PublicUser,
    @JsonIgnore val friend: PublicUser
) {
    val t = SocketMessageType.ServerSelfRequestFriend
    val d = friend.id
}
data class ServerRequestRemove(
    @JsonIgnore val self: PublicUser,
    @JsonIgnore val friend: PublicUser
) {
    var t = SocketMessageType.ServerRequestRemoved
}

object ServerDropGateway {
    val t = SocketMessageType.ServerDropGateway
    val d = null
}
object DisabledUser {
    val t = SocketMessageType.ServerSelfDisabledUser
    val d = null
}
enum class SocketMessageType {
    ClientStart,
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
    ServerSelfPendingRemove,
    ServerSelfRequestRemove,
    ServerRequestRemoved,
    ServerFriendUpdate,
    ServerMessageCreate,
    ServerSelfMessageCreate,
    ServerMessageDelete,
    ServerSelfMessageDelete,
    ServerMessageUpdate,
    ServerSelfMessageUpdate,
    ServerTyping,
    ServerSelfTyping,
    ServerDropGateway,
    ServerSelfDisabledUser
}
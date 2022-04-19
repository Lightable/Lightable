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
                        GlobalBus.post(
                            FriendUpdatePayload(
                                userSession,
                                userSession.user.identifier,
                                "state",
                                UserState.ONLINE.ordinal
                            )
                        )
                    }
                    SocketMessageType.ClientStatusAway.ordinal -> {
                        userSession.user.state = UserState.ONLINE.ordinal
                        userSession.user.save()
                        GlobalBus.post(
                            FriendUpdatePayload(
                                userSession,
                                userSession.user.identifier,
                                "state",
                                UserState.AWAY.ordinal
                            )
                        )
                    }
                    SocketMessageType.ClientStatusDND.ordinal -> {
                        userSession.user.state = UserState.ONLINE.ordinal
                        userSession.user.save()
                        GlobalBus.post(
                            FriendUpdatePayload(
                                userSession,
                                userSession.user.identifier,
                                "state",
                                UserState.DND.ordinal
                            )
                        )
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
            payload.t = SocketMessageType.ServerSelfUpdate.ordinal
            send(payload.self.ws.session, payload.self.ws.type, jsonStr = payload.toJSON())
        }
        events.subscribe<ClientTyping> { payload ->
            logger.info("Client Typing -> To = ${payload.to.user.identifier} From = ${payload.self.user.identifier}")
            payload.t = SocketMessageType.ServerTyping.ordinal
            send(payload.to.ws.session, payload.to.ws.type, payload)
            payload.t = SocketMessageType.ServerSelfTyping.ordinal
            send(payload.self.ws.session, payload.self.ws.type, payload)
        }

        // Relationships

        // Self -> You've requested a friend
        // External -> Someone has requested to be your friend
        events.subscribe<ServerPendingFriend> { payload ->
            logger.info("New Pending Friend -> To = ${payload.friend.id} From = ${payload.self.id}")
            val friendSocket = connections.values.find { u -> u.user.identifier == payload.friend.id }
            val selfSocket = connections.values.find { u -> u.user.identifier == payload.self.id }
            if (friendSocket != null) {
                send(friendSocket.ws.session, friendSocket.ws.type, payload)
            }
            if (selfSocket != null) {
                payload.t = SocketMessageType.ServerSelfRequestFriend.ordinal
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
    @JsonIgnore val self: ChattySession,
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
data class ServerRequestRemove(
    @JsonIgnore val self: PublicUser,
    @JsonIgnore val friend: PublicUser
) {
    var t = SocketMessageType.ServerRequestRemoved
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
    ServerSelfTyping
}
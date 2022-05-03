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
import me.kosert.flowbus.EventsReceiver
import me.kosert.flowbus.GlobalBus
import me.kosert.flowbus.subscribe
import org.slf4j.Logger
import rebase.*
import rebase.compression.CompressionUtil
import java.nio.ByteBuffer
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import me.kosert.flowbus.dropEvent
import okhttp3.internal.notify
import okhttp3.internal.userAgent

class WebSocketController(private val logger: Logger, private val cache: Cache, private val isProd: Boolean) {
    private val rawConnections = mutableMapOf<String, SessionWithCompression>()
    private val connections = mutableMapOf<String, ChattySession>()
    private val jsonWrap: ObjectMapper = jacksonObjectMapper().findAndRegisterModules()
    private val compressionEngine = CompressionUtil
    private val events = EventsReceiver()
    fun connection(handler: WsConnectContext) {
        val compressionType = handler.queryParam("compression") ?: "none"
        println(green(underline("WS >> Opened Connection << WS")))
        println(" ${yellow("Compression")} >> ${magenta(compressionType)}")
        println(" ${yellow("Session")} >> ${magenta(handler.sessionId)}")
        rawConnections[handler.sessionId] = SessionWithCompression(handler, compressionType)
//      logger.info("Connection established \"/ws\" ${handler.sessionId}")
        send(handler, compressionType, ReadyPayload(30000))
    }

    fun message(handler: WsMessageContext) {
        println("Raw message ${handler.message()}")
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
                properties.properties.ip = handler.session.remoteAddress.address.hostAddress
                val user = cache.users.values.find { u -> u.token.token == properties.auth }
                    ?: return session.session.closeSession(
                        1010,
                        "Authentication matching ${properties.auth} doesn't exist"
                    )
                val existingConnection = connections.values.find { u -> u.user.token.token == properties.auth}
                if (existingConnection != null) {
                    send(existingConnection.ws.session, existingConnection.ws.type, ServerDropGateway(Device(properties.properties.ip!!, properties.properties.browser, properties.properties.build, properties.properties.os)))
                    existingConnection.ws.session.closeSession(1008, "Can't have 2 connections at once! Security Risk ⚠")
                }
                if (!user.enabled) {
                    send(session.session, session.type, DisabledUser)
                    session.session.closeSession(1008, "Your account has been disabled")
                    return
                }
                connections[session.session.sessionId] = ChattySession(session, true, user, properties)
                if (cache.releases.size >= 1 && properties.properties.build != cache.releases.values.last().tag) {
                    send(session.session, session.type, ServerUpdate(cache.releases.values.last()))
                }
                println(blue("  ${underline(">> Authenticated Connection <<")}"))
                println("   ${yellow("Session")} >> ${magenta(session.session.sessionId)}")
                println("   ${yellow("Authenticated")} >> ${green("true")}")
                println(cyan("    ${underline(">> Properties <<")}"))
                println("     ${yellow("Browser")} >> ${magenta(properties.properties.browser)}")
                println("     ${yellow("IP")} >> ${magenta(properties.properties.ip!!)}")
                println("     ${yellow("Build")} >> ${magenta(properties.properties.build)}")
                send(
                    session.session,
                    session.type,
                    ServerReadyPayload(user.toPublic(), user.getFriends(), MetaInfoPayload(isProd))
                )
                return
            } else if (connections[session.session.sessionId]?.authenticated == false) {

                return session.session.closeSession(
                    1008,
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
                        return
                    }
                    SocketMessageType.ClientStatusAway.ordinal -> {
                        userSession.user.state = UserState.AWAY.ordinal
                        userSession.user.save()
                        GlobalBus.post(
                            FriendUpdatePayload(userSession.user.toPublic(), userSession.user.identifier, "state", UserState.AWAY.ordinal))
                        GlobalBus.post(SelfUpdatePayload(userSession.user.toPublic(), "state", UserState.AWAY.ordinal))
                        return
                    }
                    SocketMessageType.ClientStatusDND.ordinal -> {
                        userSession.user.state = UserState.DND.ordinal
                        userSession.user.save()
                        GlobalBus.post(FriendUpdatePayload(userSession.user.toPublic(), userSession.user.identifier, "state", UserState.DND.ordinal))
                        GlobalBus.post(SelfUpdatePayload(userSession.user.toPublic(), "state", UserState.DND.ordinal))
                        return
                    }
                    SocketMessageType.ClientTyping.ordinal -> {
                        val typingTo = connections.values.find{ f -> f.user.identifier == jsonWrap.convertValue(rawMessage["d"], String::class.java).toLong() }
                        if (typingTo != null) {
                            GlobalBus.post(ClientTyping(userSession, typingTo))
                            return
                        } else {
                            send(userSession.ws.session, userSession.ws.type, object { val t = "DoesNotExist"})
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
        val connection = connections[handler.sessionId]
        println(red(underline("WS >> Closed Connection << WS")))
        if (connection == null) {
            println(" ${yellow("Session")} >> ${magenta(handler.sessionId)}")
        } else {
            println(" ${yellow("User")} >> ${magenta(connection.user.identifier.toString())}")
            println(" ${yellow("Authenticated")} >> ${if (connection.authenticated) green("true") else red("false")}")
            println(" ${yellow("Session")} >> ${magenta(handler.sessionId)}")
            connections.remove(handler.sessionId)
            rawConnections.remove(handler.sessionId)
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
            connections.values.forEach { v -> println(v.user.identifier) }
            val friends = connections.values.find { u -> u.user.identifier== payload.id }!!.user.getFriends()
            for (friend in friends.friends) {
                val friendSession = connections.values.find { f -> f.user.identifier == friend.id }
                if (friendSession != null) {
                    send(friendSession.ws.session, friendSession.ws.type, jsonStr = payload.toJSON())
                }
            }
            GlobalBus.dropAll()
            return@subscribe
        }
        events.subscribe<SelfUpdatePayload> { payload ->
            logger.info("Self Update -> ${payload.self.id} ${payload.name} = ${payload.value}")
            val selfSocket = connections.values.find { u -> u.user.identifier == payload.self.id }
            if (selfSocket != null) {
                send(selfSocket.ws.session, selfSocket.ws.type, jsonStr = payload.toJSON())
            }
            GlobalBus.dropAll()
            return@subscribe
        }
        events.subscribe<ClientTyping> { payload ->
            logger.info("Client Typing -> To = ${payload.to.user.identifier} From = ${payload.self.user.identifier}")
            payload.t = SocketMessageType.ServerTyping.ordinal
            send(payload.to.ws.session, payload.to.ws.type, payload)
            payload.t = SocketMessageType.ServerSelfTyping.ordinal
            send(payload.self.ws.session, payload.self.ws.type, payload)
            GlobalBus.dropAll()
            return@subscribe
        }

        // Relationships

        // External pending
        events.subscribe<ServerPending> { payload ->
            logger.info("New Pending Friend -> To = ${payload.friend.id} From = ${payload.self.id}")
            val friendSocket = connections.values.find { u -> u.user.identifier == payload.friend.id }
            if (friendSocket != null) {
                send(friendSocket.ws.session, friendSocket.ws.type, payload)
            }
            GlobalBus.dropAll()
            return@subscribe
        }

        // Self Friend Request
        events.subscribe<ServerSelfPending> { payload ->
            logger.info("New Request To = ${payload.friend.id} From = ${payload.self.id}")
            val selfSocket = connections.values.find { u -> u.user.identifier == payload.self.id }
            if (selfSocket != null) {
                send(selfSocket.ws.session, selfSocket.ws.type, payload)
            }
            GlobalBus.dropAll()
            return@subscribe
        }

        // External Deny friendship request
        events.subscribe<ServerRequestRemove> { payload ->
            logger.info("Remove Pending Friend -> To = ${payload.pendingFriend.id} From = ${payload.self.id}")
            val friendSocket = connections.values.find { u -> u.user.identifier == payload.pendingFriend.id }
            if (friendSocket != null) {
                send(friendSocket.ws.session, friendSocket.ws.type, payload)
            }
            GlobalBus.dropAll()
        }

        // Self Deny friendship request
        events.subscribe<ServerSelfRequestRemove> { payload ->
            logger.info("Remove Self Pending Friend -> To = ${payload.pendingFriend.id} From = ${payload.self.id}")
            val selfSocket = connections.values.find { u -> u.user.identifier == payload.self.id }
            if (selfSocket != null) {
                send(selfSocket.ws.session, selfSocket.ws.type, payload)
            }
            GlobalBus.dropAll()
        }

        // External accept friendship
        events.subscribe<ServerRequestAccept> { payload ->
            logger.info("Accept Pending Friend -> To ${payload.t} From = ${payload.self.id}")
            val friendSocket = connections.values.find { u -> u.user.identifier == payload.pendingFriend.id }
            if (friendSocket != null) {
                send(friendSocket.ws.session, friendSocket.ws.type, payload)
            }
            GlobalBus.dropAll()
        }

        // Self accept friendship
        events.subscribe<ServerSelfRequestAccept> { payload ->
            logger.info("Accept Self Pending Friend -> To ${payload.pendingFriend.id} From = ${payload.self.id}")
            val selfSocket = connections.values.find { u -> u.user.identifier == payload.self.id }
            if (selfSocket != null) {
                send(selfSocket.ws.session, selfSocket.ws.type, payload)
            }
            GlobalBus.dropAll()
        }

        events.subscribe<ServerUpdate> { payload ->
            val sockets = connections.values.filter { u -> u.session.properties.build != payload.d.tag }
            for (socket in sockets) {
                send(socket.ws.session, socket.ws.type, payload)
            }
            GlobalBus.dropAll()
        }
    }
}

data class ReceivedMessage(
    val t: Int,
)

data class ReadyPayload(@JsonIgnore val heartbeat: Int) {
    val t = SocketMessageType.ServerStart.ordinal
    val d = object {
        val interval = heartbeat
    }
}

data class FriendUpdatePayload(
    @JsonIgnore val self: PublicUser,
    val id: Long,
    val name: String,
    val value: Any,
    var t: Int = SocketMessageType.ServerFriendUpdate.ordinal,
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
    val d = self.user.identifier.toString()

}
data class ServerReadyPayload(
    @JsonIgnore val userRaw: PublicUser,
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

data class ChattySession(
    val ws: SessionWithCompression,
    var authenticated: Boolean = false,
    var user: User,
    var session: SessionProperties
)

data class SessionProperties(
    val auth: String,
    var properties: ClientProperties
)
data class ClientProperties(
    val os: String,
    val browser: String,
    val build: String,
    var ip: String?
)
data class ServerPending(
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
    val t = SocketMessageType.ServerSelfRequestFriend.ordinal
    val d = friend.id.toString()
}
data class ServerRequestRemove(
    @JsonIgnore val self: PublicUser,
    @JsonIgnore val pendingFriend: PublicUser
) {
    val t = SocketMessageType.ServerRequestRemoved.ordinal
}
data class ServerSelfRequestRemove(
    @JsonIgnore val self: PublicUser,
    @JsonIgnore val pendingFriend: PublicUser
) {
    val t = SocketMessageType.ServerSelfRequestRemove.ordinal
    val d = pendingFriend.id.toString()
}
data class ServerRequestAccept(
    @JsonIgnore val self: PublicUser,
    @JsonIgnore val pendingFriend: PublicUser
) {
    val t = SocketMessageType.ServerRequestAccepted.ordinal
    val d = self
}
data class ServerSelfRequestAccept(
    @JsonIgnore val self: PublicUser,
    @JsonIgnore val pendingFriend: PublicUser
) {
    val t = SocketMessageType.ServerSelfRequestAccepted.ordinal
    val d = pendingFriend
}
data class ServerDropGateway(val d: Device) {
    val t = SocketMessageType.ServerDropGateway.ordinal
}
data class ServerUpdate(val d: ChattyRelease) {
    val t = SocketMessageType.ServerUpdate.ordinal
}
object DisabledUser {
    val t = SocketMessageType.ServerSelfDisabledUser.ordinal
    val d = null
}

data class Device(val ip: String, val browser: String, val build: String, val os: String)
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
    ServerSelfDisabledUser,
    ServerUpdate
}
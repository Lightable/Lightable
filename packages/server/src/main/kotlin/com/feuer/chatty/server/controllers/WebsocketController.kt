package com.feuer.chatty.server.controllers

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.feuer.chatty.Utils
import com.feuer.chatty.presend.Compressor
import com.feuer.chatty.schemas.MongoSchemas
import com.feuer.chatty.server.FleshedMessage
import com.feuer.chatty.server.SpecialDate
import com.google.gson.Gson
import io.javalin.Javalin
import io.javalin.websocket.WsContext
import io.prometheus.client.Gauge
import me.kosert.flowbus.EventsReceiver
import me.kosert.flowbus.GlobalBus
import me.kosert.flowbus.subscribe
import org.eclipse.jetty.websocket.api.CloseStatus
import org.json.JSONObject
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.setValue
import rebase.MongoDatabase
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.zip.Deflater
import java.util.zip.Inflater
import kotlin.experimental.and


/**
 * The controller for the websocket, handles disconnects, connects, reconnects and responses for any websocket event
 */

fun String.zlibCompress(): ByteArray {
    val input = this.toByteArray(charset("UTF-8"))

    // Compress the bytes
    // 1 to 4 bytes/char for UTF-8
    val output = ByteArray(input.size * 4)
    val compressor = Deflater().apply {
        setInput(input)
        finish()
    }
    val compressedDataLength: Int = compressor.deflate(output)
    return output.copyOfRange(0, compressedDataLength)
}


class WebsocketController(private val server: Javalin, private val mongoDB: MongoDatabase) {

    /**
     * Decompress a byte array using ZLIB.
     *
     * @return an UTF-8 encoded string.
     */
    fun ByteArray.zlibDecompress(): String {
        val inflater = Inflater()
        val outputStream = ByteArrayOutputStream()

        return outputStream.use {
            val buffer = ByteArray(1024)

            inflater.setInput(this)

            var count = -1
            while (count != 0) {
                count = inflater.inflate(buffer)
                outputStream.write(buffer, 0, count)
            }

            inflater.end()
            outputStream.toString("UTF-8")
        }
    }

    private val clients = ArrayList<Client>()
    private val gson = Gson()
    private val events = EventsReceiver()
    private val userCollection = mongoDB.getUserCollection()
    private val compressor = Compressor()
    private val connectedClients =
        Gauge.build("chatty_ws_connected", "Total number of connected clients to the Websocket").register()

    init {
        server.ws("/ws") { handler ->
            handler.onConnect { client ->
                connectedClients.inc()
                var compression = CompressionType.NONE
                when (client.queryParam("compression")?.lowercase()) {
                    "gzip" -> compression = CompressionType.GZIP
                    "zlib" -> compression = CompressionType.ZLIB
                    "none" -> compression = CompressionType.NONE
                }
                /**
                 * Say hi 👋
                 * {
                 *  op: 7
                 *  d: {
                 *      heartbeat: 10000
                 *     }
                 * }
                 */
                when (compression) {
                    CompressionType.NONE -> {
                        client.send(WebsocketResponse(7, object {
                            val heartbeat = 10000
                        }).json)
                    }
                    CompressionType.GZIP -> TODO("Add gzip compression")
                    CompressionType.ZLIB -> {
                        val compressed = WebsocketResponse(7, object {
                            val heartbeat = 10000
                        }).json.zlibCompress()
                        println(compressed)
                        client.send(ByteBuffer.wrap(compressed))
                    }
                }
                /**
                 * Add our client to the client list, with no authentication yet
                 */
                clients.add(Client(client, null, client.sessionId, WaitingTypes.LOGIN, compression, null))
            }
            handler.onMessage { client ->
                try {
                    val compression = clients.find { c -> c.id == client.sessionId }?.compressType!!
                    fun sendCompressedPayload(data: WebsocketResponse) {
                        when (compression) {
                            CompressionType.NONE -> {
                                client.send(data.json)
                            }
                            CompressionType.GZIP -> TODO("Add gzip compression")
                            CompressionType.ZLIB -> {
                                val compressed = data.json.zlibCompress()
                                client.send(ByteBuffer.wrap(compressed))
                            }
                        }
                    }

                    /**
                     * The client should only send JSON, anything else can be ignored.
                     */
                    val json = JSONObject(client.message())
                    val clientOP = json["op"] as Int
                    /**
                     * Client PING request
                     */
                    if (clientOP == 1) {
                        /**
                         * 🎾 The clients ping was received, lets acknowledge this!
                         * {
                         *  op: 8
                         * }
                         */
                        sendCompressedPayload(WebsocketResponse(8, null))
                        return@onMessage
                    }
                    /**
                     * Identify OPCode
                     */
                    if (clientOP == 2) {
                        /**
                         * If our websocket list contains the same client with no auth, lets continue...
                         */
                        if (clients.find { c -> c.id == client.sessionId }?.waiting == WaitingTypes.LOGIN) {
                            /**
                             * Convert to Identify JSON Payload to a Kotlin Class
                             */
                            val identify = gson.fromJson(client.message(), Identify::class.java)
                            try {

                                val user = userCollection.findOne(MongoSchemas.MongoUser::auth eq identify.token)!!
                                if (user.token.expired()) {
                                    sendPayload(compression, SendResponse(-1, object {
                                        val outdated = true
                                        val expiry = user.token.expireTime.epochSecond
                                    }), client, compressor)
                                    return@onMessage
                                }
                                /**
                                 * ❌ Remove the 0 authentication client
                                 */
                                clients.remove(clients.find { c -> c.id == client.sessionId })
                                /**
                                 * ✔️ Replace with an authenticated one!
                                 */
                                clients.add(
                                    Client(
                                        client,
                                        user.auth,
                                        client.sessionId,
                                        WaitingTypes.NONE,
                                        compression,
                                        identify.properties
                                    )
                                )
                                /**
                                 * Update their status to online if not already.
                                 */
                                userCollection.updateOne(
                                    MongoSchemas.MongoUser::auth eq identify.token,
                                    setValue(
                                        MongoSchemas.MongoUser::online,
                                        Utils.Constants.OnlineStates.ONLINE.ordinal
                                    )
                                )
                                user.online = Utils.Constants.OnlineStates.ONLINE.ordinal
                                PresenceUpdate(user).let { GlobalBus.post(it) }
                                GlobalBus.post(
                                    DeviceInfo(
                                        false,
                                        user.auth,
                                        client.session.remoteAddress.address.hostAddress,
                                        identify.properties
                                    )
                                )
                                /**
                                 * 📢 Acknowledgement that you are successfully logged in
                                 * {
                                 *  op: 0,
                                 *  d: {
                                 *      user: 0000000000000000
                                 *     }
                                 * }
                                 */
                                sendCompressedPayload(WebsocketResponse(0, object {
                                    val user = user.identifier
                                }))
                                return@onMessage
                            } catch (e: Exception) {
                                /**
                                 * Connection closed 4006
                                 * Reason: BAD AUTH
                                 */
                                client.session.close(CloseStatus(4006, "BAD AUTH")) // Bad authentication = 4006
                                return@onMessage
                            }
                        }
                    }
                } catch (e: Exception) {
                    /**
                     * Connection closed 4005
                     * Reason: BAD JSON
                     */
                    client.session.close(CloseStatus(4005, "BAD JSON")) // Bad 'data' = 4006
                    return@onMessage
                }
            }
            /**
             * :c sad to see you go...
             */
            handler.onClose { client ->
                connectedClients.dec()
                val auth =
                    clients.find { c -> c.id == client.sessionId } // Lets find our client based on the SESSION ID
                val user = auth?.let { userCollection.findOne(MongoSchemas.MongoUser::auth eq it.auth) }
                println("Clients = ${clients}\nclient = ${clients.find { c -> c.id == client.sessionId }}\nauth = ${auth}")
                println("AUTH: ${auth}")
                auth?.let {
                    userCollection.updateOne(
                        MongoSchemas.MongoUser::auth eq it.auth,
                        setValue(MongoSchemas.MongoUser::online, Utils.Constants.OnlineStates.OFFLINE.ordinal)
                    )
                } // If the auth even exists lets get the user database
                user?.online = 0
                user?.let {
                    println("HELLO?")
                    println(it.online)
                    PresenceUpdate(it)
                }?.let { GlobalBus.post(it) }

                auth?.properties?.let {
                    auth.auth?.let { it1 ->
                        DeviceInfo(
                            true, it1, client.session.remoteAddress.address.hostAddress,
                            it
                        )
                    }
                }?.let { GlobalBus.post(it) }
            }
            /**
             * Presence update.
             * MEANING anything to do with your online, status, status icon.
             */
            events.subscribe<PresenceUpdate> { event ->
                println("[Presence -> ${event.user.name} (${event.user.identifier})] - Payload:")
                println(event.user.private)
                for (client in clients) {
                    /**
                     * Lets find our friend
                     */
                    val friend = client.auth?.let { userCollection.findOne(MongoSchemas.MongoUser::auth eq it) }
                    if (event.user.friends != null) {
                        if (event.user.friends.contains(friend?.identifier)) {
                            println("Found Friend! ${friend?.name}")
                            /*
                             * 📣 Broadcast new presence to your friends!
                             * {
                             *    op: 9,
                             *    type: 1,
                             *    d: {
                             *        id: 00000000000000,
                             *        status: {
                             *                 text: 'blah'
                             *                 icon: '/path/to/something'
                             *                },
                             *        online: true
                             *       }
                             * }
                             */
                            sendPayload(client.compressType, SendResponse(1, object {
                                val id = event.user.identifier
                                val status = object {
                                    val text = event.user.status?.text
                                    val icon = event.user.status?.icon
                                }
                                val online = event.user.online
                            }), client.ws, compressor)
                        }
                    }
                }
            }
            events.subscribe<ProfileUpdate> { event ->
                for (client in clients) {
                    /**
                     * Lets find our friend
                     */
                    val friend = client.auth?.let { userCollection.findOne(MongoSchemas.MongoUser::auth eq it) }
                    if (event.user.friends != null) {
                        if (event.user.friends.contains(friend?.identifier)) {
                            /*
                            * 📣 Broadcast new profile to your friends!
                            * {
                            *    op: 9,
                            *    type: 2,
                            *    d: {
                            *        id: 00000000000000,
                            *        avatar: 'brrrrrr',
                            *        name: 'brrr',
                            *        about: 'brrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr'
                            *       }
                            * }
                            */
                            sendPayload(client.compressType, SendResponse(2, object {
                                val id = event.user.identifier
                                val avatar = event.user.avatar
                                val name = event.user.name
                                val about = event.user.about
                                val online = event.user.online
                            }), client.ws, compressor)
                        }
                    }
                }
            }
            events.subscribe<MessageCreate> { data ->
                for (client in clients) {
                    for (user in data.users) {
                        val friend = userCollection.findOne(MongoSchemas.MongoUser::identifier eq user)
                        val userData = userCollection.findOne(MongoSchemas.MongoUser::identifier eq data.message.user)
                        val websocket = userCollection.findOne(MongoSchemas.MongoUser::auth eq client.auth)
                        if (websocket != null) {
                            if (websocket.identifier == friend?.identifier && websocket.identifier != data.user) {
                                sendPayload(client.compressType, SendResponse(0, object {
                                    val user = userData?.publicReduced
                                    val message = FleshedMessage(
                                        userData!!.publicReduced,
                                        data.message.content,
                                        SpecialDate(data.message.created),
                                        data.message.identifier,
                                        data.message.embeds
                                    )
                                }), client.ws, compressor)
                            }
                            if (websocket.identifier != friend?.identifier && websocket.identifier == data.user) {
                                sendPayload(client.compressType, SendResponse(3, object {
                                    val user = userData?.publicReduced
                                    val message = data.message
                                }), client.ws, compressor)
                            }
                        }
                    }
                }
            }
            events.subscribe<FriendUpdate> { data ->
                val friendWebsocket = clients.find { c -> c.auth == data.friend.auth }
                val userWebsocket = clients.find { c -> c.auth == data.user.auth }
                println("Friend WS =  ${data.friend.auth} ${data.friend.name} ${friendWebsocket?.ws}\nUser WS = ${data.user.auth} ${data.user.name} ${userWebsocket?.ws}")
                when (data.friendType) {
                    FriendTypes.ACCEPT -> {
                        friendWebsocket?.compressType?.let {
                            sendPayload(it, SendResponse(5, object {
                                val ft = FriendTypes.ACCEPT.ordinal
                                val friend = data.user.publicReduced
                            }), friendWebsocket.ws, compressor)
                        }
                        userWebsocket?.compressType?.let {
                            sendPayload(it, SendResponse(5, object {
                                val user = data.friend.publicReduced
                                val ack = 1
                            }), userWebsocket.ws, compressor)
                        }
                    }
                    FriendTypes.DENY -> {
                        friendWebsocket?.compressType?.let {
                            sendPayload(it, SendResponse(5, object {
                                val ft = FriendTypes.DENY.ordinal
                                val friend = data.user.publicReduced
                            }), friendWebsocket.ws, compressor)
                        }
                        userWebsocket?.compressType?.let {
                            sendPayload(it, SendResponse(5, object {
                                val user = data.friend.publicReduced
                                val ack = 2
                            }), userWebsocket.ws, compressor)
                        }
                    }
                    FriendTypes.PENDING -> {
                        userWebsocket?.compressType?.let {
                            println("Pending user")
                            sendPayload(it, SendResponse(5, object {
                                val ft = FriendTypes.PENDING.ordinal
                                val ack = 3
                            }), userWebsocket.ws, compressor)
                        }
                        friendWebsocket?.compressType?.let {
                            println("Pending friend")
                            sendPayload(it, SendResponse(5, object {
                                val user = data.user.publicReduced
                                val ft = FriendTypes.PENDING.ordinal
                            }), friendWebsocket.ws, compressor)
                        }
                    }
                    FriendTypes.REMOVED -> {
                        friendWebsocket?.compressType?.let {
                            sendPayload(it, SendResponse(5, object {
                                val ft = FriendTypes.REMOVED.ordinal
                                val user = data.user.publicReduced
                            }), friendWebsocket.ws, compressor)
                        }
                        userWebsocket?.compressType?.let {
                            sendPayload(it, SendResponse(5, object {
                                val user = data.friend.publicReduced
                                val ack = 4
                            }), userWebsocket.ws, compressor)
                        }
                    }
                }
            }
            events.subscribe<Update> { data ->
                clients.forEach { client ->
                    sendPayload(client.compressType, SendResponseExtended(10, data), client.ws, compressor)
                }
            }
        }

    }

    /**
     * Default Client Identify Payload
     */
    data class Identify(val token: String, val properties: WebsocketProperties) {
        val op = 2
    }

    data class DeviceInfo(
        @JsonIgnore val removed: Boolean,
        @JsonIgnore val token: String,
        val ip: String,
        val properties: WebsocketProperties
    )

    data class WebsocketProperties(
        val os: String,
        val browser: String,
        val buildNumber: String
    )

    /**
     * Default websocket response with and op code and data
     */

    data class WebsocketResponse(private var opcode: Int, private var data: Any?) {

        init {
            if (opcode > 9) {
                throw Error("Your OP (${opcode}) is out of range for max OP of 9")
            }
        }

        val json: String = jacksonObjectMapper().writeValueAsString(object {
            val op = opcode
            val d = data
        })
    }

    /**
     * Receive responses, message, presence, profile.
     */
    data class SendResponse(private var typecode: Int, private var data: Any?) {
        private val opcode = 9

        init {

            if (typecode > 5) {
                throw Error("Your type (${typecode}) is out of range for max OP of 2")
            }
        }

        val json: String = jacksonObjectMapper().findAndRegisterModules().writeValueAsString(object {
            val op = opcode
            val type = typecode
            val d = data
        })
    }

    /**
     * Receive responses, message, presence, profile.
     */
    data class SendResponseExtended(val opcode: Int = 9, private var data: Any?) {
        val json: String = jacksonObjectMapper().findAndRegisterModules().writeValueAsString(object {
            val op = opcode
            val d = data
        })
    }

    /**
     * Our websocket client, what data do we need.
     */
    data class Client(
        val ws: WsContext,
        var auth: String?,
        var id: String,
        var waiting: WaitingTypes,
        var compressType: CompressionType,
        var properties: WebsocketProperties?
    )

    enum class WaitingTypes {
        LOGIN,
        MESSAGE,
        NONE
    }

    enum class CompressionType {
        ZLIB,
        GZIP,
        NONE
    }

    data class FriendUpdate(
        val friend: MongoSchemas.MongoUser,
        val user: MongoSchemas.MongoUser,
        val friendType: FriendTypes
    ) {
        val op = 2
        val type = 3
    }

    enum class FriendTypes {
        DENY,
        ACCEPT,
        PENDING,
        REMOVED
    }

    data class ProfileUpdate(val user: MongoSchemas.MongoUser) {
        val op = 9
        val type = 2
    }

    /**
     * Default presence update to be posted using [me.kosert.flowbus.GlobalBus]
     */
    data class PresenceUpdate(val user: MongoSchemas.MongoUser) {
        val op = 9
        val type = 1
    }

    data class MessageCreate(val user: String, val message: MongoSchemas.Message, val users: ArrayList<String>) {
        val op = 9
        val type = 0
    }

    data class Update(val release: MongoSchemas.ChattyRelease) {
        val op = 10
    }
}

private fun printByteArray(bytes: ByteArray) {
    for (b1 in bytes) {
        var s1 = String.format("%8s", Integer.toBinaryString((b1 and 0xFF.toByte()).toInt())).replace(' ', '0')
        s1 += " " + Integer.toHexString(b1.toInt())
        s1 += " $b1"
        println(s1)
    }
}

private fun sendPayload(
    compression: WebsocketController.CompressionType,
    data: WebsocketController.SendResponse,
    ctx: WsContext,
    compressor: Compressor
) {
    when (compression) {
        WebsocketController.CompressionType.NONE -> {
            ctx.send(data.json)
        }
        WebsocketController.CompressionType.GZIP -> TODO("Add gzip compression")
        WebsocketController.CompressionType.ZLIB -> {
            val compressed = data.json.zlibCompress()
            if (compressed != null) {
                ctx.send(ByteBuffer.wrap(compressed))
            }
        }
    }
}

private fun sendPayload(
    compression: WebsocketController.CompressionType,
    data: WebsocketController.SendResponseExtended,
    ctx: WsContext,
    compressor: Compressor
) {
    when (compression) {
        WebsocketController.CompressionType.NONE -> {
            ctx.send(data.json)
        }
        WebsocketController.CompressionType.GZIP -> TODO("Add gzip compression")
        WebsocketController.CompressionType.ZLIB -> {
            val compressed = data.json.zlibCompress()
            if (compressed != null) {
                ctx.send(ByteBuffer.wrap(compressed))
            }
        }
    }
}



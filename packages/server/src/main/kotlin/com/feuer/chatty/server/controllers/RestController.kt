package com.feuer.chatty.server.controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.feuer.chatty.Message.Embed
import com.feuer.chatty.Storage
import com.feuer.chatty.Utils
import com.feuer.chatty.auth.StandardToken
import com.feuer.chatty.schemas.MongoSchemas
import com.feuer.chatty.server.*
import com.google.gson.Gson
import com.mongodb.client.MongoCollection
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json.Default.decodeFromString
import me.kosert.flowbus.EventsReceiver
import me.kosert.flowbus.GlobalBus
import me.kosert.flowbus.subscribe
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.setValue
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*


class RestController(
    server: Javalin,
    private val userCollection: MongoCollection<MongoSchemas.MongoUser>,
    private val DMCollection: MongoCollection<MongoSchemas.DM>,
    private val releaseCollection: MongoCollection<MongoSchemas.ChattyRelease>,
    private val storage: Storage,
    private val gson: Gson
) {
    val globalDevices = mutableMapOf<String, MutableList<WebsocketController.DeviceInfo>>()
    var releaseAuth = UUID.randomUUID().toString()
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val events = EventsReceiver()
    private val auth = PrivateAuth("exampleadminemail@example.com", "exampleadminpassword")
    private val classState = object {
        var timing = 0
        var reached = 0
    }

    @Serializable
    data class PrivateAuth(val email: String, val password: String)

    init {
        fun loop() {
            CoroutineScope(Dispatchers.IO).launch {
                delay(5700000)
                CoroutineScope(Default).launch {
                    releaseAuth = UUID.randomUUID().toString()
                    logger.info("Auth updated for posting release")
                    loop()
                }
            }
        }
        loop()
        events.subscribe<WebsocketController.DeviceInfo> { info ->
            val currentDevices = mutableListOf<WebsocketController.DeviceInfo>()
            globalDevices[info.token]?.let { currentDevices.addAll(it) }
            if (info.removed) {
                currentDevices.removeIf { e -> e.token == info.token }
                globalDevices[info.token] = currentDevices
            } else if (!info.removed) {
                println(currentDevices)
                currentDevices.add(info)
                globalDevices[info.token] = currentDevices
                println(globalDevices)
            }
        }
        server.routes {
            get("/release/latest") {
                it.status(200).json(releaseCollection.find().reversed().first())
                return@get
            }
            get("/release/{release}") {
                val release = it.pathParam("release").replace(".", "").toInt()
                val latest = releaseCollection.find().reversed().firstOrNull()
                println("Release endpoint issued with tag (${release}) latest ${latest?.tag}")
                if (latest == null || release > latest.tag.replace(".", "").toInt()) {
                    it.status(204)
                    return@get
                } else {
                    it.status(200).json(object {
                        val url = "https://chatty-api.feuer.tech/release/download/${latest.msi}"
                        val version = latest.tag
                        val notes = latest.description
                    })
                }
                return@get
            }
            get("/releases") { data ->
                data.status(200).json(releaseCollection.find().reversed())
                return@get
            }
            path("development/") {
                post("/login") { data ->
                    val bodyAsClass = data.bodyAsClass<PrivateAuth>()
                    if (bodyAsClass != auth) {
                        data.status(403).json(object {
                            val res = "Password or email is incorrect"
                        })
                        return@post
                    }
                    data.status(200).json(object {
                        val verifier = releaseAuth
                    })
                    return@post
                }
                delete("/release/{release}") { data ->
                    val auth = data.header("Authentication")
                    if (auth != releaseAuth) {
                        data.status(403).json(object {
                            val res = "Verifier incorrect or missing"
                        }
                        )
                        return@delete
                    }
                    val res = releaseCollection.deleteOne(MongoSchemas.ChattyRelease::tag eq data.pathParam("release"))
                    val deleted = File("./releases/Chatty_${data.pathParam("release")}.msi").delete()
                    if (res.deletedCount == 0L && !deleted) {
                        data.status(400).json(object {
                            val res = "Release tag does not exist"
                        })
                        return@delete
                    } else {
                        data.status(200).json(object {
                            val res = "Release ${data.pathParam("release")} was removed"
                        })
                        return@delete
                    }
                }
                post("/release/create") { data ->
                    val bodyAsClass = data.bodyAsClass<ReleasePayload>()
                    if (bodyAsClass.auth != releaseAuth) {
                        data.status(403).json(object {
                            val res = "Verifier incorrect or missing"
                        }
                        )
                        return@post
                    }
                    val payload = MongoSchemas.ChattyRelease(
                        bodyAsClass.release.tag,
                        bodyAsClass.release.title,
                        bodyAsClass.release.description,
                        bodyAsClass.release.prerelease,
                        "/download/release/Chatty_${bodyAsClass.release.tag}.msi"
                    )

                    if (releaseCollection.findOne(MongoSchemas.ChattyRelease::tag eq bodyAsClass.release.tag) != null) {
                        data.status(403).json(object {
                            val res = "Tag already exists, remove the previous or use a new tag"
                        })
                        return@post
                    }
                    releaseCollection.insertOne(payload)
                    File("./releases/Chatty_${bodyAsClass.release.tag}.msi").writeBytes(
                        Base64.getDecoder().decode(bodyAsClass.release.msi)
                    )
                    data.status(201).json(payload)
                    GlobalBus.post(WebsocketController.Update(payload))
                    return@post
                }
            }
            /**
             * Handles all SelfClient Requests (/user/@me)
             */

            path("v2/user") {
                path("@me") {
                    get("/devices") { data ->
                        val auth = data.header("Authorization")
                        if (auth != null) {
                            data.status(200).json(object {
                                val devices = globalDevices[auth]
                            })
                            return@get
                        } else {
                            data.status(403).json(
                                object {
                                    val message = "Error occurred when attempting to grab devices"
                                    val reason = "You're probably missing authentication to use this endpoint"
                                }
                            )
                            return@get
                        }
                    }
                    post("/create") { data ->
                        try {
                            val obj = data.bodyAsClass<UserCreate>()
                            val existingUser = userCollection.findOne(MongoSchemas.MongoUser::email eq obj.email)
                            if (existingUser == null) {
                                val user = MongoSchemas.MongoUser(
                                    identifier = Utils.generateID(),
                                    name = obj.name,
                                    email = obj.email,
                                    password = obj.password,
                                    token = StandardToken(
                                        ZonedDateTime.now(ZoneOffset.UTC).plusDays(6).toInstant(),
                                        mutableSetOf("LOGIN, GENERAL")
                                    ),
                                    created = Instant.now(),
                                    avatar = null,
                                    about = null,
                                    friends = ArrayList(),
                                    pending = ArrayList(),
                                    online = Utils.Constants.OnlineStates.OFFLINE.ordinal,
                                    status = null,
                                    badges = null,
                                    admin = false,
                                    enabled = true
                                )
                                userCollection.insertOne(user)
                                data.status(201).json(user.private)
                                return@post
                            } else {
                                data.status(403).json(object {
                                    val message = "User already exists"
                                    val error = 4005
                                    val reason = "You've already created a user"
                                })
                                return@post
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            data.status(400).json(object {
                                val message = "The request was unreadable"
                                val details = e.message
                            })
                            return@post
                        }
                    }
                    post("/login") { data ->
                        try {
                            val obj = data.bodyAsClass<UserGet>()
                            val user = userCollection.findOne(MongoSchemas.MongoUser::email eq obj.email)
                            if (user == null) {
                                data.status(404).json(object {
                                    val message = "Can't login"
                                    val error = 4004
                                    val reason = "Email or Password is incorrect"
                                })
                                return@post
                            }
                            if (user.password != obj.password) {
                                data.status(404).json(object {
                                    val message = "Can't login"
                                    val error = 4004
                                    val reason = "Email or Password is incorrect"
                                })
                                return@post
                            }
                            if (user.token.expired()) {
                                val newToken = StandardToken(
                                    ZonedDateTime.now(ZoneOffset.UTC).plusDays(6).toInstant(),
                                    user.token.permissions
                                )
                                userCollection.updateOne(
                                    MongoSchemas.MongoUser::identifier eq user.identifier,
                                    setValue(MongoSchemas.MongoUser::token, newToken)
                                )
                                userCollection.updateOne(
                                    MongoSchemas.MongoUser::identifier eq user.identifier,
                                    setValue(MongoSchemas.MongoUser::auth, newToken.token)
                                )
                                user.token = newToken
                                user.private.auth = newToken.token
                                data.status(201).json(user.private)
                                return@post
                            }
                            data.status(200).json(user.private)
                            return@post
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            return@post data.res.sendError(400, "The request was unreadable")
                        }
                    }
                    patch("/update") { data ->
                        val patchbodyAsClass = data.bodyAsClass<UpdatePatch>()
                        val auth = data.header("Authorization")
                        if (auth == null) {
                            data.status(403).json(object {
                                val message = "Missing Authorization"
                            })
                            return@patch
                        }
                        val user = userCollection.findOne(MongoSchemas.MongoUser::auth eq auth)
                        if (user == null) {
                            data.status(403).json(object {
                                val message = "Incorrect Authorization"
                            })
                            return@patch
                        }
                        if (patchbodyAsClass.name != null) user.name = patchbodyAsClass.name
                        if (patchbodyAsClass.enabled != null) user.enabled = patchbodyAsClass.enabled
                        user.about = patchbodyAsClass.about
                        if (patchbodyAsClass.avatar != null) {
                            storage.addItemToUserStorage(
                                if (patchbodyAsClass.avatar.animated) "gif" else "png",
                                patchbodyAsClass.avatar.data,
                                user.identifier,
                            )
                        }
                        if (patchbodyAsClass.status != null) user.status = patchbodyAsClass.status
                        data.status(200).json(user.getUpdatedPrivate())
                        userCollection.replaceOne(MongoSchemas.MongoUser::auth eq user.auth, user)
                        return@patch
                    }
                    /**
                     * Update self user
                     */
                    patch { data ->
                        val obj = data.bodyAsClass<UserPatch>()
                        val user = userCollection.findOne(MongoSchemas.MongoUser::auth eq obj.auth)
                        user?.token?.expireTime?.let { checkExpireAuth(data, it, obj.auth) }
                        if (user == null) {
                            data.status(400).json(object {
                                val message = "Can't find that user"
                                val error = 4004
                                val reason = "Either a user with that ID doesn't exist or the ID is invalid"
                            })
                            return@patch
                        }
                        when (obj.change.type) {
                            "all" -> {
                                val patched = obj.change.obj as MongoSchemas.MongoUser.Private
                                user.name = patched.name
                                user.online = patched.online
                                user.about = patched.about
                                userCollection.replaceOne(MongoSchemas.MongoUser::auth eq user.auth, user)
                                GlobalBus.post(WebsocketController.ProfileUpdate(user))
                                data.status(200).json(user.private)
                                return@patch
                            }
                            "name" -> {
                                user.name = obj.change.new
                                userCollection.updateOne(
                                    MongoSchemas.MongoUser::auth eq user.auth,
                                    setValue(MongoSchemas.MongoUser::name, user.name)
                                )
                                data.status(201).json(user.publicReduced)
                                GlobalBus.post(WebsocketController.ProfileUpdate(user))
                                return@patch
                            }
                            "avatar" -> {
                                val path = storage.addItemToUserStorage(
                                    if (obj.change.animated != null && obj.change.animated == true) "gif" else "png",
                                    obj.change.new,
                                    user.identifier
                                )
                                userCollection.findOne(MongoSchemas.MongoUser::auth eq user.auth)
                                    ?.let { data.status(201).json(it.private) }
                                GlobalBus.post(WebsocketController.ProfileUpdate(user))
                                return@patch
                            }
                            "about" -> {
                                user.about = obj.change.new
                                userCollection.updateOne(
                                    MongoSchemas.MongoUser::auth eq user.auth,
                                    setValue(MongoSchemas.MongoUser::about, user.about)
                                )
                                data.status(201).json(user.publicReduced)
                                GlobalBus.post(WebsocketController.ProfileUpdate(user))
                                return@patch
                            }
                            "status" -> {
                                val compiledJSON = gson.toJson(obj.change.obj)
                                val decompiledJSON = gson.fromJson(compiledJSON, Status::class.java)
                                var path = ""
                                if (decompiledJSON.icon != null && decompiledJSON.icon != user.status?.icon) {
                                    path = storage.addItemToGlobalStorage("jpg", decompiledJSON.icon, user.identifier)
                                }
                                user.status = Utils.Status(decompiledJSON.text, "https://chatty-api.feuer.tech/${path}")
                                userCollection.updateOne(
                                    MongoSchemas.MongoUser::auth eq user.auth,
                                    setValue(MongoSchemas.MongoUser::status, user.status)
                                )
                                data.status(201).json(user.publicReduced)
                                GlobalBus.post(WebsocketController.PresenceUpdate(user))
                                return@patch
                            }
                            else -> {
                                data.status(404).json(object {
                                    val message = "Change Type doesn't exist"
                                    val error = 4004
                                    val reason = "Type doesn't exist"
                                })
                            }
                        }
                    }
                    /**
                     * Get self user
                     * {
                     *      id: "",
                     *      name: "",
                     *      created: 0,
                     *      avatar: "",
                     *      online: 0,
                     *      status: {
                     *        text: "",
                     *        icon: ""
                     *      },
                     *      badges: null,
                     *      auth: ""
                     *      admin: true
                     * }
                     */
                    get { data ->
                        val user = userCollection.findOne(MongoSchemas.MongoUser::auth eq data.header("Authorization"))
                        user?.token?.expireTime?.let { checkExpireAuth(data, it, user.auth) }
                        if (user == null) {
                            data.status(400).json(object {
                                val message = "Can't find that user"
                                val error = 4004
                                val reason = "Either a user with that AUTH doesn't exist or the AUTH is invalid"
                            })
                            return@get
                        }
                        data.status(200).json(object {
                            val badges = user.badges
                            val id = user.identifier
                            val name = user.name
                            val status = user.status
                            val created = user.created
                            val avatar = user.avatar
                            val about = user.about
                            val admin = user.admin
                            val online = user.online
                        })
                        return@get
                    }


                    /**
                     * Get selfuser about
                     * {
                     *   about: "About Message"
                     * }
                     */
                    get("/about") { data ->
                        val user = userCollection.findOne(MongoSchemas.MongoUser::auth eq data.header("Authorization"))
                        user?.token?.expireTime?.let { checkExpireAuth(data, it, user.auth) }
                        if (user == null) {
                            data.status(400).json(object {
                                val message = "Can't find that user"
                                val error = 4004
                                val reason = "Either a user with that AUTH doesn't exist or the AUTH is invalid"
                            })
                            return@get
                        }
                        data.status(200).json(object {
                            val about = user.about
                        })
                        return@get
                    }
                    /**
                     * All endpoints for user relationships (friends)
                     */
                    path("relationships") {
                        /**
                         * See all relationships
                         */
                        get { data ->
                            val selfUser =
                                userCollection.findOne(MongoSchemas.MongoUser::auth eq data.header("Authorization"))
                            println(data.header("Authorization"))
                            selfUser?.token?.expireTime?.let { checkExpireAuth(data, it, selfUser.auth) }
                            val friends = ArrayList<MongoSchemas.MongoUser.Public>()
                            val pendings = ArrayList<MongoSchemas.MongoUser.Public>()
                            if (selfUser == null) {
                                data.status(400).json(object {
                                    val message = "Can't find that user"
                                    val error = 4004
                                    val reason = "Either a user with that AUTH doesn't exist or the AUTH is invalid"
                                })
                                return@get
                            }

                            for (user in selfUser.friends) {
                                val friend = userCollection.findOne(MongoSchemas.MongoUser::identifier eq user)
                                friend?.public?.let { friends.add(it) }
                            }
                            for (user in selfUser.pending) {
                                val pendingUser = userCollection.findOne(MongoSchemas.MongoUser::identifier eq user)
                                pendingUser?.public?.let { pendings.add(it) }
                            }
                            data.status(200).json(object {
                                val friends = friends
                                val pending = pendings
                            })
                        }
                        /**
                         * Delete a pending relationship
                         */
                        delete("/pending/{id}/deny") { data ->
                            val selfUser =
                                userCollection.findOne(MongoSchemas.MongoUser::auth eq data.header("Authorization"))
                            selfUser?.token?.expireTime?.let { checkExpireAuth(data, it, selfUser.auth) }
                            val friend =
                                userCollection.findOne(MongoSchemas.MongoUser::identifier eq data.pathParam("id"))
                            if (selfUser == null) {
                                data.status(400).json(object {
                                    val message = "Can't find that user"
                                    val error = 4004
                                    val reason = "Either a user with that AUTH doesn't exist or the AUTH is invalid"
                                })
                                return@delete
                            }
                            if (friend == null) {
                                data.status(400).json(object {
                                    val message = "Can't find that user"
                                    val error = 4004
                                    val reason = "A user with that ID doesn't exist"
                                })
                                return@delete
                            }
                            if (!selfUser.pending.contains(friend.identifier)) {
                                data.status(403).json(object {
                                    val message = "Can't accept that friend request"
                                    val error = 4003
                                    val reason =
                                        "That pending friend request couldn't be denied because \"${friend.identifier}\" doesn't exist in pending"
                                })
                                return@delete
                            }
                            selfUser.pending.remove(data.pathParam("id"))
                            userCollection.updateOne(
                                MongoSchemas.MongoUser::auth eq data.header("Authorization"),
                                setValue(MongoSchemas.MongoUser::pending, selfUser.pending)
                            )
                            data.status(204)
                            friend.let {
                                WebsocketController.FriendUpdate(
                                    it,
                                    selfUser,
                                    WebsocketController.FriendTypes.DENY
                                )
                            }
                                .let { GlobalBus.post(it) }
                            return@delete
                        }
                        post("/pending/{id}/accept") { data ->
                            val selfUser =
                                userCollection.findOne(MongoSchemas.MongoUser::auth eq data.header("Authorization"))
                            selfUser?.token?.expireTime?.let { checkExpireAuth(data, it, selfUser.auth) }
                            val friend =
                                userCollection.findOne(MongoSchemas.MongoUser::identifier eq data.pathParam("id"))
                            if (selfUser == null) {
                                data.status(400).json(object {
                                    val message = "Can't find that user"
                                    val error = 4004
                                    val reason = "Either a user with that AUTH doesn't exist or the AUTH is invalid"
                                })
                                return@post
                            }
                            if (friend == null) {
                                data.status(400).json(object {
                                    val message = "Can't find that user"
                                    val error = 4004
                                    val reason = "A user with that ID doesn't exist"
                                })
                                return@post
                            }
                            if (!selfUser.pending.contains(friend.identifier) && !selfUser.friends.contains(friend.identifier)) {
                                data.status(403).json(object {
                                    val message = "Can't accept that friend request"
                                    val error = 4003
                                    val reason = "No pending requests exist with that ID"
                                })
                                return@post
                            }
                            selfUser.pending.remove(friend.identifier)
                            if (selfUser.friends.contains(friend.identifier)) {
                                data.status(403).json(object {
                                    val message = "Can't accept that friend request"
                                    val error = 4003
                                    val reason =
                                        "That pending friend request couldn't be accepted because they are already friended"
                                })
                                return@post
                            }
                            selfUser.friends.add(friend.identifier)
                            friend.friends.add(selfUser.identifier)
                            userCollection.updateOne(
                                MongoSchemas.MongoUser::auth eq selfUser.auth,
                                setValue(MongoSchemas.MongoUser::pending, selfUser.pending)
                            )
                            userCollection.updateOne(
                                MongoSchemas.MongoUser::auth eq selfUser.auth,
                                setValue(MongoSchemas.MongoUser::friends, selfUser.friends)
                            )

                            userCollection.updateOne(
                                MongoSchemas.MongoUser::auth eq friend.auth,
                                setValue(MongoSchemas.MongoUser::friends, friend.friends)
                            )
                            data.status(201).json(friend.publicReduced)
                            GlobalBus.post(
                                WebsocketController.FriendUpdate(
                                    friend,
                                    selfUser,
                                    WebsocketController.FriendTypes.ACCEPT
                                )
                            )
                            val userSchema = DMCollection
                            val users = ArrayList<String>()
                            val messages = ArrayList<MongoSchemas.Message>()
                            users.add(selfUser.identifier)
                            users.add(friend.identifier)
                            userSchema.insertOne(MongoSchemas.DM(users, messages, Utils.generateID()))
                        }

                        /**
                         * Delete a relationship
                         */
                        delete("{id}") { data ->
                            val selfUser =
                                userCollection.findOne(MongoSchemas.MongoUser::auth eq data.header("Authorization"))
                            selfUser?.token?.expireTime?.let { checkExpireAuth(data, it, selfUser.auth) }
                            val friend =
                                userCollection.findOne(MongoSchemas.MongoUser::identifier eq data.pathParam("id"))
                            if (selfUser == null) {
                                data.status(400).json(object {
                                    val message = "Can't find that user"
                                    val error = 4004
                                    val reason = "Either a user with that AUTH doesn't exist or the AUTH is invalid"
                                })
                                return@delete
                            }
                            friend?.friends?.remove(selfUser.identifier)
                            selfUser.friends.remove(data.pathParam("id"))
                            userCollection.updateOne(
                                MongoSchemas.MongoUser::auth eq data.header("Authorization"),
                                setValue(MongoSchemas.MongoUser::friends, selfUser.friends)
                            )
                            data.status(204)
                            friend?.let {
                                WebsocketController.FriendUpdate(
                                    it,
                                    selfUser,
                                    WebsocketController.FriendTypes.REMOVED
                                )
                            }
                                ?.let { GlobalBus.post(it) }
                            return@delete
                        }
                        /**
                         * Add a relationship
                         */
                        put("{id}") { data ->
                            val selfUser =
                                userCollection.findOne(MongoSchemas.MongoUser::auth eq data.header("Authorization"))
                            selfUser?.token?.expireTime?.let { checkExpireAuth(data, it, selfUser.auth) }
                            if (selfUser == null) {
                                data.status(400).json(object {
                                    val message = "Can't find that user"
                                    val error = 4004
                                    val reason = "Either a user with that AUTH doesn't exist or the AUTH is invalid"
                                })
                                return@put
                            }
                            val friendableUser =
                                userCollection.findOne(MongoSchemas.MongoUser::identifier eq data.pathParam("id"))
                            if (friendableUser == null) {
                                data.status(404).json(object {
                                    val message = "Can't find that user"
                                    val error = 4004
                                    val reason =
                                        "Either a user with that ID doesn't exist or the user isn't accepting friends"
                                })
                                return@put
                            } else if (friendableUser.pending.contains(selfUser.identifier)) {
                                data.status(403).json(object {
                                    val message = "You are still pending!"
                                    val error = 4004
                                    val reason = "Wait for the user to friend you back."
                                })
                                return@put
                            } else if (friendableUser.friends.contains(selfUser.identifier) || selfUser.friends.contains(
                                    friendableUser.identifier
                                )
                            ) {
                                data.status(403).json(object {
                                    val reason = "You cannot friend a user twice!"
                                })
                                return@put
                            } else if (friendableUser.identifier == selfUser.identifier) {
                                data.status(403).json(object {
                                    val reason = "You cannot add yourself!"
                                })
                                return@put
                            }
                            friendableUser.pending.add(selfUser.identifier)
                            userCollection.updateOne(
                                MongoSchemas.MongoUser::auth eq friendableUser.auth,
                                setValue(MongoSchemas.MongoUser::pending, friendableUser.pending)
                            )
                            data.status(200).json(object {
                                val pending = true
                            })
                            GlobalBus.post(
                                WebsocketController.FriendUpdate(
                                    friendableUser,
                                    selfUser,
                                    WebsocketController.FriendTypes.PENDING
                                )
                            )
                            return@put
                        }
                    }
                    /**
                     * Route containing all endpoints that use /user/@me/channels/id
                     */
                    path("channels/{id}") {
                        /**
                         * Route containing all endpoints containing /user/@me/channels/id/messages
                         */
                        path("messages") {
                            get { data ->
                                val dm = data.pathParam("id")
                                val user =
                                    userCollection.findOne(MongoSchemas.MongoUser::auth eq data.header("Authorization"))!!
                                user.token.expireTime.let { checkExpireAuth(data, it, user.auth) }
                                val friend = userCollection.findOne(MongoSchemas.MongoUser::identifier eq dm)!!

                                try {
                                    data.queryParam("limit")?.toInt()
                                } catch (e: NumberFormatException) {
                                    data.status(400).json(object {
                                        val error = "Limit must be a number"
                                    })
                                    return@get
                                }
                                var limit = data.queryParam("limit")?.toInt() ?: 10
                                val after = data.queryParam("after")
                                val userSchema = DMCollection

                                for (doc in userSchema.find().toMutableList()) {
                                    if (doc.users.find { u -> u == friend.identifier } != null) {
                                        if (doc.users.find { u -> u == user.identifier } != null) {
                                            if (limit > doc.messages.size) limit = doc.messages.size
                                            if (doc.messages.isEmpty()) {
                                                data.status(204)
                                                return@get
                                            }
                                            if (after != null) {
                                                val msgs = ArrayList<MongoSchemas.Message>()
                                                for (s in 0 until doc.messages.indexOf(doc.messages.find { msg -> msg.identifier == after }) - 1) {
                                                    if (s >= doc.messages.size) break
                                                    doc.messages.remove(doc.messages[s])
                                                }
                                                for (s in 0 until limit) {
                                                    if (s >= doc.messages.size) break
                                                    msgs.add(doc.messages[s])
                                                }
                                                val populated = ArrayList<FleshedMessage>()
                                                for (msg in msgs) {
                                                    if (msg.user == user.identifier) {
                                                        populated.add(
                                                            FleshedMessage(
                                                                user.publicReduced,
                                                                msg.content,
                                                                SpecialDate(msg.created),
                                                                msg.identifier,
                                                                msg.embeds
                                                            )
                                                        )
                                                    } else {
                                                        populated.add(
                                                            FleshedMessage(
                                                                friend.publicReduced,
                                                                msg.content,
                                                                SpecialDate(msg.created),
                                                                msg.identifier,
                                                                msg.embeds
                                                            )
                                                        )
                                                    }

                                                }
                                                data.status(200).json(
                                                    Messages(
                                                        populated,
                                                        doc.messages.first().identifier
                                                    )
                                                )
                                                return@get
                                            } else {
                                                val message2 = ArrayList<FleshedMessage>()
                                                val values = doc.messages.toMutableList()
                                                val msgs = ArrayList<FleshedMessage>()
                                                for (msg in values) {
                                                    if (msg.user == user.identifier) {
                                                        message2.add(
                                                            FleshedMessage(
                                                                user.publicReduced,
                                                                msg.content,
                                                                SpecialDate(msg.created),
                                                                msg.identifier,
                                                                msg.embeds
                                                            )
                                                        )
                                                    } else {
                                                        message2.add(
                                                            FleshedMessage(
                                                                friend.publicReduced,
                                                                msg.content,
                                                                SpecialDate(msg.created),
                                                                msg.identifier,
                                                                msg.embeds
                                                            )
                                                        )
                                                    }
                                                }
                                                message2.reverse()
                                                for (s in 0 until limit) {
                                                    msgs.add(message2[s])
                                                }
                                                msgs.reverse()

                                                data.status(200).json(
                                                    Messages(
                                                        msgs,
                                                        doc.messages.first().identifier
                                                    )
                                                )
                                                return@get
                                            }
                                        }
                                    }
                                }
                            }
                            post { data ->
                                val dm = data.pathParam("id")
                                val obj = data.bodyAsClass<GenericMessage>()
                                val user =
                                    userCollection.findOne(MongoSchemas.MongoUser::auth eq data.header("Authorization"))!!
                                user.token.expireTime.let { checkExpireAuth(data, it, user.auth) }
                                val friend = userCollection.findOne(MongoSchemas.MongoUser::identifier eq dm)!!
                                println(friend)
                                println(user)
                                if (!friend.friends.contains(user.identifier)) {
                                    data.status(403).json(
                                        object {
                                            val message = "No affinity with user, unable to DM"
                                            val code = 4008
                                            val reason =
                                                "You don't seem to be friends with this user anymore, re-friend them!"
                                        }
                                    )
                                    return@post
                                }
                                val userSchema = DMCollection

                                for (doc in userSchema.find().toMutableList()) {
                                    if (doc.users.find { u -> u == friend.identifier } != null) {
                                        if (doc.users.find { u -> u == user.identifier } != null) {
                                            println(doc.identifier)
                                            val messages = doc.messages
                                            val msg = MongoSchemas.Message(
                                                doc.users.find { u -> u == user.identifier }!!,
                                                obj.content,
                                                Instant.now(),
                                                Utils.generateID(),
                                                obj.embeds
                                            )
                                            messages.add(
                                                msg
                                            )
                                            DMCollection
                                                .updateOne(
                                                    MongoSchemas.DM::identifier eq doc.identifier,
                                                    setValue(MongoSchemas.DM::messages, messages)
                                                )
                                            data.status(201).json(
                                                object {
                                                    val message = FleshedMessage(
                                                        user.publicReduced,
                                                        msg.content,
                                                        SpecialDate(msg.created),
                                                        msg.identifier,
                                                        msg.embeds
                                                    )
                                                })
                                            GlobalBus.post(
                                                WebsocketController.MessageCreate(
                                                    doc.users.find { u -> u == user.identifier }!!,
                                                    messages.last(),
                                                    doc.users
                                                )
                                            )
                                            return@post
                                        }
                                    }

                                }

                            }
                            delete("{msgid}") { data ->
                                val dm = data.pathParam("id")
                                val msgid = data.pathParam("msgid")
                                val user =
                                    userCollection.findOne(MongoSchemas.MongoUser::auth eq data.header("Authorization"))!!
                                user.token.expireTime.let { checkExpireAuth(data, it, user.auth) }
                                val friend = userCollection.findOne(MongoSchemas.MongoUser::identifier eq dm)!!
                                if (!friend.friends.contains(user.identifier)) {
                                    data.status(403).json(
                                        object {
                                            val message = "No affinity with user, unable to DM"
                                            val code = 4008
                                            val reason =
                                                "You don't seem to be friends with this user anymore, re-friend them!"
                                        }
                                    )
                                }
                                val userSchema = DMCollection
                                for (doc in userSchema.find().toMutableList()) {
                                    if (doc.users.find { u -> u == friend.identifier } != null) {
                                        if (doc.users.find { u -> u == user.identifier } != null) {
                                            val messageByID = doc.messages.find { m -> m.identifier == msgid }
                                            if (messageByID?.user != user.identifier) {
                                                data.status(403).json(object {
                                                    val message = "Can't remove a message you didn't send"
                                                    val code = 4008
                                                    val reason =
                                                        "You didn't send this message, therefore you can't remove it."
                                                })
                                            }
                                            doc.messages.remove(messageByID)
                                            DMCollection
                                                .updateOne(
                                                    MongoSchemas.DM::identifier eq doc.identifier,
                                                    setValue(MongoSchemas.DM::messages, doc.messages)
                                                )
                                            data.status(204)
                                            return@delete
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                get("{id}") { data ->
                    try {
                        val key = data.header("Authorization")
                        val reduce = data.queryParam("reduce")
                        val user = userCollection.findOne(MongoSchemas.MongoUser::identifier eq data.pathParam("id"))
                        if (user == null) {
                            data.status(400).json(object {
                                val message = "Can't find that user"
                                val error = 4004
                                val reason = "Either a user with that ID doesn't exist or the ID is invalid"
                            })
                            return@get
                        }
                        if (reduce == null || reduce == "false") {

                            if (key == null || userCollection.findOne(MongoSchemas.MongoUser::auth eq key) == null) {
                                data.status(403).json(object {
                                    val message = "Unauthorized"
                                    val error = 4008
                                    val reason = "Only reduce can be used with non authorized endpoints"
                                })
                                return@get
                            }
                            user.token.expireTime.let { checkExpireAuth(data, it, user.auth) }
                            data.status(200).json(user.public)

                        } else if (reduce == "true") {

                            data.status(200).json(user.publicReduced)
                        }
                        return@get
                    } catch (e: java.lang.Exception) {
                        data.status(400).json(object {
                            val message = "The request was unreadable"
                            val details = e.message
                        })
                    }
                }
            }
        }
        server.exception(Exception::class.java) { e, ctx ->
            println("Exception occurred in RestController. ${e.message}")
            e.printStackTrace()
        }
    }

    private fun checkExpireAuth(ctx: Context, expire: Instant, auth: String) {
        val user = userCollection.findOne(MongoSchemas.MongoUser::auth eq auth)
        if (user?.token?.expired() == true) {
            ctx.status(403).json(object {
                val error = 5000
                val message = "Expired Auth since $expire"
                val expire = expire.toString()
                val reason = "Auth expired on $expire renew by re-logging in"
            })
            return
        }
        return
    }

    data class ReleasePayload(val auth: String, val release: ChattyReleasePayload)
    data class ChattyReleasePayload(
        val tag: String,
        val title: String,
        val description: String,
        val prerelease: Boolean,
        val msi: String
    )

    class GenericMessage(val content: String, val embeds: ArrayList<Embed>?)

    init {
        events.subscribe<UpdateCallback> {
            classState.timing = it.timing.toInt()
            classState.reached = it.reached
        }
    }

    data class PatchAvatar(val animated: Boolean, val data: String)
    data class UpdatePatch(
        val name: String?,
        val avatar: PatchAvatar?,
        val about: String?,
        val enabled: Boolean?,
        val status: Utils.Status?
    )

    data class UpdateCallback(val reached: Int, val timing: Long)
}
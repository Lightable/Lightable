package rebase.controllers

import com.datastax.oss.driver.api.core.CqlSession
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mongodb.client.MongoCollection
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.*
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documentedContent
import io.javalin.plugin.openapi.dsl.oneOf
import me.kosert.flowbus.GlobalBus
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.json
import rebase.*
import rebase.cache.DMChannelCache
import rebase.cache.UserCache
import rebase.detection.NudeDetection
import rebase.interfaces.FailImpl
import rebase.interfaces.cache.IUserCache
import rebase.messages.DMDao
import rebase.messages.Message
import rebase.schema.*
import java.util.concurrent.ExecutorService
import kotlin.system.measureTimeMillis

class UserController(
    val userCache: UserCache,
    val dmCache: DMChannelCache,
    val inviteDB: MongoCollection<InviteCode>,
    val cqlSession: CqlSession,
    val snowflake: Snowflake,
    val executor: ExecutorService,
    val isProd: Boolean,
    val fileController: FileController,
    val napi: NudeDetection
) {
    @OpenApi(
        path = "/user",
        method = HttpMethod.POST,
        requestBody = OpenApiRequestBody(content = arrayOf(OpenApiContent(from = NewUser::class))),
        responses = [
            OpenApiResponse("201", content = [OpenApiContent(UserCreateToken::class, type = "application/json")]),
            OpenApiResponse("401", content = [OpenApiContent(UserAuthFail::class, type = "application/json")]),
            OpenApiResponse("400", content = [OpenApiContent(UserAuthFail::class, type = "application/json")]),
            OpenApiResponse("403", content = [OpenApiContent(UserAuthFail::class, type = "application/json")])
        ],
        summary = "Create User",
        description = "Creates a user with email, name, and password",
        tags = ["User"],
        operationId = "createUser"
    )
    fun createUser(ctx: Context) {
        val body = ctx.bodyAsClass<NewUser>()
        if (body.validate(inviteDB, userCache, ctx)) {
            if (userCache.users.values.find { user -> user.email == body.email } == null) {
                val salt = Utils.getNextSalt()
                val user =
                    User(
                        cache = userCache,
                        email = body.email,
                        password = Utils.getSHA512(body.password, salt),
                        salt = salt,
                        name = body.username,
                        identifier = snowflake.nextId(),
                        relationships = Friends()
                    )
                user.save()
                ctx.status(201)
                    .json(
                        object {
                            val token = user.token.token
                        }
                    )
                return
            } else {
                ctx.status(403).json(UserDataFail("Email is already in use"))
                return
            }
        }
    }

    fun createTestUsers(ctx: Context) {
        if (isProd) {
            ctx.json(UserDataFail("Nice Try :)"))
        } else {
            val count = ctx.queryParam("s")?.toInt() ?: 1
            for (i in 0 until count) {
                val userCreationTiming = measureTimeMillis {
                    val email = "${Constants.getRandomString(10)}@example.com"
                    val password = "TESTPASS_${Constants.getRandomString(20)}"
                    val user = User(
                        test = true,
                        email = email,
                        password = password,
                        cache = userCache,
                        identifier = snowflake.nextId()
                    )
                    user.save(false)
                }
                print("\rCreated user in ${userCreationTiming}ms ($i)")
            }
            println()
            ctx.status(201)
        }
    }

    fun removeTestUsers(ctx: Context) {
        if (isProd) {
            ctx.json(UserDataFail("Nice Try :)"))
        } else {
            userCache.removeAllTestUsers()
        }
    }

    fun getAllUsers(ctx: Context) {
        if (isProd) {
            ctx.json(UserDataFail("Nice Try :)"))
        } else {
            ctx.status(200).json(userCache.users.values)
        }
    }

    val getSelfUserDoc =
        document()
            .operation {
                it.description("Get Self User")
                it.operationId("getSelf")
                it.summary("Get Self")
                it.addTagsItem("Self")
            }
            .result("200", oneOf(documentedContent<PrivateUser>("json", false)))
            .result("401", oneOf(documentedContent<UserAuthFail>("json", false)))
            .header<String>("Authorization")

    fun getSelfUser(ctx: Context) {
        requireAuth(userCache, ctx).run {
            this?.let { it.toPrivate().let { it1 -> ctx.status(200).json(it1) } }
        }
    }

    val loginUserDoc =
        document()
            .body<UserLogin>()
            .operation {
                it.description("Login to user")
                it.operationId("loginUser")
                it.summary("Login with password and email to get user data and token")
                it.addTagsItem("Self")
            }
            .result("200", oneOf(documentedContent<PrivateUser>("json", false)))
            .result("401", oneOf(documentedContent<UserLoginFail>("json", false)))

    fun login(ctx: Context) {
        val info = ctx.bodyAsClass<UserLogin>()
        for (user in userCache.users.values) {
            if (user.email == info.email && user.password == Utils.getSHA512(info.password, user.salt)) {
                // Wait to get expiry working
                //                if (user.token.expired()) {
                //                    user.token =
                // StandardToken(ZonedDateTime.now(ZoneOffset.UTC).plusDays(6).toInstant(),
                // user.token.permissions)
                //                    ctx.header("Token", user.token.token)
                //                    ctx.header("Permissions",
                // user.token.permissions.joinToString(","))
                //                    executor.submit {
                //                        user.save()
                //                    }
                //                }
                ctx.status(200).json(user.toPrivate())
                return
            } else {
                continue
            }
        }
        ctx.status(401).json(UserLoginFail())
    }

    val patchUserDoc =
        document()
            .body<UpdateUserPatch>()
            .operation {
                it.description("Update user")
                it.operationId("updateUser")
                it.summary("Update User")
                it.addTagsItem("Self")
            }
            .result("200", oneOf(documentedContent<PrivateUser>("json", false)))
            .result("401", oneOf(documentedContent<UserDataFail>("json", false)))

    fun update(ctx: Context) {
        val user = requireAuth(userCache, ctx)
        val updatedUser = ctx.bodyAsClass<UpdateUserPatch>()
        if (user != null) {
            if (updatedUser.name != null && NewUser.validateUsername(updatedUser.name, userCache, ctx)) user.name =
                updatedUser.name
            if (updatedUser.status != null) user.status = updatedUser.status
            if (updatedUser.email != null && NewUser.validateEmail(updatedUser.email, ctx)) user.email =
                updatedUser.email
            if (updatedUser.notice != null) user.notice = updatedUser.notice
            if (updatedUser.profileOptions != null) {
                user.profileOptions?.keys?.forEach {
                    if (updatedUser.profileOptions.containsKey(it)) {
                        user.profileOptions!![it] = updatedUser.profileOptions[it]!!
                    }
                }
            }

            user.save()
            ctx.status(200).json(user.toPrivate())
            GlobalBus.post(FriendUpdatePayload(user.toPublic(), user.identifier, "user", user.toPublic()))
            GlobalBus.post(SelfUpdatePayload(user.toPublic(), "self", user.toPublic()))
            return
        }
    }

    fun updateAvatar(ctx: Context) {
        val user = requireAuth(userCache, ctx)
        if (user != null) {
            val file = ctx.uploadedFile("avatar")
            if (file == null) {
                ctx.status(400).json(UserDataFail("Form upload must contain image file"))
            } else {

                when (file.contentType) {
                    "image/gif" -> {
                        ctx.status(403).json(UserDataFail("Not yet supported"))
                        return
                    }
                    "image/png", "image/jpeg" -> {
                        try {
                            val image = Avatar(false, snowflake.nextId())
                            fileController.addAvatar(napi, user.identifier, image.identifier, file, "png")
                            user.avatar = image
                            user.save()
                            GlobalBus.post(
                                FriendUpdatePayload(
                                    user.toPublic(),
                                    user.toPublic().id,
                                    "user",
                                    user.toPublic()
                                )
                            )
                            GlobalBus.post(SelfUpdatePayload(user.toPublic(), "self", user.toPublic()))
                            GlobalBus.post(
                                FriendUpdatePayload(
                                    user.toPublic(),
                                    user.identifier,
                                    "user",
                                    user.toPublic()
                                )
                            )
                            ctx.json(201).json(user.toPublic())
                            return
                        } catch (e: InvalidAvatarException) {
                            ctx.status(403).json(object {
                                val code = "LEWD_AVATAR"
                                val message = e.message
                                val prediction = e.predictions
                            })
                            user.avatar = null
                            user.save()
                            return
                        }

                    }
                    else -> {
                        ctx.status(403).json(UserDataFail("Content type must be ImageType"))
                        return
                    }
                }
            }
        }
    }

    inner class Profiles {
        @OpenApi(
            path = "/profiles/{profile}",
            method = HttpMethod.GET,
            responses = [
                OpenApiResponse("400"),
                OpenApiResponse(
                    "403",
                    content = [OpenApiContent(UserNotEnabledFail::class, type = "application/json")]
                ),
                OpenApiResponse("200", content = [OpenApiContent(PublicUser::class, type = "application/json")])
            ],
            pathParams = [
                OpenApiParam(
                    name = "profile",
                    type = String::class,
                    description = "The user you want to find",
                    required = true,
                    allowEmptyValue = false,
                    isRepeatable = true
                )
            ],
            headers = [
                OpenApiParam(
                    name = "Authorization",
                    type = String::class,
                    description = "The Token authorization header",
                    required = false,
                    allowEmptyValue = false,
                    isRepeatable = false
                ),
            ],
            summary = "Get public profile",
            description = "Get profile, if public, or if friends with this user",
            tags = ["Profile", "User"]
        )
        fun getProfile(ctx: Context) {
            val user = requireAuthOptional(userCache, ctx)
            val profileKey = ctx.pathParam("name")
            val profile = userCache.users.values.find { u -> u.name == profileKey }
            if (profile == null) {
                ctx.status(400)
                return
            }
            if (!user.failed && profile.profileOptions?.get("IsPublic") == true) {
                ctx.status(200).json(profile.toPublic())
                return
            } else if (!user.failed && profile.relationships.friends.contains(user.user?.identifier)) {
                ctx.status(200).json(profile.toPublic())
                return
            }
            return
        }

        @OpenApi(
            path = "/admin/users/enabled",
            method = HttpMethod.GET,
            responses = [
                OpenApiResponse(
                    "200",
                    content = [OpenApiContent(DeveloperController.UserPagedPayload::class, type = "application/json")]
                ),
                OpenApiResponse(
                    "403",
                    content = [OpenApiContent(UserDataFail::class, type = "application/json")]
                )
            ],
            queryParams = [
                OpenApiParam(
                    name = "page",
                    type = Int::class,
                    description = "The page you want to get",
                    required = false,
                    allowEmptyValue = false,
                    isRepeatable = false
                )
            ],
            headers = [
                OpenApiParam(
                    name = "Authorization",
                    type = String::class,
                    description = "The token authorization header",
                    required = true,
                    allowEmptyValue = false,
                    isRepeatable = false
                ),
                OpenApiParam(
                    name = "search",
                    type = String::class,
                    description = "The search value",
                    required = false,
                    allowEmptyValue = true,
                    isRepeatable = false
                ),
            ],
            summary = "Get enabled users",
            description = "Get a paginated list of enabled users",
            tags = ["User", "Admin"],
            operationId = "adminGetEnabledUsers"
        )
        fun getProfiles(ctx: Context) {
            val user = requireAuth(userCache, ctx)
            val wantedPage = ctx.queryParam("page")?.toInt() ?: 0
            val search = ctx.queryParam("search")
            if (user != null) {
                val profilesAll = userCache.users.values.filter { u -> u.profileOptions?.get("IsPublic") == true }
                val profilesRaw = profilesAll.chunked(50)
                if (profilesRaw.size <= wantedPage) {
                    ctx.status(400).json(UserDataFail("Page exceeded length of ${profilesRaw.size - 1}"))
                    return
                }
                val selectedPage = profilesRaw[wantedPage]
                val profiles = mutableListOf<PublicUser>()
                if (search != null && search.isNotEmpty()) {
                    profilesAll.forEach {
                        profiles.add(it.toPublic())
                    }
                } else {
                    selectedPage.forEach {
                        profiles.add(it.toPublic())
                    }
                }
                val payload = DeveloperController.UserPagedPayload(profiles, profilesRaw.size)
                ctx.status(200).json(payload)
                return
            }
        }
    }

    inner class Relationships {
        val createPendingRelationshipDoc =
            document()
                .operation {
                    it.description("Create Relationship")
                    it.operationId("createRelationship")
                    it.summary("Create Relationship")
                    it.addTagsItem("Self")
                    it.addTagsItem("Relationship")
                }
                .result("201", oneOf(documentedContent<PublicUser>("json", true)))
                .result("204", null)
                .result("403", oneOf(documentedContent<RelationshipRequestFail>("json", false)))
                .result("401", oneOf(documentedContent<UserAuthFail>("json", false)))
                .header<String>("Authorization")
                .pathParam<String>("name")

        fun addRelationship(ctx: Context) {
            val user = requireAuth(userCache, ctx)
            val friend = userCache.users.values.find { u -> u.name == ctx.pathParam("name") }
            if (user != null) {
                if (friend == null) {
                    ctx.status(400).json(RelationshipRequestFail("Friend doesn't exist"))
                    return
                }

                if (!user.checkValidFriendRequest(friend.identifier)) {
                    ctx.status(403)
                        .json(
                            RelationshipRequestFail(
                                "You are already friends with this user or are already pending!"
                            )
                        )
                    return
                }
                if (friend.identifier == user.identifier) {
                    ctx.status(403).json(RelationshipRequestFail("You can't add yourself"))
                    return
                }
                if (user.relationships.pending.contains(friend.identifier) && friend.relationships.requests.contains(
                        user.identifier
                    )
                ) {
                    if (user.acceptRequest(friend.identifier)) {
                        val dmChannelID = snowflake.nextId()
                        val dmChannel = DMChannel(
                            dmCache,
                            identifier = dmChannelID,
                            users = mutableListOf(user.identifier, friend.identifier),
                            dao = DMDao("dm_${dmChannelID}", cqlSession)
                        )
                        dmChannel.dao!!.init()
                        println("Ran dao")
                        ctx.status(201).json(userCache.users[friend.identifier]?.toPublic()!!)
                        dmCache.saveOrReplaceChannel(dmChannel)
                        return
                    } else {
                        ctx.status(403).json(UserDataFail("${friend.identifier} isn't pending with you"))
                        return
                    }
                }
                user.addRequest(friend.identifier)
                ctx.status(201).json(friend.toPublic()).run {
                    user.save()
                    friend.save()
                }
                return
            }
        }

        val getSelfRelationshipsDoc =
            document()
                .operation {
                    it.description("Get Relationships")
                    it.operationId("getRelationships")
                    it.summary("Get Relationships")
                    it.addTagsItem("Self")
                    it.addTagsItem("Relationship")
                }
                .result("200", oneOf(documentedContent<PublicUser>("json", true)))
                .result("204", null)
                .result("401", oneOf(documentedContent<UserAuthFail>("json", false)))
                .header<String>("Authorization")

        fun getRelationships(ctx: Context) {
            val user = requireAuth(userCache, ctx)
            if (user != null) {
                val friends = user.getFriends()
                if (friends.isEmpty()) {
                    ctx.status(204)
                    return
                } else {
                    ctx.status(200).json(friends)
                    return
                }
            }
        }

        val getSelfRelationshipDoc =
            document()
                .operation {
                    it.description("Get Relationship")
                    it.operationId("getRelationship")
                    it.summary("Get Relationship")
                    it.addTagsItem("Self")
                    it.addTagsItem("Relationship")
                }
                .result("200", oneOf(documentedContent<PublicUser>("json", false)))
                .result("401", oneOf(documentedContent<UserAuthFail>("json", false)))
                .header<String>("Authorization")

        fun getRelationship(ctx: Context) {
            val user = requireAuth(userCache, ctx)
            val userPath = ctx.pathParam("id")
            val friend = userCache.users.values.find { u -> u.identifier == userPath.toLong() }
            if (user != null) {
                if (user.checkValidFriend(userPath.toLong()) && friend != null) {
                    ctx.status(200).json(friend.toPublic())
                } else {
                    ctx.status(403)
                        .json(UserDataFail("$userPath is not your friend. Add them first!"))
                }
            }
        }

        val removePendingRelationshipDoc =
            document()
                .operation {
                    it.description("Remove Pending Relationship")
                    it.operationId("removePendingRelationship")
                    it.summary("Remove Pending Relationship")
                    it.addTagsItem("Self")
                    it.addTagsItem("Relationship")
                }
                .result<Unit>("204")
                .result("403", oneOf(documentedContent<UserDataFail>("json", false)))
                .header<String>("Authorization")

        fun removePendingRelationship(ctx: Context) {
            val user = requireAuth(userCache, ctx)
            val pendingUser = ctx.pathParam("id")
            if (user != null) {
                if (user.relationships.pending.contains(pendingUser.toLong())) {
                    user.removePendingFriend(pendingUser.toLong())
                    ctx.status(204)
                } else {
                    ctx.status(403).json(UserDataFail("$pendingUser isn't pending with you"))
                }
            }
        }

        val addPendingRelationshipDoc =
            document()
                .operation {
                    it.description("Add Pending Relationship")
                    it.operationId("acceptPendingRelationship")
                    it.summary("Add Pending Relationship")
                    it.addTagsItem("Self")
                    it.addTagsItem("Relationship")
                }
                .result("201", oneOf(documentedContent<PublicUser>("json", false)))
                .result("403", oneOf(documentedContent<UserDataFail>("json", false)))
                .header<String>("Authorization")

        fun acceptPendingRelationship(ctx: Context) {
            val user = requireAuth(userCache, ctx)
            val pendingUser = ctx.pathParam("id")
            if (user != null) {
                if (user.acceptRequest(pendingUser.toLong())) {
                    val dmChannelID = snowflake.nextId()
                    val dmChannel = DMChannel(
                        dmCache,
                        identifier = dmChannelID,
                        users = mutableListOf(user.identifier, pendingUser.toLong()),
                        dao = DMDao("dm_${dmChannelID}", cqlSession)
                    )
                    dmChannel.dao!!.init()
                    ctx.status(201).json(userCache.users[pendingUser.toLong()]?.toPublic()!!)
                    dmCache.saveOrReplaceChannel(dmChannel)
                } else {
                    ctx.status(403).json(UserDataFail("$pendingUser isn't pending with you"))
                }
            }
        }

        val removeRelationshipDoc =
            document()
                .operation {
                    it.description("Remove Relationship")
                    it.operationId("deleteRelationship")
                    it.summary("Remove Relationship")
                    it.addTagsItem("Self")
                    it.addTagsItem("Relationship")
                }
                .result<Unit>("204")
                .result("403", oneOf(documentedContent<UserDataFail>("json", false)))
                .header<String>("Authorization")

        fun removeRelationship(ctx: Context) {
            val user = requireAuth(userCache, ctx)
            val relationshipID = ctx.pathParam("id")
            if (user != null) {
                // Inverse checking for pending / already friends
                if (!user.checkValidFriendRequest(relationshipID.toLong()) && !user.relationships.pending.contains(
                        relationshipID.toLong()
                    )
                ) {
                    user.removeFriend(relationshipID.toLong())
                    ctx.status(204)
                } else {
                    ctx.status(403).json(UserDataFail("You are only pending or are not friends with this user!"))
                }
            }
        }
    }

    inner class DMChannel {
        @OpenApi(
            path = "/user/@me/{id}/messages",
            method = HttpMethod.GET,
            responses = [
                OpenApiResponse(
                    "200",
                    content = [OpenApiContent(Message::class, isArray = true, type = "application/json")]
                ),
                OpenApiResponse(
                    "403",
                    content = [OpenApiContent(
                        DMChannelResponse.InvalidChannelPermissions::class,
                        type = "application/json"
                    )]
                ),
                OpenApiResponse(
                    "400",
                    content = [OpenApiContent(DMChannelResponse.TooManyMessages::class, type = "application/json")]
                ),
                OpenApiResponse(
                    "404",
                    content = [OpenApiContent(DMChannelResponse.ChannelDoesntExist::class, type = "application/json")]
                ),
                OpenApiResponse(
                    "501",
                    content = [OpenApiContent(DMChannelResponse.FeatureDoesntExist::class, type = "application/json")]
                )
            ],
            queryParams = [
                OpenApiParam(
                    name = "type",
                    type = DMChannelTypes::class,
                    description = "Type of channel you want to get messages from. FRIEND/GROUP",
                    required = true,
                    allowEmptyValue = false,
                    isRepeatable = false
                ),
                OpenApiParam(
                    name = "before",
                    type = Long::class,
                    description = "Optional, what messages do you want before the given message id",
                    required = false,
                    allowEmptyValue = false,
                    isRepeatable = false
                ),
                OpenApiParam(
                    name = "after",
                    type = Long::class,
                    description = "Optional, what messages do you want after the given message id",
                    required = false,
                    allowEmptyValue = false,
                    isRepeatable = false
                )
            ],
            summary = "Get messages from a DM channel",
            description = "Get up to 100 messages from a DM channel",
            tags = ["User", "Message", "Channel"],
            operationId = "DMChannelMessagesGet"
        )

        fun getMessages(ctx: Context) {
            val user = requireAuth(userCache, ctx)
            val id = ctx.pathParam("id").toLong()
            val after = ctx.queryParam("after")?.toLong()
            val before = ctx.queryParam("before")?.toLong()
            val limit = ctx.queryParam("limit")?.toInt() ?: 50

            if (user != null) {
                if (limit > 100) {
                    ctx.status(400).json(DMChannelResponse.TooManyMessages())
                    return
                }
                when (ctx.queryParam("type")) {
                    "FRIEND" -> {
                        val friend = userCache.users[id]
                        if (friend == null) {
                            ctx.status(404).json(DMChannelResponse.ChannelDoesntExist())
                            return
                        }
                        // First filter by users in the channel
                        val dmChannels = dmCache.channels.values.filter { c ->
                            c.users.containsAll(
                                listOf(
                                    user.identifier,
                                    friend.identifier
                                )
                            )
                        }
                        // Then by type of channel, in this case FRIEND
                        val friendChannel = dmChannels.find { c -> c.type == DMChannelTypes.FRIEND.ordinal }
                        // I don't know how this could be null but shit happens (。・・)ノ
                        if (friendChannel == null) {
                            ctx.status(404).json(DMChannelResponse.ChannelDoesntExist())
                            return
                        } else if (friendChannel.dao == null) {
                            ctx.status(500).json(DMChannelResponse.BrokenChannelContext())
                            return
                        } else {
                            if (after != null) {
                                val messages = friendChannel.dao!!.getMessagesAfterLastID(after, limit)
                                ctx.status(200).json(messages)
                                return
                            } else if (before != null) {
                                val messages = friendChannel.dao!!.getMessagesBeforeLastID(before, limit)
                                ctx.status(200).json(messages)
                                return
                            } else {
                                val messages = friendChannel.dao!!.getMessages(limit)
                                ctx.status(200).json(messages)
                                return
                            }
                        }
                    }
                    "GROUP" -> {
                        // TODO: Finish Group Channels
                        ctx.status(501).json(DMChannelResponse.FeatureDoesntExist())
                        return
                    }
                    else -> {
                        ctx.status(400).json(DMChannelResponse.ChannelTypeDoesntExist())
                        return
                    }
                }
            }
        }

        @OpenApi(
            path = "/user/@me/{id}/messages/send",
            method = HttpMethod.POST,
            requestBody = OpenApiRequestBody(content = arrayOf(OpenApiContent(from = MessageCreate::class))),
            responses = [
                OpenApiResponse("201", content = [OpenApiContent(Message::class, type = "application/json")]),
                OpenApiResponse(
                    "400",
                    content = [OpenApiContent(DMChannelResponse.ChannelDoesntExist::class, type = "application/json")]
                ),
                OpenApiResponse(
                    "403",
                    content = [OpenApiContent(
                        DMChannelResponse.InvalidChannelPermissions::class,
                        type = "application/json"
                    )]
                ),
                OpenApiResponse(
                    "404",
                    content = [OpenApiContent(DMChannelResponse.ChannelDoesntExist::class, type = "application/json")]
                ),
                OpenApiResponse(
                    "500",
                    content = [OpenApiContent(DMChannelResponse.BrokenChannelContext::class, type = "application/json")]
                ),
                OpenApiResponse(
                    "501",
                    content = [OpenApiContent(DMChannelResponse.FeatureDoesntExist::class, type = "application/json")]
                )
            ],
            pathParams = [
                OpenApiParam(
                    name = "id",
                    type = String::class,
                    description = "ID of the group channel or friend",
                    required = true,
                    allowEmptyValue = false,
                    isRepeatable = false
                )
            ],
            queryParams = [
                OpenApiParam(
                    name = "type",
                    type = DMChannelTypes::class,
                    description = "Type of channel you want to send a message to. FRIEND/GROUP",
                    required = true,
                    allowEmptyValue = false,
                    isRepeatable = false
                )
            ],
            headers = [
                OpenApiParam(
                    name = "Authorization",
                    type = String::class,
                    description = "The User token authorization header",
                    required = true,
                    allowEmptyValue = false,
                    isRepeatable = false
                )
            ],
            summary = "Send message to a DM channel",
            description = "Send a message to a valid dm channel",
            tags = ["User", "Message", "Channel"],
            operationId = "DMChannelMessageCreate"
        )
        fun sendMessage(ctx: Context) {
            val user = requireAuth(userCache, ctx)
            val id = ctx.pathParam("id").toLong()
            val createMessage = ctx.bodyAsClass<MessageCreate>()
            if (user != null) {
                when (ctx.queryParam("type")) {
                    "FRIEND" -> {
                        val friend = userCache.users[id]
                        if (friend == null) {
                            ctx.status(404).json(DMChannelResponse.ChannelDoesntExist())
                            return
                        }
                        // First filter by users in the channel
                        val dmChannels = dmCache.channels.values.filter { c ->
                            c.users.containsAll(
                                listOf(
                                    user.identifier,
                                    friend.identifier
                                )
                            )
                        }
                        // Then by type of channel, in this case FRIEND
                        val friendChannel = dmChannels.find { c -> c.type == DMChannelTypes.FRIEND.ordinal }
                        // I don't know how this could be null but shit happens (。・・)ノ
                        if (friendChannel == null) {
                            ctx.status(404).json(DMChannelResponse.ChannelDoesntExist())
                            return
                        } else if (friendChannel.dao == null) {
                            ctx.status(500).json(DMChannelResponse.BrokenChannelContext())
                            return
                        } else {
                            if (user.validFriendship(friend.identifier)) {
                                val message = Message(
                                    id = snowflake.nextId(),
                                    content = createMessage.content,
                                    author = user.identifier
                                )
                                val messageCreateTiming = measureTimeMillis {
                                    friendChannel.dao!!.createMessage(message)
                                }
                                ctx.status(201).json(message).header(
                                    "Server-Timing",
                                    "creation;desc=\"Message creation time on DB\";dur=${messageCreateTiming}"
                                )
                                GlobalBus.post(ServerMessageCreate(friend.identifier, message))
                                return
                            } else {
                                ctx.status(403).json(DMChannelResponse.InvalidChannelPermissions())
                                return
                            }
                        }

                    }
                    "GROUP" -> {
                        // TODO: Finish Group Channels
                        ctx.status(501).json(DMChannelResponse.FeatureDoesntExist())
                        return
                    }
                    else -> {
                        ctx.status(400).json(DMChannelResponse.ChannelTypeDoesntExist())
                    }
                }
            }
        }

        fun deleteMessage(ctx: Context) {
            val user = requireAuth(userCache, ctx)
            val id = ctx.pathParam("id").toLong()
            val messageID = ctx.queryParam("messageID")?.toLong()
            if (user != null) {
                if (messageID == null) {
                    ctx.status(400)
                    return
                }
                when (ctx.queryParam("type")) {
                    "FRIEND" -> {
                        val friend = userCache.users[id]
                        if (friend == null) {
                            ctx.status(404).json(DMChannelResponse.ChannelDoesntExist())
                            return
                        }
                        // First filter by users in the channel
                        val dmChannels = dmCache.channels.values.filter { c ->
                            c.users.containsAll(
                                listOf(
                                    user.identifier,
                                    friend.identifier
                                )
                            )
                        }
                        // Then by type of channel, in this case FRIEND
                        val friendChannel = dmChannels.find { c -> c.type == DMChannelTypes.FRIEND.ordinal }
                        // I don't know how this could be null but shit happens (。・・)ノ
                        if (friendChannel == null) {
                            ctx.status(404).json(DMChannelResponse.ChannelDoesntExist())
                            return
                        } else if (friendChannel.dao == null) {
                            ctx.status(500).json(DMChannelResponse.BrokenChannelContext())
                            return
                        } else {
                            val message = friendChannel.dao!!.getByID(messageID)
                            if (message == null) {
                                ctx.status(404)
                                return
                            } else {
                                if (message.author != user.identifier) {
                                    ctx.status(403).json(DMChannelResponse.MissingPermissions())
                                    return
                                } else {
                                    friendChannel.dao!!.delete(messageID, {
                                        ctx.status(204).header(
                                            "Server-Timing",
                                            "creation;desc=\"Message deletion time on DB\";dur=${it}"
                                        )
                                        return@delete
                                    }, {
                                        ctx.status(500).json(DMChannelResponse.BrokenChannelContext())
                                        return@delete
                                    })
                                }
                            }
                        }
                    }
                    "GROUP" -> {
                        // TODO: Finish Group Channels
                        ctx.status(501).json(DMChannelResponse.FeatureDoesntExist())
                        return
                    }
                    else -> {
                        ctx.status(400).json(DMChannelResponse.ChannelTypeDoesntExist())
                    }
                }
            }
        }
    }

    object DMChannelResponse {
        class MissingPermissions : FailImpl {
            override val code: String
                get() = "MISSING_PERMISSIONS"
            override val message: String
                get() = "You're missing permissions to perform this action on the channel"
        }

        class TooManyMessages : FailImpl {
            override val code: String
                get() = "TOO_MANY_MESSAGES"
            override val message: String
                get() = "For your current account you can only request up to 100 messages (?limit=0-100)"
        }

        class ChannelTypeDoesntExist : FailImpl {
            override val code: String
                get() = "CHANNEL_TYPE_DOESNT_EXIST"
            override val message: String
                get() = "This channel type does not exist. The valid channel types are: [FRIEND, GROUP]"
        }

        class FeatureDoesntExist : FailImpl {
            override val code: String
                get() = "FEATURE_DOESNT_EXIST"
            override val message: String
                get() = "This feature has yet to implemented or allowed for the general public, check back later"
        }

        class BrokenChannelContext : FailImpl {
            override val code: String
                get() = "INVALID_CHANNEL"
            override val message: String
                get() = "Something went wrong when trying to access this channel"
        }

        class InvalidChannelPermissions : FailImpl {
            override val code: String
                get() = "INVALID_CHANNEL_PERMISSIONS"
            override val message: String
                get() = "You are not friends with this user or they have blocked you"
        }

        class ChannelDoesntExist : FailImpl {
            override val code: String
                get() = "CHANNEL_NOT_FOUND"
            override val message: String
                get() = "The channel was recently deleted or is an invalid id"
        }
    }


    class UserAuthFail : FailImpl {
        override val code: String
            get() = "AUTH_USER_FAIL"
        override val message: String
            get() = "Your authorization is incorrect or missing"
    }

    class UserLoginFail : FailImpl {
        override val code: String
            get() = "USER_LOGIN_FAIl"
        override val message: String
            get() = "Email or password was not found"
    }

    class UserDataFail(val bad: String) : FailImpl {
        override val code: String
            get() = "DATA_USER_FAIL"
        override val message = bad
    }

    data class RelationshipRequestFail(val bad: String) : FailImpl {
        override val code: String
            get() = "FRIEND_REQUEST_FAIL"
        override val message = bad
    }

    class UserNotEnabledFail : FailImpl {
        override val code: String
            get() = "USER_ENABLED_FALSE"
        override val message: String
            get() = "User is not enabled, wait for an admin to enable your account"
    }

    data class UpdateUserPatch(
        val name: String?,
        val email: String?,
        val notice: UserNotice?,
        val status: Status?,
        val profileOptions: MutableMap<String, Boolean>?
    )

    data class UserCreateToken(val token: String)
    data class UserLogin(val email: String, val password: String)
    data class MessageCreate(val content: String)
    data class NewUser(val code: String, val email: String, val password: String, val username: String) {

        fun validate(inviteDB: MongoCollection<InviteCode>, userCache: UserCache, ctx: Context): Boolean {
            val emailVal = validateEmail(email, ctx)
            val passwordVal = validatePassword(password, ctx)
            val usernameVal = validateUsername(username, userCache, ctx)
            val validateCodeVal = validateCode(inviteDB, code, email, ctx)
            return emailVal.and(passwordVal).and(usernameVal).and(validateCodeVal)
        }

        companion object {


            fun validateCode(
                invites: MongoCollection<InviteCode>,
                code: String,
                email: String,
                ctx: Context?
            ): Boolean {
                val codeInv = invites.findOne(InviteCode::email eq email)?.code
                if (codeInv != null && codeInv == code) {
                    return true
                }
                ctx?.status(403)?.json(UserDataFail("Code required"))
                return false
            }

            fun validatePassword(password: String, ctx: Context?): Boolean {
                if (password.isBlank() || password.length < 6) {
                    ctx?.status(400)?.json(UserDataFail("Password is empty or less than 6 characters"))
                    return false
                }
                return true
            }

            fun validateEmail(email: String, ctx: Context?): Boolean {
                if (!email.matches(Constants.REGEX.EMAIL)) {
                    ctx?.status(400)?.json(UserDataFail("Email is not correct format"))
                    return false
                }
                return true
            }

            fun validateUsername(username: String, cache: IUserCache, ctx: Context?): Boolean {
                if (username.isBlank()) {
                    ctx?.status(400)?.json(UserDataFail("Username is blank"))
                    return false
                } else if (cache.sameName(username)) {
                    ctx?.status(400)?.json(UserDataFail("Username is already used"))
                    return false
                }
                return true
            }
        }
    }
}

fun requireAuth(userCache: UserCache, ctx: Context): User? {
    val auth = ctx.header("Authorization")
    if (auth == null) {
        ctx.status(401).json(UserController.UserAuthFail())
    } else {
        val user = userCache.users.values.find { user -> user.token.token == auth }
        return if (user == null) {
            ctx.status(401).json(UserController.UserAuthFail())
            null
        } else if (!user.enabled) {
            ctx.status(403).json(UserController.UserNotEnabledFail())
            null
        } else {
            user
        }
    }
    return null
}

fun requireAuthOptional(userCache: UserCache, ctx: Context): PotentialUser {
    val auth = ctx.header("Authorization")
    if (auth == null) {
        ctx.header("With-No-Auth", true.toString())
        return PotentialUser(null, false)
    } else {
        val user = userCache.users.values.find { user -> user.token.token == auth }
        if (user == null) {
            ctx.header("With-No-User", true.toString())
            return PotentialUser(null, false)
        } else if (!user.enabled) {
            ctx.status(403).json(UserController.UserNotEnabledFail())
            return PotentialUser(null, true)
        }
    }
    return PotentialUser(null, true)
}

data class PotentialUser(val user: User?, val failed: Boolean)

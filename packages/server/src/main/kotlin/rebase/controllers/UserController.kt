package rebase.controllers

import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.*
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documentedContent
import io.javalin.plugin.openapi.dsl.oneOf
import me.kosert.flowbus.GlobalBus
import rebase.*
import rebase.interfaces.FailImpl
import rebase.interfaces.cache.IUserCache
import rebase.schema.*
import java.util.concurrent.ExecutorService
import kotlin.system.measureTimeMillis

class UserController(
    val cache: Cache,
    val snowflake: Snowflake,
    val executor: ExecutorService,
    val isProd: Boolean,
    val fileController: FileController
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
        println("Users -> ${cache.users.values}\nPersonal Email -> ${body.email}")
        println("Cache users -> Body.email -> ${cache.users.values.find { user -> user.email == body.email } == null}")
        println("Body validate -> ${body.validate(cache, ctx)}")
        if (body.validate(cache, ctx)) {
            if (cache.users.values.find { user -> user.email == body.email } == null) {
                val salt = Utils.getNextSalt()
                val user =
                    User(
                        cache = cache,
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
                        cache = cache,
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
            cache.removeAllTestUsers()
        }
    }

    fun getAllUsers(ctx: Context) {
        if (isProd) {
            ctx.json(UserDataFail("Nice Try :)"))
        } else {
            ctx.status(200).json(cache.users.values)
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
        requireAuth(cache, ctx).run {
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
        for (user in cache.users.values) {
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
        val user = requireAuth(cache, ctx)
        val updatedUser = ctx.bodyAsClass<UpdateUserPatch>()
        if (user != null) {
            if (updatedUser.name != null && NewUser.validateUsername(updatedUser.name, cache, ctx)) user.name =
                updatedUser.name
            if (updatedUser.status != null) user.status = updatedUser.status
            if (updatedUser.email != null && NewUser.validateEmail(updatedUser.email, ctx)) user.email =
                updatedUser.email
            if (updatedUser.notice != null) user.notice = updatedUser.notice
            user.save()
            ctx.status(200).json(user.toPublic())
            GlobalBus.post(FriendUpdatePayload(user.toPublic(), user.identifier, "user", user.toPublic()))
            GlobalBus.post(SelfUpdatePayload(user.toPublic(), "self", user.toPublic()))
            return
        }
    }

    fun updateAvatar(ctx: Context) {
        val user = requireAuth(cache, ctx)
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
                        val image = Avatar(false, snowflake.nextId())
                        fileController.addAvatar(user.identifier, image.identifier, file, "png")
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
                        GlobalBus.post(FriendUpdatePayload(user.toPublic(), user.identifier, "user", user.toPublic()))
                        ctx.json(201).json(user.toPublic())

                        return
                    }
                    else -> {
                        ctx.status(403).json(UserDataFail("Content type must be ImageType"))
                        return
                    }
                }
            }
        }
    }

    inner class Relationships {
        val createPendingRelationshipDoc =
            document()
                .operation {
                    it.description("Create Pending Relationship")
                    it.operationId("createRelationship")
                    it.summary("Create Pending Relationship")
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
            val user = requireAuth(cache, ctx)
            val friend = cache.users.values.find { u -> u.name == ctx.pathParam("name") }
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
                } else {
                    if (friend.identifier == user.identifier) {
                        ctx.status(403).json(RelationshipRequestFail("You can't add yourself"))
                        return
                    }
                    user.addRequest(friend.identifier)
                    ctx.status(201).json(friend.toPublic()).run {
                        user.save()
                        friend.save()
                    }
                    return
                }
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
            val user = requireAuth(cache, ctx)
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
            val user = requireAuth(cache, ctx)
            val userPath = ctx.pathParam("id")
            val friend = cache.users.values.find { u -> u.identifier == userPath.toLong() }
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
            val user = requireAuth(cache, ctx)
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
            val user = requireAuth(cache, ctx)
            val pendingUser = ctx.pathParam("id")
            if (user != null) {
                if (user.acceptRequest(pendingUser.toLong())) {
                    ctx.status(201).json(cache.users[pendingUser.toLong()]?.toPublic()!!)
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
            val user = requireAuth(cache, ctx)
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
    )

    data class UserCreateToken(val token: String)
    data class UserLogin(val email: String, val password: String)

    data class NewUser(val email: String, val password: String, val username: String) {

        fun validate(cache: Cache, ctx: Context): Boolean {
            val emailVal = validateEmail(email, ctx)
            println("Email validation: ${emailVal}")
            val passwordVal = validatePassword(password, ctx)
            println("Password validation: ${passwordVal}")
            val usernameVal = validateUsername(username, cache, ctx)
            println("Username validation: ${usernameVal}")
            return emailVal.and(passwordVal).and(usernameVal)
        }

        companion object {

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

fun requireAuth(cache: Cache, ctx: Context): User? {
    val auth = ctx.header("Authorization")
    cache.users.values.forEach {
        println("User ${it.name} (${it.identifier}) - ${it.token.token} Compare $auth")
    }
    if (auth == null) {
        ctx.status(401).json(UserController.UserAuthFail())
    } else {
        val user = cache.users.values.find { user -> user.token.token == auth }
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

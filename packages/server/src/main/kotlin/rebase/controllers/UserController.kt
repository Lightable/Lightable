package rebase.controllers

import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documentedContent
import io.javalin.plugin.openapi.dsl.oneOf
import java.util.concurrent.ExecutorService
import rebase.*
import rebase.interfaces.FailImpl

class UserController(val cache: Cache, val snowflake: Snowflake, val executor: ExecutorService) {
    val createUserDoc =
        document()
            .body<NewUser> {
                it.required = true
                it.description = "Contains data to create the user"
            }
            .operation {
                it.description("Create User")
                it.operationId("createUser")
                it.summary("Create User")
                it.addTagsItem("User")
            }
            .result("201", oneOf(documentedContent<String>("json", false)))
            .result("401", oneOf(documentedContent<UserAuthFail>("json", false)))
            .result("400", oneOf(documentedContent<UserDataFail>("json", false)))
            .result("403", oneOf(documentedContent<UserDataFail>("json", false)))

    fun createUser(ctx: Context) {
        val body = ctx.bodyAsClass<NewUser>()
        if (body.validate(ctx) &&
            cache.users.values.find { user -> user.email == body.email } == null
        ) {
            val user =
                User(
                    cache = cache,
                    email = body.email,
                    password = body.password,
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
            if (user.email == info.email && user.password == info.password) {
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

        fun addRelationship(ctx: Context) {
            val user = requireAuth(cache, ctx)
            val friend =
                cache.users.values.find { u -> u.identifier == ctx.pathParam("id").toLong() }
            if (user != null) {
                if (!user.checkValidFriendRequest(ctx.pathParam("id").toLong())) {
                    ctx.status(403)
                        .json(
                            RelationshipRequestFail(
                                "You are already friends with this user or are already pending!"
                            )
                        )
                    return
                } else if (friend != null) {
                    friend.addRequest(user.identifier)
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
                if (!user.checkValidFriendRequest(relationshipID.toLong()) &&
                    !user.relationships.pending.contains(relationshipID.toLong())
                ) {
                    user.removeFriend(relationshipID.toLong())
                    ctx.status(204)
                } else {
                    ctx.status(403)
                        .json(
                            UserDataFail("You are only pending or are not friends with this user!")
                        )
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



    data class UserLogin(val email: String, val password: String)

    data class NewUser(val email: String, val password: String, val username: String) {
        fun validate(ctx: Context): Boolean {
            val emailVal = validateEmail(ctx)
            val passwordVal = validatePassword(ctx)
            val usernameVal = validateUsername(ctx)
            return emailVal.and(passwordVal).and(usernameVal)
        }

        private fun validatePassword(ctx: Context): Boolean {
            if (password.isBlank() || password.length < 6) {
                ctx.status(400).json(UserDataFail("Password is empty or less than 6 characters"))
                return false
            }
            return true
        }

        private fun validateEmail(ctx: Context): Boolean {
            if (!email.matches(Constants.REGEX.EMAIL)) {
                ctx.status(400).json(UserDataFail("Email is not correct format"))
                return false
            }
            return true
        }

        private fun validateUsername(ctx: Context): Boolean {
            if (username.isBlank()) {
                ctx.status(400).json(UserDataFail("Username is blank"))
                return false
            }
            return true
        }
    }

    init {}
}
fun requireAuth(cache: Cache, ctx: Context): rebase.User? {
    val auth = ctx.header("Authorization")
    cache.users.values.forEach {
        println("User ${it.name} (${it.identifier}) - ${it.token.token} Compare $auth")
    }
    if (auth == null) {
        ctx.status(401).json(UserController.UserAuthFail())
    } else {
        val user = cache.users.values.find { user -> user.token.token == auth }
        return if (user == null || !user.enabled) {
            ctx.status(401).json(UserController.UserAuthFail())
            null
        } else {
            user
        }
    }
    return null
}

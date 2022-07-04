package rebase.controllers

import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.*
import me.kosert.flowbus.EventsReceiver
import me.kosert.flowbus.GlobalBus
import me.kosert.flowbus.subscribe
import org.joda.time.Instant
import rebase.cache.UserCache
import rebase.schema.ChattyRelease
import rebase.schema.PublicUser

class DeveloperController(val userCache: UserCache) {
    val events = EventsReceiver()

    @OpenApi(
        path = "/admin/disable/{id}",
        method = HttpMethod.PATCH,
        responses = [
            OpenApiResponse("204")
        ],
        headers = [
            OpenApiParam(
                name = "Authorization",
                type = String::class,
                description = "The Admin token authorization header",
                required = true,
                allowEmptyValue = false,
                isRepeatable = false
            )
        ],
        pathParams = [
            OpenApiParam(
                name = "id",
                type = Int::class,
                description = "ID of the user you want to disable",
                required = true,
                allowEmptyValue = false,
                isRepeatable = false
            )
        ],
        summary = "Disable user",
        description = "Disable a user with the given id",
        tags = ["User", "Admin"],
        operationId = "adminDisableUser"
    )
    fun disableUser(ctx: Context) {
        val user = requireAuth(userCache, ctx)
        val userPath = ctx.pathParam("id")
        val userExt = userCache.users[userPath.toLong()]
        if (user != null && user.admin && userExt != null) {
            userExt.enabled = false
            userExt.save()
            ctx.status(204)
        } else {
            ctx.status(400).json(UserController.UserDataFail("User doesn't exist or you are not an Admin"))
        }
    }

    @OpenApi(
        path = "/admin/enable/{id}",
        method = HttpMethod.PATCH,
        responses = [
            OpenApiResponse("204"),
            OpenApiResponse(
                "403",
                content = [OpenApiContent(UserController.UserDataFail::class, type = "application/json")]
            )
        ],
        headers = [
            OpenApiParam(
                name = "Authorization",
                type = String::class,
                description = "The Admin token authorization header",
                required = true,
                allowEmptyValue = false,
                isRepeatable = false
            )
        ],
        pathParams = [
            OpenApiParam(
                name = "id",
                type = Int::class,
                description = "ID of the user you want to enable",
                required = true,
                allowEmptyValue = false,
                isRepeatable = false
            )
        ],
        summary = "Enable user",
        description = "Enable a user with the given id",
        tags = ["User", "Admin"],
        operationId = "adminEnableUser"
    )
    fun enableUser(ctx: Context) {
        val user = requireAuth(userCache, ctx)
        val userPath = ctx.pathParam("id")
        val userExt = userCache.users[userPath.toLong()]
        if (user != null && user.admin && userExt != null) {
            userExt.enabled = true
            userExt.save()
            ctx.status(204)
        } else {
            ctx.status(400).json(UserController.UserDataFail("User doesn't exist or you are not an Admin"))
        }
    }

    @OpenApi(
        path = "/admin/delete/{id}",
        method = HttpMethod.DELETE,
        responses = [
            OpenApiResponse("204"),
            OpenApiResponse(
                "403",
                content = [OpenApiContent(UserController.UserDataFail::class, type = "application/json")]
            )
        ],
        headers = [
            OpenApiParam(
                name = "Authorization",
                type = String::class,
                description = "The Admin token authorization header",
                required = true,
                allowEmptyValue = false,
                isRepeatable = false
            )
        ],
        summary = "Delete user",
        description = "Delete a user with the given id",
        tags = ["User", "Admin"],
        operationId = "adminDeleteUser"
    )
    fun deleteUser(ctx: Context) {
        val user = requireAuth(userCache, ctx)
        val userPath = ctx.pathParam("id")
        if (user != null && user.admin) {
            userCache.deleteUser(userPath.toLong())
            ctx.status(204)
        } else {
            ctx.status(403).json(UserController.UserDataFail("User doesn't exist or you are not an Admin"))
        }
    }

    @OpenApi(
        path = "/admin/users/enabled",
        method = HttpMethod.GET,
        responses = [
            OpenApiResponse(
                "200",
                content = [OpenApiContent(UserController.UserDataFail::class, type = "application/json")]
            ),
            OpenApiResponse(
                "403",
                content = [OpenApiContent(UserController.UserDataFail::class, type = "application/json")]
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
                description = "The Admin token authorization header",
                required = true,
                allowEmptyValue = false,
                isRepeatable = false
            )
        ],
        summary = "Get enabled users",
        description = "Get a paginated list of enabled users",
        tags = ["User", "Admin"],
        operationId = "adminGetEnabledUsers"
    )
    fun getEnabledUsers(ctx: Context) {
        val user = requireAuth(userCache, ctx)
        val wantedPage = ctx.queryParam("page")?.toInt() ?: 0
        if (user != null && user.admin) {
            val enabledUsersRaw = userCache.users.values.filter { u -> u.enabled }.chunked(50)
            if (enabledUsersRaw.size <= wantedPage) {
                ctx.status(400).json(UserController.UserDataFail("Page exceeded length of ${enabledUsersRaw.size - 1}"))
                return
            }
            val selectedPage = enabledUsersRaw[wantedPage]
            val enabledUsers = mutableListOf<PublicUser>()
            selectedPage.forEach {
                enabledUsers.add(it.toPublic())
            }
            val payload = UserPagedPayload(enabledUsers, enabledUsersRaw.size)
            ctx.status(200).json(payload)
            return
        } else {
            ctx.status(403).json(UserController.UserDataFail("User doesn't exist or you are not an Admin"))
            return
        }
    }
    @OpenApi(
        path = "/admin/users/disabled",
        method = HttpMethod.GET,
        responses = [
            OpenApiResponse(
                "200",
                content = [OpenApiContent(UserController.UserDataFail::class, type = "application/json")]
            ),
            OpenApiResponse(
                "403",
                content = [OpenApiContent(UserController.UserDataFail::class, type = "application/json")]
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
                description = "The Admin token authorization header",
                required = true,
                allowEmptyValue = false,
                isRepeatable = false
            )
        ],
        summary = "Get disabled users",
        description = "Get a paginated list of disabled users",
        tags = ["User", "Admin"],
        operationId = "adminGetDisabledUsers"
    )
    fun getDisabledUsers(ctx: Context) {
        val user = requireAuth(userCache, ctx)
        val wantedPage = ctx.queryParam("page")?.toInt() ?: 0
        if (user != null && user.admin) {
            val disabledUsersRaw = userCache.users.values.filter { u -> !u.enabled }.chunked(50)
            if (disabledUsersRaw.size <= wantedPage && disabledUsersRaw.isNotEmpty()) {
                ctx.status(400).json(UserController.UserDataFail("Page exceeded length of ${disabledUsersRaw.size - 1}"))
                return
            } else if (disabledUsersRaw.isEmpty()) {
                ctx.status(204)
                return
            }
            val selectedPage = disabledUsersRaw[wantedPage]
            val disabledUsers = mutableListOf<PublicUser>()
            selectedPage.forEach {
                disabledUsers.add(it.toPublic())
            }
            val payload = UserPagedPayload(disabledUsers, disabledUsers.size-1)
            ctx.status(200).json(payload)
            return
        } else {
            ctx.status(403).json(UserController.UserDataFail("User doesn't exist or you are not an Admin"))
            return
        }
    }
    @OpenApi(
        path = "/admin/delete/{id}",
        method = HttpMethod.DELETE,
        responses = [
            OpenApiResponse("201", content = [OpenApiContent(ChattyRelease::class, type = "application/json")]),
            OpenApiResponse(
                "403",
                content = [OpenApiContent(UserController.UserDataFail::class, type = "application/json")]
            )
        ],
        headers = [
            OpenApiParam(
                name = "Authorization",
                type = String::class,
                description = "The Admin token authorization header",
                required = true,
                allowEmptyValue = false,
                isRepeatable = false
            )
        ],
        summary = "Delete user",
        description = "Delete a user with the given id",
        tags = ["User", "Admin"],
        operationId = "adminDeleteUser"
    )
    fun createRelease(ctx: Context) {
        val user = requireAuth(userCache, ctx)
        val body = ctx.bodyAsClass<ReleasePayload>()
        if (user != null && user.admin) {
            if (!body.checkForBlank()) {
                ctx.status(400).json("The following must not be blank: 'version','title','url','signature'")
                return
            }
            val release = ChattyRelease(
                body.version.replace(".", "").toInt(),
                body.version,
                body.title,
                body.notes,
                body.signature,
                body.url
            )
            userCache.saveOrReplaceRelease(release)
            ctx.status(201).json(release)
            GlobalBus.post(ServerUpdate(release))
        } else {
            ctx.status(403).json(UserController.UserDataFail("You are not an Admin"))
        }
    }

    init {
        events.subscribe<ConnectedClient> {
            when (it.inc) {
                true -> connectedClients.inc()
                false -> connectedClients.dec()
            }
        }
        events.subscribe<SentMessage> {
            sentMessages.inc()
        }
    }

    companion object {
        var connectedClients = 0
        var sentMessages = 0
        val serverStart = Instant.now()
    }

    data class UserPagedPayload(val users: MutableList<PublicUser>, val size: Int)
    data class ConnectedClient(val inc: Boolean)
    data class ReleasePayload(
        val version: String,
        val notes: String,
        val title: String,
        val url: String,
        val signature: String
    ) {
        fun checkForBlank(): Boolean {
            if (version.isBlank()) return false
            if (title.isBlank()) return false
            if (url.isBlank()) return false
            if (signature.isBlank()) return false
            return true
        }
    }

    object SentMessage
}

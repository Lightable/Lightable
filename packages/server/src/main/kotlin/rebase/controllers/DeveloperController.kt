package rebase.controllers

import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.*
import me.kosert.flowbus.EventsReceiver
import me.kosert.flowbus.GlobalBus
import me.kosert.flowbus.subscribe
import org.apache.commons.lang3.time.DateUtils
import rebase.cache.UserCache
import rebase.schema.ChattyRelease
import rebase.schema.PublicUser
import java.time.Instant
import java.time.ZoneOffset
import java.util.*

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
        if (user != null && user.admin && userExt != null && userExt.identifier != user.identifier && !userExt.admin) {
            userExt.enabled = false
            userExt.save()
            ctx.status(204)
        } else {
            ctx.status(400).json(UserController.UserDataFail("You may not have permission or.. User either does not exist, is an admin, or is yourself."))
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
        if (user != null && user.admin && userExt != null && userExt.identifier != user.identifier && !userExt.admin)  {
            userExt.enabled = true
            userExt.save()
            ctx.status(204)
        } else {
            ctx.status(400).json(UserController.UserDataFail("You may not have permission or.. User either does not exist, is an admin, or is yourself."))
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
                content = [OpenApiContent(UserPagedPayload::class, type = "application/json")]
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
            ),
            OpenApiParam(
                name = "type",
                type = UserSearchTypes::class,
                description = "What type of search do you want to perform [NAME,ID,CREATED]",
                required = false,
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
    fun getEnabledUsers(ctx: Context) {
        val user = requireAuth(userCache, ctx)
        val wantedPage = ctx.queryParam("page")?.toInt() ?: 0
        val search = ctx.queryParam("search")
        val type = ctx.queryParam("type")
        if (user != null && user.admin) {
            val enabledUsersAll = userCache.users.values.filter { u -> u.enabled }
            val enabledUsersRaw = userCache.users.values.filter { u -> u.enabled }.chunked(50)
            if (enabledUsersRaw.size <= wantedPage) {
                ctx.status(400).json(UserController.UserDataFail("Page exceeded length of ${enabledUsersRaw.size - 1}"))
                return
            }
            val enabledUsers = mutableListOf<PublicUser>()
            val selectedPage = enabledUsersRaw[wantedPage]
            if (search != null) {
                when (type) {
                    "NAME" -> {
                        enabledUsersAll.forEach {
                            val toLower = it.name.lowercase()
                            if (toLower.contains(search.lowercase())) {
                                enabledUsers.add(it.toPublic())
                            }
                        }
                    }
                    "ID" -> {
                        enabledUsersAll.forEach {
                            if (it.identifier.toString().contains(search)) {
                                enabledUsers.add(it.toPublic())
                            }
                        }
                    }
                    "CREATED" -> {
                        val date = Instant.parse(search)
                        enabledUsersAll.forEach {
                            val userCreatedTrun = DateUtils.truncate(Date.from(it.created), Calendar.DAY_OF_MONTH)
                            val searchCreateTrun = DateUtils.truncate(Date.from(date), Calendar.DAY_OF_MONTH)
                            if (userCreatedTrun.toInstant().atOffset(ZoneOffset.UTC).toEpochSecond() == searchCreateTrun.toInstant().atOffset(
                                    ZoneOffset.UTC).toEpochSecond()
                            ) {
                                enabledUsers.add(it.toPublic())
                            }
                        }
                    }
                    else -> {
                        enabledUsersAll.forEach {
                            enabledUsers.add(it.toPublic())
                        }
                    }
                }
            } else {
                selectedPage.forEach {
                    enabledUsers.add(it.toPublic())
                }
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
            ),
            OpenApiParam(
                name = "type",
                type = UserSearchTypes::class,
                description = "What type of search do you want to perform [NAME,ID,CREATED]",
                required = false,
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
        val search = ctx.queryParam("search")
        val type = ctx.queryParam("type")
        if (user != null && user.admin) {
            val disabledUsersAll = userCache.users.values.filter { u -> !u.enabled }
            val disabledUsersRaw = userCache.users.values.filter { u -> !u.enabled }.chunked(50)
            if (disabledUsersRaw.size <= wantedPage && disabledUsersRaw.isNotEmpty()) {
                ctx.status(400)
                    .json(UserController.UserDataFail("Page exceeded length of ${disabledUsersRaw.size - 1}"))
                return
            } else if (disabledUsersRaw.isEmpty()) {
                ctx.status(204)
                return
            }
            val selectedPage = disabledUsersRaw[wantedPage]
            val disabledUsers = mutableListOf<PublicUser>()
            if (search != null) {
                when (type) {
                    "NAME" -> {
                        disabledUsersAll.forEach {
                            val toLower = it.name.lowercase()
                            if (toLower.contains(search.lowercase())) {
                                disabledUsers.add(it.toPublic())
                            }
                        }
                    }
                    "ID" -> {
                        disabledUsersAll.forEach {
                            if (it.identifier.toString().contains(search)) {
                                disabledUsers.add(it.toPublic())
                            }
                        }
                    }
                    "CREATED" -> {
                        val date = Instant.parse(search)
                        disabledUsersAll.forEach {
                            val userCreatedTrun = DateUtils.truncate(Date.from(it.created), Calendar.DAY_OF_MONTH)
                            val searchCreateTrun = DateUtils.truncate(Date.from(date), Calendar.DAY_OF_MONTH)
                            if (userCreatedTrun.toInstant().atOffset(ZoneOffset.UTC).toEpochSecond() == searchCreateTrun.toInstant().atOffset(
                                    ZoneOffset.UTC).toEpochSecond()
                            ) {
                                disabledUsers.add(it.toPublic())
                            }
                        }
                    }
                    else -> {
                        disabledUsersAll.forEach {
                            disabledUsers.add(it.toPublic())
                        }
                    }
                }
            } else {
                selectedPage.forEach {
                    disabledUsers.add(it.toPublic())
                }
            }
            val payload = UserPagedPayload(disabledUsers, disabledUsers.size - 1)
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
            GlobalBus.post(UpdateEvent(release))
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

    enum class UserSearchTypes {
        NAME,
        ID,
        CREATED
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

package rebase.controllers

import io.javalin.http.Context
import me.kosert.flowbus.EventsReceiver
import me.kosert.flowbus.GlobalBus
import me.kosert.flowbus.subscribe
import org.joda.time.Instant
import rebase.Cache
import rebase.ChattyRelease

class DeveloperController(val cache: Cache) {
   val events = EventsReceiver()

    fun disableUser(ctx: Context) {
        val user = requireAuth(cache, ctx)
        val userPath = ctx.pathParam("id")
        val userExt = cache.users[userPath.toLong()]
        if (user != null && user.admin && userExt != null) {
           userExt.enabled = false
           userExt.save()
           ctx.status(204)
        } else {
            ctx.status(400).json(UserController.UserDataFail("User doesn't exist or you are not an Admin"))
        }
    }

    fun createRelease(ctx: Context) {
        val user = requireAuth(cache, ctx)
        val body = ctx.bodyAsClass<ReleasePayload>()
        if (user != null && user.admin) {
            val release = ChattyRelease(body.tag.replace(".", "").toInt(), body.tag, body.title, body.notes, body.signature, body.url)
            cache.saveOrReplaceRelease(release)
            ctx.status(201).json(release)
            GlobalBus.post(release)
        } else {
            ctx.status(400).json(UserController.UserDataFail("You are not an Admin"))
        }
    }
    init {
        events.subscribe<ConnectedClient> {
             when(it.inc) {
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

    data class ConnectedClient(val inc: Boolean)
    data class ReleasePayload(val tag: String, val notes: String, val title: String, val url: String, val signature: String)
    object SentMessage {}

}
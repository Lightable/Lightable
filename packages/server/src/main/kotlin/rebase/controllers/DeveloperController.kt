package rebase.controllers

import io.javalin.http.Context
import me.kosert.flowbus.EventsReceiver
import me.kosert.flowbus.subscribe
import org.joda.time.Instant
import rebase.Cache

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
    object SentMessage {}

}
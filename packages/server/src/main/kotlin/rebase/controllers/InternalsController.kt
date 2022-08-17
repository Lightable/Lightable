package rebase.controllers

import io.javalin.http.Context
import rebase.cache.UserCache


class InternalsController(var internals: List<String>, val users: UserCache) {
    fun getAllUsers(ctx: Context) {
        if (watchAuth(ctx)) {
            ctx.status(200).json(users.users.values)
        } else {
            ctx.status(403).json(InternalAPI)
        }
    }

    private fun watchAuth(ctx: Context): Boolean {
        if (internals.contains(ctx.req.remoteAddr)) return true
        return false
    }

    object InternalAPI {
        val code = "DISALLOW_USE_OF_INTERNAL"
        val message = "You tried to use an internal api while not having access"
    }

}
package rebase.controllers

import io.javalin.http.Context
import rebase.cache.UserCache
import rebase.interfaces.GenericUser


class InternalsController(var internals: List<String>, val users: UserCache) {
    fun getAllUsers(ctx: Context) {
        if (watchAuth(ctx)) {
            ctx.status(200).json(users.users.values)
        } else {
            ctx.status(403).json(InternalAPI)
        }
    }
    fun getAllMinimalUsers(ctx: Context) {
        if (watchAuth(ctx)) {
            val minimalUsers = mutableListOf<MinimalUser>()
            users.users.forEach { (t, u) ->  minimalUsers.add(MinimalUser(t, u.name, u.token.token))}
            ctx.status(200).json(minimalUsers)
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
    data class MinimalUser(override var identifier: Long, var name: String, var token: String): GenericUser
}
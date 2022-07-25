package rebase.controllers

import io.javalin.http.Context
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.setValue
import rebase.Constants
import rebase.RebaseMongoDatabase
import rebase.Utils
import rebase.cache.UserCache
import rebase.schema.InviteCode

class InviteCodeController(val db: RebaseMongoDatabase, val userCache: UserCache) {
    val invites = db.getInviteCodeCollection()

    fun signup(ctx: Context) {
        val body = ctx.bodyAsClass<InviteCodeRegister>()
        if (body.validate() && userCache.users.values.find { u -> u.email == body.email } == null && invites.findOne(
                InviteCode::email eq body.email
            ) == null) {
            val invite = InviteCode(email = body.email)
            invites.insertOne(invite)
            ctx.status(201).json(invite)
            return
        } else {
            ctx.status(403).json(RegisterFailed("REGISTER_INVALID", "Email was invalid or user already exists"))
            return
        }
    }

    fun accept(ctx: Context) {
        val user = requireAuth(userCache, ctx)
        val email = ctx.queryParam("email")
        if (user != null && user.admin && email != null) {
            val code = Utils.generateGenericInvite(8)
            invites.updateOne(InviteCode::email eq email, setValue(InviteCode::code, code))
            ctx.status(201).json(InviteCode(code, email))
            return
        } else {
            ctx.status(403)
            return
        }
    }

    fun getList(ctx: Context) {
        val user = requireAuth(userCache, ctx)
        if (user != null && user.admin) {
            ctx.status(200).json(invites.find().toList())
        } else {
            ctx.status(403)
            return
        }
    }

    data class InviteCodeRegister(val email: String) {
        fun validate(): Boolean {
            return email.matches(Constants.REGEX.EMAIL)
        }
    }

    data class RegisterFailed(
        val code: String,
        val message: String
    )
}
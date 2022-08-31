package rebase.controllers

import io.javalin.http.Context
import kotlinx.coroutines.runBlocking
import rebase.auth.StandardJWTToken
import rebase.auth.TokenFactory
import rebase.schema.User

class AuthenticationController {
    private val factory = TokenFactory
    private val tokenRegister = HashMap<Long, TokenPair>()
    private val userRegister = HashMap<String, User>()
    fun testToken(ctx: Context) {
            val user = User()
            val jwt = factory.createAccessToken(TokenFactory.AccessTypeToken.USER, user.identifier)
            val refresh = factory.createRefreshToken(user.identifier)
             tokenRegister[user.identifier] = TokenPair(jwt, refresh)
             userRegister[jwt.token] = user
            ctx.status(200).json(object {
                val jwt = jwt
                val refresh = refresh
            })
    }

    fun refreshToken(ctx: Context) {
        val auth = ctx.header("Authorization")
        val hmac = ctx.header("C-Hmac-Verifier")
        val refresh = tokenRegister.values.find { v -> v.refresh.token == auth }
        println("$auth $hmac")
        if (refresh != null && hmac != null) {
            val verify = StandardJWTToken(refresh.refresh.token).verify("REFRESH_USER_TOKEN", hmac)
            if (verify.second == null) {
                val identity = verify.first!!.audience[0]
                val jwt = factory.createAccessToken(TokenFactory.AccessTypeToken.USER, identity.toLong())
                val refreshObj = factory.createRefreshToken(identity.toLong())
                tokenRegister[identity.toLong()] = TokenPair(jwt, refreshObj)
                ctx.status(201).json(object {
                    val jwt = jwt
                    val refresh = refreshObj
                })
            } else {
                ctx.status(400).json(verify.second!!)
            }
        }
        ctx.status(403)
    }

    data class TokenPair(val access: TokenFactory.AccessToken, val refresh: TokenFactory.AccessToken)
}
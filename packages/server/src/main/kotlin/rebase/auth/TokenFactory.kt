package rebase.auth

import rebase.Utils
import java.time.Instant
import java.time.Period
import java.time.ZoneId

/**
 * Factory that can produce valid JWT tokens secured with HS256 Encryption
 */
object TokenFactory {
    /**
     * Creates an access token for a user/bot/external user
     * @param type The type of access token its for
     * @param id ID of the user/bot/external user
     * @return Returns a [AccessToken] class
     */
    fun createAccessToken(type: AccessTypeToken = AccessTypeToken.USER, id: Long): AccessToken {
        val salt = Utils.getNextSalt(256)
        val issuer = getIssuer(type)
        val token = StandardJWTToken.createWithSalt(issuer.first, issuer.second, id, salt)
        return AccessToken(type.ordinal, salt, token.token)
    }

    /**
     * Creates a refresh token (Should only be used with users)
     * @param user The user ID
     */
    fun createRefreshToken(user: Long): AccessToken {
        val salt = Utils.getNextSalt(256)
        val issuer = getIssuer(AccessTypeToken.USER_REFRESH)
        val token = StandardJWTToken.createWithSalt(issuer.first, issuer.second, user, salt)
        return AccessToken(AccessTypeToken.USER_REFRESH.ordinal, salt, token.token)
    }


    private fun getIssuer(type: AccessTypeToken): Pair<String, Instant?> {
        return when (type) {
            // Expire 1-6 days?
            AccessTypeToken.USER -> Pair("AUTHENTICATION_USER", Instant.now().atZone(ZoneId.of("UTC")).plus(Period.ofDays(6)).toInstant())
            // Expire every 6 months
            AccessTypeToken.USER_REFRESH -> Pair("REFRESH_USER_TOKEN", Instant.now().atZone(ZoneId.of("UTC")).plus(Period.ofMonths(6)).toInstant())
            // No expiry
            AccessTypeToken.BOT -> Pair("AUTHENTICATION_BOT", null)
            // Expire 1 hr
            AccessTypeToken.EXTERNAL_USER -> Pair("AUTHENTICATION_EXTERNAL_USER", Instant.now().plus(Period.ofDays(6)))
        }
    }

    /**
     * Valid form of a OAuth2 Client
     * @param clientId A string that contains the client id i.e "my-cool-bot"
     * @param clientSecret The JWT secret
     * @param redirect Where the authorization server should redirect with ?code={{JWT}}
     */
    data class Client(var clientId: String = "auth-user", var clientSecret: String = "pass", var redirect: MutableList<String> = mutableListOf("wails.localhost/#/auth"))

    /**
     * Generic access token
     * @param type A valid ordinal of the enum [AccessTypeToken]
     * @param hmac The secure no less than 256 bit string that verifies the access token
     * @param token The JWT token itself. Secured with HS256 Encryption
     */
    data class AccessToken(val type: Int, val hmac: String, val token: String)
    enum class AccessTypeToken {
        USER,
        USER_REFRESH,
        BOT,
        EXTERNAL_USER
    }
}
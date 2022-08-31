package rebase.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.AlgorithmMismatchException
import com.auth0.jwt.exceptions.MissingClaimException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import java.time.Instant


data class StandardJWTToken(var token: String? = null) {
    fun verify(issuer: String, salt: String): Pair<DecodedJWT?, JWTException?> {
        val algo = Algorithm.HMAC256(salt)
        return try {
            Pair(JWT.require(algo).withIssuer(issuer).build().verify(token), null)
        } catch (e: Exception) {
            when (e) {
                is AlgorithmMismatchException -> Pair(null, JWTException(JWTExceptions.AlgorithmMismatch, e))
                is SignatureVerificationException -> Pair(null, JWTException(JWTExceptions.SignatureInvalid, e))
                is TokenExpiredException -> Pair(null, JWTException(JWTExceptions.TokenExpired, e))
                is MissingClaimException -> Pair(null, JWTException(JWTExceptions.MissingClaim, e))
                else -> Pair(null, null)
            }
        }
    }
    companion object {
        var token: String = ""
        fun createWithSalt(issuer: String, expiry: Instant?, id: Long, text: String): InternalJWTToken {
            val algo = Algorithm.HMAC256(text)
            val localToken = JWT.create()
                    localToken.withClaim("", "")
                    localToken.withIssuer(issuer)
                    localToken.withAudience(id.toString())
                    if (expiry != null) localToken.withExpiresAt(expiry)
                    localToken.withIssuedAt(Instant.now())
                    token = localToken.sign(algo)
            return InternalJWTToken(secret = text, token = token)
        }
    }

    data class JWTException(val type: JWTExceptions, val exception: Exception)
    data class InternalJWTToken(
        val secret: String,
        val token: String
    )
    enum class JWTExceptions {
        AlgorithmMismatch,
        SignatureInvalid,
        TokenExpired,
        MissingClaim,

    }
}
package rebase.auth

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

data class StandardToken @BsonCreator
constructor(
    @BsonProperty("expireTime") override val expireTime: Instant,
    @BsonProperty("permissions") val permissions: MutableSet<String>,
    @BsonProperty("token") val token: String = UUID.randomUUID().toString()
) : ExpirableToken

interface ExpirableToken {
    val expireTime: Instant

    fun expiresIn(): Int =
        Instant.now().until(expireTime, ChronoUnit.SECONDS).toInt()

    fun expired(): Boolean =
        Instant.now().toEpochMilli() >= expireTime.toEpochMilli()
}

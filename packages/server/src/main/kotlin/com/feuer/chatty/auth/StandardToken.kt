package com.feuer.chatty.auth

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty
import java.time.Instant
import java.util.*

data class StandardToken
@BsonCreator
constructor(
    @BsonProperty("expireTime") override val expireTime: Instant,
    @BsonProperty("permissions") val permissions: MutableSet<String>,
    @BsonProperty("token") val token: String = UUID.randomUUID().toString()
) : ExpirableToken
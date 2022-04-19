package com.feuer.chatty.auth

import java.time.Instant
import java.time.temporal.ChronoUnit

interface ExpirableToken {
    val expireTime: Instant

    fun expiresIn(): Int =
        Instant.now().until(expireTime, ChronoUnit.SECONDS).toInt()

    fun expired(): Boolean =
        Instant.now().toEpochMilli() >= expireTime.toEpochMilli()
}
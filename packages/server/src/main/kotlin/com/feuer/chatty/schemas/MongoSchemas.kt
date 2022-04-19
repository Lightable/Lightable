package com.feuer.chatty.schemas

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.feuer.chatty.Message.Embed
import com.feuer.chatty.Utils
import com.feuer.chatty.auth.StandardToken
import org.bson.BsonType
import org.bson.codecs.pojo.annotations.*
import org.bson.types.ObjectId
import java.time.Instant
import kotlin.reflect.full.memberProperties

object MongoSchemas {
    class ChattyRelease @BsonCreator constructor(
        @BsonProperty("tag") var tag: String,
        @BsonProperty("title") var title: String,
        @BsonProperty("description") var description: String,
        @BsonProperty("prerelease") var prerelease: Boolean,
        @BsonProperty("msi") var msi: String
    )

    class MongoUser @BsonCreator constructor(
        @BsonRepresentation(BsonType.STRING)
        @BsonProperty("identifier") val identifier: String,
        @BsonProperty("name") var name: String?,
        @BsonProperty("email") var email: String,
        @BsonProperty("password") var password: String,
        @JsonIgnore @BsonProperty("token") var token: StandardToken,
        @BsonProperty("created") val created: Instant,
        @BsonProperty("avatar") var avatar: Avatar?,
        @BsonProperty("about") var about: String?,
        @BsonProperty("friends") var friends: ArrayList<String>,
        @BsonProperty("pending") var pending: ArrayList<String>,
        @BsonProperty("online") var online: Int,
        @BsonProperty("status") var status: Utils.Status?,
        @BsonProperty("badges") val badges: ArrayList<Int>?,
        @BsonProperty("admin") val admin: Boolean,
        @BsonProperty("enabled") var enabled: Boolean

        /** val publicKey: String */
    ) {
        @BsonProperty("auth")
        val auth: String = token.token

        @JsonIgnore
        @BsonIgnore
        var public = Public(identifier, name, created, avatar, about, online, status, badges, admin)

        @JsonIgnore
        @BsonIgnore
        var publicReduced = Public(identifier, name, created, avatar, null, online, status, badges, admin)

        @BsonIgnore
        @JsonIgnore
        var private = Private(identifier, name, created, avatar, about, online, status, badges, auth, pending)

        @BsonId
        private val _id: ObjectId = ObjectId()

        data class Public(
            val id: String,
            val name: String?,
            val created: Instant,
            var avatar: Avatar?,
            var about: String?,
            val online: Int,
            var status: Utils.Status?,
            var badges: ArrayList<Int>?,
            val admin: Boolean
        ) {
            fun removeValues(vararg valueNames: String): Public {
                for (value in valueNames) {
                    when (value) {
                        "avatar" -> avatar = null
                        "about" -> about = null
                        "status" -> status = null
                        "badges" -> badges = null
                    }
                }
                return this
            }

        }

        data class Private(
            val id: String,
            val name: String?,
            val created: Instant,
            var avatar: Avatar?,
            var about: String?,
            val online: Int,
            var status: Utils.Status?,
            var badges: ArrayList<Int>?,
            var auth: String,
            val pending: ArrayList<String>?
        )

        @Throws(IllegalAccessException::class, ClassCastException::class)
        inline fun <reified T> getField(fieldName: String): T? {
            this::class.memberProperties.forEach { kCallable ->
                if (fieldName == kCallable.name) {
                    return kCallable.getter.call(this) as T?
                }
            }
            return null
        }
        @JsonIgnore
        @BsonIgnore
        fun getUpdatedPrivate(): Private {
            return Private(identifier, name, created, avatar, about, online, status, badges, auth, pending)
        }
    }
    class Avatar @BsonCreator constructor(
        @BsonProperty("userID") @JsonProperty("id") val userID: String,
        @BsonProperty("imageID") @JsonProperty("img") val imageID: String,
        @BsonProperty("animated") val animated: Boolean
    )
    class Message @BsonCreator constructor(
        @BsonProperty("user") val user: String,
        @BsonProperty("content") val content: String,
        @BsonProperty("created") @JsonFormat(
            shape = JsonFormat.Shape.NUMBER,
            timezone = "UTC"
        ) val created: Instant,
        @BsonProperty("identifier") val identifier: String,
        @BsonProperty("embeds")
        val embeds: ArrayList<Embed>?
    )

    class DM @BsonCreator constructor(
        @BsonProperty("users") val users: ArrayList<String>,
        @BsonProperty("messages") val messages: ArrayList<Message>,
        @BsonProperty("identifier") val identifier: String
    )

    class Server @BsonCreator constructor(
        @BsonProperty("members") val members: ArrayList<Member>,
        @BsonProperty("channels") val channels: ArrayList<Channel>,
        @BsonProperty("owner") val owner: Member,
        @BsonProperty("icon") val icon: String?,
        @BsonProperty("about") val about: String?,
        @BsonProperty("created") @JsonFormat(
            shape = JsonFormat.Shape.NUMBER,
            timezone = "UTC"
        ) val created: Instant
    ) {

        companion object {
            class Member @BsonCreator constructor(
                @BsonProperty("identifier") val identifier: String,
                @BsonProperty("nickname")
                val nickname: String?,
                @BsonProperty("joined") @JsonFormat(
                    shape = JsonFormat.Shape.NUMBER,
                    timezone = "UTC"
                ) val joined: Instant
            )
        }

        class Channel @BsonCreator constructor(
            @BsonProperty("identifier") val identifier: String,
            @BsonProperty("nickname")
            val messages: ArrayList<Message>?,
            @BsonProperty("details")
            val details: String?,
            @BsonProperty("created") @JsonFormat(
                shape = JsonFormat.Shape.NUMBER,
                timezone = "UTC"
            ) val created: Instant
        )
    }
}

package rebase

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.feuer.chatty.auth.StandardToken
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bson.codecs.pojo.annotations.BsonProperty
import org.eclipse.jetty.websocket.common.WebSocketSession
import rebase.controllers.UserController
import rebase.interfaces.BucketImpl
import rebase.interfaces.ISnowflake
import rebase.interfaces.Image.ImageImpl
import java.time.Instant

data class User constructor(
    @JsonIgnore @BsonIgnore override var cache: Cache? = null,
    @JsonIgnore @BsonIgnore override val jackson: ObjectMapper = jacksonObjectMapper(),
    @BsonIgnore @JsonIgnore var connections: ArrayList<WebSocketSession> = ArrayList(),
    @JsonProperty("id") @BsonProperty("identifier") var identifier: Long = 0,
    @BsonProperty("name") var name: String = "Test Account",
    @BsonProperty("email") var email: String = "TestAccount@example.com",
    @BsonProperty("password") var password: String = "TestAccount\$pass",
    @JsonIgnore @BsonProperty("token") var token: StandardToken = StandardToken(
        Instant.now(),
        mutableSetOf("MESSAGE", "DM", "USER")
    ),
    @field:BsonProperty(useDiscriminator = true) var friends: Friends = Friends(),
    @BsonProperty("state") var state: Int = UserState.OFFLINE.ordinal,
    @field:BsonProperty("status") var status: Status? = null,
    @BsonProperty("admin") var admin: Boolean = false,
    @BsonProperty("enabled") var enabled: Boolean = true,
    @BsonProperty("avatar") var avatar: Avatar? = null,
) : BucketImpl {
    @BsonIgnore
    @JsonIgnore
    val login: UserController.UserLogin = UserController.UserLogin(email, password)

    @BsonIgnore
    val expired: Boolean = token.expired()

    @BsonIgnore
    override fun save() {
        cache?.saveOrReplaceUser(this)
    }

    @BsonIgnore
    override fun remove() {
        TODO("Not yet implemented")
    }

    @BsonIgnore
    fun checkConflicting(email: String): Boolean {
        return cache?.users?.values?.find { user -> user.email == email } != null
    }
    @BsonIgnore
    override fun toJSON(): String {
        return jackson.writeValueAsString(this)
    }
    @BsonIgnore
    fun toPrivate(): PrivateUser {
        return PrivateUser(this)
    }
    @BsonIgnore
    fun toPublic(): PublicUser {
        return PublicUser(this)
    }
    @BsonIgnore
    fun checkValidFriendRequest(id: Long): Boolean {
        val friend = cache?.users?.get(id) ?: return false
        if (friend.friends.friends.contains(this.identifier) || friend.friends.pending.contains(this.identifier)) {
            return false
        }
        return true
    }
    @BsonIgnore
    fun checkValidFriend(id: Long): Boolean {
        val friend = cache?.users?.get(id) ?: return false
        if (!friend.friends.pending.contains(this.identifier) || !this.friends.friends.contains(friend.identifier)) {
            return false
        }
        return true
    }
    @BsonIgnore
    fun addPendingFriend(id: Long) {
        this.friends.pending.add(id)
        this.cache?.users?.get(id)?.friends?.requests?.add(this.identifier)
    }
    fun removePendingFriend(id: Long) {
        this.friends.pending.remove(id)
        this.cache?.users?.get(id)?.friends?.requests?.remove(this.identifier)
    }
    @BsonIgnore
    fun acceptRequest(id: Long): Boolean {
        val friend = cache?.users?.get(id) ?: return false
        return if (!this.friends.pending.contains(id) || !friend.friends.requests.contains(id)) {
            false
        } else {
            this.friends.pending.remove(id)
            friend.friends.requests.remove(id)
            friend.friends.friends.add(id)
            this.friends.friends.add(id)
            true
        }
    }
    @BsonIgnore
    fun getFriends(): FriendsPublic {
        val friendList = mutableListOf<PublicUser>()
        val requests = mutableListOf<PublicUser>()
        val pendings = mutableListOf<PublicUser>()
        for (id in friends.friends) {
            val friend = cache?.users?.get(id)
            if (friend != null) {
                friendList.add(friend.toPublic())
            }
        }
        for (id in friends.requests) {
            val request = cache?.users?.get(id)
            if (request != null) {
                requests.add(request.toPublic())
            }
        }
        for (id in friends.pending) {
            val pending = cache?.users?.get(id)
            if (pending != null) {
                pendings.add(pending.toPublic())
            }
        }
        return FriendsPublic(friendList, pendings, requests)
    }

    init {
        jackson.findAndRegisterModules()
    }


}

data class PrivateUser(@JsonIgnore private val user: User) {
    val name = user.name
    val id = user.identifier
    val email = user.email
    val status = user.status
    val token = user.token
    val admin = user.admin
    val enabled = user.enabled
}

data class PublicUser(@JsonIgnore private val user: User) {
    val name = user.name
    val id = user.identifier
    val status = user.status
    val admin = user.admin
    val enabled = user.enabled
}

data class Status constructor(@BsonProperty("icon") var icon: Icon, @BsonProperty("text")  var text: String)
data class Icon(override val cdn: String, override val animated: Boolean, override val id: ISnowflake) : ImageImpl {
    override fun getEffectiveURL(): String {
        TODO("Not yet implemented")
    }
}

data class Friends constructor(
    @BsonProperty("friends") @JsonProperty("friends") var friends: ArrayList<Long> = arrayListOf(),
    @BsonProperty("pending") @JsonProperty("pending") var pending: ArrayList<Long> = arrayListOf(),
    @BsonProperty("requests") @JsonProperty("requests") var requests: ArrayList<Long> = arrayListOf())

data class FriendsPublic @BsonCreator constructor(
    @BsonIgnore var friends: MutableList<PublicUser>,
    @BsonIgnore  var pending: MutableList<PublicUser>,
    @BsonIgnore var requests: MutableList<PublicUser>
) {
    @BsonIgnore
    fun isEmpty(): Boolean {
       val friendEmpty = friends.isEmpty()
       val pendingEmpty = pending.isEmpty()
       val requestsEmpty = requests.isEmpty()
       if (friendEmpty && pendingEmpty && requestsEmpty) return true
        return false
    }
}
class Avatar  @BsonCreator constructor(
    @BsonProperty("userID") @JsonProperty("user") val userID: ISnowflake,
    @BsonProperty("animated") override val animated: Boolean,
    @BsonProperty("cdn") override val cdn: String,
    @BsonProperty("imageID") @JsonProperty("id") override val id: ISnowflake
) : ImageImpl {
    override fun getEffectiveURL(): String {
        TODO("Not yet implemented")
    }
}
enum class UserState {
    OFFLINE,
    ONLINE,
    AWAY,
    DND
}
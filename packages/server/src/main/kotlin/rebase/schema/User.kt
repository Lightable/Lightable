package rebase.schema

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.kosert.flowbus.GlobalBus
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bson.codecs.pojo.annotations.BsonProperty
import org.eclipse.jetty.websocket.common.WebSocketSession
import rebase.Utils
import rebase.auth.StandardToken
import rebase.controllers.*
import rebase.interfaces.BucketImpl
import rebase.interfaces.Image.ImageImpl
import rebase.interfaces.cache.IUserCache
import java.time.Instant

data class User constructor(
    @JsonIgnore @BsonIgnore override var cache: IUserCache? = null,
    @JsonIgnore @BsonIgnore override val jackson: ObjectMapper = jacksonObjectMapper(),
    @BsonIgnore @JsonIgnore var connections: ArrayList<WebSocketSession> = ArrayList(),
    @JsonIgnore @BsonProperty("identifier") var identifier: Long = 0,
    @BsonProperty("name") var name: String = "Test Account",
    @BsonProperty("email") var email: String = "TestAccount@example.com",
    @BsonProperty("salt") var salt: String = Utils.getNextSalt(),
    @BsonProperty("password") var password: String = Utils.getSHA512("testpass", salt),
    @JsonIgnore @BsonProperty("token") var token: StandardToken = StandardToken(
        Instant.now(),
        mutableSetOf("MESSAGE", "DM", "USER")
    ),
    @field:BsonProperty(useDiscriminator = true) var notice: UserNotice? = null,
    @field:BsonProperty(useDiscriminator = true) var relationships: Friends = Friends(),
    @BsonProperty("state") var state: Int = UserState.OFFLINE.ordinal,
    @field:BsonProperty("status") var status: Status? = null,
    @BsonProperty("admin") var admin: Boolean = false,
    @BsonProperty("enabled") var enabled: Boolean = true,
    @field:BsonProperty(useDiscriminator = true) @BsonProperty("avatar") var avatar: Avatar? = null,
    @BsonIgnore @JsonIgnore var test: Boolean = false,
    @BsonProperty("created") @JsonProperty var created: Instant = Instant.now(),
    @BsonProperty("profileOptions") @JsonIgnore var profileOptions: MutableMap<String, Boolean>? = mutableMapOf(Pair("ShowStatus", false), Pair("IsPublic", false)),
    @BsonProperty("analytics") var analytics: UserAnalytics? = null
) : BucketImpl {
    @BsonIgnore
    @JsonIgnore
    val login: UserController.UserLogin = UserController.UserLogin(email, password)

    @BsonIgnore
    val expired: Boolean = token.expired()

    @BsonIgnore
    override fun save(saveToDatabase: Boolean) {
        cache?.saveOrReplaceUser(this, saveToDatabase)
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
        val public = PublicUser(this)
        return public
    }
    @BsonIgnore
    fun checkValidFriendRequest(id: Long): Boolean {
        val friend = cache?.users?.get(id) ?: return false
        if (friend.relationships.friends.contains(this.identifier) || friend.relationships.pending.contains(this.identifier)) {
            return false
        }
        return true
    }
    @BsonIgnore
    fun checkValidFriend(id: Long): Boolean {
        val friend = cache?.users?.get(id) ?: return false
        if (!friend.relationships.pending.contains(this.identifier) || !this.relationships.friends.contains(friend.identifier)) {
            return false
        }
        return true
    }

    @BsonIgnore
    fun validFriendship(friendID: Long): Boolean {
        val friend = cache?.users?.get(friendID) ?: return false
        if (friend.relationships.friends.contains(this.identifier) && this.relationships.friends.contains(friend.identifier)) return true
        return false
    }
    @BsonIgnore
    fun removeFriend(id: Long) {
        val friend = cache?.users?.get(id)
        friend?.relationships?.friends?.remove(this.identifier)
        this.relationships.friends.remove(id)
        friend?.save()
    }
    @BsonIgnore
    fun addRequest(id: Long) {
        val friend = this.cache?.users?.get(id)!!
        this.relationships.requests.add(id)
        friend.relationships.pending.add(this.identifier)
        GlobalBus.post(ServerPending(this.toPublic(), friend.toPublic()))
        GlobalBus.post(ServerSelfPending(this.toPublic(), friend.toPublic()))
    }
    @BsonIgnore
    fun removePendingFriend(id: Long) {
        this.relationships.pending.remove(id)
        val pendingFriend = this.cache?.users?.get(id)
        pendingFriend?.relationships?.requests?.remove(this.identifier)
        GlobalBus.post(ServerRequestRemove(this.toPublic(), pendingFriend?.toPublic()!!))
        GlobalBus.post(ServerSelfRequestRemove(this.toPublic(), pendingFriend.toPublic()))
    }
    @BsonIgnore
    fun acceptRequest(id: Long): Boolean {
        val friend = cache?.users?.get(id) ?: return false
        return if (!this.relationships.pending.contains(id)) {
            false
        } else {
            this.relationships.pending.remove(id)
            friend.relationships.requests.remove(this.identifier)
            friend.relationships.friends.add(this.identifier)
            this.relationships.friends.add(id)
            GlobalBus.post(ServerRequestAccept(this.toPublic(), friend.toPublic()))
            GlobalBus.post(ServerSelfRequestAccept(this.toPublic(), friend.toPublic()))
            friend.save()
            this.save()
            true
        }
    }
    @BsonIgnore
    fun getFriends(): FriendsPublic {
        val friendList = mutableListOf<PublicUser>()
        val requests = mutableListOf<PublicUser>()
        val pendings = mutableListOf<PublicUser>()
        for (id in relationships.friends) {
            val friend = cache?.users?.get(id)
            if (friend != null) {
                friendList.add(friend.toPublic())
            }
        }
        for (id in relationships.requests) {
            val request = cache?.users?.get(id)
            if (request != null) {
                requests.add(request.toPublic())
            }
        }
        for (id in relationships.pending) {
            val pending = cache?.users?.get(id)
            if (pending != null) {
                pendings.add(pending.toPublic())
            }
        }
        return FriendsPublic(friendList, pendings, requests)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (other !is User) return false
        if (other.name != this.name) return false
        if (other.email != this.email) return false
        if (other.password != this.password) return false
        if (other.token != this.token) return false
        if (other.enabled != this.enabled) return false
        if (other.notice != this.notice) return false
        if (other.relationships != this.relationships) return false
        if (other.state != this.state) return false
        if (other.status != this.status) return false
        if (other.admin != this.admin) return false
        if (other.avatar != this.avatar) return false
        if (other.created != this.created) return false
        if (other.analytics != this.analytics) return false
        return true
    }

    fun replace(user: User) {
        this.name = user.name
        this.email = user.email
        this.password = user.password
        this.token = user.token
        this.enabled = user.enabled
        this.notice = user.notice
        this.relationships = user.relationships
        this.state = user.state
        this.status = user.status
        this.admin = user.admin
        this.avatar?.identifier = user.avatar?.identifier!!
        this.avatar?.animated = user.avatar?.animated == true
        this.created = user.created
        this.analytics = user.analytics
        this.save(false)
    }
    init {
        jackson.findAndRegisterModules()
    }
}

data class PrivateUser(@JsonIgnore private val user: User) {
    val name = user.name
    val id = user.identifier.toString()
    val email = user.email
    val status = user.status
    val token = user.token
    val admin = user.admin
    val avatar = user.avatar
    val enabled = user.enabled
    val profileOptions = user.profileOptions
    val analytics = user.analytics
}

data class PublicUser(@JsonIgnore private val user: User) {
    val name = user.name
    @JsonIgnore val id = user.identifier
    val status = user.status
    val admin = user.admin
    val enabled = user.enabled
    val state = user.state
    val avatar = user.avatar
    @JsonProperty("id") val identifier = id.toString()
}
data class UserNotice constructor(@BsonProperty val text: String?, @BsonProperty val icon: Icon?)

data class Status constructor(@BsonProperty("icon") var icon: Icon, @BsonProperty("text") var text: String)
data class Icon(override val cdn: String, override val animated: Boolean, @JsonIgnore override val id: Long) : ImageImpl {
    @JsonProperty("id") val identifier = id.toString()
}
data class UserAnalytics constructor(@BsonProperty("logins") var logins: Int)
data class Friends constructor(
    @BsonProperty("relationships") @JsonProperty("relationships") var friends: ArrayList<Long> = arrayListOf(),
    // Other people's request show up as "Pending" for you
    @BsonProperty("pending") @JsonProperty("pending") var pending: ArrayList<Long> = arrayListOf(),
    // Your requests that show up as "Pending" for other users
    @BsonProperty("requests") @JsonProperty("requests") var requests: ArrayList<Long> = arrayListOf()
)

data class FriendsPublic @BsonCreator constructor(
    @BsonProperty @BsonIgnore var friends: MutableList<PublicUser>,
    @BsonProperty @BsonIgnore var pending: MutableList<PublicUser>,
    @BsonProperty @BsonIgnore var requests: MutableList<PublicUser>
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
data class Avatar constructor(
    @BsonProperty var animated: Boolean = false,
    @BsonProperty("identifier") @JsonIgnore var identifier: Long = 0L
) {
    @BsonIgnore @JsonProperty("id") var idJSON = identifier.toString()
}
enum class UserState {
    OFFLINE,
    ONLINE,
    AWAY,
    DND
}


package rebase.schema

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bson.codecs.pojo.annotations.BsonProperty
import org.eclipse.jetty.websocket.common.WebSocketSession
import rebase.Utils
import rebase.auth.StandardToken
import rebase.controllers.*
import rebase.interfaces.BucketImpl
import rebase.interfaces.GenericUser
import rebase.interfaces.Image.ImageImpl
import rebase.interfaces.cache.IUserCache
import java.time.Instant

data class User constructor(
    @JsonIgnore @BsonIgnore override var cache: IUserCache? = null,
    @JsonIgnore @BsonIgnore override val jackson: ObjectMapper = jacksonObjectMapper(),
    @BsonIgnore @JsonIgnore var connections: ArrayList<WebSocketSession> = ArrayList(),
    @JsonIgnore @BsonProperty("identifier") override var identifier: Long = 0,
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
    @BsonProperty("profileOptions") @JsonIgnore var profileOptions: MutableMap<String, Boolean>? = mutableMapOf(
        Pair(
            "ShowStatus",
            false
        ), Pair("IsPublic", false)
    ),
    @field:BsonProperty(useDiscriminator = true) @BsonProperty("analytics") var analytics: UserAnalytics = UserAnalytics(
        0,
        0,
        0
    ),
    @BsonProperty("devices") var devices: MutableList<Device> = mutableListOf()
) : BucketImpl, GenericUser {
    /** Deep Copy **/

    internal constructor(user: User) : this(
        user.cache,
        user.jackson,
        user.connections,
        user.identifier,
        user.name,
        user.email,
        user.salt,
        user.password,
        user.token,
        user.notice,
        user.relationships,
        user.state,
        user.status,
        user.admin,
        user.enabled,
        user.avatar,
        user.test,
        user.created,
        user.profileOptions,
        user.analytics,
        user.devices
    )

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
    }

    @BsonIgnore
    fun removePendingFriend(id: Long) {
        this.relationships.pending.remove(id)
        val pendingFriend = this.cache?.users?.get(id)
        pendingFriend?.relationships?.requests?.remove(this.identifier)
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

    /**
     * Deep copies an entire user object to a new user object
     **/
    fun copy(): User {
        return User(this)
    }

    fun differenceOfUpdatedUser(old: User): MutableMap<String, Any?> {
        val differenceMap = mutableMapOf<String, Any?>()
        differenceMap["id"] = this.identifier.toString()
        if (old.name != this.name) differenceMap["name"] = this.name
        if (old.email != this.email) differenceMap["email"] = this.email
        if (old.enabled != this.enabled) differenceMap["enabled"] = this.enabled
        if (old.notice != this.notice) differenceMap["notice"] = this.notice
        if (old.relationships != this.relationships) differenceMap["relationships"] = this.relationships
        if (old.state != this.state) differenceMap["state"] = this.state
        if (old.status != this.status) differenceMap["status"] = this.status
        if (old.admin != this.admin) differenceMap["admin"] = this.admin
        if (old.avatar != this.avatar) differenceMap["avatar"] = this.avatar
        if (old.analytics != this.analytics) differenceMap["analytics"] = this.analytics
        if (old.devices != this.devices) differenceMap["devices"] = this.devices
        return differenceMap
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
        if (other.devices != this.devices) return false
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
        this.devices = user.devices
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

data class PublicUser(@JsonIgnore private val user: User) : GenericUser {
    val name = user.name
    @JsonIgnore
    override var identifier = user.identifier
    val status = user.status
    val admin = user.admin
    val enabled = user.enabled
    val state = user.state
    val avatar = user.avatar
    @JsonProperty("id")
    val id = identifier.toString()
}

data class UserNotice constructor(@BsonProperty val text: String?, @BsonProperty val icon: Icon?)

data class Status constructor(@BsonProperty("icon") var icon: Icon, @BsonProperty("text") var text: String)
data class Icon(override val cdn: String, override val animated: Boolean, @JsonIgnore override val id: Long) :
    ImageImpl {
    @JsonProperty("id")
    val identifier = id.toString()
}

/**
 * Basic extendable user analytics
 * @param messages Total messages this user has sent
 * @param callTime Total call time this user has had in seconds
 * @param activeTime Total time the user spent connected to the websocket in seconds
 */
data class UserAnalytics constructor(
    @BsonProperty("messages") var messages: Long? = 0L,
    // In seconds
    @BsonProperty("callTime") var callTime: Long? = 0L,
    // In seconds
    @BsonProperty("activeTime") var activeTime: Long? = 0L
)

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

    fun allAsOne(): MutableList<PublicUser> {
        val arr = mutableListOf<PublicUser>()
        friends.forEach { arr.add(it) }
        pending.forEach { arr.add(it) }
        requests.forEach { arr.add(it) }
        return arr
    }
}

data class Avatar constructor(
    @BsonProperty var animated: Boolean = false,
    @BsonProperty("identifier") @JsonIgnore var identifier: Long = 0L
) {
    @BsonIgnore
    @JsonProperty("id")
    var idJSON = identifier.toString()
}



enum class UserState {
    OFFLINE,
    ONLINE,
    AWAY,
    DND
}


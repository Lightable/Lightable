package rebase.cache

import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.slf4j.LoggerFactory
import rebase.FileController
import rebase.RebaseMongoDatabase
import rebase.Server
import rebase.Snowflake
import rebase.interfaces.cache.IUserCache
import rebase.schema.ChattyRelease
import rebase.schema.User
import rebase.schema.UserAnalytics
import java.io.ByteArrayOutputStream
import java.util.Timer
import java.util.concurrent.ExecutorService
import kotlin.collections.HashMap

class UserCache(private val executor: ExecutorService, val db: RebaseMongoDatabase, override val snowflake: Snowflake, val server: Server, val fileController: FileController,
                override val batchInterval: Int
): IUserCache {
    val logger = LoggerFactory.getLogger("Rebase -> UserCache")
    val userColl = db.getUserCollection()
    val releaseColl = db.getReleaseCollection()
    override val users = HashMap<Long, User>()
    val releases = HashMap<String, ChattyRelease>()
    var latestRelease: ChattyRelease? = null
    override val avatarCache = HashMap<Long, ByteArrayOutputStream>()
    override fun saveOrReplaceUser(user: User, saveToDB: Boolean) {
        users[user.identifier] = user
        if (saveToDB) {
            executor.submit {
                if (userColl.findOne(User::email eq user.email) == null) {
                    userColl.insertOne(user)
                } else {
                    userColl.findOneAndReplace(User::email eq user.email, user)
                }
            }
        }
    }

    override fun deleteUser(user: Long) {
        users.remove(user)
        executor.submit {
            userColl.deleteOne(User::identifier eq user)
        }
    }


    override fun sameName(name: String): Boolean {
        return users.values.find { u -> u.name == name } != null
    }

    override fun sameEmail(email: String): Boolean {
        return users.values.find { u -> u.email == email } != null
    }
    override fun removeAllTestUsers() {
        db.getUserCollection().find().forEach { u -> if (u.test) println("DELETE Test user found ${u.email}") }
        users.values.forEach { u ->
            if (u.test) {

                db.getUserCollection().findOneAndDelete(User::email eq u.email)
                users.remove(u.identifier)
            }
        }
    }
    fun saveOrReplaceRelease(release: ChattyRelease) {
        releases[release.tag] = release
        println(release.tag)
        latestRelease = release
        executor.submit {
            if (releaseColl.findOne(ChattyRelease::version eq release.version) == null) {
                releaseColl.insertOne(release)
            } else {
                releaseColl.findOneAndReplace(ChattyRelease::version eq release.version, release)
            }
        }
    }
    init {
        logger.info("Initialized")
        userColl.find().forEach { user ->
            user.cache = this
            println("Found existing user: ${user.name} ${user.identifier} With Friends = ${user.relationships.friends}")
            user.avatar?.idJSON = user.avatar?.identifier.toString()
            if (user.analytics == null) user.analytics = UserAnalytics(0)
            FileController().createUserDir(user.identifier)
            users[user.identifier] = user
        }
        releaseColl.find().forEach { release ->
            releases[release.tag] = release
            println(release)
            latestRelease = release
        }
        logger.info("Creating test user account")
        val testID = snowflake.nextId()
        users[testID] = User(test = true, identifier = snowflake.nextId())
        logger.info("Created test account -> ${users[testID]}")
        if (batchInterval >= 0) {
            Timer().scheduleAtFixedRate(UserUpdateBatch(), 5000, (batchInterval * 1000).toLong())
        }
    }

    inner class UserUpdateBatch : java.util.TimerTask() {
        override fun run() {
            logger.info("Batch get is running... (${batchInterval}s)")
            userColl.find().forEach { user ->
                val existing = users[user.identifier]
                if (existing == null) {
                    user.cache = this@UserCache
                    logger.info("Found existing user (${user.identifier}) on DB that isn't on this API instance... Creating it now")
                    user.avatar?.idJSON = user.avatar?.identifier.toString()
                    FileController().createUserDir(user.identifier)
                    users[user.identifier] = user
                } else if (existing != user) {
                    existing.replace(user)
                }
            }
        }
    }
    companion object {
    }
}

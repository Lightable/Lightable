package rebase

import com.feuer.chatty.getResizedMaintainingAspect
import io.javalin.core.util.FileUtil
import me.kosert.flowbus.EventsReceiver
import me.kosert.flowbus.subscribe
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.ExecutorService
import javax.imageio.ImageIO
import kotlin.collections.HashMap


class Cache(private val executor: ExecutorService, val db: RebaseMongoDatabase, private val snowflake: Snowflake, val server: Server, val fileController: FileController) {
    val logger = LoggerFactory.getLogger("Rebase -> CACHE")
    val userColl = db.getUserCollection()
    val users = HashMap<Long, User>()
    val userAvatarCache = HashMap<Long, ByteArrayOutputStream>()
    fun saveOrReplaceUser(user: User, saveToDB: Boolean = true) {
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
    fun removeAllTestUsers() {
        db.getUserCollection().find().forEach { u -> if (u.test) println("DELETE Test user found ${u.email}") }
        users.values.forEach { u ->
            if (u.test) {

                db.getUserCollection().findOneAndDelete(User::email eq u.email)
                users.remove(u.identifier)
            }
        }

    }
    init {
        logger.info("Initialized")
        userColl.find().forEach { user ->
            user.cache = this
            println("Found existing user: ${user.name} ${user.identifier}")
            users[user.identifier] = user
            user.avatar?.idJSON = user.avatar?.identifier.toString()
            FileController().createUserDir(user.identifier)

        }
        logger.info("Creating test user account")
        val testID = snowflake.nextId()
        users[testID] = User(test = true, identifier = snowflake.nextId())
        logger.info("Created test account -> ${users[testID]}")
    }

    companion object {

    }
}
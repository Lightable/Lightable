package rebase

import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ExecutorService


class Cache(private val executor: ExecutorService, val db: RebaseMongoDatabase) {
    val logger = LoggerFactory.getLogger("Rebase -> CACHE")
    val userColl = db.getUserCollection()
    val users = HashMap<Long, User>()
    fun saveOrReplaceUser(user: User) {
        users[user.identifier] = user
        executor.submit {
            if (userColl.findOne(User::email eq user.email) == null) {
                logger.info("Inserting NewUser ${user.identifier}")
                userColl.insertOne(user)
            } else {
                logger.info("Replacing NewUser ${user.identifier}")
                userColl.findOneAndReplace(User::email eq user.email, user)
            }

        }
    }

    init {
        logger.info("Initialized")
        userColl.find().forEach { user ->
            user.cache = this
            println("Found existing user: ${user.name} ${user.identifier}")
            users[user.identifier] = user
        }
    }

    companion object
}
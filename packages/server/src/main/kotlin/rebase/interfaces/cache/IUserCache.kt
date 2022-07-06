package rebase.interfaces.cache

import org.slf4j.ILoggerFactory
import rebase.Snowflake
import rebase.schema.User
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService

interface IUserCache {
    val snowflake: Snowflake
    val batchInterval: Int
    val users: HashMap<Long, User>
    val avatarCache: HashMap<Long, ByteArrayOutputStream>
    fun saveOrReplaceUser(user: User, saveToDB: Boolean = true)
    fun deleteUser(user: Long)
    fun removeAllTestUsers()
    fun sameName(name: String): Boolean
    fun sameEmail(email: String): Boolean
}
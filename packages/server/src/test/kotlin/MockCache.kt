
import org.slf4j.LoggerFactory
import rebase.FileController
import rebase.Snowflake
import rebase.interfaces.cache.IUserCache
import rebase.schema.ChattyRelease
import rebase.schema.User
import java.io.ByteArrayOutputStream

class MockCache(override val snowflake: Snowflake, override val batchInterval: Int): IUserCache {
    val logger = LoggerFactory.getLogger("Rebase -> CACHE")
    override val users = HashMap<Long, User>()
    override val avatarCache = HashMap<Long, ByteArrayOutputStream>()

    override fun saveOrReplaceUser(user: User, saveToDB: Boolean) {
        users[user.identifier] = user
    }

    override fun deleteUser(user: Long) {
        TODO("Not yet implemented")
    }

    override fun removeAllTestUsers() {
        users.values.forEach { u ->
            if (u.test) {
                users.remove(u.identifier)
            }
        }
    }

    override fun sameName(name: String): Boolean {
        return users.values.find { u -> u.name == name } != null
    }

    override fun sameEmail(email: String): Boolean {
        return users.values.find { u -> u.email == email } != null
    }

    companion object {
    }
}
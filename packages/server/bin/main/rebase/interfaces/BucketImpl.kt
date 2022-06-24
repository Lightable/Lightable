package rebase.interfaces

import com.fasterxml.jackson.databind.ObjectMapper
import rebase.interfaces.cache.IUserCache

interface BucketImpl {
    val cache: IUserCache?
    val jackson: ObjectMapper
    fun save(saveToDatabase: Boolean = true)
    fun remove()
    fun toJSON(): String
}

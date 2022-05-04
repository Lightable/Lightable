package rebase.interfaces

import com.fasterxml.jackson.databind.ObjectMapper
import rebase.Cache

interface BucketImpl {
    val cache: Cache?
    val jackson: ObjectMapper
    fun save(saveToDatabase: Boolean = true)
    fun remove()
    fun toJSON(): String
}

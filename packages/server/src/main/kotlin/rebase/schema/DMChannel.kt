package rebase.schema

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bson.codecs.pojo.annotations.BsonProperty
import rebase.interfaces.cache.IDMCache
import rebase.messages.DMDao

data class DMChannel constructor(
    @JsonIgnore @BsonIgnore var cache: IDMCache? = null,
    @JsonIgnore @BsonIgnore val jackson: ObjectMapper = jacksonObjectMapper(),
    @JsonIgnore @BsonProperty("identifier") var identifier: Long = 0,
    @JsonIgnore var users: MutableList<Long> = mutableListOf(),
    @BsonProperty("type") val type: Int = DMChannelTypes.FRIEND.ordinal,
    @JsonIgnore @BsonIgnore var dao: DMDao? = null
    )  {
    @BsonProperty("StringID") @JsonProperty("id") val id = identifier.toString()

    @BsonIgnore
    fun addUser(id: Long) {
        if (users.contains(id)) {
            return
        } else {
            this.users.add(id)
        }
    }
    @BsonIgnore
    fun removeUser(id: Long) {
        if (users.contains(id)) {
            this.users.remove(id)
        } else {
            return
        }
    }
    @BsonIgnore
     fun save(saveToDatabase: Boolean) {
        this.cache?.saveOrReplaceChannel(this, saveToDatabase)
    }
    @BsonIgnore
    fun remove() {
        TODO("Not yet implemented")
    }
    @BsonIgnore
     fun toJSON(): String {
        TODO("Not yet implemented")
    }

    companion object {

    }
}
enum class DMChannelTypes {
    FRIEND,
    GROUP
}
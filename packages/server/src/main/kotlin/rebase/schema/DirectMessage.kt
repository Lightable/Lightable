package rebase.schema

import com.fasterxml.jackson.annotation.JsonIgnore
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

/**
 * Direct Message class for MongoDB
 * @param id Use this to recognize DM from DM keyspace
 * @param users Users contained in the DM (For Groups)
 */
data class DirectMessage @BsonCreator constructor(
    @BsonProperty("id") @JsonIgnore var id: Long = 0,
    @BsonProperty("users") @JsonIgnore val users: MutableList<Long> = mutableListOf()
)
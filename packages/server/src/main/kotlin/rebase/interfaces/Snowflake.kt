package rebase.interfaces

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bson.codecs.pojo.annotations.BsonProperty
import rebase.TimeUtil


class Snowflake @BsonCreator constructor(
    @BsonProperty("identifier") override val identifier: Long
) : ISnowflake {
    @BsonIgnore
    val timeCreated = TimeUtil.getTimeCreated(identifier).toEpochSecond()
}

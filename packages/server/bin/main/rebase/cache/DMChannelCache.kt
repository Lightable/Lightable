package rebase.cache

import com.datastax.oss.driver.api.core.CqlSession
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import rebase.RebaseMongoDatabase
import rebase.Snowflake
import rebase.interfaces.cache.IDMCache
import rebase.messages.DMDao
import rebase.schema.DMChannel
import java.util.concurrent.ExecutorService

class DMChannelCache(private val executor: ExecutorService, private val db: RebaseMongoDatabase, private val session: CqlSession, override val snowflake: Snowflake) : IDMCache {
    override val channels: HashMap<Long, DMChannel> = HashMap()
    private val logger: Logger = LoggerFactory.getLogger("Rebase -> DMChannelCache")
    val dmChannelColl = db.getDMCollection()

    override fun saveOrReplaceChannel(channel: DMChannel, saveToDB: Boolean) {
        channels[channel.identifier] = channel
        if (saveToDB) {
            executor.submit {
                if (dmChannelColl.findOne(DMChannel::id eq channel.id) == null) {
                    dmChannelColl.insertOne(channel)
                } else {
                    dmChannelColl.findOneAndReplace(DMChannel::id eq channel.id, channel)
                }
            }
        }
    }

    init {
        logger.info("Initialized")
        if (dmChannelColl.countDocuments() > 0) {
            dmChannelColl.find().forEach { channel ->
                channel.cache = this
                channel.dao = DMDao("dm_${channel.identifier}", session)
                channel.dao!!.init()
                logger.info("Found DMChannel ${channel.identifier}")
                channels[channel.identifier] = channel
            }
        }
    }
}
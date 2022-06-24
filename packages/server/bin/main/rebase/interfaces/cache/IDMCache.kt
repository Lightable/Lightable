package rebase.interfaces.cache

import rebase.Snowflake
import rebase.schema.DMChannel
import rebase.schema.User

interface IDMCache {
    val snowflake: Snowflake
    val channels: HashMap<Long, DMChannel>
    fun saveOrReplaceChannel(channel: DMChannel, saveToDB: Boolean = true)
}
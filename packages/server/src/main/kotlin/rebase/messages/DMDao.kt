package rebase.messages

import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.ResultSet
import com.datastax.oss.driver.api.core.cql.SimpleStatement
import com.datastax.oss.driver.api.core.type.DataTypes
import com.datastax.oss.driver.api.mapper.annotations.*
import com.datastax.oss.driver.api.querybuilder.QueryBuilder
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.Instant

@Dao
interface ChannelDao {
    val session: CqlSession
    val table: String

    @Select
    fun getByID(messageID: Long): Message?
    @Select
    fun getByUser(userID: Long): Message?
    @Select
    fun getByContent(content: String): MutableList<Message>

    fun createMessage(message: Message)
}
@Dao
data class DMDao(override val table: String, override val session: CqlSession): ChannelDao {
    override fun getByID(messageID: Long): Message? {
        TODO("Not yet implemented")
    }

    override fun getByUser(userID: Long): Message? {
        val select = session.execute("SELECT content, created, type, edited FROM zenspace_messages.${table} WHERE author = '${userID}'")
        val messages = mutableListOf<Message>()

        TODO("Not yet implemented")
    }

    override fun getByContent(content: String): MutableList<Message> {
        val select = session.execute("SELECT id, content, system, type, channel, author, created, edited FROM zenspace_messages.${table} WHERE content LIKE '${content}' ALLOW FILTERING").all()
        val messages = mutableListOf<Message>()
        select.forEach { row ->
            println(jacksonObjectMapper().writeValueAsString(row))
        }
        return mutableListOf()
    }
    fun init() {
        createKeyspace()
        useKeyspace()
        val createTable = SchemaBuilder.createTable(table)
            .ifNotExists()
            .withPartitionKey("id", DataTypes.TEXT)
            .withColumn("content", DataTypes.TEXT)
            .withColumn("system", DataTypes.BOOLEAN)
            .withColumn("type", DataTypes.INT)
            .withColumn("channel", DataTypes.TEXT)
            .withColumn("author", DataTypes.TEXT)
            .withColumn("created", DataTypes.TIMESTAMP)
            .withColumn("edited", DataTypes.TIMESTAMP)
        executeStatement(createTable.build())
    }
    override fun createMessage(message: Message) {
        val insert = QueryBuilder.insertInto(table)
            .value("id", QueryBuilder.bindMarker())
            .value("content", QueryBuilder.bindMarker())
            .value("system", QueryBuilder.bindMarker())
            .value("type", QueryBuilder.bindMarker())
            .value("channel", QueryBuilder.bindMarker())
            .value("author", QueryBuilder.bindMarker())
            .value("created", QueryBuilder.bindMarker())
            .value("edited", QueryBuilder.bindMarker()).build()
        insert.setKeyspace(KEYSPACE)
        val prepareStatement = session.prepare(insert)
        val statement = prepareStatement.bind()
            .setString(0, message.id.toString())
            .setString(1, message.content)
            .setBoolean(2, message.system)
            .setInt(3, message.type)
            .setString(4, message.channel.toString())
            .setString(5, message.author.toString())
            .setInstant(6, message.created)
            .setInstant(7, message.edited)
        session.execute(statement)
    }
    private fun executeStatement(statement: SimpleStatement): ResultSet {
        statement.keyspace = CqlIdentifier.fromCql(KEYSPACE)
        return session.execute(statement)
    }
    private fun createKeyspace() {
        val createKeyspace = SchemaBuilder.createKeyspace(KEYSPACE)
            .ifNotExists()
            .withSimpleStrategy(1)
            session.execute(createKeyspace.build())
    }
    private fun useKeyspace() {
        session.execute("USE ${CqlIdentifier.fromCql(KEYSPACE)}")
    }
    companion object {
        val KEYSPACE = "zenspace_messages"
    }
}
@Entity
data class Message(
               @PartitionKey var id: Long? = null,
               var content: String? = null,
               var system: Boolean = false,
               var type: Int = MessageType.Text.ordinal,
               var channel: Long? = null,
               var author: Long? = null,
               var created: Instant? = Instant.now(),
               var edited: Instant? = null) {
}


enum class MessageType {
    Text,
    Attachment,
    FriendAccept
}
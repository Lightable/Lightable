package rebase.messages

import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.ResultSet
import com.datastax.oss.driver.api.core.cql.SimpleStatement
import com.datastax.oss.driver.api.core.type.DataTypes
import com.datastax.oss.driver.api.mapper.annotations.Dao
import com.datastax.oss.driver.api.mapper.annotations.Entity
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey
import com.datastax.oss.driver.api.mapper.annotations.Select
import com.datastax.oss.driver.api.querybuilder.QueryBuilder
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.joda.time.DateTime
import java.math.BigInteger
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * ChannelDao the DAO for any Channel Messages
 * @param session The current CQL Session
 * @param table The table (includes id) dm_{id}
 */
@Dao
interface ChannelDao {
    val session: CqlSession
    val table: String

    /**
     * Get message by id
     * @param messageID Message ID
     */
    @Select
    fun getByID(messageID: Long): Message?

    /**
     * Get message by user id that sent it
     * @param userID User ID
     */
    @Select
    fun getByUser(userID: Long): Message?

    /**
     * Get content
     * @param content The 'EXACT' message content i.e "This is a message" NOT "message"
     */
    @Select
    fun getByContent(content: String): MutableList<Message>

    /**
     * Get all messages
     */
    @Select
    fun getAll(): MutableList<Message>

    /**
     * Get messages after last id
     * @param id Message ID
     * @param limit How (up to) messages do you want back
     */
    fun getMessagesAfterLastID(id: Long, limit: Int): MutableList<Message>

    /**
     * Get messages before last ID
     * @param id Message ID
     * @param limit How (up to) messages do you want back
     */
    fun getMessagesBeforeLastID(id: Long, limit: Int): MutableList<Message>

    /**
     * Delete all messages
     * @param executeCallback Get the timing it took to delete the messages
     * @param errorCallback If an error occurs, callback to handle it.
     */
    fun deleteAll(executeCallback: (timing: Long) -> Unit, errorCallback: (e: Exception) -> Unit)

    /**
     * Create message
     * @param message The message to add to the Direct Message DAO
     */
    fun createMessage(message: Message)

    /**
     * Delete message based on message id
     * @param msgid Message ID
     * @param executeCallback Get the timing it took to delete the message
     * @param errorCallback If an error occurred, callback to handle it.
     */
    fun delete(msgid: Long, executeCallback: (timing: Long) -> Unit, errorCallback: (e: Exception) -> Unit)
}

@Dao
data class DMDao(override val table: String, override val session: CqlSession) : ChannelDao {
    override fun getByID(messageID: Long): Message? {
        TODO("Not yet implemented")
    }

    override fun getByUser(userID: Long): Message? {
        val select = session.execute("SELECT content, created, type, edited FROM ${KEYSPACE}.${table}.${table} WHERE author = '${userID}'")
        val messages = mutableListOf<Message>()
        TODO("Not yet implemented")
    }

    override fun getByContent(content: String): MutableList<Message> {
        val select = session.execute("SELECT id, content, system, type, channel, author, created, edited FROM ${KEYSPACE}.${table}.${table} WHERE content LIKE '${content}' ALLOW FILTERING").all()
        val messages = mutableListOf<Message>()
        select.forEach { row ->
            messages.add(
                Message(
                    id = row.getString("id")!!.toLong(),
                    content = row.getString("content"),
                    system = row.getBoolean("system"),
                    channel = row.getString("channel")?.toLong(),
                    author = row.getString("author")?.toLong(),
                    created = row.getInstant("created")!!,
                    edited = row.getInstant("edited")
                )
            )
        }
        return messages
    }

    override fun getAll(): MutableList<Message> {
        val select = session.execute("SELECT id, content, system, type, channel, author, created, edited FROM ${KEYSPACE}.${table}")
        val messages = mutableListOf<Message>()
        select.forEach { row ->
            messages.add(
                Message(
                    id = row.getString("id")!!.toLong(),
                    content = row.getString("content"),
                    system = row.getBoolean("system"),
                    channel = row.getString("channel")?.toLong(),
                    author = row.getString("author")?.toLong(),
                    created = row.getInstant("created")!!,
                    edited = row.getInstant("edited")
                )
            )
        }
        return messages
    }

    override fun getMessagesAfterLastID(id: Long, limit: Int): MutableList<Message> {
        val firstSelect = session.execute("SELECT * FROM ${KEYSPACE}.${table} WHERE id < ${BigInteger.valueOf(id)}").all()
        println(jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(firstSelect))
        return mutableListOf()
    }

    override fun getMessagesBeforeLastID(id: Long, limit: Int): MutableList<Message> {
        TODO("Not yet implemented")
    }

    override fun deleteAll(executeCallback: (timing: Long) -> Unit, errorCallback: (e: Exception) -> Unit) {
        val startTiming = System.currentTimeMillis()
        try {
            session.execute("TRUNCATE zenspace_messages.${table}")
        } catch (e: Exception) {
            errorCallback(e)
        } finally {
            executeCallback(System.currentTimeMillis() - startTiming)
        }
    }

    override fun delete(msgid: Long, executeCallback: (timing: Long) -> Unit, errorCallback: (e: Exception) -> Unit) {
        val startTiming = System.currentTimeMillis()
        try {
            session.execute("DELETE FROM ${KEYSPACE}.${table}.${table} WHERE id='${msgid}' IF EXISTS;")
        } catch (e: Exception) {
            errorCallback(e)
        } finally {
            executeCallback(System.currentTimeMillis() - startTiming)
        }
    }

    fun init() {
        createKeyspace()
        useKeyspace()
        val createTable = SchemaBuilder.createTable(table)
            .ifNotExists()
            .withPartitionKey("id", DataTypes.BIGINT)
            .withColumn("content", DataTypes.TEXT)
            .withColumn("system", DataTypes.BOOLEAN)
            .withColumn("type", DataTypes.INT)
//            .withColumn("embeds", DataTypes.TEXT)
            .withColumn("channel", DataTypes.BIGINT)
            .withColumn("author", DataTypes.BIGINT)
            .withColumn("created", DataTypes.TIMESTAMP)
            .withColumn("edited", DataTypes.TIMESTAMP)
            .withColumn("gameData", DataTypes.TEXT)
        executeStatement(createTable.build())
    }

    override fun createMessage(message: Message) {
        val rendered = "INSERT INTO ${KEYSPACE}.${table} (id, content, system, type, channel, author, created, edited, gameData) VALUES (${BigInteger.valueOf(message.id)}, '${message.content}', ${message.system}, ${message.type}, ${message.channel}, ${message.author}, '${message.created.toEpochMilli()}', NULL, '${message.gameData}');"
        println("Rendered Query $rendered")
        session.execute(rendered)
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
    @PartitionKey var id: Long = 0L,
    var content: String? = null,
    var system: Boolean = false,
    var type: Int = MessageType.Text.ordinal,
    var channel: Long? = null,
    var author: Long? = null,
    val created: Instant = Instant.now(),
    var edited: Instant? = null,
    val gameData: String? = null
) {

    init {
        println("New message created at ${created}")
    }
}

interface IGame {
    val name: String
    val players: MutableList<Long>
    val created: Instant
    var gameData: Any
    fun start()
    fun pause()
    fun stop()
    fun update(data: Any)
}

class Connect4(
    override val name: String,
    override val players: MutableList<Long>,
    override val created: Instant,
) : IGame {
    val rows = mutableListOf<Row>()

    // Rows
    override var gameData: Any = mutableListOf<Connect4>()
    override fun start() {
        TODO("Not yet implemented")
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun update(data: Any) {
        TODO("Not yet implemented")
    }

    class Row() {
        var full = false
        val columns = mutableListOf<Column>()
        fun isColumnAvailable(location: Int): Boolean {
            return columns[location].occupied
        }

        fun whatIsInColumn(location: Int): Player? {
            return columns[location].occupiedWithPlayer
        }

        fun putPlayerInColumn(location: Int, player: Player) {
            columns[location].occupiedWithPlayer = player
        }

        init {
            for (i in 1..7) {
                this.columns.add(Column())
            }
        }
    }

    class Column {
        var occupied = false
        var occupiedWithPlayer: Player? = null
    }

    data class Player(val color: String)

    init {
        for (i in 1..6) {
            this.rows.add(Row())
        }
    }
}

enum class MessageType {
    Text,
    Attachment,
    GameStart,
    GameEnd
}
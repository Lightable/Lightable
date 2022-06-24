package rebase.messages

import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.cql.ResultSet
import com.datastax.oss.driver.api.core.cql.SimpleStatement
import com.datastax.oss.driver.api.core.type.DataTypes
import com.datastax.oss.driver.api.querybuilder.QueryBuilder
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder
import com.datastax.oss.driver.internal.core.type.codec.BigIntCodec
import rebase.Snowflake

class TestMessageRepo(val snowflake: Snowflake, val session: CqlSession) {
    fun createTable() {
        createTable(null)
    }

    fun createTable(keyspace: String?) {
        val deleteTable = executeStatement(SchemaBuilder.dropTable(TABLE).ifExists().build(), keyspace)
        val createTable = SchemaBuilder.createTable(TABLE)
            .withPartitionKey("id", DataTypes.TEXT)
            .withColumn("content", DataTypes.TEXT)
            .withColumn("created", DataTypes.TIMESTAMP)
        executeStatement(createTable.build(), keyspace)
    }

    fun addTestMessage(message: TestMessage, keyspace: String?): Long? {
        message.id = snowflake.nextId()
        val insertInto = QueryBuilder.insertInto(TABLE)
            .value("id", QueryBuilder.bindMarker())
            .value("content", QueryBuilder.bindMarker())
            .value("created", QueryBuilder.bindMarker())
        var insertStatement = insertInto.build()
        if (keyspace != null) {
            insertStatement = insertStatement.setKeyspace(keyspace)
        }
        val preparedStatement = session.prepare(insertStatement)
        val statement = preparedStatement.bind()
            .setString(0, message.id!!.toString())
            .setString(1, message.content)
            .setInstant(2, message.created)
        session.execute(statement)
        return message.id
    }

    fun getAll(keyspace: String?): MutableList<TestMessage> {
        val select = QueryBuilder.selectFrom(TABLE).all()
        val resultSet = executeStatement(select.build(), keyspace).all()
        val convertedResults = mutableListOf<TestMessage>()
        resultSet.forEach { x ->
            convertedResults.add(TestMessage(x.getString("id")?.toLong(), x.getString("content"), x.getInstant("created")))
        }
        return convertedResults
    }
    private fun executeStatement(statement: SimpleStatement, keyspace: String?): ResultSet {
        if (keyspace != null) {
            statement.keyspace = CqlIdentifier.fromCql(keyspace)
        }
        return session.execute(statement)
    }

    companion object {
        private const val TABLE = "testmessages"
    }
}
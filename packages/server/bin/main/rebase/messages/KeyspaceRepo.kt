package rebase.messages

import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder

class KeyspaceRepo(private val session: CqlSession) {
    fun createKeyspace(keyspace: String, nof: Int) {
        val createKeyspace = SchemaBuilder.createKeyspace(keyspace)
            .ifNotExists()
            .withSimpleStrategy(nof)
        session.execute(createKeyspace.build())
    }
    fun useKeyspace(keyspace: String) {
        session.execute("USE ${CqlIdentifier.fromCql(keyspace)}")
    }
}
package rebase.messages

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.CqlSessionBuilder
import java.net.InetSocketAddress

class ScyllaConnector {
    private lateinit var session: CqlSession;

    fun connect(node: String, port: Int, center: String) {
        var builder = CqlSession.builder()
        builder.addContactPoint(InetSocketAddress(node, port))
        builder.withLocalDatacenter(center)
        session = builder.build()
    }

    fun getSession(): CqlSession {
        return this.session
    }

    fun close() {
        session.close()
    }
}
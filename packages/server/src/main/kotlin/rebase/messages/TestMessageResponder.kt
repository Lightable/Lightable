package rebase.messages

import rebase.Snowflake
import java.time.Instant

fun main() {
    val connector = ScyllaConnector()
    val snowflake = Snowflake()
    connector.connect("192.168.50.111", 9097, "datacenter1")
    val session = connector.getSession()
    val dmDB = DMDao("dm_1992992922", session)
    dmDB.init()
    dmDB.getByContent("message")
}
import com.datastax.oss.driver.api.core.CqlSession
import org.junit.jupiter.api.*
import rebase.Snowflake
import rebase.messages.DMDao
import rebase.messages.Message
import rebase.messages.ScyllaConnector
import kotlin.system.measureTimeMillis

@DisplayName("Test Messages")
class MessageTest {
    private val connector = ScyllaConnector()
    val snowflake = Snowflake()
    var session: CqlSession? = null

    @DisplayName("Test DM Messages")
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class DM {
        private val dmDB = DMDao("dm_0", session!!)
        private val virtualCache = HashMap<Long, Message>()

        @Test
        fun `Create 10 Messages`() {
            val timing = measureTimeMillis {
                for (i in 0..10) {
                    val message = Message(snowflake.nextId(), "This is message #${i}", author = 0)
                    dmDB.createMessage(message)
                    virtualCache[message.id] = message
                }
            }
            println("Finished in ${timing}ms")
        }

        @Test
        fun `Retrieve All Messages`() {
            if (dmDB.getAll().size < 10) {
                fail("Should've got exactly 10 messages")
            } else {
                return
            }
        }

        @Test
        fun `Retrieve Message By ID`() {
            val secondMessage = virtualCache.keys.toMutableList()[1]
            val message = dmDB.getByID(secondMessage)
            if (message != null) {
                return
            } else {
                fail("Could not retrieve message with id '${secondMessage}'")
            }
        }

        @Test
        fun `Retrieve Messages By Author ID`() {
            val messages = dmDB.getMessagesByUser(0)
            if (messages.size >= 9) {
                return
            } else {
                fail("Should have more than 8 messages the user sent")
            }
        }

        @Test
        fun `Retrieve Messages By Content`() {
            val messages = dmDB.getMessagesByContent("This is message #3")
            if (messages.isNotEmpty()) {
                return
            } else {
                fail("Message with content 'This is message #3' should not be empty")
            }
        }

        @Test
        fun `Retrieve 5 Messages Before ID`() {
            val messages = dmDB.getMessagesBeforeLastID(virtualCache.keys.toMutableList()[9], 5)
            println("Message Size ${messages.size}")
            if (messages.size == 5) {
                return
            } else {
                fail("Message size should be equal to 5")
            }
        }

        @Test
        fun `Retrieve 5 Messages After ID`() {
            val messages = dmDB.getMessagesAfterLastID(virtualCache.keys.toMutableList()[1], 5)
            println("Message Size ${messages.size}")
            if (messages.size == 5) {
                return
            } else {
                fail("Message size should be equal to 4")
            }
        }

        @Test
        fun `Delete Message By ID`() {
            val firstMessage = virtualCache.keys.first()
            dmDB.delete(firstMessage, {
                println("Took ${it}ms")
            }, {
                fail("Something went wrong", it)
            })
        }


        init {
            dmDB.init()
            println("Deleting all messages from previous tests if any...")
            dmDB.deleteAll({
                println("Messages deleted in ${it}ms")
            }, {
                println("Something went wrong..")
                it.printStackTrace()
            })
        }
    }
    init {
        println("🔌 Attempting connection to Scylla Instance...")
        connector.connect("drone", 9042, "datacenter1")
        println("✅ Connection established!")
        session = connector.getSession()
    }
}
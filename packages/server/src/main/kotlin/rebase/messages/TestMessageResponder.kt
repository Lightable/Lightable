package rebase.messages

import rebase.Snowflake
import java.time.Instant
import kotlin.system.measureTimeMillis

fun main() {
    val connector = ScyllaConnector()
    val snowflake = Snowflake()
    connector.connect("192.168.50.111", 9042, "datacenter1")
    val session = connector.getSession()
    val dmDB = DMDao("dm_1992992922", session)
    dmDB.init()
    dmDB.deleteAll(executeCallback = {
        println("All DM Messages deleted in ${it}ms")
    }, errorCallback = {

    })
    val messageId = snowflake.nextId()
    val messageCreateTiming = measureTimeMillis {
        dmDB.createMessage(Message(messageId, "This is a message", false, MessageType.Text.ordinal))
        Thread.sleep(200)
        dmDB.createMessage(Message(snowflake.nextId(), "This is a message 1", false, MessageType.Text.ordinal))
        Thread.sleep(200)
        dmDB.createMessage(Message(snowflake.nextId(), "This is a message 2", false, MessageType.Text.ordinal))
        Thread.sleep(200)
        dmDB.createMessage(Message(snowflake.nextId(), "This is a message 3", false, MessageType.Text.ordinal))
    }
    println("Created message in ${messageCreateTiming}ms")
//    val existedMessage = dmDB.getAll()
//    println("Found Message? $existedMessage")
    Thread.sleep(200)
    val getExactMessage = dmDB.getMessagesAfterLastID(messageId, 50)
//    val deleteMessage = dmDB.delete(messageId, {
//        println("Message with id $messageId deleted in ${it}ms")
//        val afterDeleteExistingMessages = dmDB.getAll()
//        println("Found Message? $afterDeleteExistingMessages")
//
//    }, {
//
//    })
//    println("Waiting 10s and then deleting all rows")
    readln()
    dmDB.deleteAll(executeCallback = {
        println("All DM Messages deleted in ${it}ms")
    }, errorCallback = {

    })
}
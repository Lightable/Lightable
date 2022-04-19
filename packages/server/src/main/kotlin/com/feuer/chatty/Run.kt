package com.feuer.chatty

import com.feuer.chatty.server.Server
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import rebase.MongoDatabase
import kotlin.system.measureTimeMillis


fun main(args: Array<String>) {
    val logger: Logger = LoggerFactory.getLogger("Main")
    val coldLog: Logger = LoggerFactory.getLogger("Main (Cold)")
    var logreq = false
    if (args.contains("--logreq") || args.contains("-LR")) {
        logreq =
            true; println("Flag [${args[0]}] - Logging requests can potentially cause a lot more CPU usage! You've been warned.")
    }
    println(Utils.BANNER)
    println("Chatty Backend ${Utils.VERSION}\n\n-><-LOGGING-><-")

    val globalExceptionHandler = Handler()
    Thread.setDefaultUncaughtExceptionHandler(globalExceptionHandler)
    val db = MongoDatabase()
    val storage = Storage(db)
    runBlocking {
        coldLog.info("Performing cold start on DB (Mongo) [Following output may be hidden based on log level]")
        val timing = measureTimeMillis {
            val userCol = db.getUserCollection()
            for (u in userCol.find()) {
                coldLog.debug("[CACHE] - User [${u.name} (${u.identifier})] - Created: ${u.created.toEpochMilli()}")
            }
        }
        coldLog.info("Finished cold start on DB (Mongo) in ${timing}ms")
    }
    val server = Server(db, storage, logreq)
    server.start(5898)
    server.startWS()
    server.startRestEndpoints()
}
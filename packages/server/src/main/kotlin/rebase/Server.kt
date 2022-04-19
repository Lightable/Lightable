package rebase

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.feuer.chatty.Utils
import com.github.ajalt.mordant.terminal.Terminal
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.core.util.RouteOverviewPlugin
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation
import io.javalin.plugin.openapi.dsl.documented
import io.javalin.plugin.openapi.ui.ReDocOptions
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.swagger.v3.oas.models.info.Info
import org.slf4j.LoggerFactory
import rebase.controllers.WebSocketController
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

val t = Terminal()
fun main(args: Array<String>) {
    Thread.currentThread().name = "Server (Main)"
    Thread.setDefaultUncaughtExceptionHandler { thread, err ->
        clearConsole()
        println("Exception at " + t.colors.info.invoke(thread.name) + "\n")
        println("Estimated stack cause: ${err.stackTrace.last()}")
        println("[${t.colors.danger.invoke("Begin Trace")}]\n")
        print(err.stackTraceToString())
        println("[${t.colors.danger.invoke("Finish Trace")}]\n")
        println("[${t.colors.brightGreen.invoke("Context")}]")
        println("[Cause]: ${err.cause}")
        println("[Message]: ${err.message}")
        println("[Message -> Local]: ${err.localizedMessage}")
        println("[${t.colors.brightGreen.invoke("End Context")}]\n")
        println("[${t.colors.brightMagenta.invoke("Thread Stack")}]")
        thread.stackTrace.forEach { trace ->
            println(trace.toString())
        }
    }
    val logger = LoggerFactory.getLogger("Server")
    val root: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
    var port = 8080
    var isProd = false
    println(Utils.BANNER)
    println("${t.colors.brightRed.invoke("---->>")} Config ${t.colors.brightBlue.invoke("<<----")}")
    if (args[args.indexOf("--port") + 1].isNotBlank()) {
        port = args[args.indexOf("--port") + 1].toInt()
    } else if (!System.getenv("port").isNullOrBlank()) {
        port = System.getenv("port").toInt()
    }
    println(t.colors.brightBlue.invoke("PORT: $port"))
    if (args.contains("--verbose") || System.getenv("verbose").toBoolean()) {
        println("${t.colors.brightYellow.invoke("Verbose is enabled ")}⚠️")
        root.level = Level.DEBUG
    } else {
        root.level = Level.INFO
    }
    if (args.contains("--batch") || System.getenv("batchdb").toBoolean()) {
        println("${t.colors.brightYellow.invoke("Batching DB requests is enabled ")}⚠️")
    }
    if (args.contains("--prod") || System.getenv("prod").toBoolean()) {
        println("${t.colors.brightGreen.invoke("Production mode is enabled ")}✅")
        isProd = true
    }
    println("${t.colors.brightRed.invoke("---->>")} Logs ${t.colors.brightBlue.invoke("<<----\n")}")
    val snowflake = Snowflake()
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy").withZone(ZoneId.systemDefault())
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss").withZone(ZoneId.systemDefault())
    val javalin = Javalin.create {
        it.showJavalinBanner = false
        it.enableCorsForAllOrigins()
        it.maxRequestSize = 20971520
        it.registerPlugin(RouteOverviewPlugin("/routes"))
        it.registerPlugin(getConfiguredOpenApiPlugin())
        it.requestLogger { ctx, executionTimeMs ->
            val date = Instant.now()
            val dateStr = "${dateFormatter.format(date)} " + "- ${timeFormatter.format(date)}"
            println(
                "[Rebase]: $dateStr |   ${executionTimeMs}ms   | ${ctx.req.remoteHost}  | ${
                    determineMethodColor(
                        ctx.method()
                    )
                }  \"${ctx.path()}\""
            )
        }
    }!!
    javalin.exception(java.lang.Exception::class.java) { e, ctx ->
        println("\nException at " + t.colors.info.invoke(ctx.path()) + "\n")
        println("[${t.colors.danger.invoke("Begin Trace")}]")
        print(e.stackTraceToString())
        println("[${t.colors.danger.invoke("Finish Trace")}]\n")
        println("[${t.colors.brightGreen.invoke("Context")}]")
        println("[Cause]: ${e.cause}")
        println("[Message]: ${e.message}")
        println("[Message -> Local]: ${e.localizedMessage}")
        println("[${t.colors.brightGreen.invoke("End Context")}]\n")
    }
    javalin.start(port)
    val db = RebaseMongoDatabase()
    val async = Executors.newCachedThreadPool()
    val cache = Cache(async, db)
    val websocketController = WebSocketController(logger, cache, isProd)
    javalin.exception(Exception::class.java) { e, ctx ->
        ctx.json(object { val e = e.message }).status(500)
    }
    javalin.routes {
        ws("/ws") {
            it.onConnect(websocketController::connection)
            it.onMessage(websocketController::message)
        }
        path("/user") {
            val user = rebase.controllers.UserController(cache, snowflake, async)
            post(documented(user.createUserDoc, user::createUser))
            path("/@me") {
                post("login", documented(user.loginUserDoc, user::login))
                get(documented(user.getSelfUserDoc, user::getSelfUser))
                path("/relationships") {
                    val relationship = user.Relationships()
                    post("{id}", documented(relationship.createPendingRelationshipDoc, relationship::addRelationship))
                    get(documented(relationship.getSelfRelationshipsDoc, relationship::getRelationships))
                    get("{id}", documented(relationship.getSelfRelationshipDoc, relationship::getRelationship))
                    path("/pending") {
                        post("/pending/{id}", documented(relationship.addPendingRelationshipDoc, relationship::acceptPendingRelationship))
                        delete("/pending/{id}", documented(relationship.removePendingRelationshipDoc, relationship::removePendingRelationship))
                    }
                    delete("{id}", documented(relationship.removeRelationshipDoc, relationship::removeRelationship))
                }
            }


        }
    }


}

fun determineMethodColor(method: String): String {
    val GETBG = t.colors.blue.bg("     $method     ")
    val POSTBG = t.colors.brightBlue.bg("   $method   ")
    val DELETEBG = t.colors.red.bg("   $method   ")
    val PATCHBG = t.colors.green.bg("   $method   ")
    val OPTIONBG = t.colors.gray.bg("   $method   ")
    val HEADBG = t.colors.magenta.bg("   $method   ")
    val PUTBG = t.colors.yellow.bg("   $method   ")
    return when (method) {
        "GET" -> GETBG
        "POST" -> POSTBG
        "DELETE" -> DELETEBG
        "PATCH" -> PATCHBG
        "OPTIONS" -> OPTIONBG
        "HEAD" -> HEADBG
        "PUT" -> PUTBG
        else -> t.colors.cyan.bg(method)
    }
}
fun getConfiguredOpenApiPlugin() = OpenApiPlugin(
    OpenApiOptions(
        Info().apply {
            version("0.0.1")
            description("Chatty API")
        }
    ).activateAnnotationScanningFor("com.feuer.chatty.rebase").apply {
        path("/swagger-docs") // endpoint for OpenAPI json
        swagger(SwaggerOptions("/swagger-ui")) // endpoint for swagger-ui
        reDoc(ReDocOptions("/redoc")) // endpoint for redoc
    })

fun clearConsole() {
    try {
        val os = System.getProperty("os.name")
        if (os.contains("Windows")) {
            Runtime.getRuntime().exec("cls")
        } else {
            Runtime.getRuntime().exec("clear")
        }
    } catch (e: Exception) {
        //  Handle any exceptions.
    }
}





package rebase

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.github.ajalt.mordant.terminal.Terminal
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.core.util.RouteOverviewPlugin
import io.javalin.http.staticfiles.Location
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.dsl.documented
import io.javalin.plugin.openapi.ui.ReDocOptions
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.swagger.v3.oas.models.info.Info
import org.slf4j.LoggerFactory
import rebase.controllers.CDNController
import rebase.controllers.DeveloperController
import rebase.controllers.WebSocketController
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

val t = Terminal()

class Server(var dbhost: String = "localhost",
             var dbport: Int = 27017,
             var dbuser: String = "root",
             var dbpass: String = "rootpass") {
    val logger: org.slf4j.Logger = LoggerFactory.getLogger("Server")!!
    val root: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
    var port = 8080

    var isProd = false
    private val snowflake = Snowflake()
    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd-MM-yyyy").withZone(ZoneId.systemDefault())
    private val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("hh:mm:ss").withZone(ZoneId.systemDefault())
    private val async: ExecutorService = Executors.newCachedThreadPool()
    private val fileController = FileController()
    private val db: RebaseMongoDatabase = RebaseMongoDatabase(dbuser, dbpass, dbhost, dbport)
    val javalin: Javalin = Javalin.create {
        it.showJavalinBanner = false
        it.enableCorsForAllOrigins()
        it.maxRequestSize = 20971520
        it.registerPlugin(RouteOverviewPlugin("/routes"))
        it.registerPlugin(getConfiguredOpenApiPlugin())
        it.addStaticFiles { static ->
            static.directory = "./releases"
            static.location = Location.EXTERNAL
            static.hostedPath = "/release"
        }
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
    }
    val cache = Cache(async, db, snowflake, this, fileController)
    private val websocketController = WebSocketController(logger, cache, isProd)

    private val developerController = DeveloperController(cache)
    val user = rebase.controllers.UserController(cache, snowflake, async, isProd, fileController)
    private val cdnController = CDNController(cache, fileController)
    init {
        Thread.currentThread().name = "Server (Main)"
        Thread.setDefaultUncaughtExceptionHandler { thread, err ->
            clearConsole()
            System.err.println("Exception at " + t.colors.info.invoke(thread.name) + "\n")
            System.err.println("Estimated stack cause: ${err.stackTrace.last()}")
            System.err.println("[${t.colors.danger.invoke("Begin Trace")}]\n")
            System.err.println(err.stackTraceToString())
            System.err.println("[${t.colors.danger.invoke("Finish Trace")}]\n")
            System.err.println("[${t.colors.brightGreen.invoke("Context")}]")
            System.err.println("[Cause]: ${err.cause}")
            System.err.println("[Message]: ${err.message}")
            System.err.println("[Message -> Local]: ${err.localizedMessage}")
            System.err.println("[${t.colors.brightGreen.invoke("End Context")}]\n")
            System.err.println("[${t.colors.brightMagenta.invoke("Thread Stack")}]")
            thread.stackTrace.forEach { trace ->
                System.err.println(trace.toString())
            }
        }
        javalin.routes {
            ws("/ws") {
                it.onConnect(websocketController::connection)
                it.onMessage(websocketController::message)
                it.onClose(websocketController::close)
            }
            path("/cdn") {
                path("/releases") {
                    get(cdnController::getReleases)
                    get("{release}", cdnController::getRelease)
                }
                path("/user") {
                    get("/{user}/avatars/avatar_{avatar}", cdnController::getUserAvatar)
                }
            }
            path("/admin") {
                patch("/disable/{id}", developerController::disableUser)
                post("/release", developerController::createRelease)
            }
            path("/user") {
                post("/test", user::createTestUsers)
                post(user::createUser)
                get(user::getAllUsers)
                path("/@me") {
                    post("login", documented(user.loginUserDoc, user::login))
                    get(documented(user.getSelfUserDoc, user::getSelfUser))
                    patch(documented(user.patchUserDoc, user::update))
                    post("/avatar", user::updateAvatar)
                    path("/relationships") {
                        val relationship = user.Relationships()
                        post(
                            "{name}",
                            documented(relationship.createPendingRelationshipDoc, relationship::addRelationship)
                        )
                        get(documented(relationship.getSelfRelationshipsDoc, relationship::getRelationships))
                        get("{id}", documented(relationship.getSelfRelationshipDoc, relationship::getRelationship))
                        path("/pending") {
                            post(
                                "{id}",
                                documented(
                                    relationship.addPendingRelationshipDoc,
                                    relationship::acceptPendingRelationship
                                )
                            )
                            delete(
                                "{id}",
                                documented(
                                    relationship.removePendingRelationshipDoc,
                                    relationship::removePendingRelationship
                                )
                            )
                        }
                        delete("{id}", documented(relationship.removeRelationshipDoc, relationship::removeRelationship))
                    }
                }
            }
        }
    }

    fun determineMethodColor(method: String): String {
        val GETBG = t.colors.blue.bg("      $method       ")
        val POSTBG = t.colors.brightBlue.bg("       $method       ")
        val DELETEBG = t.colors.red.bg("       $method       ")
        val PATCHBG = t.colors.green.bg("       $method       ")
        val OPTIONBG = t.colors.gray.bg("       $method       ")
        val HEADBG = t.colors.magenta.bg("       $method       ")
        val PUTBG = t.colors.yellow.bg("       $method       ")
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
        ).activateAnnotationScanningFor("rebase.controllers").apply {
            path("/swagger-docs") // endpoint for OpenAPI json
            swagger(SwaggerOptions("/swagger-ui")) // endpoint for swagger-ui
            reDoc(ReDocOptions("/redoc")) // endpoint for redoc
        }
    )

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
}

fun main(args: Array<String>) {
    println(Utils.BANNER)
    var dbhost: String = "localhost"
    var dbport: Int = 27017
    var dbuser: String = "root"
    var dbpass: String = "rootpass"
    File("./releases").mkdir()
    File("./storage").mkdir()
    if (!System.getenv("dbport").isNullOrBlank()) {
        dbport = System.getenv("dbport").toInt()
    }
    if (!System.getenv("dbuser").isNullOrBlank()) {
        dbuser = System.getenv("dbuser")
    }
    if (!System.getenv("dbhost").isNullOrBlank()) {
        dbhost = System.getenv("dbhost")
    }
     if (!System.getenv("dbpass").isNullOrBlank()) {
        dbpass = System.getenv("dbpass")
    }
    val server = Server(dbhost, dbport, dbuser, dbpass)

    println("${t.colors.brightRed.invoke("---->>")} Config ${t.colors.brightBlue.invoke("<<----")}")
    if (args[args.indexOf("--port") + 1].isNotBlank()) {
        server.port = args[args.indexOf("--port") + 1].toInt()
    } else if (!System.getenv("port").isNullOrBlank()) {
        server.port = System.getenv("port").toInt()
    }
    println(t.colors.brightBlue.invoke("PORT: ${server.port}"))
    if (args.contains("--verbose") || System.getenv("verbose").toBoolean()) {
        println("${t.colors.brightYellow.invoke("Verbose is enabled ")}⚠️")
        server.root.level = Level.DEBUG
    } else {
        server.root.level = Level.INFO
    }
    if (args.contains("--batch") || System.getenv("batchdb").toBoolean()) {
        println("${t.colors.brightYellow.invoke("Batching DB requests is enabled ")}⚠️")
    }
    if (args.contains("--prod") || System.getenv("prod").toBoolean()) {
        println("${t.colors.brightGreen.invoke("Production mode is enabled ")}✅")
        server.isProd = true
    }

    if (server.isProd) {
        val file = File("err.clog")
        val fos = FileOutputStream(file)
        val ps = PrintStream(fos)
        System.setErr(ps)
    }
    println("${t.colors.brightRed.invoke("---->>")} Logs ${t.colors.brightBlue.invoke("<<----\n")}")
    server.javalin.start(server.port)
}

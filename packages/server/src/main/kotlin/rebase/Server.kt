package rebase

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.datastax.oss.driver.api.core.CqlSession
import com.github.ajalt.mordant.terminal.Terminal
import com.sun.management.OperatingSystemMXBean
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
import me.kosert.flowbus.EventsReceiver
import me.kosert.flowbus.GlobalBus
import me.kosert.flowbus.subscribe
import okio.ByteString.Companion.encodeUtf8
import okio.internal.commonAsUtf8ToByteArray
import org.slf4j.LoggerFactory
import rebase.cache.DMChannelCache
import rebase.cache.UserCache
import rebase.controllers.CDNController
import rebase.controllers.DeveloperController
import rebase.controllers.WebSocketController
import rebase.detection.NudeDetection
import rebase.generator.EmbedImageGenerator
import rebase.messages.ScyllaConnector
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.lang.management.ManagementFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Timer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.imageio.ImageIO

val t = Terminal()

class Server(
    var dbhost: String = "localhost",
    var dbport: Int = 27017,
    var dbuser: String = "root",
    var dbpass: String = "rootpass",
    var batchInterval: Int,
    var nudeAPIGateway: String = "http://localhost:8089",
    var session: CqlSession
) {
    val logger: org.slf4j.Logger = LoggerFactory.getLogger("Server")!!
    var totalRequests = 0
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
    private val napi = NudeDetection(nudeAPIGateway)
    private val imageGen = EmbedImageGenerator()
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
        it.accessManager { handler, ctx, _ ->
            if (serverOverloaded) {
                ctx.status(503)
                    .html("<!DOCTYPE html><head><title>ZenSpace is overloaded</title></head><body> <h1>Server is Overloaded, Please Wait.<br>CPU Usage: ${serverCPUUsage}%</h1></body> <style>:root { font-family: Arial; color: red; font-weight: bold; }</style><script>setInterval(() => {location.reload()}, 5000)</script></html>")
            } else {
                handler.handle(ctx)
            }
        }
        it.requestLogger { ctx, executionTimeMs ->
            val date = Instant.now()
            val dateStr = "${dateFormatter.format(date)} " + "- ${timeFormatter.format(date)}"
            totalRequests += 1
            if (!isProd) {
                println(
                    "[Rebase]: $dateStr |   ${executionTimeMs}ms   | ${ctx.req.remoteHost}  | ${
                        determineMethodColor(
                            ctx.method()
                        )
                    }  \"${ctx.path()}\""
                )
            }
        }
    }
    val userCache = UserCache(async, db, snowflake, this, fileController, batchInterval)
    val dmCache = DMChannelCache(async, db, session, snowflake, batchInterval)
    private val websocketController = WebSocketController(logger, userCache, isProd)

    private val developerController = DeveloperController(userCache)
    val user = rebase.controllers.UserController(userCache, dmCache, session, snowflake, async, isProd, fileController, napi)
    private val cdnController = CDNController(userCache, fileController)
    private var serverOverloaded = false
    private var serverCPUUsage = 0L
    private val events = EventsReceiver()

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
            get("/experimental/image/generate") {
                val type = it.queryParam("type")
                var text = it.queryParam("text") ?: "No Text"
                val color = it.queryParam("color") ?: "ffffff"
                val stamp = it.queryParam("stamp").toBoolean()
                val matched = Regex("(\\[\\[(?:0x)?\\d+\\w+\\b\\]\\])").findAll(text)
                matched.forEach { matchResult ->
                    val resultUnparsed = matchResult.value.replace("[[", "")
                    val result = resultUnparsed.replace("]]", "")
                    text = text.replace(matchResult.value, String(Character.toChars(Integer.decode(result))))
                }
                try {
                    Color.decode("#$color")
                } catch (e: Exception) {
                    e.printStackTrace()
                    it.status(401)
                    return@get
                }
                when (type) {
                    "EMBED" -> {
                        val image = imageGen.generateEmbed(text, "#$color", stamp)
                        val baos = ByteArrayOutputStream()
                        ImageIO.write(image.image, "webp", baos)
                        val imgInBytes = baos.toByteArray()
                        it.contentType("image/webp")
                        it.res.setContentLength(imgInBytes.size)
                        it.res.addHeader("X-Image-Gen-Timing", image.timing.toString())
                        it.res.addHeader("Content-Disposition", "inline; filename=\"Image took ${image.timing}ms to generate\"")
                        it.status(201).result(imgInBytes)
                    }
                    else -> {
                        it.status(401)
                    }
                }
            }
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
                path("/users") {
                    get("enabled", developerController::getEnabledUsers)
                    get("disabled", developerController::getDisabledUsers)
                    patch("enable/{id}", developerController::enableUser)
                    patch("disable/{id}", developerController::disableUser)
                    delete("delete/{id}", developerController::deleteUser)
                }
                post("/release", developerController::createRelease)
            }
            path("/user") {
                post("/test", user::createTestUsers)
                post(user::createUser)
                get(user::getAllUsers)
                path("/profiles") {
                    val profiles = user.Profiles()
                    get("/", profiles::getProfiles)
                    get("profile/{id}", profiles::getProfile)
                }
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
                    val dmChannel = user.DMChannel()
                    path("/{id}/messages") {
                        get(dmChannel::getMessages)
                        post("send", dmChannel::sendMessage)
                    }
                }
            }
        }
        events.subscribe<GetCPUUsage.CPUUsageUpdate> {
            this.serverOverloaded = it.usage > 75
            this.serverCPUUsage = it.usage
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
    var dbBatchUpdateInterval = System.getenv("batchint").toInt() ?: 30
    var nudeAPIGateway = System.getenv("napi")
    var prod = false
    val root: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
    println("Working -> ${File("./storage").absolutePath}")

    File("./releases").mkdir()
    File("./storage").mkdir()
    if (System.getenv("verbose").toBoolean()) {
        println("${t.colors.brightYellow.invoke("Verbose is enabled ")}⚠️")
        root.level = Level.DEBUG
    } else {
        root.level = Level.INFO
    }
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
    if (System.getenv("prod").toBoolean()) {
        println("${t.colors.brightGreen.invoke("Production mode is enabled ")}✅")
        prod = true
    }
    println("Nudity Detection Gateway -> $nudeAPIGateway")
    val scyllaHost = System.getenv("SCYLLA_HOST") ?: "192.168.50.111"
    val connector = ScyllaConnector()
    connector.connect(scyllaHost, 9042, "datacenter1")
    val server = Server(dbhost, dbport, dbuser, dbpass, dbBatchUpdateInterval, nudeAPIGateway, connector.getSession())
    server.isProd = prod
    println("${t.colors.brightRed.invoke("---->>")} Config ${t.colors.brightBlue.invoke("<<----")}")
    if (!System.getenv("port").isNullOrBlank()) {
        server.port = System.getenv("port").toInt()
    }
    println(t.colors.brightBlue.invoke("PORT: ${server.port}"))

    if (System.getenv("batchdb").toBoolean()) {
        println("${t.colors.brightYellow.invoke("Batching DB requests is enabled ")}⚠️")
    }


    if (server.isProd) {
        val file = File("err.clog")
        val fos = FileOutputStream(file)
        val ps = PrintStream(fos)
        System.setErr(ps)
    }
    println("${t.colors.brightRed.invoke("---->>")} Logs ${t.colors.brightBlue.invoke("<<----\n")}")
    server.javalin.start(server.port)
    Timer().scheduleAtFixedRate(GetCPUUsage(), 0, 2000)
}


class GetCPUUsage() : java.util.TimerTask() {
    override fun run() {
        val osBean: OperatingSystemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)
        GlobalBus.post(CPUUsageUpdate((osBean.processCpuLoad * 100).toLong()))
    }

    data class CPUUsageUpdate(val usage: Long)
}
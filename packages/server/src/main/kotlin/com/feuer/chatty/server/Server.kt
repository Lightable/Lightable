package com.feuer.chatty.server


import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.feuer.chatty.Message.Embed
import com.feuer.chatty.Storage
import com.feuer.chatty.Utils
import com.feuer.chatty.schemas.MongoSchemas
import com.feuer.chatty.server.controllers.RestController
import com.feuer.chatty.server.controllers.SSEController
import com.feuer.chatty.server.controllers.WebsocketController
import com.google.gson.Gson
import io.javalin.Javalin
import io.javalin.core.util.RouteOverviewPlugin
import io.javalin.http.Context
import io.javalin.http.staticfiles.Location
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.ui.ReDocOptions
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.prometheus.client.Counter
import io.prometheus.client.exporter.HTTPServer
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.bson.codecs.pojo.annotations.BsonCreator
import org.eclipse.jetty.server.handler.StatisticsHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import rebase.MongoDatabase
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.Instant
import kotlin.reflect.full.memberProperties


class Server(val mongoDB: MongoDatabase, val storage: Storage, val logreq: Boolean) {
    var server: Javalin? = null
    val gson = Gson()
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val clients = mutableListOf<String>()
    var lastSentPayload: Any? = null
    val statsServer = StatisticsHandler()
    private val userCollection = mongoDB.getUserCollection()
    private val metricsServer = HTTPServer(7080)
    fun start(port: Int): Server {
        server = server!!.start(port)
        server!!.before {
            it.res.addHeader("Server", "Comput")
        }
        return this
    }

    fun startWS() {
        WebsocketController(server!!, mongoDB)
    }

    fun startRestEndpoints() {
        server!!.get("/") {
            it.redirect("/redoc", 301)
        }

        RestController(
            server!!,
            mongoDB.getUserCollection(),
            mongoDB.getDMCollection(),
            mongoDB.getReleaseCollection(),
            storage,
            gson
        )

        SSEController(server!!)
        /**
         * Completely Disabled
         * Will Be removed 1.9.5
         */

        server!!.exception(Exception::class.java) { e, ctx ->
            e.printStackTrace()
            ctx.status(500).json(object {
                val exception = e::class.java.name
                val message = e.message
                val cause = e.cause
                val CLASS = e.stackTrace.first()
                val stack: Array<StackTraceElement>? =
                    if (ctx.req.getHeader("detailed") != null && ctx.req.getHeader("detailed") == "true") e.stackTrace else null
            }
            )
        }
    }

    init {
        val totalRequests =
            Counter.build().name("chatty_backend_total_requests").help("Total requests for chatty backend").register()
        server = Javalin.create { config ->
            run {
                config.enableCorsForAllOrigins()
                config.showJavalinBanner = true
                config.addStaticFiles {
                    it.location = Location.EXTERNAL
                    it.directory = "./storage/"
                    it.hostedPath = "/cdn"
                }
                config.registerPlugin(RouteOverviewPlugin("/routes"))
                config.maxRequestSize = 10000000 // show all routes on specified path
                config.requestLogger { ctx, timing ->
                    totalRequests.inc()
                    if (logreq) {
                        reqLog(ctx, timing)
                    }
                }
            }

        }
    }

    private fun getOpenApiOptions(): OpenApiOptions {
        val applicationInfo: Info = Info()
            .version(Utils.VERSION)
            .description("Chatty Backend Systems")
            .title("Chatty API")
            .license(License().name("MIT"))
        return OpenApiOptions(applicationInfo).path("/docs")
            .swagger(SwaggerOptions("/sd").title("Chatty Swagger Documentation")) // Activate the swagger ui
            .reDoc(ReDocOptions("/redoc").title("Chatty ReDoc Documentation")) // Active the ReDoc UI
    }

    private fun convertPayload(payload: Any): String? {
        return gson.toJson(payload)
    }

    private fun reqLog(ctx: Context, timing: Float) {
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING
        if (!clients.contains(ctx.ip())) {
            clients.add(ctx.ip())
            logger.info("Client issued a request, total unique clients ${clients.size}")
        }
        startHeader("Request (${ctx.handlerType().name})")
        logger.info("   \u001B[31mURL -> ${ctx.url()}")
        if (ctx.queryString() != null) {
            logger.info("   \u001B[31mQueries -> ${ctx.queryParamMap()}}")
        }
        if (ctx.body().isNotBlank()) {
            logger.info("   \u001B[31mRequest Payload -> ${Utils.formatSize(ctx.body().length.toLong())}")
        }
        logger.info("   \u001B[31mResponse Payload -> ${Utils.formatSize(lastSentPayload.toString().length.toLong())}")
        logger.info("   \u001B[31mTiming -> Took ${df.format(timing)}ms\u001B[0m")
        endHeader("Request (${ctx.handlerType().name})")
    }

    private fun startHeader(name: String) {
        logger.info("\u001B[34m$name\u001B[0m")
    }

    private fun endHeader(name: String) {
        logger.info("\u001B[34m$name\u001B[0m")
    }
}


enum class WebsocketTypes {
    CONNECTED,
    DISCONNECT,
    ERRORED,
    MESSAGE,
    PING,
    PONG
}

enum class EventTypes {
    MESSAGE,
    MESSAGEDELETE,
    PROFILEUPDATE,
    DELETE,
    FRIEND,
    STATUS
}

enum class OPTypes {
    CONNECT,
    MESSAGE,
    ERROR,
    DISCONNECT,
    LOGIN,
    IN,
    PING
}

data class GenericUser(
    val auth: String,
    /** val publicKey: String */
)

data class GenericMessage(val auth: String, val content: String, val embeds: ArrayList<Embed>?)
data class UserCreate(val email: String, val password: String, val name: String, val type: String)
data class UserGet(val email: String, val password: String)
data class AvatarData(val auth: String, val avatar: String)
data class AboutMeData(val auth: String, val about: String)
data class AddFriend(val auth: String, val user: String)
data class UserPatch(val auth: String, val change: Changeable)
data class Changeable(val type: String, val new: String, val obj: Any?, val animated: Boolean?)
data class User(val name: String, val userid: String, val avatar: String, val created: String, val status: Boolean)
data class Status(val text: String?, val icon: String?)

@Throws(IllegalAccessException::class, ClassCastException::class)
inline fun <reified T> Any.getField(fieldName: String): T? {
    this::class.memberProperties.forEach { kCallable ->
        if (fieldName == kCallable.name) {
            return kCallable.getter.call(this) as T?
        }
    }
    return null
}

class ClientPing {
    val op = OPTypes.PING.ordinal
}

object Events {
    enum class UserUpdates {
        AVATAR, ABOUT, NAME
    }

    class ProfileUpdate(val profile: Any, val auth: String, private val type: UserUpdates) {
        val op = OPTypes.MESSAGE.ordinal
        val profileOP = type.ordinal
    }

    class WebsocketError(val message: String, val e: Exception) {
        val op = OPTypes.ERROR.ordinal
        val error = e.message
        val cause = e.cause
    }

    class WebsocketPong {
        val op = OPTypes.MESSAGE.ordinal
        val type = WebsocketTypes.PONG.ordinal
    }

    class FriendUpdate(val friend: MongoSchemas.MongoUser.Public) {
        val op = OPTypes.MESSAGE.ordinal
        val etype = EventTypes.FRIEND.ordinal
    }

    class StatusUpdate(val friend: User, val user: MongoSchemas.MongoUser) {
        val op = OPTypes.MESSAGE.ordinal
        val etype = EventTypes.FRIEND.ordinal
    }
}

class TooMuchSeverLoad(message: String) : Exception(message)
data class Messages(val messages: ArrayList<FleshedMessage>, val end: String)
data class FleshedMessage @BsonCreator constructor(
    val user: MongoSchemas.MongoUser.Public,
    val content: String,
    @JsonFormat(
        shape = JsonFormat.Shape.NUMBER,
        timezone = "UTC"
    )
    val created: SpecialDate,
    val id: String,
    val embeds: ArrayList<Embed>?
)

data class SpecialDate(@JsonIgnore private val date: Instant) {
    val sec = date.toEpochMilli() / 1000
    val milli = date.toEpochMilli()
}



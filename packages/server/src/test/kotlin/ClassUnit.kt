import com.github.ajalt.mordant.rendering.TextColors
import io.javalin.http.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import rebase.Server
import java.lang.AssertionError
import kotlin.system.measureTimeMillis

class ClassUnit {
    private val ctx = mockk<Context>(relaxed = true)
    private val server = Server()
    @Test
    fun `POST to create 50 example users`() {
        every {
            ctx.queryParam("s")
        } returns "50"
        val timeCreation = measureTimeMillis {
            server.user.createTestUsers(ctx)
        }
        val message = "Specified completion time was 200ms test was completed in ${timeCreation}ms"
        println("50 users created in ${timeCreation}ms")
        verify {
            ctx.status(201)
        }
        if (timeCreation > 200) {
            TestLogger.logFailure(message)
            throw AssertionError(message)
        } else {
            TestLogger.logSuccess(message)
        }
    }

    @Test
    fun `Delete all TEST users`() {
        val timeDeletion = measureTimeMillis {
            server.user.removeTestUsers(ctx)
        }
        val message = "Specified completion time was 200ms test was completed in ${timeDeletion}ms"
        println(message)
    }
    init {
        println("Waiting for visual VM")
    }
}

object TestLogger {
    fun logFailure(message: String) {
        println(TextColors.red("❌ $message"))
    }
    fun logSuccess(message: String) {
        println(TextColors.green("✔️ $message"))
    }
}

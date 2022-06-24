package rebase.controllers

import io.javalin.http.Context
import rebase.cache.UserCache
import rebase.Snowflake
import java.util.concurrent.ExecutorService

class AnalyticsController(val userCache: UserCache, val snowflake: Snowflake, val executor: ExecutorService) {
    fun getMessageAnalytic(ctx: Context) {
        val totalMessages = 80000
        val totalDM = 1000
    }
}

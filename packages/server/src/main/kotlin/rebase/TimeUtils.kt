package rebase

import rebase.interfaces.ISnowflake
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.annotation.Nonnull


object TimeUtil {
    private const val EPOCH = 1420070400000L
    private const val TIMESTAMP_OFFSET: Long = 22
    private val dtFormatter: DateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
    fun getTimestamp(millisTimestamp: Long): Long {
        return millisTimestamp - EPOCH shl TIMESTAMP_OFFSET.toInt()
    }

    @Nonnull
    fun getTimeCreated(entityId: Long): OffsetDateTime {
        val timestamp = (entityId ushr TIMESTAMP_OFFSET.toInt()) + EPOCH
        val gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        gmt.timeInMillis = timestamp
        return OffsetDateTime.ofInstant(gmt.toInstant(), gmt.timeZone.toZoneId())
    }

    @Nonnull
    fun getTimeCreated(@Nonnull entity: ISnowflake): OffsetDateTime {
        return getTimeCreated(entity.identifier)
    }

    @Nonnull
    fun getDateTimeString(@Nonnull time: OffsetDateTime): String {
        return time.format(dtFormatter)
    }
}
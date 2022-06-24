package rebase.messages

import java.time.Instant
import java.util.*


data class TestMessage(
    var id: Long? = null,
    var content: String? = null,
    var created: Instant? = null
)


package rebase.events

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@OptIn(DelicateCoroutinesApi::class)
class EventBus {
    private val _events = MutableSharedFlow<Any?>(
        extraBufferCapacity = 1
    )
    var delay: Long = 0
    var _eventsSent = 0
    var _eventsReceived = 0

    val events = _events.asSharedFlow()

    suspend fun post(event: Any?) {
        _events.emit(event)
        delay = System.currentTimeMillis()
        _eventsSent += 1
    }

    fun postGlobal(event: Any?) {
        GlobalScope.launch { _events.emit(event) }
        _eventsSent += 1
    }
    fun postWithPotentialFail(event: Any?) {
        _events.tryEmit(event)
        _eventsSent += 1
    }

    suspend inline fun <reified T>subscribe(crossinline callback: suspend (event: T) -> Unit) {
        // it as is generic for compiler to not complain
        this.events.filter { it is T }.collectLatest {
            _eventsReceived += 1
            callback(it as T)
            logger.debug("S/R (Flow): ${_eventsSent}/${_eventsReceived} (${System.currentTimeMillis() - delay}ms)")
        }
    }


    init {
        _events.onEach(EventBus::printlnDebug).launchIn(GlobalScope)
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger("Rebase -> EventBusFlow")
        private fun printlnDebug(obj: Any?) {
            logger.debug("(Flow ${if (obj != null) obj::class.java.name else "No payload"}): $obj")
        }
    }
 }
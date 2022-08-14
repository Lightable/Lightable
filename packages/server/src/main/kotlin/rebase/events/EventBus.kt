package rebase.events

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

@OptIn(DelicateCoroutinesApi::class)
class EventBus {
    private val _events = MutableSharedFlow<Any?>(
        extraBufferCapacity = 1
    )

    val events = _events.asSharedFlow()

    suspend fun post(event: Any?) {
        _events.emit(event)
    }

    fun postGlobal(event: Any?) {
        GlobalScope.launch { _events.emit(event) }
    }
    fun postWithPotentialFail(event: Any?) {
        _events.tryEmit(event)
    }

    suspend inline fun <reified T>subscribe(crossinline callback: suspend (event: T) -> Unit) {
        // it as is generic for compiler to not complain
        this.events.filter { it is T }.collectLatest {
            logger.debug("Received event $it")
            callback(it as T)
        }
    }


    init {
        _events.onEach(EventBus::printlnDebug).launchIn(GlobalScope)
    }

    companion object {
        val logger = LoggerFactory.getLogger("Rebase -> EventBusFlow")
        private fun printlnDebug(obj: Any?) {
            logger.debug("On flow: $obj")
        }
    }
 }
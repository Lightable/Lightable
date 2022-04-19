package com.feuer.chatty.server.controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.javalin.Javalin
import io.javalin.http.sse.SseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.kosert.flowbus.EventsReceiver
import me.kosert.flowbus.subscribe
import java.util.*

class SSEController(private val server: Javalin) {
    val events = EventsReceiver()
    val clients = mutableListOf<SseClient>()
    init {

        server.before("/updates") {
            it.res.addHeader("Cache-Control", "no-cache")
            it.res.addHeader("X-Accel-Buffering", "no")
        }
        server.sse("/updates") {
            clients.add(it)
            it.sendEvent(jacksonObjectMapper().writeValueAsString(object { val active = true }))

            it.onClose {
                clients.remove(it)
            }
        }
        events.subscribe<WebsocketController.Update> {
            clients.forEach { client ->
                client.sendEvent( jacksonObjectMapper().writeValueAsString(it))
            }
        }
        fun loop() {
            CoroutineScope(Dispatchers.IO).launch {
                delay(60000)
                try {
                    CoroutineScope(Dispatchers.Default).launch {
                        for (client in clients) {
                            println("💓 Issued")
                            client.sendEvent(jacksonObjectMapper().writeValueAsString(object {
                                val heartbeat = null
                            }))
                        }
                        loop()
                    }
                } catch (e: Exception) {
                    println("Android OS exception. These are typical.. Ignoring...")
                }
            }
        }
        loop()
    }
}
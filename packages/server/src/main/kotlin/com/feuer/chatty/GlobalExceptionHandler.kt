package com.feuer.chatty

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Thread.UncaughtExceptionHandler

class GlobalExceptionHandler {
    fun performArithmeticOperation(num1: Int, num2: Int): Int {
        return num1 / num2
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val globalExceptionHandler = Handler()
            Thread.setDefaultUncaughtExceptionHandler(globalExceptionHandler)
            GlobalExceptionHandler().performArithmeticOperation(10, 0)
        }
    }
}

internal class Handler : UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        LOGGER.info("Unhandled exception caught, in thread = {${t.id}} with error = {${e.message}}")
        e.printStackTrace()
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(Handler::class.java)
    }
}
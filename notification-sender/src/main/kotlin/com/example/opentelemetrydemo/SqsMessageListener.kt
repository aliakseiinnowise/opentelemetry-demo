package com.example.opentelemetrydemo

import io.opentelemetry.api.baggage.Baggage
import mu.KLogging
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.model.Message

fun interface SqsMessageListener {

    suspend fun onMessage(message: Message)
}

@Component
class LoggingListener : SqsMessageListener {
    override suspend fun onMessage(message: Message) {
        logger.info { "Message: $message" }

        val test = Baggage.current().getEntryValue("test").toBoolean()
        logger.info { "Test environment: $test" }
    }

    companion object : KLogging()
}

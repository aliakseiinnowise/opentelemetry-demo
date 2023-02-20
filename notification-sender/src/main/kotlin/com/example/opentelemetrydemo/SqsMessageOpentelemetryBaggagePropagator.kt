package com.example.opentelemetrydemo

import com.fasterxml.jackson.databind.ObjectMapper
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapGetter
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.model.Message

@Component
class SqsMessageOpentelemetryBaggageContextExtractor(
    private val objectMapper: ObjectMapper,
) {

    fun extract(message: Message): Context {
        val baggageString = objectMapper.readTree(message.body())
            .get(MESSAGE_ATTRIBUTE_FIELD)
            ?.get(OPEN_TELEMETRY_BAGGAGE_FIELD)
            ?.get(VALUE_FIELD)
            ?.asText()

        return W3CBaggagePropagator.getInstance().extract(Context.current(), baggageString, BaggageGetter)
    }

    private object BaggageGetter : TextMapGetter<String> {

        private const val BAGGAGE_KEY = "baggage"

        override fun keys(carrier: String) = listOf(BAGGAGE_KEY)
        override fun get(carrier: String?, key: String) = if (key == BAGGAGE_KEY) carrier else null
    }

    companion object {
        private const val MESSAGE_ATTRIBUTE_FIELD = "MessageAttributes"
        private const val OPEN_TELEMETRY_BAGGAGE_FIELD = "opentelemetery-baggage"
        private const val VALUE_FIELD = "Value"
    }
}

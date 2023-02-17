package com.example.opentelemetrydemo

import io.opentelemetry.api.baggage.Baggage
import io.opentelemetry.context.Context
import io.opentelemetry.instrumentation.reactor.v3_1.ContextPropagationOperator
import io.opentelemetry.instrumentation.reactor.v3_1.ContextPropagationOperator.getOpenTelemetryContext
import io.opentelemetry.instrumentation.reactor.v3_1.ContextPropagationOperator.storeOpenTelemetryContext
import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * Temporary substitution until normal instrumentation is not implemented.
 * See [github issue](https://github.com/open-telemetry/opentelemetry-java-instrumentation/issues/7436)
 */
@Component
class OpentelemetryBaggagePropagatingWebFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val headers = exchange.request.headers

        logger.debug { "Received headers: $headers" }

        val baggage = headers[BAGGAGE_HEADER].orEmpty()
            .map { it.toBaggagePair() }
            .fold(Baggage.current().toBuilder()) { acc, (key, value) -> acc.put(key, value) }
            .build()

        return chain.filter(exchange)
            .contextWrite {
                val openTelemetryContext = baggage.storeInContext(getOpenTelemetryContext(it, Context.root()))
                storeOpenTelemetryContext(it, openTelemetryContext)
            }
    }

    private fun String.toBaggagePair(): Pair<String, String> {
        val separatorIndex = indexOf(BAGGAGE_SEPARATOR)
        val key = substring(0, separatorIndex)
        val value = substring(separatorIndex + 1, length)
        return key to value
    }

    companion object : KLogging() {
        private const val BAGGAGE_HEADER = "baggage"
        private const val BAGGAGE_SEPARATOR = '='
    }
}

package com.example.opentelemetrydemo.webflux

import com.example.opentelemetrydemo.TestEnvironmentFlagPropagator
import io.opentelemetry.context.Context
import io.opentelemetry.instrumentation.reactor.v3_1.ContextPropagationOperator.getOpenTelemetryContext
import io.opentelemetry.instrumentation.reactor.v3_1.ContextPropagationOperator.storeOpenTelemetryContext
import mu.KLogging
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * Temporary substitution until normal instrumentation is not implemented.
 * See [github issue](https://github.com/open-telemetry/opentelemetry-java-instrumentation/issues/7436)
 */
class OpentelemetryBaggagePropagatingWebFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val headers = exchange.request.headers

        logger.info { "Received headers: $headers" }

        return chain.filter(exchange)
            .contextWrite {
                val opentelemetryContext = getOpenTelemetryContext(it, Context.current())
                val modifiedContext = TestEnvironmentFlagPropagator.extract(opentelemetryContext, exchange.request, WebfluxRequestTextMapGetter)
                storeOpenTelemetryContext(it, modifiedContext)
            }
    }

    companion object : KLogging()
}

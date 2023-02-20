package com.example.opentelemetrydemo

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.baggage.Baggage
import io.opentelemetry.instrumentation.spring.webflux.v5_0.client.SpringWebfluxTelemetry
import kotlinx.coroutines.reactor.awaitSingle
import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux

@Component
class ExternalUserGateway(
    webClientBuilder: WebClient.Builder,
    openTelemetry: OpenTelemetry,
) {

    private val webClient: WebClient

    init {
        val instrumentation = SpringWebfluxTelemetry.create(openTelemetry)

        webClient = webClientBuilder
            .baseUrl("http://localhost:8081")
            .filters(instrumentation::addClientTracingFilter)
            .build()
    }

    suspend fun getUsers(): List<User> {
        logger.info { "2: ${TestEnvironmentFlag.isInTestEnv()}" }

        return webClient.get()
            .uri("/users")
            .retrieve()
            .bodyToFlux<User>()
            .collectList()
            .awaitSingle()
            .also {
                logger.info { "3: ${TestEnvironmentFlag.isInTestEnv()}" }
            }
    }

    companion object : KLogging()
}

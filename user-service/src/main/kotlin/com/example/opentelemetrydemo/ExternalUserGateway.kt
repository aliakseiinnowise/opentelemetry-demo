package com.example.opentelemetrydemo

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux

@Component
class ExternalUserGateway(
    webClientBuilder: WebClient.Builder,
) {

    private val webClient = webClientBuilder.baseUrl("http://localhost:8081").build()

    suspend fun getUsers() = webClient.get()
        .uri("/users")
        .retrieve()
        .bodyToFlux<User>()
        .collectList()
        .awaitSingle()
}

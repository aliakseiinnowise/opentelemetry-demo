package com.example.opentelemetrydemo

import io.opentelemetry.api.baggage.Baggage
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserResource(
    private val externalUserGateway: ExternalUserGateway,
) {

    @PostMapping("/notify/{name}")
    suspend fun notifyUser(@PathVariable name: String, @RequestParam test: String) {
        val baggage = Baggage.current().toBuilder()
            .put("test", test)
            .build()

        val opentelemetryContext = baggage.storeInContext(Context.current())

        withContext(currentCoroutineContext() + opentelemetryContext.asContextElement()) {
            logger.info { "1: ${Baggage.current().getEntryValue("test")}" }

            val users = externalUserGateway.getUsers()
            logger.info { "users: $users" }

            logger.info { "4: ${Baggage.current().getEntryValue("test")}" }
        }
    }

    companion object : KLogging()
}

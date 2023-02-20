package com.example.opentelemetrydemo

import io.opentelemetry.api.baggage.Baggage
import io.opentelemetry.context.Context
import io.opentelemetry.context.ContextStorage
import io.opentelemetry.context.ContextStorageProvider
import io.opentelemetry.context.Scope
import io.opentelemetry.instrumentation.reactor.v3_1.ContextPropagationOperator.getOpenTelemetryContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.asCoroutineContext
import mu.KLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/users")
class UsersResource {

    @GetMapping
    suspend fun getUsers(): List<User> {
        val reactorContext = currentCoroutineContext()[ReactorContext]!!.context
        val opentelemeteryContext = getOpenTelemetryContext(reactorContext, Context.current())
        val testEnvironment = Baggage.fromContext(opentelemeteryContext).getEntryValue("test").toBoolean()

        logger.info { "Test environment: $testEnvironment" }

        return if (testEnvironment) {
            listOf(User("test", "test@gmail.com"))
        } else {
            listOf(
                User(
                    name = "Yoshito",
                    email = "yoshito.amazaki@gmail.com"
                ),
                User(
                    name = "Igor",
                    email = "igor.shmeltsov@gmail.com",
                )
            )
        }

    }

    companion object : KLogging()
}

data class User(
    val name: String,
    val email: String,
)

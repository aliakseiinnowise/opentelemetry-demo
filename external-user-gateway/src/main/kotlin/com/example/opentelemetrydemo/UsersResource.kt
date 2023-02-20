package com.example.opentelemetrydemo

import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import io.opentelemetry.instrumentation.reactor.v3_1.ContextPropagationOperator.getOpenTelemetryContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UsersResource(
    private val userProviderGateway: UserProviderGateway,
) {

    @GetMapping
    suspend fun getUsers() = withOpentelemetryContext {
        userProviderGateway.getUsers()
    }

    companion object : KLogging()
}

data class User(
    val name: String,
    val email: String,
)

suspend fun <T> withOpentelemetryContext(block: suspend CoroutineScope.() -> T): T {
    val coroutineContext = currentCoroutineContext()
    val reactorContext = coroutineContext[ReactorContext]?.context ?: throw IllegalStateException("No reactor context is present")
    val opentelemeteryContext = getOpenTelemetryContext(reactorContext, Context.current())

    return withContext(coroutineContext + opentelemeteryContext.asContextElement()) {
        block()
    }
}

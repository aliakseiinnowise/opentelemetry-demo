package com.example.opentelemetrydemo

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.baggage.Baggage
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
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
    private val snsMessagePublisher: SnsMessagePublisher,
    openTelemetry: OpenTelemetry,
) {
    private val tracer = openTelemetry.getTracer("user-resource")

    @PostMapping("/notify/{name}")
    suspend fun notifyUser(@PathVariable name: String, @RequestParam test: String) {
        val opentelemetryContext = Context.current()

        val baggage = Baggage.current().toBuilder()
            .put("test", test)
            .build()

        val span = tracer.spanBuilder("notify-user")
            .setParent(opentelemetryContext)
            .startSpan()

        val testFlag = TestEnvironmentFlag(test == "true")

        withContext(currentCoroutineContext() + opentelemetryContext.with(span).with(baggage).with(testFlag).asContextElement()) {

            logger.info { "1: ${TestEnvironmentFlag.isInTestEnv()}" }

            val users = externalUserGateway.getUsers()
            logger.info { "users: $users" }

            users.firstOrNull { it.name == name }?.let { snsMessagePublisher.publish(it) }

            logger.info { "4: ${TestEnvironmentFlag.isInTestEnv()}" }
        }
    }

    companion object : KLogging()
}

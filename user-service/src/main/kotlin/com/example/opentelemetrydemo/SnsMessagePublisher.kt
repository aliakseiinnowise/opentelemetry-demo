package com.example.opentelemetrydemo

import com.fasterxml.jackson.databind.ObjectMapper
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator
import io.opentelemetry.context.Context
import kotlinx.coroutines.future.await
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import java.util.UUID

@Component
class SnsMessagePublisher(
    private val snsClient: SnsAsyncClient,
    private val objectMapper: ObjectMapper,
) {

    @Value("\${sns.topic}")
    private lateinit var topicArn: String

    suspend fun publish(user: User) {
        val attributes = currentBaggageAsString()
            ?.let {
                mapOf(
                    OPEN_TELEMETRY_BAGGAGE_FIELD to MessageAttributeValue.builder().dataType("String").stringValue(it).build(),
                )
            }

        val req = PublishRequest.builder()
            .topicArn(topicArn)
            .message(objectMapper.writeValueAsString(user))
            .messageDeduplicationId(UUID.randomUUID().toString())
            .messageGroupId("group")
            .messageAttributes(attributes)
            .build()

        snsClient.publish(req).await()

        logger.info { "Published message for user: $user" }
    }

    private fun currentBaggageAsString(): String? {
        val stringHolder = Array<String?>(1) { null }
        W3CBaggagePropagator.getInstance().inject(Context.current(), stringHolder) { holder, _, value ->
            holder?.set(0, value)
        }
        return stringHolder[0]
    }

    companion object : KLogging() {
        private const val OPEN_TELEMETRY_BAGGAGE_FIELD = "opentelemetery-baggage"
    }
}

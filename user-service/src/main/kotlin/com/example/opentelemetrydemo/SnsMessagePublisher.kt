package com.example.opentelemetrydemo

import com.fasterxml.jackson.databind.ObjectMapper
import io.opentelemetry.api.baggage.Baggage
import kotlinx.coroutines.future.await
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.SnsAsyncClient
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
        val req = PublishRequest.builder()
            .topicArn(topicArn)
            .message(objectMapper.writeValueAsString(user))
            .messageDeduplicationId(UUID.randomUUID().toString())
            .messageGroupId("group")
            .build()

        snsClient.publish(req).await()

        logger.info { "Published message for user: $user" }
    }

    companion object : KLogging()
}

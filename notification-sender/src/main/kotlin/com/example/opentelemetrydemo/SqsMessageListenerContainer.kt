package com.example.opentelemetrydemo

import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.time.Duration.Companion.seconds

/**
 * The main component for retrieving messages from [sqs](https://aws.amazon.com/sqs/).
 *
 * Messages are fetched from [queueUrl] with [long polling](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-short-and-long-polling.html#sqs-long-polling).
 * After every message is forwarded to [sqsMessageListener] that processes them. Message processing is performed asynchronous.
 *
 * @param listenerCoroutineDispatcher dispatcher where queue polling and message processing take place
 */
@Component
class SqsMessageListenerContainer(
    private val sqsMessageListener: SqsMessageListener,
    private val sqsClient: SqsAsyncClient,

    private val sqsMessageOpentelemetryBaggageContextExtractor: SqsMessageOpentelemetryBaggageContextExtractor,
) {

    companion object : KLogging() {
        private const val MAX_NUMBER_OF_MESSAGES = 10 // 10 is a max allowed value
        private const val WAIT_TIME_SECONDS = 20 // 20 is a max allowed value
        private val AFTER_ERROR_DELAY = 5.seconds
    }

    private val listenerCoroutineScope = CoroutineScope(Dispatchers.Default)

    @Value("\${queue.url}")
    private lateinit var queueUrl: String

    @Volatile
    private var stopped = false
    private var rootListenerJob: Job? = null

    /**
     * Starts queue polling and message processing
     */
    @PostConstruct
    fun start() {
        rootListenerJob = listenerCoroutineScope.launch {
            val receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(MAX_NUMBER_OF_MESSAGES)
                .waitTimeSeconds(WAIT_TIME_SECONDS)
                .build()

            logger.info { "Listening to the sqs queue: $queueUrl" }

            while (!stopped) {
                val messages = try {
                    sqsClient.receiveMessage(receiveRequest).await().messages()
                } catch (e: Exception) {
                    logger.error(e) { "Error during sqs messages fetch for queue: $queueUrl occurred: " }
                    if (stopped) break // if listener should be closed, there is no need to wait
                    delay(AFTER_ERROR_DELAY) // to avoid infinite loop execution
                    continue
                }

                logger.debug { "Processing ${messages.size} messages" }
                messages.forEach { launch { processMessage(it) } }
            }

            logger.info { "Stopped sqs queue: $queueUrl listener. No more messages will be accepted" }
        }
    }

    private suspend fun processMessage(message: Message) =
        withContext(currentCoroutineContext() + sqsMessageOpentelemetryBaggageContextExtractor.extract(message).asContextElement()) {
            try {
                logger.info { "Received message with id: ${message.messageId()}" }

                sqsMessageListener.onMessage(message)

                val deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build()
                sqsClient.deleteMessage(deleteRequest).await()

                logger.info { "Message with id: ${message.messageId()} is processed and successfully removed from sqs" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to process sqs message with id: ${message.messageId()}" }
            }
        }

    @PreDestroy
    fun stop() {
        if (stopped) {
            return
        }

        logger.info { "Stopping SqsListenerContainer for queue: $queueUrl" }

        stopped = true
        rootListenerJob?.cancel() // do not wait for processing of already received messages
        listenerCoroutineScope.cancel()

        logger.info { "SqsListenerContainer for queue: $queueUrl is stopped" }
    }
}

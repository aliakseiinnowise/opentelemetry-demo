package com.example.opentelemetrydemo

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.sqs.SqsAsyncClient

@Configuration
class SqsConfiguration {

    @Bean
    fun sqsAsyncClient(): SqsAsyncClient = SqsAsyncClient.create()
}

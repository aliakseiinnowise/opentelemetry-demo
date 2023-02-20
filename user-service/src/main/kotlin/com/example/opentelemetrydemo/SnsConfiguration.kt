package com.example.opentelemetrydemo

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.sns.SnsAsyncClient

@Configuration
class SnsConfiguration {

    @Bean
    fun snsAsyncClient(): SnsAsyncClient = SnsAsyncClient.create()
}

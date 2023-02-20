package com.example.opentelemetrydemo

import com.example.opentelemetrydemo.webflux.OpentelemetryBaggagePropagatingWebFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PropagatorConfig {

    @Bean
    fun testEnvironmentFlagPropagator() = TestEnvironmentFlagPropagator

    @Bean
    fun opentelemetryBaggagePropagatingWebFilter() = OpentelemetryBaggagePropagatingWebFilter()
}

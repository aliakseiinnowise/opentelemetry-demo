package com.example.opentelemetrydemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ExternalUserGatewayApplication

fun main(args: Array<String>) {
    runApplication<ExternalUserGatewayApplication>(*args)
}

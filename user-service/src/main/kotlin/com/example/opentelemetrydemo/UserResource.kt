package com.example.opentelemetrydemo

import mu.KLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserResource(
    private val externalUserGateway: ExternalUserGateway,
) {

    @PostMapping("/notify")
    suspend fun notifyUser(@RequestParam name: String) {
        val users = externalUserGateway.getUsers()
        logger.info { "users: $users" }
    }

    companion object : KLogging()
}

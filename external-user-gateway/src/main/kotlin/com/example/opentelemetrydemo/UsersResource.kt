package com.example.opentelemetrydemo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UsersResource {

    @GetMapping
    suspend fun getUsers() = listOf(
        User(
            name = "Yoshito",
            email = "yoshito.amazaki@gmail.com"
        ),
        User(
            name = "Igor",
            email = "igor.shmeltsov@gmail.com",
        )
    )
}

data class User(
    val name: String,
    val email: String,
)

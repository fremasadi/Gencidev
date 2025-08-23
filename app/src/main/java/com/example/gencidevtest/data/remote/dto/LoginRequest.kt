package com.example.gencidevtest.data.remote.dto

data class LoginRequest(
    val username: String,
    val password: String,
    val expiresInMins: Int = 30
)
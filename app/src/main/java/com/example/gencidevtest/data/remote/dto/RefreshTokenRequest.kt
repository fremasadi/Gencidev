package com.example.gencidevtest.data.remote.dto

data class RefreshTokenRequest(
    val refreshToken: String,
    val expiresInMins: Int = 30
)
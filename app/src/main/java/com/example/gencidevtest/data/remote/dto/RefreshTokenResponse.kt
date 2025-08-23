package com.example.gencidevtest.data.remote.dto

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)
package com.example.gencidevtest.data.remote.api

import com.example.gencidevtest.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}

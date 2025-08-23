package com.example.gencidevtest.domain.repository

import com.example.gencidevtest.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun logout()
    fun isLoggedIn(): Flow<Boolean>
    fun getCurrentUser(): Flow<User?>
    suspend fun refreshToken(): Result<Boolean>
}
package com.example.gencidevtest.data.repository

import com.example.gencidevtest.data.local.preferences.AuthPreferences
import com.example.gencidevtest.data.remote.api.AuthApiService
import com.example.gencidevtest.data.remote.dto.LoginRequest
import com.example.gencidevtest.domain.model.User
import com.example.gencidevtest.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val authPreferences: AuthPreferences
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val response = apiService.login(LoginRequest(username, password))

            // Save to preferences
            authPreferences.saveAuthData(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                userId = response.id.toString(),
                username = response.username,
                email = response.email,
                firstName = response.firstName,
                lastName = response.lastName
            )

            Result.success(
                User(
                    id = response.id,
                    username = response.username,
                    email = response.email,
                    firstName = response.firstName,
                    lastName = response.lastName,
                    image = response.image
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        authPreferences.clearAuthData()
    }

    override fun isLoggedIn(): Flow<Boolean> = authPreferences.isLoggedIn()

    override fun getCurrentUser(): Flow<User?> {
        return kotlinx.coroutines.flow.combine(
            authPreferences.getUsername(),
            authPreferences.getFirstName()
        ) { username, firstName ->
            if (username != null && firstName != null) {
                User(
                    id = 0, // You can get from preferences if needed
                    username = username,
                    email = "", // Get from preferences if needed
                    firstName = firstName,
                    lastName = "", // Get from preferences if needed
                    image = ""
                )
            } else null
        }
    }

    override suspend fun refreshToken(): Result<Boolean> {
        return try {
            val refreshToken = authPreferences.getRefreshToken().first()
            if (refreshToken != null) {
                // Implement refresh token logic here if needed
                Result.success(true)
            } else {
                Result.failure(Exception("No refresh token available"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
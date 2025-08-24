// app/src/main/java/com/example/gencidevtest/data/repository/AuthRepositoryImpl.kt
package com.example.gencidevtest.data.repository

import android.util.Log
import com.example.gencidevtest.data.local.AuthPreferences
import com.example.gencidevtest.data.remote.api.AuthApiService
import com.example.gencidevtest.data.remote.dto.LoginRequest
import com.example.gencidevtest.domain.model.User
import com.example.gencidevtest.domain.repository.AuthRepository
import com.example.gencidevtest.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val authPreferences: AuthPreferences,
    private val userRepository: UserRepository // Added Room repository
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            Log.d(TAG, "Starting login process for username: $username")
            val response = apiService.login(LoginRequest(username, password))
            Log.d(TAG, "Login API response received: ${response.username}")

            val user = User(
                id = response.id,
                username = response.username,
                email = response.email,
                firstName = response.firstName,
                lastName = response.lastName,
                image = response.image
            )

            // Save to DataStore (for tokens and quick access)
            authPreferences.saveAuthData(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                userId = response.id.toString(),
                username = response.username,
                email = response.email,
                firstName = response.firstName,
                lastName = response.lastName
            )
            Log.d(TAG, "Auth data saved to DataStore")

            // Save to Room Database (for profile data and offline access)
            userRepository.clearCurrentUser() // Clear previous current user
            userRepository.saveUser(
                user = user,
                gender = response.gender,
                isCurrentUser = true
            ).fold(
                onSuccess = {
                    Log.d(TAG, "User data saved to Room database successfully")
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to save user to Room database", exception)
                    // Continue even if Room save fails, don't break login flow
                }
            )

            Log.d(TAG, "Login completed successfully")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            Log.d(TAG, "Starting logout process")

            // Clear DataStore
            authPreferences.clearAuthData()
            Log.d(TAG, "Auth data cleared from DataStore")

            // Clear current user from Room
            userRepository.clearCurrentUser().fold(
                onSuccess = {
                    Log.d(TAG, "Current user cleared from Room database")
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to clear current user from Room database", exception)
                }
            )

            Log.d(TAG, "Logout completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
        }
    }

    override fun isLoggedIn(): Flow<Boolean> = authPreferences.isLoggedIn()

    override fun getCurrentUser(): Flow<User?> {
        // First try to get from Room (more complete data)
        return userRepository.getCurrentUser()
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
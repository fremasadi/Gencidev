// app/src/main/java/com/example/gencidevtest/domain/usecase/GetCurrentUserUseCase.kt
package com.example.gencidevtest.domain.usecase

import com.example.gencidevtest.domain.model.User
import com.example.gencidevtest.domain.repository.AuthRepository
import com.example.gencidevtest.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository // Added Room repository
) {
    // Get current user from Room (preferred for complete data)
    fun fromRoom(): Flow<User?> = userRepository.getCurrentUser()

    // Get current user from DataStore (fallback)
    fun fromDataStore(): Flow<User?> = authRepository.getCurrentUser()

    // Default implementation - tries Room first, then DataStore
    operator fun invoke(): Flow<User?> = userRepository.getCurrentUser()
}
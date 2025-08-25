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

    // Default implementation - tries Room first, then DataStore
    operator fun invoke(): Flow<User?> = userRepository.getCurrentUser()
}
// app/src/main/java/com/example/gencidevtest/domain/usecase/GetCurrentUserProfileUseCase.kt
package com.example.gencidevtest.domain.usecase

import com.example.gencidevtest.domain.model.User
import com.example.gencidevtest.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<User?> = userRepository.getCurrentUser()

}
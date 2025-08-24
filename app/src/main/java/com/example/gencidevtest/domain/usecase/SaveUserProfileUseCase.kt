// app/src/main/java/com/example/gencidevtest/domain/usecase/SaveUserProfileUseCase.kt
package com.example.gencidevtest.domain.usecase

import com.example.gencidevtest.domain.model.User
import com.example.gencidevtest.domain.repository.UserRepository
import javax.inject.Inject

class SaveUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        user: User,
        gender: String? = null,
        isCurrentUser: Boolean = false
    ): Result<Unit> {
        return userRepository.saveUser(user, gender, isCurrentUser)
    }
}
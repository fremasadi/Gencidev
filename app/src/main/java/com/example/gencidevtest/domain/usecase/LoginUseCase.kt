package com.example.gencidevtest.domain.usecase

import com.example.gencidevtest.domain.model.User
import com.example.gencidevtest.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        if (username.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Username dan password tidak boleh kosong"))
        }
        return authRepository.login(username, password)
    }
}
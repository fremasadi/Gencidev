// app/src/main/java/com/example/gencidevtest/domain/repository/UserRepository.kt
package com.example.gencidevtest.domain.repository

import com.example.gencidevtest.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun saveUser(user: User, gender: String? = null, isCurrentUser: Boolean = false): Result<Unit>
    suspend fun getUserById(userId: Int): Result<User?>
    suspend fun getUserByUsername(username: String): Result<User?>
    fun getCurrentUser(): Flow<User?>
    suspend fun getCurrentUserSync(): User?
    fun getAllUsers(): Flow<List<User>>
    suspend fun setUserAsCurrentUser(userId: Int): Result<Unit>
    suspend fun clearCurrentUser(): Result<Unit>
    suspend fun deleteUser(userId: Int): Result<Unit>
    suspend fun deleteAllUsers(): Result<Unit>
    suspend fun updateUser(user: User): Result<Unit>
}
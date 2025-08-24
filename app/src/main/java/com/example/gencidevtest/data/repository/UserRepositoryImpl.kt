// app/src/main/java/com/example/gencidevtest/data/repository/UserRepositoryImpl.kt
package com.example.gencidevtest.data.repository

import com.example.gencidevtest.data.local.dao.UserDao
import com.example.gencidevtest.data.local.entity.toDomain
import com.example.gencidevtest.data.local.entity.toEntity
import com.example.gencidevtest.domain.model.User
import com.example.gencidevtest.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun saveUser(user: User, gender: String?, isCurrentUser: Boolean): Result<Unit> {
        return try {
            val rowId = userDao.insertUser(user.toEntity(gender, isCurrentUser))
            if (rowId > 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to insert user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(userId: Int): Result<User?> {
        return try {
            val userEntity = userDao.getUserById(userId)
            Result.success(userEntity?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserByUsername(username: String): Result<User?> {
        return try {
            val userEntity = userDao.getUserByUsername(username)
            Result.success(userEntity?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): Flow<User?> {
        return userDao.getCurrentUser().map { userEntity ->
            userEntity?.toDomain()
        }
    }

    override suspend fun getCurrentUserSync(): User? {
        return try {
            userDao.getCurrentUserSync()?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { userEntities ->
            userEntities.map { it.toDomain() }
        }
    }

    override suspend fun setUserAsCurrentUser(userId: Int): Result<Unit> {
        return try {
            userDao.setUserAsCurrentUser(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCurrentUser(): Result<Unit> {
        return try {
            val rowsAffected = userDao.clearCurrentUser()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(userId: Int): Result<Unit> {
        return try {
            val rowsDeleted = userDao.deleteUserById(userId)
            if (rowsDeleted > 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found or not deleted"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllUsers(): Result<Unit> {
        return try {
            userDao.deleteAllUsers()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val existingUser = userDao.getUserById(user.id)
            if (existingUser != null) {
                val updatedUser = user.toEntity(
                    gender = existingUser.gender,
                    isCurrentUser = existingUser.isCurrentUser
                )
                val rowsUpdated = userDao.updateUser(updatedUser)
                if (rowsUpdated > 0) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("User not updated"))
                }
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
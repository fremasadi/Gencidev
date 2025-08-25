package com.example.gencidevtest.data.local.dao

import androidx.room.*
import com.example.gencidevtest.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUserSync(): UserEntity?

    @Query("SELECT * FROM users ORDER BY lastLoginTime DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity): Int

    @Delete
    suspend fun deleteUser(user: UserEntity): Int

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Int): Int

    @Query("UPDATE users SET isCurrentUser = 0")
    suspend fun clearCurrentUser(): Int

    @Query("UPDATE users SET isCurrentUser = 1, lastLoginTime = :loginTime WHERE id = :userId")
    suspend fun setCurrentUser(userId: Int, loginTime: Long = System.currentTimeMillis()): Int

    @Transaction
    suspend fun setUserAsCurrentUser(userId: Int) {
        clearCurrentUser()
        setCurrentUser(userId)
    }

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers(): Int
}
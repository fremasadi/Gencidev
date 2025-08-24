package com.example.gencidevtest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.gencidevtest.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val image: String,
    val gender: String? = null,
    val isCurrentUser: Boolean = false, // Flag untuk menandai user yang sedang login
    val lastLoginTime: Long = System.currentTimeMillis()
)

// Extension function untuk convert ke Domain Model
fun UserEntity.toDomain(): User {
    return User(
        id = id,
        username = username,
        email = email,
        firstName = firstName,
        lastName = lastName,
        image = image
    )
}

// Extension function untuk convert dari Domain Model
fun User.toEntity(gender: String? = null, isCurrentUser: Boolean = false): UserEntity {
    return UserEntity(
        id = id,
        username = username,
        email = email,
        firstName = firstName,
        lastName = lastName,
        image = image,
        gender = gender,
        isCurrentUser = isCurrentUser,
        lastLoginTime = System.currentTimeMillis()
    )
}
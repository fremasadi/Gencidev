// app/src/main/java/com/example/gencidevtest/data/local/entity/CategoryEntity.kt
package com.example.gencidevtest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.gencidevtest.domain.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val slug: String,
    val name: String,
    val url: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

// Extension functions for conversion
fun CategoryEntity.toDomain(): Category {
    return Category(
        slug = slug,
        name = name,
        url = url
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        slug = slug,
        name = name,
        url = url
    )
}
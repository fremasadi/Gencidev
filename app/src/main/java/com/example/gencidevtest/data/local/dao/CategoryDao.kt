package com.example.gencidevtest.data.local.dao

import androidx.room.*
import com.example.gencidevtest.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllCategoriesSync(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE slug = :slug")
    suspend fun getCategoryBySlug(slug: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>): List<Long>

    @Update
    suspend fun updateCategory(category: CategoryEntity): Int

    @Delete
    suspend fun deleteCategory(category: CategoryEntity): Int

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories(): Int

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Query("SELECT MAX(lastUpdated) FROM categories")
    suspend fun getLastUpdateTime(): Long?

    @Transaction
    suspend fun replaceAllCategories(categories: List<CategoryEntity>) {
        deleteAllCategories()
        insertCategories(categories)
    }
}
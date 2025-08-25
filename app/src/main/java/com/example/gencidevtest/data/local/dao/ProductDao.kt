package com.example.gencidevtest.data.local.dao

import androidx.room.*
import com.example.gencidevtest.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY id ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY id ASC")
    suspend fun getAllProductsSync(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Int): ProductEntity?

    @Query("SELECT * FROM products WHERE category = :category ORDER BY id ASC")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY id ASC")
    suspend fun getProductsByCategorySync(category: String): List<ProductEntity>

    @Query("SELECT * FROM products WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' ORDER BY id ASC")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' ORDER BY id ASC")
    suspend fun searchProductsSync(query: String): List<ProductEntity>

    @Query("SELECT DISTINCT category FROM products ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>): List<Long>

    @Update
    suspend fun updateProduct(product: ProductEntity): Int

    @Delete
    suspend fun deleteProduct(product: ProductEntity): Int

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: Int): Int

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts(): Int

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Query("SELECT MAX(lastUpdated) FROM products")
    suspend fun getLastUpdateTime(): Long?

    @Transaction
    suspend fun replaceAllProducts(products: List<ProductEntity>) {
        deleteAllProducts()
        insertProducts(products)
    }

    // Method to check if cache is stale (older than specified time in milliseconds)
    @Query("SELECT COUNT(*) FROM products WHERE lastUpdated < :staleTime")
    suspend fun countStaleProducts(staleTime: Long): Int
}
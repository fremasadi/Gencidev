package com.example.gencidevtest.data.local.dao

import androidx.room.*
import com.example.gencidevtest.data.local.entity.CartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM carts ORDER BY lastUpdated DESC")
    fun getAllCarts(): Flow<List<CartEntity>>

    @Query("SELECT * FROM carts ORDER BY lastUpdated DESC")
    suspend fun getAllCartsSync(): List<CartEntity>

    @Query("SELECT * FROM carts WHERE id = :cartId")
    suspend fun getCartById(cartId: Int): CartEntity?

    @Query("SELECT * FROM carts WHERE userId = :userId ORDER BY lastUpdated DESC")
    fun getCartsByUserId(userId: Int): Flow<List<CartEntity>>

    @Query("SELECT * FROM carts WHERE userId = :userId ORDER BY lastUpdated DESC")
    suspend fun getCartsByUserIdSync(userId: Int): List<CartEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCart(cart: CartEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarts(carts: List<CartEntity>): List<Long>

    @Update
    suspend fun updateCart(cart: CartEntity): Int

    @Delete
    suspend fun deleteCart(cart: CartEntity): Int

    @Query("DELETE FROM carts WHERE id = :cartId")
    suspend fun deleteCartById(cartId: Int): Int

    @Query("DELETE FROM carts WHERE userId = :userId")
    suspend fun deleteCartsByUserId(userId: Int): Int

    @Query("DELETE FROM carts")
    suspend fun deleteAllCarts(): Int

    @Query("SELECT COUNT(*) FROM carts")
    suspend fun getCartCount(): Int

    @Query("SELECT COUNT(*) FROM carts WHERE userId = :userId")
    suspend fun getCartCountByUserId(userId: Int): Int

    @Query("SELECT MAX(lastUpdated) FROM carts")
    suspend fun getLastUpdateTime(): Long?

    @Query("SELECT MAX(lastUpdated) FROM carts WHERE userId = :userId")
    suspend fun getLastUpdateTimeByUserId(userId: Int): Long?

    @Transaction
    suspend fun replaceAllCarts(carts: List<CartEntity>) {
        deleteAllCarts()
        insertCarts(carts)
    }

    @Transaction
    suspend fun replaceCartsByUserId(userId: Int, carts: List<CartEntity>) {
        deleteCartsByUserId(userId)
        insertCarts(carts)
    }

    // Method to check if cache is stale (older than specified time in milliseconds)
    @Query("SELECT COUNT(*) FROM carts WHERE lastUpdated < :staleTime")
    suspend fun countStaleCarts(staleTime: Long): Int

    @Query("SELECT COUNT(*) FROM carts WHERE userId = :userId AND lastUpdated < :staleTime")
    suspend fun countStaleCartsByUserId(userId: Int, staleTime: Long): Int

    // Method to get total items and value for quick access
    @Query("SELECT SUM(totalQuantity) FROM carts WHERE userId = :userId")
    suspend fun getTotalItemsByUserId(userId: Int): Int?

    @Query("SELECT SUM(discountedTotal) FROM carts WHERE userId = :userId")
    suspend fun getTotalValueByUserId(userId: Int): Double?
}
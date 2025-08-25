// app/src/main/java/com/example/gencidevtest/data/repository/CartRepositoryImpl.kt
package com.example.gencidevtest.data.repository

import android.util.Log
import com.example.gencidevtest.data.local.dao.CartDao
import com.example.gencidevtest.data.local.entity.toDomain
import com.example.gencidevtest.data.local.entity.toEntity
import com.example.gencidevtest.data.remote.api.CartApiService
import com.example.gencidevtest.data.remote.dto.AddCartProductRequest
import com.example.gencidevtest.data.remote.dto.AddCartRequest
import com.example.gencidevtest.data.remote.dto.CartDto
import com.example.gencidevtest.data.remote.dto.CartProductDto
import com.example.gencidevtest.data.util.NetworkUtil
import com.example.gencidevtest.domain.model.AddToCartRequest
import com.example.gencidevtest.domain.model.Cart
import com.example.gencidevtest.domain.model.CartProduct
import com.example.gencidevtest.domain.repository.CartRepository
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val apiService: CartApiService,
    private val cartDao: CartDao,
    private val networkUtil: NetworkUtil
) : CartRepository {

    companion object {
        private const val TAG = "CartRepositoryImpl"
        private const val CACHE_DURATION_MS = 3 * 60 * 1000L // 3 minutes for carts (shorter than products)
    }

    override suspend fun getCarts(limit: Int, skip: Int): Result<List<Cart>> {
        return try {
            Log.d(TAG, "Getting carts with limit: $limit, skip: $skip")

            // First, try to get cached data
            val cachedCarts = cartDao.getAllCartsSync()
            Log.d(TAG, "Found ${cachedCarts.size} cached carts")

            // Check if we should use cache or fetch from network
            val shouldFetchFromNetwork = networkUtil.isNetworkAvailable() &&
                    (cachedCarts.isEmpty() || isCacheStale())

            if (shouldFetchFromNetwork) {
                Log.d(TAG, "Fetching carts from network")

                try {
                    val response = apiService.getCarts(limit, skip)
                    val carts = response.carts.map { it.toDomain() }

                    // Cache the carts
                    Log.d(TAG, "Caching ${carts.size} carts to local database")
                    cartDao.replaceAllCarts(carts.map { it.toEntity() })

                    Log.d(TAG, "Successfully fetched and cached ${carts.size} carts")
                    Result.success(carts)
                } catch (e: Exception) {
                    Log.e(TAG, "Network request failed, using cached data if available", e)

                    if (cachedCarts.isNotEmpty()) {
                        Log.d(TAG, "Using ${cachedCarts.size} cached carts as fallback")
                        Result.success(cachedCarts.map { it.toDomain() })
                    } else {
                        Result.failure(e)
                    }
                }
            } else {
                Log.d(TAG, "Using cached carts (${cachedCarts.size} items)")
                Result.success(cachedCarts.map { it.toDomain() })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getCarts", e)
            Result.failure(e)
        }
    }

    override suspend fun addToCart(userId: Int, products: List<AddToCartRequest>): Result<Cart> {
        return try {
            // Use userId = 1 as default if userId is 0 or invalid
            val validUserId = if (userId <= 0) 1 else userId

            val request = AddCartRequest(
                userId = validUserId,
                products = products.map {
                    AddCartProductRequest(it.productId, it.quantity)
                }
            )

            Log.d(TAG, "Adding to cart with request: userId=$validUserId, products=${products.map { "id:${it.productId}, qty:${it.quantity}" }}")

            val response = apiService.addCart(request)
            val cart = response.toDomain()

            // Cache the new cart
            cartDao.insertCart(cart.toEntity())
            Log.d(TAG, "Successfully added to cart and cached. Cart ID: ${response.id}, Total: ${response.total}")

            Result.success(cart)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to cart", e)
            Log.e(TAG, "Error message: ${e.message}")

            // Enhanced error details
            if (e is retrofit2.HttpException) {
                Log.e(TAG, "HTTP Error Code: ${e.code()}")
                Log.e(TAG, "HTTP Error Body: ${e.response()?.errorBody()?.string()}")
            }

            Result.failure(e)
        }
    }



    // Cache management methods
    private suspend fun isCacheStale(): Boolean {
        val lastUpdateTime = cartDao.getLastUpdateTime() ?: return true
        val currentTime = System.currentTimeMillis()
        val isStale = (currentTime - lastUpdateTime) > CACHE_DURATION_MS
        Log.d(TAG, "Cart cache stale check: last update was ${currentTime - lastUpdateTime}ms ago, stale: $isStale")
        return isStale
    }

}

// Extension functions to convert DTO to Domain
private fun CartDto.toDomain(): Cart {
    return Cart(
        id = id,
        products = products.map { it.toDomain() },
        total = total,
        discountedTotal = discountedTotal,
        userId = userId,
        totalProducts = totalProducts,
        totalQuantity = totalQuantity
    )
}

private fun CartProductDto.toDomain(): CartProduct {
    return CartProduct(
        id = id,
        title = title,
        price = price,
        quantity = quantity,
        total = total,
        discountPercentage = discountPercentage,
        discountedTotal = getDiscountedTotal(), // Use helper function
        thumbnail = thumbnail
    )
}
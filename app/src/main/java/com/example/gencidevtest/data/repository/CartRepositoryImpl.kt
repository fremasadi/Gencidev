// Enhanced CartRepositoryImpl dengan debug logging
// app/src/main/java/com/example/gencidevtest/data/repository/CartRepositoryImpl.kt
package com.example.gencidevtest.data.repository

import android.util.Log
import com.example.gencidevtest.data.remote.api.CartApiService
import com.example.gencidevtest.data.remote.dto.AddCartProductRequest
import com.example.gencidevtest.data.remote.dto.AddCartRequest
import com.example.gencidevtest.data.remote.dto.CartDto
import com.example.gencidevtest.data.remote.dto.CartProductDto
import com.example.gencidevtest.domain.model.AddToCartRequest
import com.example.gencidevtest.domain.model.Cart
import com.example.gencidevtest.domain.model.CartProduct
import com.example.gencidevtest.domain.repository.CartRepository
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val apiService: CartApiService
) : CartRepository {

    companion object {
        private const val TAG = "CartRepositoryImpl"
    }

    override suspend fun getCarts(limit: Int, skip: Int): Result<List<Cart>> {
        return try {
            Log.d(TAG, "Getting carts with limit: $limit, skip: $skip")
            val response = apiService.getCarts(limit, skip)
            val carts = response.carts.map { it.toDomain() }
            Log.d(TAG, "Successfully got ${carts.size} carts")
            Result.success(carts)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting carts", e)
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

            Log.d(TAG, "Successfully added to cart. Cart ID: ${response.id}, Total: ${response.total}")

            Result.success(response.toDomain())
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
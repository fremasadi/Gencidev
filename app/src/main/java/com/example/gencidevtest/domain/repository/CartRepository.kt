package com.example.gencidevtest.domain.repository

import com.example.gencidevtest.domain.model.AddToCartRequest
import com.example.gencidevtest.domain.model.Cart

interface CartRepository {
    suspend fun getCarts(limit: Int = 30, skip: Int = 0): Result<List<Cart>>
    suspend fun addToCart(userId: Int, products: List<AddToCartRequest>): Result<Cart>
}
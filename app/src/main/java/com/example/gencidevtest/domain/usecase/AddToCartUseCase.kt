package com.example.gencidevtest.domain.usecase

import com.example.gencidevtest.domain.model.AddToCartRequest
import com.example.gencidevtest.domain.model.Cart
import com.example.gencidevtest.domain.repository.CartRepository
import javax.inject.Inject

class AddToCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(userId: Int, productId: Int, quantity: Int = 1): Result<Cart> {
        val products = listOf(AddToCartRequest(productId, quantity))
        return cartRepository.addToCart(userId, products)
    }
}
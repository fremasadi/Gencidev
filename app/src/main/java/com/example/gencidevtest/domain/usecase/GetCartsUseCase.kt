package com.example.gencidevtest.domain.usecase

import com.example.gencidevtest.domain.model.Cart
import com.example.gencidevtest.domain.repository.CartRepository
import javax.inject.Inject

class GetCartsUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(limit: Int = 30, skip: Int = 0): Result<List<Cart>> {
        return cartRepository.getCarts(limit, skip)
    }
}
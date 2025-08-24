package com.example.gencidevtest.domain.usecase

import com.example.gencidevtest.domain.model.Product
import com.example.gencidevtest.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductByIdUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(productId: Int): Result<Product> {
        return productRepository.getProductById(productId)
    }
}
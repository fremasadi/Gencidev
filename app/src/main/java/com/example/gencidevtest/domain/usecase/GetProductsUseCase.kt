package com.example.gencidevtest.domain.usecase

import com.example.gencidevtest.domain.model.Product
import com.example.gencidevtest.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(limit: Int = 30, skip: Int = 0): Result<List<Product>> {
        return productRepository.getProducts(limit, skip)
    }
}
package com.example.gencidevtest.domain.usecase

import com.example.gencidevtest.domain.model.Category
import com.example.gencidevtest.domain.repository.ProductRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(): Result<List<Category>> {
        return productRepository.getCategories()
    }
}
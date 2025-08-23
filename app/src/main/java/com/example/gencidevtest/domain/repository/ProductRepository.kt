package com.example.gencidevtest.domain.repository

import com.example.gencidevtest.domain.model.Category
import com.example.gencidevtest.domain.model.Product

interface ProductRepository {
    suspend fun getProducts(limit: Int = 30, skip: Int = 0): Result<List<Product>>
    suspend fun searchProducts(query: String, limit: Int = 30, skip: Int = 0): Result<List<Product>>
    suspend fun getCategories(): Result<List<Category>>
    suspend fun getProductsByCategory(category: String, limit: Int = 30, skip: Int = 0): Result<List<Product>>
}
package com.example.gencidevtest.data.repository

import com.example.gencidevtest.data.remote.api.ProductApiService
import com.example.gencidevtest.data.remote.dto.ProductDto
import com.example.gencidevtest.domain.model.Product
import com.example.gencidevtest.domain.repository.ProductRepository
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val apiService: ProductApiService
) : ProductRepository {

    override suspend fun getProducts(limit: Int, skip: Int): Result<List<Product>> {
        return try {
            val response = apiService.getProducts(limit, skip)
            val products = response.products.map { it.toDomain() }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchProducts(query: String, limit: Int, skip: Int): Result<List<Product>> {
        return try {
            val response = apiService.searchProducts(query, limit, skip)
            val products = response.products.map { it.toDomain() }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Extension function to convert DTO to Domain
private fun ProductDto.toDomain(): Product {
    return Product(
        id = id,
        title = title,
        description = description,
        category = category,
        price = price,
        discountPercentage = discountPercentage,
        rating = rating,
        stock = stock,
        brand = brand ?: "",
        thumbnail = thumbnail,
        images = images
    )
}
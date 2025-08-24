package com.example.gencidevtest.data.remote.api

import com.example.gencidevtest.data.remote.dto.CategoryDto
import com.example.gencidevtest.data.remote.dto.ProductDto
import com.example.gencidevtest.data.remote.dto.ProductsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApiService {
    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int = 30,
        @Query("skip") skip: Int = 0
    ): ProductsResponse

    @GET("products/{id}")
    suspend fun getProductById(
        @Path("id") productId: Int
    ): ProductDto

    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("limit") limit: Int = 30,
        @Query("skip") skip: Int = 0
    ): ProductsResponse

    @GET("products/categories")
    suspend fun getCategories(): List<CategoryDto>

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(
        @Path("category") category: String,
        @Query("limit") limit: Int = 30,
        @Query("skip") skip: Int = 0
    ): ProductsResponse
}
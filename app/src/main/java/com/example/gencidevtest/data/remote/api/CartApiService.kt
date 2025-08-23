package com.example.gencidevtest.data.remote.api

import com.example.gencidevtest.data.remote.dto.AddCartRequest
import com.example.gencidevtest.data.remote.dto.CartDto
import com.example.gencidevtest.data.remote.dto.CartsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CartApiService {
    @GET("carts")
    suspend fun getCarts(
        @Query("limit") limit: Int = 100,
        @Query("skip") skip: Int = 0
    ): CartsResponse

    @POST("carts/add")
    suspend fun addCart(@Body request: AddCartRequest): CartDto
}
// app/src/main/java/com/example/gencidevtest/data/remote/dto/CartResponse.kt
package com.example.gencidevtest.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CartsResponse(
    val carts: List<CartDto>,
    val total: Int,
    val skip: Int,
    val limit: Int
)

data class CartDto(
    val id: Int,
    val products: List<CartProductDto>,
    val total: Double,
    val discountedTotal: Double,
    val userId: Int,
    val totalProducts: Int,
    val totalQuantity: Int
)

data class CartProductDto(
    val id: Int,
    val title: String,
    val price: Double,
    val quantity: Int,
    val total: Double,
    val discountPercentage: Double,
    @SerializedName("discountedTotal")
    val discountedTotal: Double? = null,
    @SerializedName("discountedPrice")
    val discountedPrice: Double? = null,
    val thumbnail: String
) {
    // Helper to get the actual discounted total
    fun getDiscountedTotal(): Double = discountedTotal ?: discountedPrice ?: total
}

data class AddCartRequest(
    val userId: Int,
    val products: List<AddCartProductRequest>
)

data class AddCartProductRequest(
    val id: Int,  // API expects 'id', not 'productId'
    val quantity: Int
)
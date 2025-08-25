package com.example.gencidevtest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.gencidevtest.domain.model.Cart
import com.example.gencidevtest.domain.model.CartProduct
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "carts")
@TypeConverters(CartConverters::class)
data class CartEntity(
    @PrimaryKey
    val id: Int,
    val products: List<CartProductEntity>,
    val total: Double,
    val discountedTotal: Double,
    val userId: Int,
    val totalProducts: Int,
    val totalQuantity: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class CartProductEntity(
    val id: Int,
    val title: String,
    val price: Double,
    val quantity: Int,
    val total: Double,
    val discountPercentage: Double,
    val discountedTotal: Double,
    val thumbnail: String
)

// Type converters for List<CartProductEntity>
class CartConverters {
    @TypeConverter
    fun fromCartProductList(value: List<CartProductEntity>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toCartProductList(value: String): List<CartProductEntity> {
        val listType = object : TypeToken<List<CartProductEntity>>() {}.type
        return Gson().fromJson(value, listType)
    }
}

// Extension functions for conversion
fun CartEntity.toDomain(): Cart {
    return Cart(
        id = id,
        products = products.map { it.toDomain() },
        total = total,
        discountedTotal = discountedTotal,
        userId = userId,
        totalProducts = totalProducts,
        totalQuantity = totalQuantity
    )
}

fun Cart.toEntity(): CartEntity {
    return CartEntity(
        id = id,
        products = products.map { it.toEntity() },
        total = total,
        discountedTotal = discountedTotal,
        userId = userId,
        totalProducts = totalProducts,
        totalQuantity = totalQuantity
    )
}

fun CartProductEntity.toDomain(): CartProduct {
    return CartProduct(
        id = id,
        title = title,
        price = price,
        quantity = quantity,
        total = total,
        discountPercentage = discountPercentage,
        discountedTotal = discountedTotal,
        thumbnail = thumbnail
    )
}

fun CartProduct.toEntity(): CartProductEntity {
    return CartProductEntity(
        id = id,
        title = title,
        price = price,
        quantity = quantity,
        total = total,
        discountPercentage = discountPercentage,
        discountedTotal = discountedTotal,
        thumbnail = thumbnail
    )
}
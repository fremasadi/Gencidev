package com.example.gencidevtest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.gencidevtest.domain.model.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "products")
@TypeConverters(ProductConverters::class)
data class ProductEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val discountPercentage: Double,
    val rating: Double,
    val stock: Int,
    val brand: String,
    val thumbnail: String,
    val images: List<String>,
    val sku: String,
    val weight: Int,
    val tags: List<String>,
    val lastUpdated: Long = System.currentTimeMillis()
)

// Type converters for List<String>
class ProductConverters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}

// Extension function to convert ProductEntity to Domain Model
fun ProductEntity.toDomain(): Product {
    return Product(
        id = id,
        title = title,
        description = description,
        category = category,
        price = price,
        discountPercentage = discountPercentage,
        rating = rating,
        stock = stock,
        brand = brand,
        thumbnail = thumbnail,
        images = images,
        sku = sku,
        weight = weight,
        tags = tags
    )
}

// Extension function to convert Domain Model to ProductEntity
fun Product.toEntity(): ProductEntity {
    return ProductEntity(
        id = id,
        title = title,
        description = description,
        category = category,
        price = price,
        discountPercentage = discountPercentage,
        rating = rating,
        stock = stock,
        brand = brand,
        thumbnail = thumbnail,
        images = images,
        sku = sku,
        weight = weight,
        tags = tags
    )
}
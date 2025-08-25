package com.example.gencidevtest.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gencidevtest.data.local.dao.CartDao
import com.example.gencidevtest.data.local.dao.CategoryDao
import com.example.gencidevtest.data.local.dao.ProductDao
import com.example.gencidevtest.data.local.dao.UserDao
import com.example.gencidevtest.data.local.entity.CartEntity
import com.example.gencidevtest.data.local.entity.CategoryEntity
import com.example.gencidevtest.data.local.entity.ProductEntity
import com.example.gencidevtest.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        CategoryEntity::class,
        CartEntity::class

    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun cartDao(): CartDao

}
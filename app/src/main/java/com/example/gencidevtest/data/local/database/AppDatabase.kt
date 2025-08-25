// app/src/main/java/com/example/gencidevtest/data/local/database/AppDatabase.kt
package com.example.gencidevtest.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.gencidevtest.data.local.dao.CategoryDao
import com.example.gencidevtest.data.local.dao.ProductDao
import com.example.gencidevtest.data.local.dao.UserDao
import com.example.gencidevtest.data.local.entity.CategoryEntity
import com.example.gencidevtest.data.local.entity.ProductEntity
import com.example.gencidevtest.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        CategoryEntity::class
    ],
    version = 2, // Updated version due to new entities
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "genci_dev_test_database"
                )
                    .fallbackToDestructiveMigration() // For development only
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
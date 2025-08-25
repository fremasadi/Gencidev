// app/src/main/java/com/example/gencidevtest/di/DatabaseModule.kt
package com.example.gencidevtest.di

import android.content.Context
import androidx.room.Room
import com.example.gencidevtest.data.local.dao.CartDao
import com.example.gencidevtest.data.local.dao.CategoryDao
import com.example.gencidevtest.data.local.dao.ProductDao
import com.example.gencidevtest.data.local.dao.UserDao
import com.example.gencidevtest.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "genci_dev_test_database"
        )
            .fallbackToDestructiveMigration() // For development only
            .build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideCartDao(database: AppDatabase): CartDao {  // Added CartDao provider
        return database.cartDao()
    }
}
// app/src/main/java/com/example/gencidevtest/data/local/database/AppDatabase.kt
package com.example.gencidevtest.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.gencidevtest.data.local.dao.UserDao
import com.example.gencidevtest.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

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
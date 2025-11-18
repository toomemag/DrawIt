package com.example.drawit.data.local

import android.content.Context
import androidx.room.Room

object AppDatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
        buildDatabase(context.applicationContext).also { INSTANCE = it }
    }

    private fun buildDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "drawit_database"
            )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    fun close() {
        INSTANCE?.close()
        INSTANCE = null
    }
}
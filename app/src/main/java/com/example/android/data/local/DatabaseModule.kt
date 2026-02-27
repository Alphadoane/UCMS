package com.example.android.data.local

import android.content.Context
import androidx.room.Room

object DatabaseModule {
    @Volatile private var instance: AppDatabase? = null

    fun database(context: Context): AppDatabase = instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app.db"
        ).fallbackToDestructiveMigration().build().also { instance = it }
    }
}




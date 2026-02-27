package com.example.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProfileEntity::class,
        CourseEntity::class,
        GradeEntity::class,
        CourseWorkEntity::class
    ], 
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun academicsDao(): AcademicsDao
}




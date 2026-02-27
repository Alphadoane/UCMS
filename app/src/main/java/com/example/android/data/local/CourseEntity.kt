package com.example.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val course_code: String,
    val course_name: String,
    val credits: Int,
    val semester: String,
    val status: String
)

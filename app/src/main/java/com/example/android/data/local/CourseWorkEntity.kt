package com.example.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "course_work")
data class CourseWorkEntity(
    @PrimaryKey val id: String, // combined courseCode_title usually unique enough or UUID
    val course_code: String,
    val title: String,
    val description: String?,
    val due_date: String,
    val status: String,
    val max_marks: Double
)

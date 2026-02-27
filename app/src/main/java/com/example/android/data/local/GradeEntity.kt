package com.example.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grades")
data class GradeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val course_code: String,
    val course_name: String,
    val score: Double,
    val grade: String,
    val status: String, // Published or Pending
    val student_id: String // For multiple users support
)

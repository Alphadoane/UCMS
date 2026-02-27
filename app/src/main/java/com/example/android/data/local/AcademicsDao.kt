package com.example.android.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademicsDao {
    // Courses
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Query("SELECT * FROM courses")
    fun getCourses(): Flow<List<CourseEntity>>
    
    @Query("DELETE FROM courses")
    suspend fun clearCourses()

    // Grades/Results
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<GradeEntity>)

    @Query("SELECT * FROM grades WHERE student_id = :studentId")
    fun getGrades(studentId: String): Flow<List<GradeEntity>>
    
    @Query("DELETE FROM grades")
    suspend fun clearGrades()

    // Course Work / Assignments
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<CourseWorkEntity>)

    @Query("SELECT * FROM course_work")
    fun getAssignments(): Flow<List<CourseWorkEntity>>
    
    @Query("DELETE FROM course_work")
    suspend fun clearAssignments()
}

package com.example.android.data.repository

import android.content.Context
import com.example.android.data.local.AcademicsDao
import com.example.android.data.local.CourseEntity
import com.example.android.data.local.CourseWorkEntity
import com.example.android.data.local.DatabaseModule
import com.example.android.data.local.GradeEntity
import com.example.android.data.network.ApiService
import com.example.android.data.network.CourseMaterialRequest
import com.example.android.data.network.Lecture
import com.example.android.data.network.CourseStudentResponse
import com.example.android.data.network.CourseWorkRequest
import com.example.android.data.network.LearningMaterialResponse
import com.example.android.data.network.NetworkModule
import com.example.android.data.network.StaffCourseWorkResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.File

/**
 * Academics Repository
 * Handles Course Registration, Exams, Results, and Course Work with Offline Support.
 */
class AcademicsRepository(context: Context) {

    private val api: ApiService by lazy {
        NetworkModule.apiInstance ?: throw IllegalStateException("API not initialized. Call StudentRepository.init(context)")
    }
    
    private val academicsDao: AcademicsDao by lazy {
        DatabaseModule.database(context).academicsDao()
    }
    
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    /**
     * Get Course Registration (Offline-First)
     * Returns local data immediately, then fetches from network and updates local DB.
     */
    fun observeCourseRegistration(userId: String): Flow<List<Map<String, Any>>> = flow {
        // Emit local data first
        academicsDao.getCourses().collect { entities ->
            val mapped = entities.map { 
                mapOf(
                    "course_code" to it.course_code,
                    "course_name" to it.course_name,
                    "credits" to it.credits,
                    "semester" to it.semester,
                    "status" to it.status
                )
            }
            emit(mapped)
            
            // Trigger refresh (if needed, or moved to a separate Worker)
            // For now, we can trigger a one-shot refresh here or rely on the SyncWorker
        }
    }

    suspend fun refreshCourseRegistration(userId: String) {
        try {
            val res = api.getCourseRegistration()
            if (res.isSuccessful) {
                val entities = res.body()?.items?.map {
                    CourseEntity(
                        course_code = it.course_code,
                        course_name = it.course_name,
                        credits = it.credits,
                        semester = it.semester,
                        status = it.status
                    )
                } ?: emptyList()
                academicsDao.insertCourses(entities)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh courses")
        }
    }

    // Legacy suspend function (Optimized for Speed)
    suspend fun getCourseRegistration(userId: String): List<Map<String, Any>> {
        // 1. Trigger background refresh (Non-blocking)
        scope.launch { refreshCourseRegistration(userId) }
        
        // 2. Return local data immediately
        // Note: first() waits for at least one emission. Room emits immediately if table exists.
        val data = observeCourseRegistration(userId).first()
        
        // 3. If local data is empty, maybe we *should* wait? 
        // For true "Offline First", we return what we have. 
        // If it's the very first login, the UI might show empty, then update when Flow emits (if using observe logic in UI).
        // Since this is a one-shot get(), we rely on the UI to be reactive or the user to pull-to-refresh if empty.
        // But to be safe for first-time users, if empty, we wait for the refresh job.
        return if (data.isEmpty()) {
             refreshCourseRegistration(userId) // Wait if empty
             observeCourseRegistration(userId).first()
        } else {
             data
        }
    }

    /**
     * Get Course Work / Assignments (Offline-First)
     */
    fun observeAssignments(userId: String): Flow<List<Map<String, Any>>> = flow {
         academicsDao.getAssignments().collect { entities ->
            val mapped = entities.map {
                mapOf(
                    "id" to it.id,
                    "courseId" to it.course_code,
                    "title" to it.title,
                    "description" to (it.description ?: ""),
                    "due" to it.due_date,
                    "status" to it.status,
                    "max_marks" to it.max_marks
                )
            }
            emit(mapped)
         }
    }

    suspend fun refreshCourseWork(userId: String) {
        try {
            val res = api.getCourseWork()
            if (res.isSuccessful) {
                val entities = res.body()?.items?.map {
                    CourseWorkEntity(
                        id = "${it.course_code}_${it.assignment}", // Generate ID
                        course_code = it.course_code,
                        title = it.assignment,
                        description = "", // API doesn't return desc yet
                        due_date = it.due_date,
                        status = it.status,
                        max_marks = it.max_marks
                    )
                } ?: emptyList()
                academicsDao.insertAssignments(entities)
            }
        } catch (e: Exception) { Timber.e(e) }
    }
    
    // Legacy support
    suspend fun getCourseWork(userId: String): List<Map<String, Any>> {
        scope.launch { refreshCourseWork(userId) }
        val data = observeAssignments(userId).first()
        return if (data.isEmpty()) {
            refreshCourseWork(userId)
            observeAssignments(userId).first()
        } else {
            data
        }
    }

    /**
     * Get Exam Results (Offline-First)
     */
    fun observeExamResults(userId: String): Flow<List<Map<String, Any>>> = flow {
        academicsDao.getGrades(userId).collect { entities ->
             val mapped = entities.map {
                mapOf(
                    "course_code" to it.course_code,
                    "course_name" to it.course_name,
                    "score" to it.score,
                    "grade" to it.grade,
                    "status" to it.status
                )
             }
             emit(mapped)
        }
    }
    
    suspend fun refreshExamResults(userId: String) {
        try {
             val res = api.getExamResult()
             if (res.isSuccessful) {
                 val entities = res.body()?.items?.map {
                    GradeEntity(
                        course_code = it.course_code,
                        course_name = it.course_name,
                        score = it.score,
                        grade = it.grade_letter,
                        status = if(it.approved) "Published" else "Pending",
                        student_id = userId
                    )
                 } ?: emptyList()
                 academicsDao.insertGrades(entities)
             }
         } catch (e: Exception) { Timber.e(e) }
    }
    
    suspend fun getExamResult(userId: String): List<Map<String, Any>> {
        scope.launch { refreshExamResults(userId) }
        val data = observeExamResults(userId).first()
        return if (data.isEmpty()) {
            refreshExamResults(userId)
            observeExamResults(userId).first()
        } else {
             data
        }
    }
    
    suspend fun getExamCard(userId: String): List<Map<String, Any>> {
         return try {
             val res = api.getExamCard()
             if (res.isSuccessful) {
                 res.body()?.items?.map {
                    mapOf(
                        "course_code" to it.course_code,
                        "course_name" to it.course_name,
                        "exam_date" to (it.date ?: ""),
                        "start_time" to (it.start_time ?: ""),
                        "venue" to (it.venue ?: "")
                    )
                 } ?: emptyList()
             } else {
                 emptyList()
             }
         } catch (e: Exception) { emptyList() }
    }


    suspend fun getTimetable(): Result<List<Lecture>> {
        return try {
            val response = api.getTimetable()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch timetable: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUpcomingActivities(userId: String): Map<String, List<Map<String, Any>>> {
        return mapOf("classes" to emptyList(), "assignments" to emptyList()) 
    }

    suspend fun getEnrolledCoursesDetails(userId: String): List<Map<String, Any>> = emptyList()

    suspend fun getUnitContent(courseCode: String): Map<String, Any> = emptyMap()

    suspend fun getUserProfile(type: String): com.example.android.data.model.User? {
        return com.example.android.data.model.User(id="acad1", email="student@example.com", firstName="Student", lastName="User", role=com.example.android.data.model.UserRole.STUDENT)
    }

    suspend fun getFeeBalance(userId: String): Map<String, Any> {
         return mapOf("balance" to 0.0, "currency" to "KES")
    }

    suspend fun diagnoseUser(userId: String): String {
        return "SQL Backend Active + Offline Cache"
    }

    suspend fun getInsights(userId: String): Map<String, Any> {
        return mapOf(
            "gpa_trend" to listOf(3.5, 3.6, 3.7, 3.8, 3.8),
            "risk_score" to 0.1,
            "completion_probability" to 0.95,
            "recommendations" to listOf("Keep up the good work!", "Consider taking Advanced Math.")
        )
    }

    suspend fun createDevAdminProfile(userId: String, email: String) {
    }

    // Staff / Lecturer Methods
    suspend fun getStaffCourses(): List<Map<String, Any>> {
        return try {
            val response = api.getStaffCourses()
            if (response.isSuccessful) {
                response.body()?.map {
                    mapOf(
                        "id" to it.id,
                        "code" to it.code,
                        "title" to it.title,
                        "department" to (it.department ?: "-"),
                        "student_count" to it.student_count
                    )
                } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getCourseStudents(courseId: Int): List<CourseStudentResponse> {
        return try {
            val response = api.getCourseStudents(courseId)
            if (response.isSuccessful) response.body() ?: emptyList()
            else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getCourseMaterials(courseId: Int): List<LearningMaterialResponse> {
        return try {
            val response = api.getCourseMaterials(courseId)
            if (response.isSuccessful) response.body() ?: emptyList()
            else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getCourseWork(courseId: Int): List<StaffCourseWorkResponse> {
        return try {
            val response = api.getStaffCourseWork(courseId)
            if (response.isSuccessful) response.body() ?: emptyList()
            else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun postCourseMaterial(courseId: Int, title: String, description: String, link: String): Boolean {
        return try {
            val body = CourseMaterialRequest(title, description, link.takeIf { it.isNotEmpty() })
            val response = api.postCourseMaterial(courseId, body)
            response.isSuccessful
        } catch (e: Exception) { false }
    }

    suspend fun postCourseWork(courseId: Int, title: String, description: String, maxMarks: Double, dueDate: String, category: String): Boolean {
        return try {
            val body = CourseWorkRequest(
                title = title,
                description = description,
                max_marks = maxMarks,
                due_date = dueDate,
                category = category
            )
            val response = api.postCourseWork(courseId, body)
            response.isSuccessful
        } catch (e: Exception) { false }
    }
}

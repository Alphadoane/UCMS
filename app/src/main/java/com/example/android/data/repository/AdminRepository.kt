package com.example.android.data.repository

import android.content.Context
import com.example.android.data.network.ApiService
import com.example.android.data.network.NetworkModule

/**
 * Admin Repository
 * Handles Admin tasks and Staff tasks.
 */
class AdminRepository(context: Context? = null) {

    private val api: ApiService by lazy {
        NetworkModule.apiInstance ?: throw IllegalStateException("API not initialized. Call StudentRepository.init(context)")
    }

    suspend fun getSystemStats(): Result<ApiService.AdminStatsResponse> {
        return try {
            val response = api.getAdminStats()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch stats"))
            }
        } catch (e: Exception) {
             Result.failure(e)
        }
    }

    suspend fun createSemester(name: String, startDate: String, endDate: String): Result<String> = Result.success("stub_id")
    suspend fun getSemesters(): List<Map<String, Any>> = emptyList()
    suspend fun endSemester(semesterId: String): Result<Unit> = Result.success(Unit)
    suspend fun createUnit(code: String, title: String, department: String): Result<String> = Result.success(code)
    suspend fun getUnits(): List<Map<String, Any>> = emptyList()
    suspend fun getOptions(): Result<ApiService.AllocationOptionsResponse> {
        return try {
            val response = api.getAllocationOptions()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch options"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun allocateLecture(req: ApiService.AllocateLectureRequest): Result<Unit> {
        return try {
            val response = api.allocateLecture(req)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Allocation failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun assignLecturerToUnit(unitCode: String, lecturerId: String, lecturerName: String): Result<Unit> = Result.success(Unit)
    suspend fun getAllTransactions(limit: Long = 50): List<Map<String, Any>> = emptyList()
    suspend fun verifyTransaction(transactionId: String, adminId: String): Result<Unit> = Result.success(Unit)
    suspend fun logAudit(userId: String, action: String, details: String) {}
    suspend fun getAuditLogs(limit: Long = 100): List<Map<String, Any>> = emptyList()
    
    // Staff methods included here for now
    suspend fun getStaffUnits(staffId: String): List<Map<String, Any>> = emptyList()
    suspend fun getEnrolledStudents(unitCode: String): List<Map<String, Any>> = emptyList()
    suspend fun updateStudentGrade(studentId: String, unitCode: String, grades: Map<String, Any>): Result<Unit> = Result.success(Unit)
    suspend fun uploadCourseMaterial(courseId: String, title: String, type: String, uri: String, lecturerId: String): Result<Unit> = Result.success(Unit)
    suspend fun getCourseMaterials(courseId: String): List<Map<String, Any>> = emptyList()

    suspend fun searchUser(query: String): com.example.android.data.model.User? {
        // Mock implementation
        return null
    }

    suspend fun createUser(fullName: String, email: String?, role: com.example.android.data.model.UserRole, regNumber: String?, department: String? = null): Result<Unit> {
        return try {
            val response = api.createUser(com.example.android.data.model.CreateUserRequest(fullName, email, role.name, regNumber, department))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to create user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun resetUserPassword(userId: String): Result<String> {
        return try {
            val response = api.resetUserPassword(userId)
            if (response.isSuccessful) {
                val body = response.body()
                val msg = body?.get("detail") ?: "Password reset successfully"
                Result.success(msg)
            } else {
                Result.failure(Exception("Failed to reset: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(type: String): com.example.android.data.model.User? {
        // Mock implementation
        return com.example.android.data.model.User(id="staff1", email="staff@example.com", firstName="Staff", lastName="Member", role=com.example.android.data.model.UserRole.STAFF)
    }

    suspend fun sendBroadcast(title: String, message: String, targetAudience: String): Result<Unit> {
        return try {
            val response = api.sendBroadcast(com.example.android.data.network.BroadcastRequest(title, message, targetAudience))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to send broadcast: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

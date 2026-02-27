package com.school.studentportal.shared.data.repository

import com.school.studentportal.shared.data.model.*
import com.school.studentportal.shared.data.network.SharedApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminRepository(private val api: SharedApiService) {

    private val _users = MutableStateFlow<List<UserDto>>(emptyList())
    val users: StateFlow<List<UserDto>> = _users.asStateFlow()

    private val _stats = MutableStateFlow<AdminStatsResponse?>(null)
    val stats: StateFlow<AdminStatsResponse?> = _stats.asStateFlow()

    private val _elections = MutableStateFlow<List<Election>>(emptyList())
    val elections: StateFlow<List<Election>> = _elections.asStateFlow()

    private val _transactions = MutableStateFlow<List<FinanceTransaction>>(emptyList())
    val transactions: StateFlow<List<FinanceTransaction>> = _transactions.asStateFlow()

    private val _applications = MutableStateFlow<List<AdmissionApplication>>(emptyList())
    val applications: StateFlow<List<AdmissionApplication>> = _applications.asStateFlow()

    suspend fun refreshApplications(phase: String? = null): Result<List<AdmissionApplication>> {
        val result = api.getAdminApplications(phase)
        if (result.isSuccess) {
            _applications.value = result.getOrNull() ?: emptyList()
        }
        return result
    }

    suspend fun verifyDocument(docId: Int, approve: Boolean, reason: String = ""): Result<AdmissionDocument> {
        val action = if (approve) "approve" else "reject"
        return api.verifyDocument(docId, action, reason)
    }

    suspend fun updateApplicationPhase(appId: String, phase: String, reason: String? = null): Result<AdmissionApplication> {
        return api.updateApplicationPhase(appId, phase, reason)
    }

    suspend fun enrollStudent(appId: String): Result<Map<String, String>> {
        return api.enrollStudent(appId)
    }

    suspend fun refreshTransactions(): Result<List<FinanceTransaction>> {
        val result = api.getAdminTransactions()
        if (result.isSuccess) {
            val response = result.getOrNull()
            _transactions.value = response?.transactions ?: emptyList()
            return Result.success(response?.transactions ?: emptyList())
        }
        return Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
    }

    suspend fun refreshUsers(): Result<List<UserDto>> {
        val result = api.getUsers()
        if (result.isSuccess) {
            _users.value = result.getOrNull() ?: emptyList()
        }
        return result
    }

    suspend fun refreshStats(): Result<AdminStatsResponse> {
        val result = api.getAdminStats()
        if (result.isSuccess) {
            _stats.value = result.getOrNull()
        }
        return result
    }

    suspend fun refreshElections(): Result<List<Election>> {
        val result = api.getAdminElections()
        if (result.isSuccess) {
            _elections.value = result.getOrNull() ?: emptyList()
        }
        return result
    }

    suspend fun createElection(title: String, description: String, endDate: String): Result<Election> {
        return api.createElection(ElectionRequest(title, description, endDate))
    }

    suspend fun addCandidate(electionId: Int, name: String, slogan: String): Result<Candidate> {
        return api.addCandidate(electionId, CandidateRequest(name, slogan))
    }

    // Curriculum & Units (Mocked for parity with Android app)
    suspend fun getUnits(): List<Map<String, Any>> = listOf(
        mapOf("code" to "CS101", "title" to "Intro to CS", "department" to "Computer Science"),
        mapOf("code" to "MATH101", "title" to "Calculus I", "department" to "Mathematics")
    )

    suspend fun createUnit(code: String, title: String, department: String): Result<String> = Result.success(code)
    
    suspend fun assignLecturerToUnit(unitCode: String, lecturerId: String, lecturerName: String): Result<Unit> = Result.success(Unit)

    // Semesters (Mocked)
    suspend fun getSemesters(): List<Map<String, Any>> = listOf(
        mapOf("id" to "1", "name" to "Jan-Apr 2024", "startDate" to "2024-01-08", "endDate" to "2024-04-26", "isActive" to true),
        mapOf("id" to "2", "name" to "Sep-Dec 2023", "startDate" to "2023-09-04", "endDate" to "2023-12-15", "isActive" to false)
    )

    suspend fun createSemester(name: String, startDate: String, endDate: String): Result<String> = Result.success("stub_id")
    
    suspend fun endSemester(id: String): Result<Unit> = Result.success(Unit)

    suspend fun downloadFile(url: String): Result<ByteArray> {
        return api.downloadFile(url)
    }

    suspend fun getAllocationOptions(): Result<AllocationOptionsResponse> {
        return api.getAdminAllocationOptions()
    }

    suspend fun allocateLecture(
        courseId: Int,
        employeeId: String,
        day: String,
        startTime: String,
        endTime: String,
        venue: String,
        studentIds: List<String>
    ): Result<TimetableEntry> {
        val request = AllocateLectureRequest(
            course_id = courseId,
            employee_id = employeeId,
            day = day,
            start_time = startTime,
            end_time = endTime,
            venue = venue,
            student_ids = studentIds
        )
        return api.allocateLecture(request)
    }

    suspend fun publishResults(semesterId: Int, courseId: Int? = null): Result<ApiResponse> {
        return api.publishResults(PublishResultsRequest(semesterId, courseId))
    }
}

package com.school.studentportal.shared.data.repository

import com.school.studentportal.shared.data.model.*
import com.school.studentportal.shared.data.network.SharedApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AcademicsRepository(private val api: SharedApiService) {
    
    private val _availableCourses = MutableStateFlow<List<AcademicCourse>>(emptyList())
    val availableCourses: StateFlow<List<AcademicCourse>> = _availableCourses.asStateFlow()

    private val _enrolledCourses = MutableStateFlow<List<AcademicCourse>>(emptyList())
    val enrolledCourses: StateFlow<List<AcademicCourse>> = _enrolledCourses.asStateFlow()

    private val _examResults = MutableStateFlow<List<ExamResult>>(emptyList())
    val examResults: StateFlow<List<ExamResult>> = _examResults.asStateFlow()

    private val _timetable = MutableStateFlow<List<TimetableEntry>>(emptyList())
    val timetable: StateFlow<List<TimetableEntry>> = _timetable.asStateFlow()

    suspend fun refreshAvailableCourses(): Result<List<AcademicCourse>> {
        val result = api.getAvailableCourses()
        if (result.isSuccess) {
            _availableCourses.value = result.getOrNull() ?: emptyList()
        }
        return result
    }

    suspend fun refreshEnrolledCourses(): Result<List<AcademicCourse>> {
        val result = api.getEnrolledCourses()
        if (result.isSuccess) {
            _enrolledCourses.value = result.getOrNull() ?: emptyList()
        }
        return result
    }

    suspend fun registerCourses(courseIds: List<Int>): Result<EnrollmentResponse> {
        return api.enrollCourses(courseIds)
    }

    suspend fun refreshExamResults(): Result<List<ExamResult>> {
        val result = api.getExamResults()
        if (result.isSuccess) {
            _examResults.value = result.getOrNull() ?: emptyList()
        }
        return result
    }

    suspend fun refreshTimetable(): Result<List<TimetableEntry>> {
        val result = api.getTimetable()
        if (result.isSuccess) {
            _timetable.value = result.getOrNull() ?: emptyList()
        }
        return result
    }

    suspend fun getResultSlip(semesterId: Int? = null): Result<ResultSlip> {
        return api.getResultSlip(semesterId)
    }

    suspend fun downloadResultSlipPdf(semesterId: Int? = null): Result<ByteArray> {
        return api.downloadResultSlipPdf(semesterId)
    }
}

package com.school.studentportal.shared.data.repository

import com.school.studentportal.shared.data.model.CourseWork
import com.school.studentportal.shared.data.model.StaffCourse
import com.school.studentportal.shared.data.model.StudentGrade
import com.school.studentportal.shared.data.model.SubmitGradesRequest
import com.school.studentportal.shared.data.model.StudentGradeUpdate
import com.school.studentportal.shared.data.model.ApiResponse
import com.school.studentportal.shared.data.network.SharedApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StaffRepository(private val api: SharedApiService) {

    private val _courseWork = MutableStateFlow<List<CourseWork>>(emptyList())
    val courseWork: StateFlow<List<CourseWork>> = _courseWork.asStateFlow()

    private val _staffCourses = MutableStateFlow<List<StaffCourse>>(emptyList())
    val staffCourses: StateFlow<List<StaffCourse>> = _staffCourses.asStateFlow()

    suspend fun getStaffCourses(): Result<List<StaffCourse>> {
        val result = api.getStaffCourses()
        if (result.isSuccess) {
            _staffCourses.value = result.getOrNull() ?: emptyList()
        }
        return result
    }
    
    suspend fun refreshStaffCourses() {
        getStaffCourses()
    }

    suspend fun getCourseStudents(courseId: Int): Result<List<StudentGrade>> {
        return api.getCourseStudents(courseId)
    }

    suspend fun submitGrades(courseId: Int, updates: List<StudentGradeUpdate>): Result<ApiResponse> {
        val request = SubmitGradesRequest(courseId, updates)
        return api.submitGrades(request)
    }

    suspend fun getLecturerCourseWork(): Result<List<CourseWork>> {
        val result = api.getLecturerCourseWork()
        if (result.isSuccess) {
            _courseWork.value = result.getOrNull() ?: emptyList()
        }
        return result
    }

    suspend fun createCourseWork(courseWork: CourseWork): Result<CourseWork> {
        return api.createCourseWork(courseWork)
    }

    suspend fun uploadContent(request: com.school.studentportal.shared.data.model.ContentUploadRequest): Result<ApiResponse> {
        return api.uploadContent(request)
    }
}

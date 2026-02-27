package com.school.studentportal.shared.data.repository

import com.school.studentportal.shared.data.model.*
import com.school.studentportal.shared.data.network.SharedApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AnalyticsRepository(private val api: SharedApiService) {

    private val _adminReport = MutableStateFlow<AdminReportSummary?>(null)
    val adminReport = _adminReport.asStateFlow()

    private val _studentAnalytics = MutableStateFlow<StudentAnalytics?>(null)
    val studentAnalytics = _studentAnalytics.asStateFlow()

    suspend fun refreshAdminReports(): Result<AdminReportSummary> {
        val result = api.getAdminGlobalReports()
        if (result.isSuccess) {
            _adminReport.value = result.getOrNull()
        }
        return result
    }

    suspend fun refreshStudentAnalytics(): Result<StudentAnalytics> {
        val result = api.getStudentPerformanceAnalytics()
        if (result.isSuccess) {
            _studentAnalytics.value = result.getOrNull()
        }
        return result
    }
}

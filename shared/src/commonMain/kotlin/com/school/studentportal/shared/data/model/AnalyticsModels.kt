package com.school.studentportal.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AdminReportSummary(
    val totalUsers: Int,
    val totalComplaints: Int,
    val resolvedComplaints: Int,
    val totalRevenue: Double,
    val votingTurnout: Double,
    val activeStudentsWeekly: Int,
    val moduleEngagement: Map<String, Int>, // e.g., "Virtual Campus" -> 1500
    val collectionRate: Double, // Percentage 0.0 - 100.0
    val averageResponseTimeHours: Double // e.g. 48.5
)

@Serializable
data class StudentAnalytics(
    val studentId: String,
    val gpaTrend: List<GpaPoint>,
    val financialStatus: FinancialSummary,
    val attendanceRate: Double,
    val assignmentCompletion: Double
)

@Serializable
data class GpaPoint(
    val semester: String,
    val gpa: Double
)

@Serializable
data class FinancialSummary(
    val totalBilled: Double,
    val totalPaid: Double,
    val balance: Double,
    val paymentConsistencyScore: Int // 0-100
)

package com.example.android.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AdminStats(
    @SerialName("total_users") val totalUsers: Int,
    @SerialName("students") val students: Int,
    @SerialName("staff") val staff: Int,
    @SerialName("active_elections") val activeElections: Int,
    @SerialName("pending_tickets") val pendingTickets: Int,
    @SerialName("pending_admissions") val pendingAdmissions: Int = 0,
    @SerialName("db_status") val dbStatus: String
)

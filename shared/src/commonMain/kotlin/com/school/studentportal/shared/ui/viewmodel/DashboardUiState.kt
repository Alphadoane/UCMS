package com.school.studentportal.shared.ui.viewmodel

import com.school.studentportal.shared.data.model.User

data class DashboardUiState(
    val user: User? = null,
    val feeData: Map<String, Any>? = null,
    val courses: List<Map<String, Any>> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

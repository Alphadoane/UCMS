package com.example.android.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.android.data.model.User
import com.example.android.data.model.UserRole
import com.example.android.ui.viewmodel.HomeViewModel
import com.school.studentportal.shared.ui.screens.HomeScreen
import com.school.studentportal.shared.ui.viewmodel.DashboardUiState

@Composable
fun HomeScreen(
    passedUserId: String? = null,
    initialUser: User? = null,
    onLogout: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    // Initialize ViewModel once
    androidx.compose.runtime.LaunchedEffect(initialUser) {
        if (initialUser != null) {
            viewModel.init(initialUser)
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    // Map App User to Shared User
    val sharedUser = uiState.user?.toShared()

    val sharedUiState = DashboardUiState(
        user = sharedUser,
        feeData = uiState.feeData,
        courses = uiState.courses,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage
    )

    com.school.studentportal.shared.ui.screens.HomeScreen(
        uiState = sharedUiState,
        onLogout = onLogout,
        onNavigate = onNavigate,
        onOpenDrawer = onOpenDrawer
    )
}

fun User.toShared(): com.school.studentportal.shared.data.model.User {
    return com.school.studentportal.shared.data.model.User(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        role = com.school.studentportal.shared.data.model.UserRole.valueOf(role.name),
        regNumber = regNumber,
        course = course,
        department = department
    )
}

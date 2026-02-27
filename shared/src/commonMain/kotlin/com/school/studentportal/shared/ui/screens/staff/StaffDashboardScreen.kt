package com.school.studentportal.shared.ui.screens.staff

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.data.model.User
import com.school.studentportal.shared.ui.components.DashboardCard
import com.school.studentportal.shared.ui.components.InfoRow
import com.school.studentportal.shared.ui.components.MenuActionItem
import androidx.compose.runtime.getValue

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.school.studentportal.shared.data.repository.StaffRepository
import com.school.studentportal.shared.ui.Routes

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun StaffDashboardScreen(
    user: User, 
    repository: StaffRepository,
    onNavigate: (String) -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val assignedCourses by repository.staffCourses.collectAsState()

    LaunchedEffect(Unit) {
        repository.refreshStaffCourses()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Staff Portal", style = MaterialTheme.typography.headlineSmall)

        // Staff Profile Header
        DashboardCard(title = "Profile", icon = Icons.Default.Person, color = Color(0xFF1976D2)) {
            InfoRow(label = "Name", value = user.fullName)
            InfoRow(label = "Department", value = user.department ?: "N/A")
            InfoRow(label = "Employee ID", value = user.regNumber ?: "N/A")
        }

        // Teaching Tools
        DashboardCard(title = "Teaching Tools (${assignedCourses.size} Units)", icon = Icons.Default.Class, color = Color(0xFF1565C0)) {
            MenuActionItem(icon = Icons.Default.Assignment, label = "My Courses & Grading") {
                onNavigate("staff_courses")
            }
            MenuActionItem(icon = Icons.Default.Schedule, label = "My Timetable") {
                onNavigate("student_timetable")
            }
        }

        // Student Advisory
        DashboardCard(title = "Student Advisory", icon = Icons.Default.Group, color = Color(0xFF0097A7)) {
            MenuActionItem(icon = Icons.Default.Search, label = "Lookup Student") {
                onNavigate("staff_student_lookup")
            }
            MenuActionItem(icon = Icons.Default.Chat, label = "Mentorship Chat") {
                onNavigate(Routes.VC_ZOOM_ROOMS)
            }
        }
    }
}

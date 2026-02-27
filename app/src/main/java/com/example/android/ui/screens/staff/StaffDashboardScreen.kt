package com.example.android.ui.screens.staff

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.android.data.model.User
import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.DashboardCard
import com.example.android.ui.components.InfoRow
import com.example.android.ui.components.MenuActionItem

@Composable
fun StaffDashboardScreen(user: User, onNavigate: (String) -> Unit, onOpenDrawer: () -> Unit = {}) {
    PortalScaffold(
        title = "Staff Portal",
        navigationIcon = {
            com.example.android.ui.components.IntegratedMenuToggle(onOpenDrawer = onOpenDrawer)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Staff Profile Header (Simplified)
            DashboardCard(title = "Profile", icon = Icons.Default.Person, color = Color(0xFF1976D2)) {
                InfoRow(label = "Name", value = user.fullName)
                InfoRow(label = "Department", value = user.department ?: "N/A")
                InfoRow(label = "Employee ID", value = user.regNumber ?: "N/A")
            }

            // Teaching Tools
            DashboardCard(title = "Teaching Tools", icon = Icons.Default.Class, color = Color(0xFF1565C0)) {
                MenuActionItem(icon = Icons.Default.Assignment, label = "My Courses & Grading") {
                     onNavigate("staff_courses")
                }
                MenuActionItem(icon = Icons.Default.Schedule, label = "My Timetable") {
                     onNavigate("student_timetable") // Reuse existing timetable for now
                }
            }

            // Student Advisory
            DashboardCard(title = "Student Advisory", icon = Icons.Default.Group, color = Color(0xFF0097A7)) {
                MenuActionItem(icon = Icons.Default.Search, label = "Lookup Student") {
                    onNavigate("staff_student_lookup")
                }
                MenuActionItem(icon = Icons.Default.Chat, label = "Mentorship Chat") {
                     // In real app, open chat. linked to nothing for now or office hours
                     onNavigate("vc_zoom_rooms") // Virtual Office
                }
            }
        }
    }
}

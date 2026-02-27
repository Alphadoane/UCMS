package com.school.studentportal.shared.ui.screens

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

@Composable
fun SupportDashboardScreen(
    user: User, 
    onNavigate: (String) -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
            //.padding(16.dp), // Scaffold handles padding
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Agent Status
        DashboardCard(title = "Agent Status: Online", icon = Icons.Default.HeadsetMic, color = Color(0xFF2E7D32)) {
            Text("Assigned Tickets: 3", style = MaterialTheme.typography.bodyMedium)
            Text("Performance: 98%", style = MaterialTheme.typography.bodyMedium)
        }

        // Ticket Queue
        DashboardCard(title = "Ticket Management", icon = Icons.Default.ConfirmationNumber, color = Color(0xFFEF6C00)) {
            MenuActionItem(icon = Icons.Default.List, label = "View Ticket Queue") {
                onNavigate("support_tickets")
            }
            MenuActionItem(icon = Icons.Default.Search, label = "User Diagnostics") {
                onNavigate("support_user_lookup")
            }
        }

        // System Health
        DashboardCard(title = "System Health", icon = Icons.Default.Dns, color = Color(0xFF455A64)) {
             InfoRow(label = "Firestore Status", value = "Operational")
             InfoRow(label = "Auth Service", value = "Operational")
             InfoRow(label = "Storage", value = "Operational")
             InfoRow(label = "App Version", value = "1.2.0-prod")
        }
    }
}

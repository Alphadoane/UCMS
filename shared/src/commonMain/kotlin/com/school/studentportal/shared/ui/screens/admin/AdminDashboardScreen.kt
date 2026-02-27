package com.school.studentportal.shared.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.school.studentportal.shared.data.model.User
import com.school.studentportal.shared.data.repository.AdminRepository
import com.school.studentportal.shared.ui.Routes

@Composable
fun AdminDashboardScreen(
    user: User, 
    repository: AdminRepository,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {},
    onOpenDrawer: () -> Unit = {}
) {
    val stats by repository.stats.collectAsState()

    LaunchedEffect(Unit) {
        repository.refreshStats()
    }

    val services = listOf(
        AdminService("My Profile", Icons.Default.Person, Color(0xFF3F51B5), "View personal details", Routes.PROFILE),
        AdminService("Admissions", Icons.Default.PersonAdd, Color(0xFF673AB7), "Review & process new applicants", "admin_admissions"),
        AdminService("Programmes", Icons.Default.School, Color(0xFF00695C), "Manage academic programs", "admin_programs"),
        AdminService("Units", Icons.Default.Book, Color(0xFF009688), "Manage course units", "admin_units"),
        AdminService("Semesters", Icons.Default.DateRange, Color(0xFFEF6C00), "Manage semesters", "admin_semesters"),
        AdminService("Allocations", Icons.Default.Schedule, Color(0xFF004D40), "Assign lecturers to courses", "admin_allocation"),
        AdminService("Library", Icons.Default.Book, Color(0xFF8D6E63), "Manage library books", "admin_library"),
        AdminService("Finance", Icons.Default.AttachMoney, Color(0xFF2E7D32), "Verify payments & audits", "admin_finance"),
        AdminService("Virtual Campus", Icons.Default.VideoCall, Color(0xFF1565C0), "Manage Zoom classes", "admin_zoom"),
        AdminService("Users & Roles", Icons.Default.ManageAccounts, Color(0xFF455A64), "Create users, manage access", Routes.ADMIN_USER_MGMT),
        AdminService("Elections", Icons.Default.HowToVote, Color(0xFFE65100), "Manage voting events", Routes.ADMIN_ELECTION),
        AdminService("Complaints", Icons.Default.Report, Color(0xFFD32F2F), "Review academic issues", Routes.COMPLAINTS),
        AdminService("Campus Life", Icons.Default.Newspaper, Color(0xFFE91E63), "Post images & news", Routes.ADMIN_CAMPUS_LIFE),
        AdminService("Health Admin", Icons.Default.HealthAndSafety, Color(0xFFC62828), "Manage appointments & alerts", Routes.ADMIN_HEALTH),
        AdminService("Result Mgmt", Icons.Default.Grade, Color(0xFFC2185B), "Publish exam results", Routes.ADMIN_RESULT_MGMT),
        AdminService("Global Reports", Icons.Default.Insights, Color(0xFF673AB7), "System intelligence & analytics", Routes.ADMIN_REPORTS)
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 1200.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            // Header: System Status & Welcome
            AdminHeaderSection(user, stats, onLogout)
    
            Spacer(modifier = Modifier.height(24.dp))
    
            // Grid of Services (Custom implementation to avoid nested scroll issues)
            VerticalGrid(
                items = services,
                columns = 2,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) { service ->
                ServiceCard(
                    title = service.title,
                    icon = service.icon,
                    color = service.color,
                    description = service.description,
                    onClick = { onNavigate(service.route) }
                )
            }
        }
    }
}

data class AdminService(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val description: String,
    val route: String
)

@Composable
fun <T> VerticalGrid(
    items: List<T>,
    columns: Int,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val rows = items.chunked(columns)
        for (rowItems in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (item in rowItems) {
                    Box(modifier = Modifier.weight(1f)) {
                        content(item)
                    }
                }
                // Fill empty space if last row is incomplete
                if (rowItems.size < columns) {
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminHeaderSection(user: User, stats: com.school.studentportal.shared.data.model.AdminStatsResponse?, onLogout: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.AdminPanelSettings, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("System Administrator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Logged in as ${user.firstName}", style = MaterialTheme.typography.bodyMedium)
                }
                IconButton(onClick = onLogout) {
                    Icon(Icons.Default.Logout, "Logout", tint = MaterialTheme.colorScheme.error)
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                 StatusChip(label = "Users", value = stats?.total_users?.toString() ?: "0")
                 StatusChip(label = "Tickets", value = stats?.pending_tickets?.toString() ?: "0")
                 StatusChip(label = "Status", value = stats?.db_status ?: "Unknown", isGood = stats?.db_status == "Healthy")
            }
        }
    }
}

@Composable
fun StatusChip(label: String, value: String, isGood: Boolean = true) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (isGood) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.error)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
    }
}

@Composable
fun ServiceCard(title: String, icon: ImageVector, color: Color, description: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)), // Light background
        modifier = Modifier.fillMaxWidth().height(160.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                description, 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

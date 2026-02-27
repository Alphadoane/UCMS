package com.school.studentportal.shared.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import com.school.studentportal.shared.utils.GreetingUtils
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.collections.*
import com.school.studentportal.shared.data.model.User
import com.school.studentportal.shared.data.model.UserRole
import com.school.studentportal.shared.ui.viewmodel.DashboardUiState
import com.school.studentportal.shared.ui.components.LoadingView
import com.school.studentportal.shared.ui.components.ErrorView
import com.school.studentportal.shared.ui.components.GamerProfileCard
import com.school.studentportal.shared.ui.components.DashboardCard
import com.school.studentportal.shared.ui.components.QuickAppCard
import com.school.studentportal.shared.data.repository.StaffRepository
import com.school.studentportal.shared.data.repository.AdminRepository
import com.school.studentportal.shared.ui.screens.staff.StaffDashboardScreen
import com.school.studentportal.shared.ui.screens.admin.AdminDashboardScreen

import com.school.studentportal.shared.ui.Routes

@Composable
fun HomeScreen(
    uiState: DashboardUiState,
    staffRepository: StaffRepository? = null,
    adminRepository: AdminRepository? = null,
    onLogout: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onOpenDrawer: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                val greeting = remember { GreetingUtils().getGreeting() }
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
                Text(
                    text = uiState.user?.firstName ?: "Member",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                // Role Badge
                if (uiState.user != null) {
                    Surface(
                        color = when(uiState.user!!.role) {
                            UserRole.ADMIN -> Color(0xFF455A64)
                            UserRole.STAFF, UserRole.LECTURER -> Color(0xFF1976D2)
                            UserRole.TECHNICAL_SUPPORT -> Color(0xFF388E3C)
                            UserRole.STUDENT -> Color(0xFFFFA000)
                        },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = uiState.user!!.role.name.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            }
            IconButton(
                onClick = { onLogout() },
                modifier = Modifier
                    .background(Color(0xFFECEFF1), CircleShape)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = Color(0xFF455A64)
                )
            }
        }
        

        if (uiState.isLoading && uiState.user == null) {
            LoadingView()
        } else {
            uiState.user?.let { currentUser ->
                when (currentUser.role) {
                    UserRole.STUDENT -> StudentDashboard(currentUser, uiState.feeData, uiState.courses, onNavigate, onOpenDrawer)
                    UserRole.STAFF, UserRole.LECTURER -> {
                        if (staffRepository != null) {
                            StaffDashboardScreen(currentUser, staffRepository, onNavigate, onOpenDrawer)
                        } else {
                            ErrorView("Staff repository not initialized")
                        }
                    }
                    UserRole.ADMIN -> {
                        if (adminRepository != null) {
                            AdminDashboardScreen(currentUser, adminRepository, onNavigate, onLogout, onOpenDrawer)
                        } else {
                            ErrorView("Admin repository not initialized")
                        }
                    }
                    UserRole.TECHNICAL_SUPPORT -> SupportDashboardScreen(currentUser, onNavigate, onOpenDrawer)
                }
            } ?: ErrorView("User profile not found. Please contact support.")
        }
    }
}

// --- Dashboard Variants ---

@Composable
fun StudentDashboard(
    user: User, 
    feeData: Map<String, Any>? = null, 
    courses: List<Map<String, Any>> = emptyList(), 
    onNavigate: (String) -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    // 1. Student Profile
    GamerProfileCard(
        name = user.fullName,
        regNo = user.regNumber ?: "N/A",
        course = user.course ?: "Computer Science",
        gpa = 3.8,
        avatarUrl = user.avatarUrl
    )

    // 2. My Day (Hero Section)
    DashboardCard(
        title = "My Day",
        icon = Icons.Default.Schedule,
        color = MaterialTheme.colorScheme.secondary
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Next: Data Structures", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("11:00 AM  Hall B", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(progress = 0.4f, modifier = Modifier.fillMaxWidth().height(6.dp), color = MaterialTheme.colorScheme.secondary, trackColor = Color.LightGray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { onNavigate("student_timetable") }, contentPadding = PaddingValues(horizontal = 12.dp)) {
                Text("View")
            }
        }
    }

    // 3. Student 360 Apps (Grid)
    Text("Quick Access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
    
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // App 1: Virtual Campus
        QuickAppCard(
            title = "Virtual Campus", 
            icon = Icons.Default.Videocam, 
            color = MaterialTheme.colorScheme.primary, 
            modifier = Modifier.weight(1f),
            onClick = { onNavigate("virtual_campus") }
        )
        // App 2: My Courses
        QuickAppCard(
            title = "My Courses", 
            icon = Icons.Default.School, 
            color = MaterialTheme.colorScheme.primary, 
            modifier = Modifier.weight(1f),
            onClick = { onNavigate("vc_my_courses") }
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // App 3: Campus Life
        QuickAppCard(
            title = "Campus Life", 
            icon = Icons.Default.Event, 
            color = MaterialTheme.colorScheme.secondary, 
            modifier = Modifier.weight(1f),
            onClick = { onNavigate("student_campus_life") }
        )
        // App 4: Health (Replaces Essentials)
        QuickAppCard(
            title = "Health", 
            icon = Icons.Default.LocalHospital, 
            color = MaterialTheme.colorScheme.tertiary, 
            modifier = Modifier.weight(1f),
            onClick = { onNavigate(Routes.HEALTH) }
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
         // App 5: Library
        QuickAppCard(
            title = "Library", 
            icon = Icons.Default.Book, 
            color = MaterialTheme.colorScheme.primary, 
            modifier = Modifier.weight(1f),
            onClick = { onNavigate("library") }
        )
        // App 6: Analytics
        QuickAppCard(
            title = "Performance", 
            icon = Icons.Default.Insights, 
            color = MaterialTheme.colorScheme.tertiary, 
            modifier = Modifier.weight(1f),
            onClick = { onNavigate(Routes.STUDENT_ANALYTICS) }
        )
    }

    // 4. Finance Summary
    DashboardCard(
        title = "Finance",
        icon = Icons.Default.AttachMoney,
        color = MaterialTheme.colorScheme.tertiary
    ) {
        val currency = feeData?.get("currency") as? String ?: "KES"
        val balance = (feeData?.get("balance") as? Number)?.toDouble() ?: 0.0

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Outstanding Balance", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("$currency ${balance.toLong()}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = Color(0xFF1D3762))
            }
            Button(onClick = { onNavigate(Routes.FINANCE) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                Text("Pay Now")
            }
        }
    }
}

// Check placeholders for other roles


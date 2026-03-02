package com.example.android.ui.screens.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.AcademicsRepository
import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.DashboardCard
import com.example.android.ui.components.InfoRow
import kotlinx.coroutines.launch

@Composable
fun StaffCoursesScreen(
    repository: AcademicsRepository,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    var units by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            units = repository.getStaffCourses()
            loading = false
        }
    }

    PortalScaffold(title = "My Assigned Courses", onBack = onBack) {
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (units.isEmpty()) {
                    item { Text("No courses assigned to you yet. Contact Admin.") }
                }

                items(units) { unit ->
                    val unitId = unit["id"]
                    val unitCode = unit["code"] as? String ?: ""
                    val unitTitle = unit["title"] as? String ?: "Unknown"

                    DashboardCard(
                        title = "$unitCode - $unitTitle",
                        icon = Icons.Default.Class,
                        color = Color(0xFF1565C0),
                        onClick = { onNavigate("staff_course_detail/$unitId") }
                    ) {
                        InfoRow("Department", unit["department"] as? String ?: "-")
                        InfoRow("Students", "${unit["student_count"] ?: 0}")
                        
                        Text(
                            "Click to manage students, materials, and coursework",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

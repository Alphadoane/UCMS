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
import com.example.android.data.repository.AdminRepository
import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.DashboardCard
import com.example.android.ui.components.InfoRow

@Composable
fun StaffCoursesScreen(
    repository: AdminRepository = remember { AdminRepository() },
    onNavigate: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var units by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var userId by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
         val me = repository.getUserProfile("me")
         userId = me?.id ?: ""
    }

    LaunchedEffect(Unit) {
        units = repository.getStaffUnits(userId)
        loading = false
    }

    PortalScaffold(title = "My Assigned Courses") {
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
                    val unitCode = unit["id"] as? String ?: ""
                    val unitTitle = unit["title"] as? String ?: "Unknown"

                    DashboardCard(
                        title = "$unitCode - $unitTitle",
                        icon = Icons.Default.Class,
                        color = Color(0xFF1565C0)
                    ) {
                        InfoRow("Department", unit["department"] as? String ?: "-")
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Grade Button
                            Button(
                                onClick = { onNavigate("staff_grading/$unitCode") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Grade, null)
                                    Text("Grade")
                                }
                            }
                            // Material Button
                            Button(
                                onClick = { onNavigate("staff_content/$unitCode") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF6C00)),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.UploadFile, null)
                                    Text("Materials")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

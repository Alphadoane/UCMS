package com.example.android.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.AdminRepository
import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.DashboardCard
import com.example.android.ui.components.InfoRow
import kotlinx.coroutines.launch

@Composable
fun AdminSemesterScreen(
    repository: AdminRepository = remember { AdminRepository() },
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var semesters by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }

    // Dialog form state
    var newSemesterName by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("2024-01-01") }
    var endDate by remember { mutableStateOf("2024-04-30") }

    LaunchedEffect(Unit) {
        semesters = repository.getSemesters()
        loading = false
    }

    PortalScaffold(
        title = "Semester Management",
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Semester", tint = Color.White)
            }
        }
    ) {
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (semesters.isEmpty()) {
                    item {
                        Text("No academic semesters found. Create one to begin.")
                    }
                }
                
                items(semesters) { semester ->
                    val isActive = semester["isActive"] as? Boolean ?: false
                    
                    DashboardCard(
                        title = semester["name"] as? String ?: "Unknown",
                        icon = Icons.Default.DateRange,
                        color = if (isActive) Color(0xFF2E7D32) else Color.Gray
                    ) {
                        InfoRow("Start Date", semester["startDate"] as? String ?: "-")
                        InfoRow("End Date", semester["endDate"] as? String ?: "-")
                        InfoRow("Status", if (isActive) "Active" else "Archived")
                        
                        if (isActive) {
                            Button(
                                onClick = { 
                                    scope.launch {
                                        val id = semester["id"] as? String ?: return@launch
                                        repository.endSemester(id)
                                        semesters = repository.getSemesters() // Refresh
                                        android.widget.Toast.makeText(context, "Semester Ended", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64)),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Text("End Semester")
                            }
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Start New Semester") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newSemesterName,
                            onValueChange = { newSemesterName = it },
                            label = { Text("Semester Name (e.g. Jan-Apr 2025)") }
                        )
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it }, // In real app, use DatePicker
                            label = { Text("Start Date (YYYY-MM-DD)") }
                        )
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("End Date (YYYY-MM-DD)") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        scope.launch {
                            repository.createSemester(newSemesterName, startDate, endDate)
                            showCreateDialog = false
                            semesters = repository.getSemesters()
                            android.widget.Toast.makeText(context, "Semester Created", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

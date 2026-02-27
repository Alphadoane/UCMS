package com.school.studentportal.shared.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.data.repository.AdminRepository
import com.school.studentportal.shared.ui.components.AppScaffold
import com.school.studentportal.shared.ui.components.DashboardCard
import com.school.studentportal.shared.ui.components.InfoRow
import kotlinx.coroutines.launch

@Composable
fun AdminCourseMgmtScreen(
    repository: AdminRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    var units by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }

    // Dialog state
    var unitCode by remember { mutableStateOf("") }
    var unitTitle by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Computer Science") }

    LaunchedEffect(Unit) {
        units = repository.getUnits()
        loading = false
    }

    AppScaffold(
        title = "Curriculum & Units",
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color(0xFF0097A7) // Teal
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Unit", tint = Color.White)
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
    ) {
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = 900.dp) // Wider for tables/lists
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (units.isEmpty()) {
                        item { Text("No academic units found.") }
                    }
                    
                    items(units) { unit ->
                        DashboardCard(
                            title = "${unit["code"]} - ${unit["title"]}",
                            icon = Icons.Default.Book,
                            color = Color(0xFF006064)
                        ) {
                            Column {
                                InfoRow("Department", unit["department"] as? String ?: "General")
                                val lecturer = unit["lecturerName"] as? String
                                
                                if (lecturer != null) {
                                    InfoRow("Lecturer", lecturer)
                                } else {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                repository.assignLecturerToUnit(
                                                    unit["code"] as String,
                                                    "lecturer_001",
                                                    "Prof. John Doe"
                                                )
                                                units = repository.getUnits() // Refresh
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                                    ) {
                                        Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Assign Lecturer")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Add Course Unit") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = unitCode,
                            onValueChange = { unitCode = it },
                            label = { Text("Unit Code (e.g. SCS 201)") }
                        )
                        OutlinedTextField(
                            value = unitTitle,
                            onValueChange = { unitTitle = it },
                            label = { Text("Unit Title") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        scope.launch {
                            repository.createUnit(unitCode, unitTitle, department)
                            showCreateDialog = false
                            units = repository.getUnits()
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

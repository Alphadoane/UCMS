package com.example.android.ui.screens.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.AdminRepository
import com.example.android.ui.components.PortalScaffold
import kotlinx.coroutines.launch

@Composable
fun StaffGradingScreen(
    unitCode: String,
    repository: AdminRepository = remember { AdminRepository() },
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var students by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // Temporary storage for grades being edited
    // Map<StudentId, Map<GradeType, Score>>
    val gradeInputs = remember { mutableStateMapOf<String, MutableMap<String, String>>() }

    LaunchedEffect(Unit) {
        students = repository.getEnrolledStudents(unitCode)
        loading = false
    }

    PortalScaffold(title = "Grading: $unitCode") {
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (students.isEmpty()) {
                    item { Text("No students enrolled in this course.") }
                }

                items(students) { student ->
                    val studentId = student["id"] as? String ?: ""
                    val name = "${student["firstName"]} ${student["lastName"]}"
                    
                    // Initialize inputs if needed
                    if (!gradeInputs.containsKey(studentId)) {
                        gradeInputs[studentId] = mutableMapOf(
                            "CAT1" to "",
                            "CAT2" to "",
                            "Exam" to ""
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, tint = Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(name, style = MaterialTheme.typography.titleMedium, color = Color.Black)
                            }
                            Text(
                                "Reg: ${student["regNumber"] ?: "N/A"}", 
                                style = MaterialTheme.typography.bodySmall, 
                                modifier = Modifier.padding(start = 32.dp, bottom = 8.dp)
                            )
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = gradeInputs[studentId]?.get("CAT1") ?: "",
                                    onValueChange = { gradeInputs[studentId]?.put("CAT1", it) },
                                    label = { Text("CAT 1") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                OutlinedTextField(
                                    value = gradeInputs[studentId]?.get("CAT2") ?: "",
                                    onValueChange = { gradeInputs[studentId]?.put("CAT2", it) },
                                    label = { Text("CAT 2") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                OutlinedTextField(
                                    value = gradeInputs[studentId]?.get("Exam") ?: "",
                                    onValueChange = { gradeInputs[studentId]?.put("Exam", it) },
                                    label = { Text("Exam") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                            
                            Button(
                                onClick = {
                                    scope.launch {
                                        val grades = mapOf(
                                            "cat1" to (gradeInputs[studentId]?.get("CAT1")?.toDoubleOrNull() ?: 0.0),
                                            "cat2" to (gradeInputs[studentId]?.get("CAT2")?.toDoubleOrNull() ?: 0.0),
                                            "exam" to (gradeInputs[studentId]?.get("Exam")?.toDoubleOrNull() ?: 0.0)
                                        )
                                        repository.updateStudentGrade(studentId, unitCode, grades)
                                        android.widget.Toast.makeText(context, "Saved for $name", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.align(Alignment.End).padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                            ) {
                                Text("Save Grades")
                            }
                        }
                    }
                }
            }
        }
    }
}

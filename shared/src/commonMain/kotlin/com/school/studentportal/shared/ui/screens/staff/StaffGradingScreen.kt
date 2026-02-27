package com.school.studentportal.shared.ui.screens.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.school.studentportal.shared.data.repository.StaffRepository
import com.school.studentportal.shared.data.model.StudentGrade
import com.school.studentportal.shared.data.model.StudentGradeUpdate

@Composable
fun StaffGradingScreen(
    courseId: String,
    repository: StaffRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var students by remember { mutableStateOf<List<StudentGrade>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }

    val courseIdInt = courseId.toIntOrNull() ?: 0

    // Temporary storage for grades being edited
    val gradeInputs = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(courseIdInt) {
        isRefreshing = true
        val result = repository.getCourseStudents(courseIdInt)
        if (result.isSuccess) {
            students = result.getOrNull() ?: emptyList()
            students.forEach { 
                gradeInputs[it.student_id] = it.score?.toString() ?: ""
            }
        }
        isRefreshing = false
    }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Column {
             Row(verticalAlignment = Alignment.CenterVertically) {
                 IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) }
                 Text("Grading: Units Students", style = MaterialTheme.typography.headlineSmall)
             }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (students.isEmpty()) {
                        item { Text("No students enrolled in this course.") }
                    }

                    items(students) { student ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, null, tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(student.student_name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Text(
                                    "ID: ${student.student_id}", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    modifier = Modifier.padding(start = 32.dp, bottom = 8.dp)
                                )
                                
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = gradeInputs[student.student_id] ?: "",
                                        onValueChange = { gradeInputs[student.student_id] = it },
                                        label = { Text("Total Score /100") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    
                                    var isSaving by remember { mutableStateOf(false) }
                                    Button(
                                        enabled = !isSaving,
                                        onClick = {
                                            val score = gradeInputs[student.student_id]?.toIntOrNull() ?: 0
                                            isSaving = true
                                            scope.launch {
                                                val res = repository.submitGrades(courseIdInt, listOf(StudentGradeUpdate(student.student_id, score)))
                                                if (res.isSuccess) {
                                                    snackbarHostState.showSnackbar("Saved grade for ${student.student_name}")
                                                } else {
                                                    snackbarHostState.showSnackbar("Error saving grade")
                                                }
                                                isSaving = false
                                            }
                                        }
                                    ) {
                                        if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("Save")
                                    }
                                }
                            }
                        }
                    }
                }

                if (students.isNotEmpty()) {
                    Button(
                        onClick = {
                            scope.launch {
                                val updates = gradeInputs.mapNotNull { (id, scoreStr) ->
                                    val score = scoreStr.toIntOrNull() ?: return@mapNotNull null
                                    StudentGradeUpdate(id, score)
                                }
                                val res = repository.submitGrades(courseIdInt, updates)
                                if (res.isSuccess) {
                                    snackbarHostState.showSnackbar("All grades submitted successfully")
                                } else {
                                    snackbarHostState.showSnackbar("Error submitting all grades")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Submit All Grades")
                    }
                }
            }
        }
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

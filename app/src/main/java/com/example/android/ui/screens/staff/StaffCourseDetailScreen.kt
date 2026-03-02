package com.example.android.ui.screens.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.android.data.network.CourseStudentResponse
import com.example.android.data.network.LearningMaterialResponse
import com.example.android.data.network.StaffCourseWorkResponse
import com.example.android.data.repository.AcademicsRepository
import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.DashboardCard
import com.example.android.ui.components.InfoRow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffCourseDetailScreen(
    courseId: Int,
    repository: AcademicsRepository,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var students by remember { mutableStateOf<List<CourseStudentResponse>>(emptyList()) }
    var materials by remember { mutableStateOf<List<LearningMaterialResponse>>(emptyList()) }
    var coursework by remember { mutableStateOf<List<StaffCourseWorkResponse>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(courseId) {
        scope.launch {
            try {
                students = repository.getCourseStudents(courseId)
                materials = repository.getCourseMaterials(courseId)
                coursework = repository.getCourseWork(courseId)
            } catch (e: Exception) {
                // Handle error
            } finally {
                loading = false
            }
        }
    }

    var showPostMaterial by remember { mutableStateOf(false) }
    var showPostCoursework by remember { mutableStateOf(false) }

    PortalScaffold(
        title = "Course Details",
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Students", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Materials", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Coursework", modifier = Modifier.padding(16.dp))
                }
            }

            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> StudentListSection(students)
                    1 -> MaterialsSection(materials, onPost = { showPostMaterial = true })
                    2 -> CourseworkSection(coursework, onPost = { showPostCoursework = true })
                }
            }
        }
    }

    if (showPostMaterial) {
        PostMaterialDialog(
            onDismiss = { showPostMaterial = false },
            onPost = { title, desc, link ->
                scope.launch {
                    val success = repository.postCourseMaterial(courseId, title, desc, link)
                    if (success) {
                        materials = repository.getCourseMaterials(courseId)
                        showPostMaterial = false
                    }
                }
            }
        )
    }

    if (showPostCoursework) {
        PostCourseWorkDialog(
            onDismiss = { showPostCoursework = false },
            onPost = { title, desc, maxMarks, dueDate, category ->
                scope.launch {
                    val success = repository.postCourseWork(courseId, title, desc, maxMarks, dueDate, category)
                    if (success) {
                        coursework = repository.getCourseWork(courseId)
                        showPostCoursework = false
                    }
                }
            }
        )
    }
}

@Composable
fun StudentListSection(students: List<CourseStudentResponse>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (students.isEmpty()) {
            item { Text("No students enrolled in this course.") }
        }
        items(students) { student ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = student.full_name, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Adm: ${student.admission_number}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Program: ${student.program}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun MaterialsSection(materials: List<LearningMaterialResponse>, onPost: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = onPost,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Post Learning Material")
        }
        
        LazyColumn {
            if (materials.isEmpty()) {
                item { Text("No learning materials posted yet.") }
            }
            items(materials) { material ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = material.title, style = MaterialTheme.typography.titleMedium)
                        Text(text = material.description ?: "", style = MaterialTheme.typography.bodyMedium)
                        if (!material.link.isNullOrEmpty()) {
                            Text(text = material.link!!, color = Color.Blue, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseworkSection(coursework: List<StaffCourseWorkResponse>, onPost: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = onPost,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Post Assignment / CAT")
        }

        LazyColumn {
            if (coursework.isEmpty()) {
                item { Text("No assignments or CATs posted yet.") }
            }
            items(coursework) { work ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val category = work.category
                            val icon = if (category == "cat") Icons.Default.Assessment else Icons.Default.Assignment
                            Icon(icon, null, tint = if (category == "cat") Color.Red else Color.Green)
                            Spacer(Modifier.width(8.dp))
                            Text(text = work.title, style = MaterialTheme.typography.titleMedium)
                        }
                        Text(text = "Due: ${work.due_date ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Max Marks: ${work.max_marks}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun PostMaterialDialog(onDismiss: () -> Unit, onPost: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post Learning Material") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = link, onValueChange = { link = it }, label = { Text("Link (Optional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onPost(title, description, link) }, enabled = title.isNotEmpty()) { Text("Post") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PostCourseWorkDialog(onDismiss: () -> Unit, onPost: (String, String, Double, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var maxMarks by remember { mutableStateOf("100") }
    var dueDate by remember { mutableStateOf("2026-04-30") }
    var category by remember { mutableStateOf("assignment") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post Assignment / CAT") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = category == "assignment", onClick = { category = "assignment" })
                    Text("Assignment")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(selected = category == "cat", onClick = { category = "cat" })
                    Text("CAT")
                }
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = maxMarks, onValueChange = { maxMarks = it }, label = { Text("Max Marks") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Due Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onPost(title, description, maxMarks.toDoubleOrNull() ?: 100.0, dueDate, category) }, enabled = title.isNotEmpty()) { Text("Post") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

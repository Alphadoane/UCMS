package com.school.studentportal.shared.ui.screens.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.data.model.StaffCourse
import com.school.studentportal.shared.data.repository.StaffRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffCoursesScreen(
    repository: StaffRepository,
    onBack: () -> Unit
) {
    var courses by remember { mutableStateOf<List<StaffCourse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            val result = repository.getStaffCourses()
            if (result.isSuccess) {
                courses = result.getOrNull() ?: emptyList()
            } else {
                error = result.exceptionOrNull()?.message ?: "Failed to load courses"
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Courses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // Using a text arrow if icon not available in shared, but shared usually has icons
                        // Assuming material icons are available or passed in. 
                        // For shared module, we often use expect/actual or standard compose icons
                         Text("<") 
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            } else if (error != null) {
                Text(text = "Error: $error", modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(courses) { course ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = course.title, style = MaterialTheme.typography.titleMedium)
                                Text(text = "${course.code} • ${course.student_count} Students", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    if (courses.isEmpty()) {
                        item {
                            Text("No courses assigned yet.", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

package com.example.android.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.android.data.network.AllocateLectureRequest
import com.example.android.data.network.ApiService
import com.example.android.data.network.CourseOption
import com.example.android.data.network.LecturerOption
import com.example.android.data.repository.AdminRepository
import com.example.android.ui.components.PortalScaffold
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAllocationScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { AdminRepository(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var courses by remember { mutableStateOf<List<CourseOption>>(emptyList()) }
    var lecturers by remember { mutableStateOf<List<LecturerOption>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Form State
    var selectedCourse by remember { mutableStateOf<CourseOption?>(null) }
    var selectedLecturer by remember { mutableStateOf<LecturerOption?>(null) }
    var day by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }

    // Dropdown expansion state
    var courseExpanded by remember { mutableStateOf(false) }
    var lecturerExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val result = repository.getOptions()
        if (result.isSuccess) {
            val data = result.getOrNull()
            courses = data?.courses ?: emptyList()
            lecturers = data?.lecturers ?: emptyList()
        } else {
            snackbarHostState.showSnackbar("Failed to load options")
        }
        isLoading = false
    }

    PortalScaffold(
        title = "Allocate Course",
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Course Dropdown
                ExposedDropdownMenuBox(
                    expanded = courseExpanded,
                    onExpandedChange = { courseExpanded = !courseExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCourse?.let { "${it.code} - ${it.name}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Course") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = courseExpanded,
                        onDismissRequest = { courseExpanded = false }
                    ) {
                        courses.forEach { course ->
                            DropdownMenuItem(
                                text = { Text("${course.code} - ${course.name}") },
                                onClick = {
                                    selectedCourse = course
                                    courseExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lecturer Dropdown
                ExposedDropdownMenuBox(
                    expanded = lecturerExpanded,
                    onExpandedChange = { lecturerExpanded = !lecturerExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedLecturer?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Lecturer") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lecturerExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = lecturerExpanded,
                        onDismissRequest = { lecturerExpanded = false }
                    ) {
                        lecturers.forEach { lecturer ->
                            DropdownMenuItem(
                                text = { Text(lecturer.name) },
                                onClick = {
                                    selectedLecturer = lecturer
                                    lecturerExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = day,
                    onValueChange = { day = it },
                    label = { Text("Day of Week (e.g. Monday)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time (HH:MM)") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time (HH:MM)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("Venue") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            if (selectedCourse != null && selectedLecturer != null) {
                                val req = AllocateLectureRequest(
                                    course_id = selectedCourse!!.id,
                                    employee_id = selectedLecturer!!.employee_id ?: "",
                                    day = day,
                                    start_time = startTime,
                                    end_time = endTime,
                                    venue = venue
                                )
                                val res = repository.allocateLecture(req)
                                if (res.isSuccess) {
                                    snackbarHostState.showSnackbar("Allocation successful")
                                    onNavigateBack()
                                } else {
                                    snackbarHostState.showSnackbar("Error: ${res.exceptionOrNull()?.message}")
                                }
                            } else {
                                snackbarHostState.showSnackbar("Please select Course and Lecturer")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Allocate Lecturer")
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}

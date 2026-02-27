package com.school.studentportal.shared.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.window.Dialog
import com.school.studentportal.shared.data.model.CourseOption
import com.school.studentportal.shared.data.model.LecturerOption
import com.school.studentportal.shared.data.model.StudentOption
import com.school.studentportal.shared.data.model.RoomOption
import com.school.studentportal.shared.data.repository.AdminRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAllocationScreen(
    repository: AdminRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var courses by remember { mutableStateOf<List<CourseOption>>(emptyList()) }
    var lecturers by remember { mutableStateOf<List<LecturerOption>>(emptyList()) }
    var rooms by remember { mutableStateOf<List<RoomOption>>(emptyList()) }
    var students by remember { mutableStateOf<List<StudentOption>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Form State
    var selectedCourse by remember { mutableStateOf<CourseOption?>(null) }
    var selectedLecturer by remember { mutableStateOf<LecturerOption?>(null) }
    var selectedStudents by remember { mutableStateOf<List<StudentOption>>(emptyList()) }
    var day by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }

    // Dropdown expansion state
    var courseExpanded by remember { mutableStateOf(false) }
    var lecturerExpanded by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }
    var startTimeExpanded by remember { mutableStateOf(false) }
    var endTimeExpanded by remember { mutableStateOf(false) }
    var roomExpanded by remember { mutableStateOf(false) }
    var showStudentDialog by remember { mutableStateOf(false) }

    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val timeSlots = (7..21).flatMap { hour -> 
        listOf("${hour.toString().padStart(2, '0')}:00", "${hour.toString().padStart(2, '0')}:30")
    }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMsg = null
        val result = repository.getAllocationOptions()
        if (result.isSuccess) {
            val options = result.getOrNull()
            courses = options?.courses ?: emptyList()
            lecturers = options?.lecturers ?: emptyList()
            rooms = options?.rooms ?: emptyList()
            students = options?.students ?: emptyList()
            if (courses.isEmpty() && lecturers.isEmpty()) {
                errorMsg = "No data received from server"
            }
        } else {
             val err = result.exceptionOrNull()?.message ?: "Unknown error"
             errorMsg = "Failed to load: $err"
             snackbarHostState.showSnackbar(errorMsg!!)
        }
        isLoading = false
    }

    if (showStudentDialog) {
        AlertDialog(
            onDismissRequest = { showStudentDialog = false },
            title = { Text("Select Students") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    students.forEach { student ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    val current = selectedStudents.toMutableList()
                                    if (current.contains(student)) {
                                        current.remove(student)
                                    } else {
                                        current.add(student)
                                    }
                                    selectedStudents = current
                                }
                        ) {
                            Checkbox(
                                checked = selectedStudents.contains(student),
                                onCheckedChange = null // Handled by Row click
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(student.name, style = MaterialTheme.typography.bodyMedium)
                                Text(student.admission_number, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStudentDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // No AppScaffold, use Box/Column. Parent Drawer provides header.
    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp) // Form width
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Allocate Course", style = MaterialTheme.typography.headlineSmall)
                    
                    if (errorMsg != null) {
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
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
                            if (courses.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No courses available") },
                                    onClick = { courseExpanded = false }
                                )
                            } else {
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
                            if (lecturers.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No lecturers available") },
                                    onClick = { lecturerExpanded = false }
                                )
                            } else {
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
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Student Selection
                    OutlinedTextField(
                        value = if (selectedStudents.isEmpty()) "Select Class Members (Optional)" else "${selectedStudents.size} Students Selected",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Allocate Students") },
                        trailingIcon = { IconButton(onClick = { showStudentDialog = true }) { Icon(Icons.Default.ArrowBack, contentDescription = "Select", modifier = Modifier.rotate(180f)) } },
                        enabled = true,
                        modifier = Modifier.fillMaxWidth().clickable { showStudentDialog = true },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
    
                    Spacer(modifier = Modifier.height(16.dp))
    
                    // Day Dropdown
                    ExposedDropdownMenuBox(
                        expanded = dayExpanded,
                        onExpandedChange = { dayExpanded = !dayExpanded }
                    ) {
                        OutlinedTextField(
                            value = day,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Day of Week") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = dayExpanded,
                            onDismissRequest = { dayExpanded = false }
                        ) {
                            daysOfWeek.forEach { selectedDay ->
                                DropdownMenuItem(
                                    text = { Text(selectedDay) },
                                    onClick = {
                                        day = selectedDay
                                        dayExpanded = false
                                    }
                                )
                            }
                        }
                    }
    
                    Spacer(modifier = Modifier.height(16.dp))
    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Start Time Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            ExposedDropdownMenuBox(
                                expanded = startTimeExpanded,
                                onExpandedChange = { startTimeExpanded = !startTimeExpanded }
                            ) {
                                OutlinedTextField(
                                    value = startTime,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Start") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = startTimeExpanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = startTimeExpanded,
                                    onDismissRequest = { startTimeExpanded = false }
                                ) {
                                    timeSlots.forEach { slot ->
                                        DropdownMenuItem(
                                            text = { Text(slot) },
                                            onClick = {
                                                startTime = slot
                                                startTimeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))

                        // End Time Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            ExposedDropdownMenuBox(
                                expanded = endTimeExpanded,
                                onExpandedChange = { endTimeExpanded = !endTimeExpanded }
                            ) {
                                OutlinedTextField(
                                    value = endTime,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("End") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = endTimeExpanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = endTimeExpanded,
                                    onDismissRequest = { endTimeExpanded = false }
                                ) {
                                    timeSlots.forEach { slot ->
                                        DropdownMenuItem(
                                            text = { Text(slot) },
                                            onClick = {
                                                endTime = slot
                                                endTimeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
    
                    Spacer(modifier = Modifier.height(16.dp))
    
                    // Venue Dropdown
                    ExposedDropdownMenuBox(
                        expanded = roomExpanded,
                        onExpandedChange = { roomExpanded = !roomExpanded }
                    ) {
                        OutlinedTextField(
                            value = venue,
                            onValueChange = { venue = it }, // Allow manual entry too just in case
                            label = { Text("Venue") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = roomExpanded,
                            onDismissRequest = { roomExpanded = false }
                        ) {
                            if (rooms.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No rooms listed") },
                                    onClick = { roomExpanded = false }
                                )
                            } else {
                                rooms.forEach { room ->
                                    DropdownMenuItem(
                                        text = { Text(room.name) },
                                        onClick = {
                                            venue = room.name
                                            roomExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
    
                    Spacer(modifier = Modifier.height(24.dp))
    
                    Button(
                        onClick = {
                            scope.launch {
                                if (selectedCourse != null && selectedLecturer != null) {
                                    isLoading = true
                                    val result = repository.allocateLecture(
                                        courseId = selectedCourse!!.id,
                                        employeeId = selectedLecturer!!.employee_id ?: "",
                                        day = day,
                                        startTime = startTime,
                                        endTime = endTime,
                                        venue = venue,
                                        studentIds = selectedStudents.map { it.admission_number }
                                    )
                                    isLoading = false
                                    if (result.isSuccess) {
                                        snackbarHostState.showSnackbar("Allocation successful")
                                        selectedCourse = null
                                        selectedLecturer = null
                                        selectedStudents = emptyList()
                                        day = ""
                                        startTime = ""
                                        endTime = ""
                                        venue = ""
                                    } else {
                                        val errorDetail = result.exceptionOrNull()?.message ?: "Unknown error"
                                        snackbarHostState.showSnackbar("Allocation failed: $errorDetail")
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
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter))
    }
}

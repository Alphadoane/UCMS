package com.school.studentportal.shared.ui.screens.student.virtualcampus

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.school.studentportal.shared.data.model.UserRole
import com.school.studentportal.shared.ui.components.AppScaffold
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoomRoomsScreen(
    userRole: UserRole,
    repository: com.school.studentportal.shared.data.repository.VirtualCampusRepository? = null,
    staffRepository: com.school.studentportal.shared.data.repository.StaffRepository? = null,
    onBack: () -> Unit = {},
    onJoinRoom: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var rooms by remember { mutableStateOf<List<com.school.studentportal.shared.data.model.ZoomRoom>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Staff Creation State
    var showCreateDialog by remember { mutableStateOf(false) }
    var staffCourses by remember { mutableStateOf<List<com.school.studentportal.shared.data.model.StaffCourse>>(emptyList()) }
    
    // Form State
    var selectedCourseCode by remember { mutableStateOf("") }
    var roomTitle by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    
    val isStaff = userRole == UserRole.STAFF || userRole == UserRole.LECTURER

    LaunchedEffect(Unit) {
        isLoading = true
        // Fetch rooms
        repository?.getZoomRooms()?.collect {
            rooms = it
        }
        isLoading = false
        
        // Fetch staff courses if applicable
        if (isStaff && staffRepository != null) {
            val res = staffRepository.getStaffCourses()
            if (res.isSuccess) {
                staffCourses = res.getOrNull() ?: emptyList()
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Zoom Room") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Course:")
                    // Simple Dropdown alternative or list selection
                    if (staffCourses.isEmpty()) {
                        Text("No courses assigned.", color = Color.Red)
                    } else {
                        staffCourses.forEach { course ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCourseCode = course.code }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCourseCode == course.code,
                                    onClick = { selectedCourseCode = course.code }
                                )
                                Text(course.code + " - " + course.title)
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = roomTitle,
                        onValueChange = { roomTitle = it },
                        label = { Text("Room Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    
                    val datePickerState = rememberDatePickerState()
                    var showDatePicker by remember { mutableStateOf(false) }
                    var showTimePicker by remember { mutableStateOf(false) }
                    val timePickerState = rememberTimePickerState()
                    
                    // Display formatting
                    val displayedTime = if (startTime.isNotEmpty()) startTime else "Select Start Time"

                    OutlinedTextField(
                        value = displayedTime,
                        onValueChange = { },
                        label = { Text("Start Time") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        enabled = false, // Disable typing, force click
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    )

                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDatePicker = false
                                    showTimePicker = true // Open time picker after date
                                }) { Text("Next") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }
                    
                    if (showTimePicker) {
                        AlertDialog(
                            onDismissRequest = { showTimePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    showTimePicker = false
                                    val dateMillis = datePickerState.selectedDateMillis
                                    if (dateMillis != null) {
                                        val date = Instant.fromEpochMilliseconds(dateMillis).toLocalDateTime(TimeZone.UTC).date
                                        val time = LocalTime(timePickerState.hour, timePickerState.minute)
                                        // Format: YYYY-MM-DD HH:MM
                                        val formatted = "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')} ${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
                                        startTime = formatted
                                    }
                                }) { Text("Confirm") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                            },
                            text = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Select Time", style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(16.dp))
                                    TimePicker(state = timePickerState)
                                }
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isCreating && selectedCourseCode.isNotBlank() && roomTitle.isNotBlank(),
                    onClick = {
                        // Check if room already exists for this course
                        if (rooms.any { it.course_code == selectedCourseCode }) {
                            scope.launch {
                                snackbarHostState.showSnackbar("A Zoom Room already exists for this course.")
                            }
                        } else {
                            isCreating = true
                            scope.launch {
                                try {
                                    val result = repository?.createZoomRoom(selectedCourseCode, roomTitle, startTime)
                                    if (result?.isSuccess == true) {
                                        showCreateDialog = false
                                        snackbarHostState.showSnackbar("Zoom Room Created Successfully")
                                        // Trigger refresh
                                        isLoading = true
                                        repository?.getZoomRooms()?.collect {
                                            rooms = it
                                            isLoading = false
                                        }
                                    } else {
                                        val error = result?.exceptionOrNull()?.message ?: "Failed to create room"
                                        snackbarHostState.showSnackbar("Error: $error")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Error: ${e.message}")
                                } finally {
                                    isCreating = false
                                }
                            }
                        }
                    }
                ) {
                    if (isCreating) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    AppScaffold(
        title = "Zoom Rooms (Live)",
        showTopBar = false,
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                if (isStaff) {
                    Button(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.VideoCameraFront, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("New Room")
                    }
                }
            }
            
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (rooms.isEmpty() && !isLoading) {
                    item { Text("No active zoom rooms found.") }
                }
                
                items(rooms) { room ->
                    var showDeleteConfirm by remember { mutableStateOf(false) }
                    
                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("Delete Zoom Room") },
                            text = { Text("Are you sure you want to delete this zoom room for ${room.course_code}?") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showDeleteConfirm = false
                                        scope.launch {
                                            val result = repository?.deleteZoomRoom(room.id)
                                            if (result?.isSuccess == true) {
                                                snackbarHostState.showSnackbar("Room deleted successfully")
                                                // Refresh
                                                isLoading = true
                                                repository?.getZoomRooms()?.collect {
                                                    rooms = it
                                                    isLoading = false
                                                }
                                            } else {
                                                val error = result?.exceptionOrNull()?.message ?: "Failed to delete"
                                                snackbarHostState.showSnackbar("Error: $error")
                                            }
                                        }
                                    }
                                ) { Text("Delete", color = Color.Red) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                            }
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(), 
                        colors = CardDefaults.cardColors(containerColor = Color.White), 
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${room.course_code} - ${room.course_title}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                if (room.is_host) {
                                    IconButton(onClick = { showDeleteConfirm = true }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                }
                            }
                            if (!room.title.isNullOrBlank()) {
                                Text(room.title, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                            }
                            room.start_time?.let {
                                Text("Starts: $it", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            // Show the Agora channel so the lecturer can share it
                            val channelName = room.channel_name ?: room.meeting_id ?: room.id
                            Text("Channel: $channelName", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1D3762))
                            Button(
                                // Pass Agora channel_name (not DB id) so MeetingScreen joins the right room
                                onClick = { onJoinRoom(channelName) }, 
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                            ) { 
                                Text(if (room.is_host) "Start Meeting" else "Join Room") 
                            }
                        }
                    }
                }
            }
        }
    }
}

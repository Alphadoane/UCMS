package com.school.studentportal.shared.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.data.model.*
import com.school.studentportal.shared.data.repository.SupportRepository
import com.school.studentportal.shared.data.repository.AcademicsRepository
import com.school.studentportal.shared.ui.components.AppScaffold
import kotlinx.coroutines.launch

@Composable
fun ComplaintScreen(
    user: User,
    supportRepository: SupportRepository,
    academicsRepository: AcademicsRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val complaints by supportRepository.complaints.collectAsState()
    val courses by academicsRepository.enrolledCourses.collectAsState()
    
    var currentScreen by remember { mutableStateOf("list") }
    var selectedComplaintId by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        supportRepository.refreshComplaints()
        academicsRepository.refreshEnrolledCourses()
        isLoading = false
    }

    when (currentScreen) {
        "list" -> ComplaintListScreen(
            user = user,
            complaints = complaints,
            isLoading = isLoading,
            onComplaintClick = { id -> 
                selectedComplaintId = id
                currentScreen = "detail" 
            },
            onAddComplaint = { courseId, desc, priority ->
                scope.launch {
                    supportRepository.lodgeComplaint(courseId, desc, priority)
                    supportRepository.refreshComplaints()
                }
            },
            availableCourses = courses,
            onBack = onBack
        )
        "detail" -> {
            val complaint = complaints.find { it.id == selectedComplaintId }
            if (complaint != null) {
                ComplaintDetailScreen(
                    user = user,
                    complaint = complaint,
                    onBack = { currentScreen = "list" },
                    onUpdateStatus = { newStatus ->
                        scope.launch {
                            supportRepository.updateStatus(complaint.id, newStatus)
                            supportRepository.refreshComplaints()
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintListScreen(
    user: User,
    complaints: List<ComplaintItem>,
    isLoading: Boolean,
    onComplaintClick: (Int) -> Unit,
    onAddComplaint: (Int, String, String) -> Unit,
    availableCourses: List<AcademicCourse>,
    onBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    val screenTitle = if(user.role == UserRole.STUDENT) "My Complaints" else "Pending Academic Issues"

    AppScaffold(
        title = screenTitle,
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
        floatingActionButton = {
            if (user.role == UserRole.STUDENT) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = Color(0xFF1D3762)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                }
            }
        }
    ) {
        if (isLoading && complaints.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Redundant header removed from here
                
                items(complaints) { complaint ->
                    ComplaintCard(complaint, onClick = { onComplaintClick(complaint.id) })
                }
                
                if (complaints.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No complaints found.", color = Color.Gray)
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AddComplaintDialog(
                courses = availableCourses,
                onDismiss = { showDialog = false },
                onConfirm = { courseId, desc, priority ->
                    onAddComplaint(courseId, desc, priority)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun ComplaintCard(complaint: ComplaintItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(complaint.course_name, fontWeight = FontWeight.Bold)
                StatusChip(complaint.status)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = complaint.description,
                maxLines = 2,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PriorityHigh, null, modifier = Modifier.size(14.dp), 
                    tint = if(complaint.priority == "urgent") Color.Red else Color.Gray)
                Text(
                    text = "Priority: ${complaint.priority.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = complaint.created_at.split("T")[0],
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when(status.lowercase()) {
        "submitted" -> Color(0xFF2196F3)
        "resolved" -> Color(0xFF4CAF50)
        "urgent" -> Color.Red
        "escalated" -> Color(0xFFE91E63)
        else -> Color(0xFFFF9800)
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = status.uppercase().replace("_", " "),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ComplaintDetailScreen(
    user: User,
    complaint: ComplaintItem,
    onBack: () -> Unit,
    onUpdateStatus: (String) -> Unit
) {
    AppScaffold(
        title = "Ticket Detail",
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
    ) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text("Course: ${complaint.course_name}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                StatusChip(complaint.status)
            }
            
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.1f))) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Description", fontWeight = FontWeight.Bold)
                        Text(complaint.description)
                    }
                }
            }

            if (user.role != UserRole.STUDENT) {
                item {
                    Text("Admin Actions", fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onUpdateStatus("under_review") }, modifier = Modifier.weight(1f)) {
                            Text("Review")
                        }
                        Button(onClick = { onUpdateStatus("resolved") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                            Text("Resolve")
                        }
                    }
                }
            }

            item {
                Text("Log Activity / Timeline", fontWeight = FontWeight.Bold)
            }

            items(complaint.timeline) { event ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Box(Modifier.size(12.dp).background(Color(0xFF1D3762), CircleShape).align(Alignment.CenterVertically))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(event.description, style = MaterialTheme.typography.bodyMedium)
                        Text("${event.created_at.replace("T", " ").substring(0, 16)} by ${event.user_name}", 
                             style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddComplaintDialog(
    courses: List<AcademicCourse>,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, String) -> Unit
) {
    var selectedCourseId by remember { mutableStateOf<Int?>(null) }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("medium") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lodge New Complaint") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box {
                    OutlinedTextField(
                        value = courses.find { it.id == selectedCourseId }?.title ?: "Select Course Unit",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Course Unit") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { expanded = true }) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        courses.forEach { course ->
                            DropdownMenuItem(
                                text = { Text("${course.code} - ${course.title}") },
                                onClick = { selectedCourseId = course.id; expanded = false }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Describe your issue") },
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Priority: ")
                    listOf("low", "medium", "urgent").forEach { p ->
                        InputChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.uppercase()) },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = selectedCourseId != null && description.isNotEmpty(),
                onClick = { onConfirm(selectedCourseId!!, description, priority) }
            ) { Text("Submit") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

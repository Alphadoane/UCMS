package com.example.android.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.android.ui.components.PortalScaffold

@Composable
fun CourseDetailScreen(courseId: String, onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Assignments", "Resources")

    PortalScaffold(title = "Course Details: $courseId") {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> CourseOverview(courseId)
                1 -> AssignmentList()
                2 -> CourseResources()
            }
        }
    }
}

@Composable
fun CourseOverview(courseId: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Introduction to $courseId", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("This course covers the fundamental concepts...", style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Lecturer", style = MaterialTheme.typography.labelMedium)
                Text("Dr. Smith", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AssignmentList() {
    // Offline & Queue State
    // Offline & Queue State
    // Note: OfflineManager uses WorkManager, so we don't manually track 'isOnline' here for UI toggles anymore.
    // We only track the queue to show 'Queued' status via a (hypothetical) observable if WorkManager exposes it,
    // or we assume it's queued until we get a data refresh.
    // For this refactor, we remove the simple simulation queue observation as OfflineManager now delegates entirely to WorkManager.
    // In a full production app, we would observe WorkManager's WorkInfos by Tag.

    // For now, to keep the "Queued" UI logic working without the legacy queue flow, we will simplified it:
    // When submitted, it goes to "Submitted" (optimistic) or we can listen to WorkManager.
    val workManager =
        androidx.work.WorkManager.getInstance(androidx.compose.ui.platform.LocalContext.current)
    // Complex to observe generic work by ID here without tracking IDs. 
    // We will assume "Submitted" state is sufficient for the user now that we trust WorkManager.

    // File Picker State

    // File Picker State
    var showDialog by remember { mutableStateOf(false) }
    var activeAssignmentId by remember { mutableStateOf<String?>(null) }

    // Assignments List (Local State)
    val _assignments = remember {
        mutableStateListOf(
            Assignment("A1", "Essay on Ethics", "Due: 12 Oct", false),
            Assignment("A2", "Java Project", "Due: 20 Oct", true),
            Assignment("A3", "Final Report", "Due: 15 Nov", false)
        )
    }


    // Handlers
    // Handlers
    val context = androidx.compose.ui.platform.LocalContext.current
    val userId = "2500001" // TODO: Get from Auth

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val fileName = "assignment_doc.pdf"
            activeAssignmentId?.let { id ->
                // Always queue - WorkManager handles immediate or deferred execution based on network
                showDialog = true
                com.example.android.util.OfflineManager.queueSubmission(
                    context = context,
                    assignmentId = id,
                    fileUri = it.toString(),
                    userId = userId,
                    title = "Assignment $id"
                )

                // Optimistic Update
                val index = _assignments.indexOfFirst { it.id == id }
                if (index != -1) {
                    _assignments[index] = _assignments[index].copy(isSubmitted = true)
                }
            }
        }
    }

    Column {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(_assignments) { assignment ->
                val isQueued = false // Simplified for now
                // If it is NOT in queue AND NOT submitted => Pending

                // Hacky Sync for Demo: If it was queued and now isn't, and we haven't marked submitted, mark it.
                // This requires tracking if we ever queued it, which is complex here. 
                // Instead, let's assume if OfflineManager says it's done, we get a callback?? 
                // For simplicity, let's rely on the simulation:
                // When OfflineManager processes, it sends email. We just need UI update.
                // Let's toggle "isSubmitted" when we see it leave the queue IF we knew it was there.

                Card(
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                assignment.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                assignment.dueDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        if (assignment.isSubmitted) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Submitted (Syncing in bg)",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Button(onClick = {
                                activeAssignmentId = assignment.id
                                launcher.launch("*/*")
                            }) {
                                Text("Submit")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseResources() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Lecture Notes & Slides", style = MaterialTheme.typography.titleMedium)
        // List of PDFs...
    }
}

data class Assignment(
    val id: String,
    val title: String,
    val dueDate: String,
    val isSubmitted: Boolean
)

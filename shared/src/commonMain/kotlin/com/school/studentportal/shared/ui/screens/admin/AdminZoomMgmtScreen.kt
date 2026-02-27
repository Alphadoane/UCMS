package com.school.studentportal.shared.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.data.model.ZoomRoom
import com.school.studentportal.shared.data.repository.VirtualCampusRepository
import com.school.studentportal.shared.ui.components.AppScaffold
import kotlinx.coroutines.launch

@Composable
fun AdminZoomMgmtScreen(
    repository: VirtualCampusRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var rooms by remember { mutableStateOf<List<ZoomRoom>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }

    // Dialog
    var courseCode by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        repository.getZoomRooms().collect { 
            rooms = it
            loading = false
        }
    }

    AppScaffold(
        title = "Virtual Campus Mgr",
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = Color(0xFF1565C0)) {
                Icon(Icons.Default.Add, contentDescription = "New Meeting", tint = Color.White)
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
    ) {
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = 900.dp)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Text("Active Zoom Sessions", style = MaterialTheme.typography.titleMedium) }
                    
                    if (rooms.isEmpty()) {
                        item { Text("No active sessions.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) }
                    }

                    items(rooms) { room ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                ) {
                                     Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0xFF1565C0).copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.VideoCall, contentDescription = null, tint = Color(0xFF1565C0))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = room.course_title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1565C0)
                                    )
                                }
                            
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Code", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                                    Text(room.course_code, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                                }
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Start Time", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                                    Text(room.start_time ?: "Now", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                                }
                                
                                Spacer(Modifier.height(8.dp))
                                // Join Link
                                Text(room.join_url, style = MaterialTheme.typography.bodySmall, color = Color.Blue)
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
             AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Create Zoom Class") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                         OutlinedTextField(value = courseCode, onValueChange = { courseCode = it }, label = { Text("Course Code") })
                         OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                         OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Start Time") })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        scope.launch {
                             repository.createZoomRoom(courseCode, title, startTime)
                             showDialog = false
                             // Refresh happens via flow if setup correctly, otherwise logic needed
                        }
                    }) { Text("Create") }
                },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
             )
        }
    }
}

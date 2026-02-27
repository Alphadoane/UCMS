package com.example.android.ui.screens.virtualcampus

import android.content.Intent
import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// import com.example.android.data.network.ApiService
// import com.example.android.data.network.NetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.android.util.ZoomClient

data class ZoomRoomUi(val id: String, val courseCode: String, val courseTitle: String, val startTime: String, val joinUrl: String, val hostUrl: String? = null, val isHost: Boolean = false)

@Composable
fun ZoomRoomsScreen(
    userRole: com.example.android.data.model.UserRole = com.example.android.data.model.UserRole.STUDENT,
    onJoinRoom: (String) -> Unit = {},
    repository: com.example.android.data.repository.VirtualCampusRepository = remember { com.example.android.data.repository.VirtualCampusRepository() }
) {
    val context = LocalContext.current
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val micPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    
    // Real-time Rooms
    val roomsData by repository.getZoomRooms().collectAsState(initial = emptyList())
    val rooms = remember(roomsData) {
        roomsData.map { r ->
             ZoomRoomUi(
                id = r["id"] as? String ?: "",
                courseCode = r["course_code"] as? String ?: "",
                courseTitle = r["course_title"] as? String ?: "",
                startTime = (r["start_time"] as? String) ?: "",
                joinUrl = r["join_url"] as? String ?: "",
                hostUrl = r["host_url"] as? String,
                isHost = r["is_host"] as? Boolean ?: false
            )
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isStaff = userRole == com.example.android.data.model.UserRole.STAFF || userRole == com.example.android.data.model.UserRole.ADMIN

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Zoom Rooms (Live)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            if (isStaff) {
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.VideoCameraFront, contentDescription = "Create Room", tint = Color(0xFF455A64))
                }
            }
        }
        
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(rooms) { room ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Text("${room.courseCode} - ${room.courseTitle}", fontWeight = FontWeight.Bold)
                        Text("Starts: ${room.startTime}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Button(onClick = {
                                onJoinRoom(room.id)
                            }, modifier = Modifier.weight(1f)) { 
                                Text(if (room.isHost) "Host Meeting" else "Join Meeting") 
                            }
                        }
                    }
                }
            }
        }
        
        if (showCreateDialog) {
            var courseCode by remember { mutableStateOf("") }
            var title by remember { mutableStateOf("") }
            var time by remember { mutableStateOf("Today, 10:00 AM") }
            var isCreating by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Schedule New Meeting") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = courseCode, onValueChange = { courseCode = it }, label = { Text("Course Code (e.g. SE312)") })
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Meeting Title") })
                        OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Start Time") })
                    }
                },
                confirmButton = {
                    Button(
                        enabled = !isCreating && courseCode.isNotBlank() && title.isNotBlank(),
                        onClick = {
                            isCreating = true
                            scope.launch {
                                val result = repository.createZoomRoom(courseCode, title, time)
                                isCreating = false
                                if (result.isSuccess) {
                                    showCreateDialog = false
                                    // Refresh should happen via collection
                                }
                            }
                        }
                    ) {
                        if (isCreating) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("Create")
                    }
                },
                dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } }
            )
        }
    }
}

/**
 * Join a Google Meet session via Intent.
 * Tries to open in Google Meet app if installed, otherwise falls back to browser.
 */
private fun joinGoogleMeet(context: android.content.Context, roomId: String, joinUrl: String?) {
    val meetUrl = when {
        // If joinUrl is already a Google Meet URL, use it
        !joinUrl.isNullOrBlank() && (joinUrl.contains("meet.google.com") || joinUrl.startsWith("https://meet.google.com")) -> {
            joinUrl
        }
        // Otherwise, construct Google Meet URL from room ID
        else -> {
            "https://meet.google.com/$roomId"
        }
    }
    
    try {
        // Try to open with Google Meet app Intent first
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(meetUrl)
            setPackage("com.google.android.apps.meetings") // Google Meet package name
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        // Check if Google Meet app is installed
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to browser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(meetUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        }
    } catch (e: Exception) {
        // Final fallback - open in browser
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(meetUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        } catch (ex: Exception) {
            // If all fails, user can manually open the URL
            android.util.Log.e("ZoomRoomsScreen", "Failed to open Google Meet: $meetUrl", ex)
        }
    }
}



package com.example.android.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
fun TimetableScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { com.example.android.data.repository.AcademicsRepository(context) }
    var lectures by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<List<com.example.android.data.network.ApiService.Lecture>>(emptyList()) }
    var isLoading by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }
    var error by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        val result = repository.getTimetable()
        if (result.isSuccess) {
            lectures = result.getOrNull() ?: emptyList()
        } else {
            error = result.exceptionOrNull()?.message ?: "Failed to load timetable"
        }
        isLoading = false
    }

    PortalScaffold(title = "My Day") {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error!!, color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Group by Day if needed, but for now just list all
                val today = java.time.LocalDate.now().dayOfWeek.name.lowercase().capitalize() // e.g. Monday
                
                item {
                    Text("Timetable", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                
                if (lectures.isEmpty()) {
                    item {
                        Text("No classes scheduled.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                } else {
                    // Simple heuristic for current: if day matches and time is close.
                    // For now, just show daily color coding.
                    val colors = listOf(Color(0xFF1976D2), Color(0xFF00796B), Color(0xFFE65100), Color(0xFF5E35B1))
                    
                    lectures.forEachIndexed { index, lecture ->
                         item {
                            TimetableItem(
                                time = "${lecture.start_time} - ${lecture.end_time}",
                                title = "${lecture.course_code}: ${lecture.course_title}",
                                venue = "${lecture.venue} (${lecture.day})",
                                color = colors[index % colors.size],
                                isCurrent = lecture.day.equals(today, ignoreCase = true)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimetableItem(time: String, title: String, venue: String, color: Color, isCurrent: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(70.dp)) {
            Text(time, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (isCurrent) color else Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isCurrent) Color.White else Color.Black)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (isCurrent) Color.White.copy(alpha = 0.8f) else Color.Gray, modifier = Modifier.size(14.dp))
                    Text(venue, style = MaterialTheme.typography.bodySmall, color = if (isCurrent) Color.White.copy(alpha = 0.8f) else Color.Gray)
                }
            }
        }
    }
}

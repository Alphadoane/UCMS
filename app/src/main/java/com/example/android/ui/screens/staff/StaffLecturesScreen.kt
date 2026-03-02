package com.example.android.ui.screens.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Class
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.VirtualCampusRepository
import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.DashboardCard
import com.example.android.ui.components.InfoRow
import com.example.android.ui.components.LoadingView

@Composable
fun StaffLecturesScreen(
    repository: VirtualCampusRepository = remember { VirtualCampusRepository() },
    onNavigateBack: () -> Unit
) {
    var lectures by remember { mutableStateOf<List<com.example.android.data.network.Lecture>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        lectures = repository.getLectures()
        loading = false
    }

    PortalScaffold(title = "My Lectures") {
        if (loading) {
            LoadingView()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Assigned Classes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }

                items(lectures) { lecture ->
                    DashboardCard(
                        title = lecture.course_title,
                        icon = Icons.Default.Class,
                        color = Color(0xFF1D3762)
                    ) {
                        InfoRow("Code", lecture.course_code)
                        InfoRow("Day", lecture.day)
                        InfoRow("Time", "${lecture.start_time} - ${lecture.end_time}")
                        InfoRow("Venue", lecture.venue)
                        
                        Button(
                            onClick = { /* Navigate to content upload or attendees */ },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Manage Class")
                        }
                    }
                }
                
                if (lectures.isEmpty()) {
                    item {
                        Text("You are not assigned to any classes yet.", color = Color.Gray)
                    }
                }
            }
        }
    }
}

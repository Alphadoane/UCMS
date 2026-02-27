package com.school.studentportal.shared.ui.screens.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Class
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.ui.components.DashboardCard
import com.school.studentportal.shared.ui.components.InfoRow
import kotlinx.coroutines.delay

@Composable
fun StaffLecturesScreen(
    onNavigateBack: () -> Unit
) {
    var lectures by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(600)
        lectures = listOf(
            mapOf("name" to "Intro to CS", "code" to "CS101", "day" to "Monday", "time" to "08:00 - 11:00", "venue" to "LH 1"),
            mapOf("name" to "Data Structures", "code" to "CS204", "day" to "Wednesday", "time" to "11:00 - 14:00", "venue" to "Lab 3")
        )
        loading = false
    }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Column {
            Text("My Lectures", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text("Assigned Classes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }

                    items(lectures) { lecture ->
                        DashboardCard(
                            title = lecture["name"] ?: "",
                            icon = Icons.Default.Class,
                            color = Color(0xFF1D3762)
                        ) {
                            InfoRow("Code", lecture["code"] ?: "")
                            InfoRow("Day", lecture["day"] ?: "")
                            InfoRow("Time", lecture["time"] ?: "")
                            InfoRow("Venue", lecture["venue"] ?: "")
                            
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
}

package com.school.studentportal.shared.ui.screens.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.ui.components.AppScaffold

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import com.school.studentportal.shared.data.repository.AcademicsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    repository: AcademicsRepository,
    onNavigateBack: () -> Unit
) {
    val timetable by repository.timetable.collectAsState()

    LaunchedEffect(Unit) {
        repository.refreshTimetable()
    }

    AppScaffold(
        title = "My Timetable",
        showTopBar = false,
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text("Weekly Schedule", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            
            val colors = listOf(Color(0xFF1976D2), Color(0xFF00796B), Color(0xFFE65100), Color(0xFF5E35B1))
            
            items(timetable.size) { index ->
                val entry = timetable[index]
                TimetableItem(
                    time = "${entry.start_time} - ${entry.end_time}",
                    title = "${entry.course_code}: ${entry.course_title}",
                    venue = "${entry.venue} (${entry.day})",
                    color = colors[index % colors.size],
                    isCurrent = index == 0 // Mock current logic could be added later
                )
            }

            if (timetable.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No timetable entries found", color = Color.Gray)
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
            colors = CardDefaults.cardColors(containerColor = if (isCurrent) color else MaterialTheme.colorScheme.surfaceVariant),
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

package com.school.studentportal.shared.ui.screens.student.virtualcampus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Class
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.data.model.User
import com.school.studentportal.shared.data.model.UserRole
import com.school.studentportal.shared.ui.components.AppScaffold

@Composable
fun VCDashboardScreen(user: User?, onBack: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Day", "Week", "Month")
    
    // Mock Activities Data
    val mockClasses = listOf(
        mapOf("time" to "09:00, Today", "title" to "Advanced Algorithms", "course" to "CS401", "venue" to "Lab 4", "day" to "Monday"),
        mapOf("time" to "11:30, Today", "title" to "Mobile App Dev", "course" to "CS405", "venue" to "Hall B", "day" to "Monday"),
        mapOf("time" to "14:00, Tomorrow", "title" to "Database Systems", "course" to "CS302", "venue" to "Online", "day" to "Tuesday")
    )
    val mockAssignments = listOf(
        mapOf("due" to "Friday, 18:00", "title" to "Project Proposal", "course" to "CS401")
    )

    AppScaffold(
        title = "Campus Dashboard",
        showTopBar = false,
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
    ) {
        val isStaff = user?.role == UserRole.STAFF
        
        Column(modifier = Modifier.fillMaxSize()) {
            if (isStaff) {
                TabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }) {
                            Text(title, modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (isStaff) {
                    when (selectedTab) {
                        0 -> { // Day
                            item { Text("Today's Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                            items(mockClasses.filter { it["day"] == "Monday" }) { cls ->
                                TimelineItem(
                                    time = cls["time"] as String,
                                    title = cls["title"] as String,
                                    subtitle = "${cls["course"]} @ ${cls["venue"]}",
                                    icon = Icons.Default.Class,
                                    color = Color(0xFF1976D2)
                                )
                            }
                        }
                        1 -> { // Week
                            item { Text("Weekly Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                            item { Text("Coming Soon", color = Color.Gray) }
                        }
                        else -> { // Month
                            item { Text("Monthly Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                            item { Text("Coming Soon", color = Color.Gray) }
                        }
                    }
                } else {
                    item {
                        Text("Today's Agenda", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Stay on top of your schedule", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                    
                    item {
                        val nextClass = mockClasses[0]
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3762)),
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                        Text("HAPPENING NEXT", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(nextClass["time"] as String, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(nextClass["title"] as String, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("${nextClass["course"]} • ${nextClass["venue"]}", color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }

                    item { Text("Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(mockClasses) { cls ->
                        TimelineItem(
                            time = cls["time"] as String,
                            title = cls["title"] as String,
                            subtitle = "${cls["course"]} @ ${cls["venue"]}",
                            icon = Icons.Default.Class,
                            color = Color(0xFF1976D2)
                        )
                    }

                    item { Text("Upcoming Deadlines", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(mockAssignments) { task ->
                        TimelineItem(
                            time = task["due"] as String,
                            title = task["title"] as String,
                            subtitle = task["course"] as String,
                            icon = Icons.Default.Assignment,
                            color = Color(0xFFE91E63),
                            isDeadline = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineItem(
    time: String, 
    title: String, 
    subtitle: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isDeadline: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(modifier = Modifier.width(70.dp), horizontalAlignment = Alignment.End) {
            Text(time.split(",").last().trim(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
            if(isDeadline) Text("Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
        }
        
        Column(modifier = Modifier.width(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(12.dp).background(color, CircleShape).border(2.dp, Color.White, CircleShape))
            Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.LightGray.copy(alpha = 0.5f)))
        }
        
        Card(
            modifier = Modifier.weight(1f).padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}

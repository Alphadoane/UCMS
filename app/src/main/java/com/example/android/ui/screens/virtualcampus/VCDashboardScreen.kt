package com.example.android.ui.screens.virtualcampus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.VirtualCampusRepository
import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.LoadingView

@Composable
fun VCDashboardScreen(
    repository: VirtualCampusRepository = remember { VirtualCampusRepository() }
) {
    var user by remember { mutableStateOf<com.example.android.data.network.ProfileResponse?>(null) }
    var activities by remember { mutableStateOf<Map<String, List<Map<String, Any>>>?>(null) }
    var loading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Day", "Week", "Month")

    LaunchedEffect(Unit) {
        user = repository.getUserProfile("me")
    }

    LaunchedEffect(user) {
        val currentUserId = user?.id
        if (currentUserId != null) {
            activities = repository.getUpcomingActivities(currentUserId)
        }
        loading = false
    }

    PortalScaffold(title = "Campus Dashboard") {
        if (loading) {
            LoadingView()
        } else if (activities == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No Activity Data") }
        } else {
            val isStaff = user?.role == "STAFF"
            val classes = activities!!["classes"] ?: emptyList()
            
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
                        // Staff Distribution View
                        val today = java.time.LocalDate.now().dayOfWeek.name.lowercase().capitalize()

                        when (selectedTab) {
                            0 -> { // Day
                                item { Text("Today's Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                                val todayClasses = classes.filter { (it["day"] as? String)?.equals(today, ignoreCase = true) == true }
                                if (todayClasses.isEmpty()) {
                                    item { Text("No lectures scheduled for today.") }
                                } else {
                                    items(todayClasses) { cls ->
                                        TimelineItem(
                                            time = cls["time"] as String,
                                            title = cls["title"] as String,
                                            subtitle = "${cls["course"]} @ ${cls["venue"]}",
                                            icon = Icons.Default.Class,
                                            color = Color(0xFF1976D2)
                                        )
                                    }
                                }
                            }
                            1 -> { // Week
                                item { Text("Weekly Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                                val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                                days.forEach { dayName ->
                                    val dayClasses = classes.filter { (it["day"] as? String)?.equals(dayName, ignoreCase = true) == true }
                                    if (dayClasses.isNotEmpty()) {
                                        item { 
                                            Text(dayName, style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                                            Divider(Modifier.padding(vertical = 4.dp))
                                        }
                                        items(dayClasses) { cls ->
                                            TimelineItem(
                                                time = cls["time"] as String,
                                                title = cls["title"] as String,
                                                subtitle = "${cls["course"]} @ ${cls["venue"]}",
                                                icon = Icons.Default.Class,
                                                color = Color(0xFF43A047)
                                            )
                                        }
                                    }
                                }
                            }
                            2 -> { // Month
                                item { Text("Monthly Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                                item {
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("Total Assigned Lectures: ${classes.size}", fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.height(8.dp))
                                            classes.distinctBy { it["course"] }.forEach { 
                                               Text("• ${it["course"]}: ${it["title"]}")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Original Student View
                        val assignments = activities!!["assignments"] ?: emptyList()
                        
                        item {
                            Text("Today's Agenda", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Stay on top of your schedule", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                        
                        item {
                            if (classes.isNotEmpty()) {
                                val nextClass = classes[0]
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
                        }

                        item { Text("Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                        items(classes) { cls ->
                            TimelineItem(
                                time = cls["time"] as String,
                                title = cls["title"] as String,
                                subtitle = "${cls["course"]} @ ${cls["venue"]}",
                                icon = Icons.Default.Class,
                                color = Color(0xFF1976D2)
                            )
                        }

                        item { Text("Upcoming Deadlines", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                        items(assignments) { task ->
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
        // Time Column
        Column(modifier = Modifier.width(70.dp), horizontalAlignment = Alignment.End) {
            Text(time.split(",").last().trim(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
            if(isDeadline) Text("Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
        }
        
        // Line & Dot
        Column(modifier = Modifier.width(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(12.dp).background(color, CircleShape).border(2.dp, Color.White, CircleShape))
            Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.LightGray.copy(alpha = 0.5f)))
        }
        
        // Content
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

package com.example.android.ui.screens.academics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.AcademicsRepository
import com.example.android.ui.components.*


@Composable
fun CourseWorkScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { AcademicsRepository(context) }
    var userId by remember { mutableStateOf<String?>(null) }
    val items = remember { mutableStateListOf<Map<String, Any>>() }
    val error = remember { mutableStateOf<String?>(null) }
    val loading = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val message = repository.diagnoseUser("me")
        if (message == "SQL Backend Active") {
            val user = repository.getUserProfile("me")
            userId = user?.id
        }

        // Capture userId in a local variable for smart casting
        val currentUserId = userId
        if (currentUserId != null) {
            try {
                val data = repository.getCourseWork(currentUserId)
                items.clear()
                items.addAll(data)
            } catch (e: Exception) {
                error.value = e.message
            } finally {
                loading.value = false
            }
        } else {
            // error.value = "User not logged in" // Don't show error immediately, wait for fetch
             if (userId == null) {
                 // Try fetching again or just handle empty state
                 loading.value = false
             }
        }
    }

    PortalScaffold(title = "Course Work & Analytics") {
        if (loading.value) {
            LoadingView()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ANALYTICS HEADER
                item {
                    val averageScore = if (items.isNotEmpty()) {
                        items.mapNotNull { (it["marks"] as? Number)?.toDouble() }.average()
                    } else 0.0
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3762)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Academic Performance", color = Color.White.copy(alpha = 0.8f))
                                Text(
                                    "Avg. Score: ${String.format("%.1f", averageScore)}%", 
                                    style = MaterialTheme.typography.headlineSmall, 
                                    color = Color.White, 
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Top 15% of class", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ASSIGNMENTS LIST
                item {
                    Text("Assignments", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                }

                items(items) { item ->
                    val marks = (item["marks"] as? Number)?.toDouble() ?: 0.0
                    val max = (item["max_marks"] as? Number)?.toDouble() ?: 100.0
                    val percentage = (marks / max).toFloat()
                    
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("${item["course_code"]}", fontWeight = FontWeight.Bold)
                                Text("${item["assignment"]}", color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = percentage,
                                    modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = if(percentage > 0.7) Color(0xFF4CAF50) else if(percentage > 0.5) Color(0xFFFFC107) else Color(0xFFF57C00)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("${marks.toInt()}/${max.toInt()}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

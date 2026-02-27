package com.example.android.ui.screens.virtualcampus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.VirtualCampusRepository
import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.LoadingView

@Composable
fun VCMyCoursesScreen(
    repository: VirtualCampusRepository = remember { VirtualCampusRepository() },
    onCourseClick: (String) -> Unit = {}
) {
    var userId by remember { mutableStateOf<String?>(null) }
    var courses by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var isGridView by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val me = repository.getUserProfile("me")
        userId = me?.id
    }

    LaunchedEffect(userId) {
        val currentUserId = userId
        if (currentUserId != null) {
            courses = repository.getEnrolledCoursesDetails(currentUserId)
        }
        loading = false
    }

    PortalScaffold(
        title = "My Courses",
        actions = {
            IconButton(onClick = { isGridView = !isGridView }) {
                Icon(
                    if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                    contentDescription = "Toggle View"
                )
            }
        }
    ) {
        if (loading) {
            LoadingView()
        } else if (courses.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No Enrolled Courses") }
        } else {
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(courses) { course ->
                        CourseGridCard(course, onClick = { onCourseClick(course["code"] as String) })
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(courses) { course ->
                        CourseListCard(course, onClick = { onCourseClick(course["code"] as String) })
                    }
                }
            }
        }
    }
}

@Composable
fun CourseGridCard(course: Map<String, Any>, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().height(200.dp),
        onClick = onClick
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color((course["color"] as Long)))
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    course["code"] as String, 
                    color = Color.White, 
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(course["name"] as String, fontWeight = FontWeight.Bold, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                DetailRow(Icons.Default.Person, course["lecturer"] as String)
                DetailRow(Icons.Default.AccessTime, course["time"] as String)
                DetailRow(Icons.Default.LocationOn, course["venue"] as String)
            }
        }
    }
}

@Composable
fun CourseListCard(course: Map<String, Any>, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color((course["color"] as Long)), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    (course["code"] as String).take(2), 
                    color = Color.White, 
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(course["name"] as String, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(course["lecturer"] as String, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Row {
                    Text(course["time"] as String, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(" • ", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(course["venue"] as String, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
    }
}

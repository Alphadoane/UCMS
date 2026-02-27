package com.school.studentportal.shared.ui.screens.student.virtualcampus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.ui.components.AppScaffold

@Composable
fun UnitContentScreen(
    courseCode: String,
    onBack: () -> Unit
) {
    // Mock Data
    val modules = listOf(
        mapOf("title" to "Module 1: Introduction", "resources" to listOf("Syllabus.pdf", "Introduction Video")),
        mapOf("title" to "Module 2: Core Concepts", "resources" to listOf("Lecture Notes.pdf", "Reading Material", "Exercise 1.pdf"))
    )

    AppScaffold(
        title = "Course Content: $courseCode",
        showTopBar = false,
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Announcements Header
            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFE3F2FD)).padding(16.dp)) {
                Column {
                    Text("Unit Announcements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1D3762))
                    Text("• Draft Exam timetable released.", style = MaterialTheme.typography.bodySmall)
                    Text("• Project submission deadline extended.", style = MaterialTheme.typography.bodySmall)
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(modules) { module ->
                    ModuleCard(module)
                }
            }
        }
    }
}

@Composable
fun ModuleCard(module: Map<String, Any>) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFFFB8C00))
                Spacer(modifier = Modifier.width(16.dp))
                Text(module["title"] as String, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                val resources = module["resources"] as List<String>
                resources.forEach { res ->
                    ResourceItem(res)
                }
            }
        }
    }
}

@Composable
fun ResourceItem(name: String) {
    val icon = if (name.contains("Video")) Icons.Default.PlayCircle else Icons.Default.Description
    val color = if (name.contains("Video")) Color(0xFFE53935) else Color(0xFF1976D2)
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(name, style = MaterialTheme.typography.bodySmall)
    }
}

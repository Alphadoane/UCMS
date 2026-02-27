package com.example.android.ui.screens.virtualcampus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.VirtualCampusRepository
import com.example.android.ui.components.LoadingView


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitContentScreen(
    courseCode: String,
    onBack: () -> Unit,
    repository: VirtualCampusRepository = remember { VirtualCampusRepository() }
) {
    var content by remember { mutableStateOf<Map<String, Any>?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(courseCode) {
        content = repository.getUnitContent(courseCode)
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(courseCode) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1D3762),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {}) { // Mock Join Class
                        Icon(Icons.Default.VideoCameraFront, contentDescription = "Join Live Class")
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            LoadingView()
        } else if (content == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Content unavailable") }
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                // Header
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFE3F2FD)).padding(16.dp)) {
                    Column {
                        Text(content!!["title"] as String, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1D3762))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Announcements", fontWeight = FontWeight.Bold)
                        (content!!["announcements"] as List<String>).forEach { 
                            Text("• $it", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp)) 
                        }
                    }
                }

                // Modules List
                val modules = content!!["modules"] as List<Map<String, Any>>
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
}

@Composable
fun ModuleCard(module: Map<String, Any>) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = { expanded = !expanded } // Toggle expand
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
                Spacer(modifier = Modifier.height(12.dp))
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
        Text(name, style = MaterialTheme.typography.bodyMedium)
    }
}

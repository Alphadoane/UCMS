package com.example.android.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.AdminRepository
import com.example.android.data.repository.VirtualCampusRepository
import com.example.android.ui.components.PortalScaffold
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun AdminZoomMgmtScreen(
    repository: AdminRepository = remember { AdminRepository() },
    vcRepository: VirtualCampusRepository = remember { VirtualCampusRepository() },
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var rooms by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        vcRepository.getZoomRooms().collect { rooms = it }
    }

    PortalScaffold(
        title = "Virtual Campus Mgr",
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Open Dialog */ }, containerColor = Color(0xFF1565C0)) {
                Icon(Icons.Default.Add, contentDescription = "New Meeting", tint = Color.White)
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text("Active Zoom Sessions", style = MaterialTheme.typography.titleMedium) }
            
            items(rooms) { room ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                             Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFF1565C0).copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.VideoCall, contentDescription = null, tint = Color(0xFF1565C0))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = room["course_title"] as? String ?: "Meeting",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                        }
                    
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Host", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                            Text("System Admin", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Start Time", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                            Text(room["start_time"].toString(), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                        }
                        Button(
                             onClick = { /* End Meeting Logic */ },
                             colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                             modifier = Modifier.fillMaxWidth().padding(top=8.dp)
                        ) {
                            Text("End Session")
                        }
                    }
                }
            }
        }
    }
}

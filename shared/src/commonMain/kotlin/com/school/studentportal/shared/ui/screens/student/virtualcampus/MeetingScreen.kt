package com.school.studentportal.shared.ui.screens.student.virtualcampus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.delay

@Composable
fun MeetingScreen(
    meetingId: String,
    onLeave: () -> Unit
) {
    var connectionState by remember { mutableStateOf("Connecting...") }
    var isMuted by remember { mutableStateOf(false) }
    var isVideoOff by remember { mutableStateOf(false) }
    
    val participants = listOf(
        Participant("Me", Color.Gray, true),
        Participant("Dr. Smith", Color(0xFF1D3762), false),
        Participant("Alice", Color(0xFF388E3C), false),
        Participant("Bob", Color(0xFFE91E63), false)
    )

    LaunchedEffect(Unit) {
        delay(1000)
        connectionState = "Connected"
    }

    AppScaffold(
        title = "Meeting: $meetingId",
        showTopBar = false,
        navigationIcon = { IconButton(onClick = onLeave) { Icon(Icons.Default.ArrowBack, null) } }
    ) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
            // Participant Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(participants) { p ->
                    ParticipantTile(p, if(p.name == "Me") isVideoOff else false)
                }
            }
            
            // Connection Info
            Text(
                connectionState, 
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp),
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall
            )

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControlIcon(
                    icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    active = !isMuted,
                    onClick = { isMuted = !isMuted }
                )
                ControlIcon(
                    icon = if (isVideoOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                    active = !isVideoOff,
                    onClick = { isVideoOff = !isVideoOff }
                )
                FloatingActionButton(
                    onClick = onLeave,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.CallEnd, null)
                }
            }
        }
    }
}

@Composable
fun ParticipantTile(p: Participant, isVideoMuted: Boolean) {
    Card(
        modifier = Modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isVideoMuted) {
                Box(modifier = Modifier.size(60.dp).background(p.color, CircleShape), contentAlignment = Alignment.Center) {
                    Text(p.name.take(1), color = Color.White, style = MaterialTheme.typography.headlineMedium)
                }
            } else {
                // Placeholder for Video
                Box(modifier = Modifier.fillMaxSize().background(p.color.copy(alpha = 0.3f)))
                Text("Video Stream: ${p.name}", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall)
            }
            
            Text(
                p.name, 
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun ControlIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, active: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(56.dp).background(if (active) Color.White.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.2f), CircleShape)
    ) {
        Icon(icon, null, tint = if (active) Color.White else Color.Red)
    }
}

data class Participant(val name: String, val color: Color, val isMe: Boolean)

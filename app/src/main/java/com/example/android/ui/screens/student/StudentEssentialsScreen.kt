package com.example.android.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android.ui.components.PortalScaffold

@Composable
fun StudentEssentialsScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    
    PortalScaffold(title = "Essentials") {
         Column(modifier = Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Transport") }, icon = { Icon(Icons.Default.DirectionsBus, null) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Health") }, icon = { Icon(Icons.Default.LocalHospital, null) })
            }
            
            when(selectedTab) {
                0 -> TransportView()
                1 -> HealthView()
            }
         }
    }
}

@Composable
fun TransportView() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
             Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                 Icon(Icons.Default.Warning, null, tint = Color(0xFFE65100))
                 Spacer(modifier = Modifier.width(16.dp))
                 Text("School Bus B is delayed by 10 mins due to traffic.", color = Color(0xFFE65100))
             }
        }
        
        Text("Bus Schedule (Route A)", fontWeight = FontWeight.Bold)
        ScheduleRow("07:00 AM", "Main Campus", "Departed")
        ScheduleRow("07:30 AM", "Westlands", "Arriving")
        ScheduleRow("08:00 AM", "CBD", "Scheduled")
    }
}

@Composable
fun HealthView() {
     Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
         Button(onClick = {}, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64))) {
             Icon(Icons.Default.Call, null)
             Spacer(modifier = Modifier.width(8.dp))
             Text("Emergency Hotline")
         }
         
         Text("Book Appointment", fontWeight = FontWeight.Bold)
         Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
             Column(modifier = Modifier.padding(16.dp)) {
                 Text("School Clinic", style = MaterialTheme.typography.titleMedium)
                 Text("General Practitioner Available", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                 Spacer(modifier = Modifier.height(16.dp))
                 Button(onClick = {}) { Text("Book Slot") }
             }
         }
     }
}

@Composable
fun ScheduleRow(time: String, location: String, status: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(time, fontWeight = FontWeight.Bold)
        Text(location)
        Text(status, color = if(status == "Departed") Color.Gray else Color(0xFF2E7D32))
    }
    Divider()
}

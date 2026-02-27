package com.school.studentportal.shared.ui.screens.student

import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentEssentialsScreen(onNavigateBack: () -> Unit) {
    AppScaffold(
        title = "Health",
        showTopBar = false,
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
    ) {
         HealthView()
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

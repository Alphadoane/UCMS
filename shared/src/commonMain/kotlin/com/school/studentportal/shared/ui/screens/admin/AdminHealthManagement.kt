package com.school.studentportal.shared.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.school.studentportal.shared.data.model.Appointment
import com.school.studentportal.shared.data.model.EmergencyAlert
import com.school.studentportal.shared.data.repository.SupportRepository
import com.school.studentportal.shared.ui.components.AppScaffold
import com.school.studentportal.shared.ui.components.LoadingView
import com.school.studentportal.shared.ui.components.ErrorView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHealthManagement(repository: SupportRepository, onBack: () -> Unit) {
    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var alerts by remember { mutableStateOf<List<EmergencyAlert>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(0) }

    fun refresh() {
        isLoading = true
        scope.launch {
            repository.getAppointments().onSuccess { appts ->
                appointments = appts
                repository.getEmergencyAlerts().onSuccess { alts ->
                    alerts = alts
                    isLoading = false
                }.onFailure {
                    error = it.message
                    isLoading = false
                }
            }.onFailure {
                error = it.message
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    AppScaffold(
        title = "Health & Emergency Control",
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        if (isLoading) {
            LoadingView()
        } else if (error != null) {
            ErrorView(error!!)
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Alerts (${alerts.size})", modifier = Modifier.padding(16.dp))
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Appointments (${appointments.size})", modifier = Modifier.padding(16.dp))
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (selectedTab == 0) {
                        items(alerts) { alert ->
                            EmergencyAlertCard(alert)
                        }
                    } else {
                        items(appointments) { appointment ->
                            AdminAppointmentItem(appointment)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyAlertCard(alert: EmergencyAlert) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.status == "ACTIVE") Color(0xFFFCE8E6) else Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(if (alert.status == "ACTIVE") Color.Red else Color.Gray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Warning, null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(alert.student_name ?: "Unknown Student", fontWeight = FontWeight.Bold)
                    Text("GPS: ${alert.latitude}, ${alert.longitude}", style = MaterialTheme.typography.bodySmall)
                }
                if (alert.status == "ACTIVE") {
                    AdminHealthStatusBadge("ACTIVE")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { /* Open Map */ }, modifier = Modifier.weight(1f)) {
                    Text("View on Map")
                }
                OutlinedButton(onClick = { /* Mark Resolved */ }, modifier = Modifier.weight(1f)) {
                    Text("Resolve")
                }
            }
        }
    }
}

@Composable
fun AdminAppointmentItem(appointment: Appointment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(appointment.student_name ?: "Student", fontWeight = FontWeight.Bold)
                    Text(appointment.appointment_type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                AdminHealthStatusBadge(appointment.status ?: "PENDING")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(appointment.reason, style = MaterialTheme.typography.bodyMedium)
            Text(appointment.appointment_date.take(16).replace("T", " "), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            
            if (appointment.status == "PENDING") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { /* Confirm */ }) { Text("Confirm") }
                    OutlinedButton(onClick = { /* Cancel */ }) { Text("Cancel") }
                }
            }
        }
    }
}

@Composable
fun AdminHealthStatusBadge(status: String) {
    Surface(
        color = when(status.uppercase()) {
            "ACTIVE", "URGENT" -> Color(0xFFFCE8E6)
            "CONFIRMED" -> Color(0xFFE6F4EA)
            "PENDING" -> Color(0xFFFFF7E0)
            else -> Color(0xFFF5F5F5)
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = when(status.uppercase()) {
                "ACTIVE", "URGENT" -> Color.Red
                "CONFIRMED" -> Color(0xFF137333)
                "PENDING" -> Color(0xFFE37400)
                else -> Color.Gray
            }
        )
    }
}

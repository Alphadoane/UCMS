package com.school.studentportal.shared.ui.screens.student.health

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
import com.school.studentportal.shared.data.repository.SupportRepository
import com.school.studentportal.shared.ui.components.AppScaffold
import com.school.studentportal.shared.ui.components.LoadingView
import com.school.studentportal.shared.ui.components.ErrorView
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(repository: SupportRepository, onBack: () -> Unit) {
    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showBookingDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf("HEALTH") }
    var reason by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        repository.getAppointments().onSuccess {
            appointments = it
            isLoading = false
        }.onFailure {
            error = it.message ?: "Failed to load appointments"
            isLoading = false
        }
    }

    AppScaffold(
        title = "Health & Wellness",
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Emergency Section
                item {
                    EmergencySection(onTrigger = {
                        scope.launch {
                            // Mock GPS for now
                            repository.sendEmergencyAlert(-1.286389, 36.817223).onSuccess {
                                snackbarHostState.showSnackbar("Emergency alert sent! Help is on the way.")
                            }.onFailure {
                                snackbarHostState.showSnackbar("Failed to send alert: ${it.message}")
                            }
                        }
                    })
                }

                // Quick Services
                item {
                    Text("Medical Services", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ServiceCard("General", Icons.Default.MedicalServices, Color(0xFF1E88E5), Modifier.weight(1f)) {
                            selectedType = "HEALTH"
                            showBookingDialog = true
                        }
                        ServiceCard("Psychology", Icons.Default.Favorite, Color(0xFF9C27B0), Modifier.weight(1f)) {
                            selectedType = "MENTAL_HEALTH"
                            showBookingDialog = true
                        }
                        ServiceCard("Therapy", Icons.Default.Accessibility, Color(0xFF43A047), Modifier.weight(1f)) {
                            selectedType = "THERAPY"
                            showBookingDialog = true
                        }
                    }
                }

                // Recent Appointments
                item {
                    Text("My Appointments", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                items(appointments) { appointment ->
                    AppointmentItem(appointment)
                }

                if (appointments.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No upcoming appointments found.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }

    if (showBookingDialog) {
        AlertDialog(
            onDismissRequest = { showBookingDialog = false },
            title = { Text("Book $selectedType") },
            text = {
                Column {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for visit") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        val newAppointment = Appointment(
                            appointment_type = selectedType,
                            reason = reason,
                            appointment_date = "2026-03-01T10:00:00Z"
                        )
                        repository.bookAppointment(newAppointment).onSuccess {
                            showBookingDialog = false
                            appointments = listOf(it) + appointments
                            snackbarHostState.showSnackbar("Appointment booked successfully")
                        }.onFailure {
                            snackbarHostState.showSnackbar("Booking failed: ${it.message}")
                        }
                    }
                }) {
                    Text("Confirm Booking")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBookingDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EmergencySection(onTrigger: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Emergency", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Send distress signal with GPS", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            }
            Button(
                onClick = onTrigger,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFB71C1C)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Ambulance", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceCard(title: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun AppointmentItem(appointment: Appointment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val icon = when(appointment.appointment_type) {
                "HEALTH" -> Icons.Default.MedicalServices
                "MENTAL_HEALTH" -> Icons.Default.Favorite
                "THERAPY" -> Icons.Default.Accessibility
                else -> Icons.Default.Event
            }
            Icon(icon, null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(appointment.reason, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(appointment.appointment_date.take(16).replace("T", " "), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            HealthStatusBadge(appointment.status ?: "PENDING")
        }
    }
}

@Composable
fun HealthStatusBadge(status: String) {
    Surface(
        color = when(status.uppercase()) {
            "CONFIRMED" -> Color(0xFFE8F5E9)
            "PENDING" -> Color(0xFFFFF3E0)
            else -> Color(0xFFF5F5F5)
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = when(status.uppercase()) {
                "CONFIRMED" -> Color(0xFF2E7D32)
                "PENDING" -> Color(0xFFEF6C00)
                else -> Color.Black
            }
        )
    }
}

package com.example.android.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.android.data.repository.SupportRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(onNavigateBack: () -> Unit) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { SupportRepository(context) }
    
    var showBookingDialog by remember { mutableStateOf(false) }
    var bookingType by remember { mutableStateOf("HEALTH") }
    
    var healthTips by remember { mutableStateOf<List<com.example.android.data.network.HealthTip>>(emptyList()) }
    var isLoadingTips by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repository.getHealthTips().onSuccess { tips ->
            healthTips = tips
            isLoadingTips = false
        }.onFailure { _ ->
            isLoadingTips = false
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Health & Wellness", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Emergency Hotline Section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Medical Emergency?", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("24/7 Campus Hotline", color = Color.White.copy(alpha = 0.9f))
                    }
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:999"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Call, null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(Modifier.width(8.dp))
                        Text("DIAL", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. Services Grid
            Text("Services", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HealthServiceCard(
                    title = "General Visit",
                    icon = Icons.Default.CalendarMonth,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = { 
                        bookingType = "HEALTH"
                        showBookingDialog = true 
                    }
                )
                HealthServiceCard(
                    title = "Medical Records",
                    icon = Icons.Default.Description,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                    onClick = { Toast.makeText(context, "No records found", Toast.LENGTH_SHORT).show() }
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HealthServiceCard(
                    title = "Mental Health",
                    icon = Icons.Default.SelfImprovement,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f),
                    onClick = { 
                        bookingType = "THERAPY"
                        showBookingDialog = true 
                    }
                )
                HealthServiceCard(
                    title = "Ambulance",
                    icon = Icons.Default.MedicalServices,
                    color = Color(0xFFE91E63),
                    modifier = Modifier.weight(1f),
                    onClick = { 
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                        context.startActivity(intent)
                    }
                )
            }

            // 3. Health Tips
            Text("Daily Health Tips", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            if (isLoadingTips) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (healthTips.isEmpty()) {
                HealthTipItem(
                    title = "Stay Hydrated",
                    desc = "Drink at least 8 glasses of water today for better focus.",
                    icon = Icons.Default.WaterDrop
                )
            } else {
                healthTips.forEach { tip ->
                    HealthTipItem(
                        title = tip.title,
                        desc = tip.description,
                        icon = Icons.Default.TipsAndUpdates
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    if (showBookingDialog) {
        BookingDialog(
            type = bookingType,
            repository = repository,
            onDismiss = { showBookingDialog = false }
        )
    }
}

@Composable
fun HealthTipItem(title: String, desc: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun HealthServiceCard(title: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(120.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun BookingDialog(type: String, repository: SupportRepository, onDismiss: () -> Unit) {
    var reason by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val title = if (type == "THERAPY") "Book Therapy Session" else "Book Clinic Appointment"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text("Describe your symptoms or reason for the session:")
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth().height(100.dp).padding(top = 8.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        repository.bookAppointment(type, reason)
                            .onSuccess { _ ->
                                Toast.makeText(context, "Request Sent Successfully!", Toast.LENGTH_LONG).show()
                                onDismiss()
                            }
                            .onFailure { error ->
                                Toast.makeText(context, "Booking Failed: ${error.message}", Toast.LENGTH_LONG).show()
                            }
                        isLoading = false
                    }
                },
                enabled = !isLoading && reason.isNotBlank()
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp)) else Text("Book Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

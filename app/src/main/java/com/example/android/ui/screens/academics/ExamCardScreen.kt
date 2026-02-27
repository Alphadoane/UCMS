package com.example.android.ui.screens.academics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.AcademicsRepository
import com.example.android.ui.components.*
import kotlin.random.Random

@Composable
fun ExamCardScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { AcademicsRepository(context) }
    var userId by remember { mutableStateOf<String?>(null) }
    val items = remember { mutableStateListOf<Map<String, Any>>() }
    // State for Fee Balance
    var feeBalance by remember { mutableStateOf(0.0) }
    var feeCurrency by remember { mutableStateOf("KES") }
    
    val error = remember { mutableStateOf<String?>(null) }
    val loading = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
             val me = repository.getUserProfile("me")
             userId = me?.id
        } catch(e: Exception) {}
    }

    LaunchedEffect(userId) {
        val currentUserId = userId
        if (currentUserId != null) {
            try {
                // 1. Fetch Exam Data
                val examData = repository.getExamCard(currentUserId)
                items.clear()
                items.addAll(examData)
                
                // 2. Fetch Fee Data for Validation
                val feeData = repository.getFeeBalance(currentUserId)
                feeBalance = (feeData["balance"] as? Number)?.toDouble() ?: 0.0
                feeCurrency = feeData["currency"] as? String ?: "KES"
                
            } catch (e: Exception) {
                error.value = e.message
            } finally {
                loading.value = false
            }
        } else {
            error.value = "User not logged in"
            loading.value = false
        }
    }

    PortalScaffold(title = "Exam Card") {
        when {
            loading.value -> LoadingView()
            error.value != null -> ErrorView(message = error.value!!)
            else -> {
                // LOGIC: Fee Clearance Gate
                if (feeBalance > 0) {
                    // BLOCKED STATE
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFFF57C00)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Exam Card Locked",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF57C00)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "You have an outstanding fee balance of $feeCurrency ${String.format("%,.2f", feeBalance)}.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Please clear your balance to access your Exam Card.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { /* Navigate to Finance */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64))
                        ) {
                            Text("Pay Fees Now")
                        }
                    }
                } else {
                    // CLEARED STATE - Digital Pass
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Digital Pass Header with QR
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3762)), // Theme Blue
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("OFFICIAL EXAM PASS", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                                    Text("Semester 1, 2024/2025", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Fee Cleared", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                                // Simulated dynamic QR Code
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .padding(4.dp)
                                ) {
                                    SimulatedQRCode()
                                }
                            }
                        }

                        // Exam List
                        Text(
                            "Scheduled Exams", 
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.Gray
                        )
                        
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(items) { item ->
                                val date = item["exam_date"] as? String ?: "TBA"
                                InfoCard(
                                    title = "${item["course_code"]} - ${item["course_name"]}",
                                    subtitle = "Venue: ${item["venue"]}\nDate: $date",
                                    icon = Icons.Default.Event
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimulatedQRCode() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cellSize = size.width / 10
        for (i in 0..9) {
            for (j in 0..9) {
                // Random pattern generation
                if (Random.nextBoolean()) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(i * cellSize, j * cellSize),
                        size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                    )
                }
            }
        }
        // Corner markers (finder patterns)
        drawRect(Color.Black, Offset(0f, 0f), androidx.compose.ui.geometry.Size(cellSize * 3, cellSize * 3))
        drawRect(Color.White, Offset(cellSize * 0.5f, cellSize * 0.5f), androidx.compose.ui.geometry.Size(cellSize * 2, cellSize * 2))
        drawRect(Color.Black, Offset(cellSize * 1f, cellSize * 1f), androidx.compose.ui.geometry.Size(cellSize, cellSize))

        drawRect(Color.Black, Offset(size.width - cellSize * 3, 0f), androidx.compose.ui.geometry.Size(cellSize * 3, cellSize * 3))
        drawRect(Color.White, Offset(size.width - cellSize * 2.5f, cellSize * 0.5f), androidx.compose.ui.geometry.Size(cellSize * 2, cellSize * 2))
        drawRect(Color.Black, Offset(size.width - cellSize * 2f, cellSize * 1f), androidx.compose.ui.geometry.Size(cellSize, cellSize))
        
        drawRect(Color.Black, Offset(0f, size.height - cellSize * 3), androidx.compose.ui.geometry.Size(cellSize * 3, cellSize * 3))
        drawRect(Color.White, Offset(cellSize * 0.5f, size.height - cellSize * 2.5f), androidx.compose.ui.geometry.Size(cellSize * 2, cellSize * 2))
        drawRect(Color.Black, Offset(cellSize * 1f, size.height - cellSize * 2f), androidx.compose.ui.geometry.Size(cellSize, cellSize))
    }
}

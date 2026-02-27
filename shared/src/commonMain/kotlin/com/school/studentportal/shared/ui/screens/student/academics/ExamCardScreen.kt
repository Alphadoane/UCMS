package com.school.studentportal.shared.ui.screens.student.academics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.ui.components.AppScaffold
import com.school.studentportal.shared.ui.components.InfoCard

@Composable
fun ExamCardScreen(onBack: () -> Unit = {}) {
    val feeBalance = 0.0 // Mock Cleared
    val examData = listOf(
        mapOf("course_code" to "CS401", "course_name" to "Advanced Algorithms", "venue" to "Lab 4", "exam_date" to "2026-02-10 09:00"),
        mapOf("course_code" to "CS405", "course_name" to "Mobile App Dev", "venue" to "Hall B", "exam_date" to "2026-02-12 11:30")
    )

    if (feeBalance > 0) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Lock, null, modifier = Modifier.size(64.dp), tint = Color(0xFFF57C00))
                Spacer(modifier = Modifier.height(24.dp))
                Text("Exam Card Locked", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFFF57C00))
                Text("Please clear your balance to access your Exam Card.", textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3762)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("OFFICIAL EXAM PASS", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                            Text("Semester 1, 2024/2025", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Fee Cleared", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        Box(modifier = Modifier.size(80.dp).background(Color.White, RoundedCornerShape(8.dp)).padding(4.dp)) {
                            SimulatedQRCode()
                        }
                    }
                }

                Text("Scheduled Exams", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.titleSmall, color = Color.Gray)
                
                LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(examData) { item ->
                        InfoCard(
                            title = "${item["course_code"]} - ${item["course_name"]}",
                            subtitle = "Venue: ${item["venue"]}\nDate: ${item["exam_date"]}",
                            icon = Icons.Default.Event
                        )
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
                if ((i + j) % 3 == 0 || (i * j) % 5 == 0) { // Stable pseudo-random pattern
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(i * cellSize, j * cellSize),
                        size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                    )
                }
            }
        }
        // Corner markers
        drawRect(Color.Black, Offset(0f, 0f), androidx.compose.ui.geometry.Size(cellSize * 3, cellSize * 3))
        drawRect(Color.White, Offset(cellSize * 0.5f, cellSize * 0.5f), androidx.compose.ui.geometry.Size(cellSize * 2, cellSize * 2))
        drawRect(Color.Black, Offset(cellSize * 1f, cellSize * 1f), androidx.compose.ui.geometry.Size(cellSize, cellSize))
    }
}

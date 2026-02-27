package com.example.android.ui.screens.academics

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.android.data.repository.AcademicsRepository
import com.example.android.ui.components.*


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExamAuditScreen(
    // userId: String? = FirebaseAuth.getInstance().currentUser?.uid
) {
    val context = LocalContext.current
    val repository = remember { AcademicsRepository(context) }
    var userId by remember { mutableStateOf<String?>(null) }
    val items = remember { mutableStateListOf<Map<String, Any>>() }
    val error = remember { mutableStateOf<String?>(null) }
    val loading = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val user = repository.getUserProfile("me")
        userId = user?.id

        if (userId != null) {
            try {
                // Fetch mock data or real data if available. For audit, we'll simulate a full transcript here
                loading.value = true
                kotlinx.coroutines.delay(500) // Sim network
                val transcript = listOf(
                    mapOf("year" to "Year 1", "semester" to "Sem 1", "course_code" to "CS101", "grade" to "A", "points" to 4.0),
                    mapOf("year" to "Year 1", "semester" to "Sem 1", "course_code" to "MATH101", "grade" to "B", "points" to 3.0),
                    mapOf("year" to "Year 1", "semester" to "Sem 2", "course_code" to "CS102", "grade" to "A", "points" to 4.0),
                    mapOf("year" to "Year 1", "semester" to "Sem 2", "course_code" to "PHY101", "grade" to "C", "points" to 2.0),
                    
                    mapOf("year" to "Year 2", "semester" to "Sem 1", "course_code" to "CS201", "grade" to "A", "points" to 4.0),
                    mapOf("year" to "Year 2", "semester" to "Sem 1", "course_code" to "STAT201", "grade" to "F", "points" to 0.0), // Retake
                    mapOf("year" to "Year 2", "semester" to "Sem 2", "course_code" to "CS202", "grade" to "B", "points" to 3.0)
                )
                items.addAll(transcript)
            } catch (e: Exception) {
                error.value = e.message
            } finally {
                loading.value = false
            }
        }
    }
    
    // Grouping and Calculations
    val groupedByYear = items.groupBy { it["year"] as String }
    val cumulativePoints = items.sumOf { (it["points"] as Double) }
    val cgpa = if (items.isNotEmpty()) cumulativePoints / items.size else 0.0

    PortalScaffold(title = "Academic Transcript") {
        if (loading.value) {
            LoadingView()
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // CGPA Header
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3762))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Cumulative GPA", color = Color.White.copy(alpha = 0.7f))
                        Text(
                            String.format("%.2f", cgpa),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (items.any { it["grade"] == "F" }) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.background(Color(0xFFF57C00), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Outstanding Retakes", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    groupedByYear.forEach { (year, yearItems) ->
                        stickyHeader {
                            Surface(color = Color(0xFFF5F5F5), modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = year,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                        
                        items(yearItems) { item ->
                            TranscriptItem(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TranscriptItem(item: Map<String, Any>) {
    val grade = item["grade"] as String
    val isFail = grade == "F" || grade == "X" // X = Missing
    
    val color = if (grade == "A") Color(0xFF4CAF50) else if (isFail) Color(0xFFF57C00) else Color.Black

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(item["course_code"] as String, fontWeight = FontWeight.SemiBold)
                Text(item["semester"] as String, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(grade, fontWeight = FontWeight.Bold, color = color, style = MaterialTheme.typography.titleMedium)
                if (isFail) {
                    Text(if (grade == "F") "Retake Required" else "Missing Mark", color = Color(0xFFF57C00), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Divider(color = Color.LightGray.copy(alpha = 0.3f))
    }
}

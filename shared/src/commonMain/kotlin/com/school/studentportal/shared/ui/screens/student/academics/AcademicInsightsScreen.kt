package com.school.studentportal.shared.ui.screens.student.academics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.ui.components.AppScaffold

@Composable
fun AcademicInsightsScreen(onBack: () -> Unit = {}) {
    // Mock Data
    val trend = listOf(3.2f, 3.4f, 3.1f, 3.8f, 3.7f)
    val risk = 0.15f
    val completion = 0.92f

    Column(
        modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().height(250.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3762)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Performance Trajectory", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                    Text("GPA Trend", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    GPAGraph(trend = trend, modifier = Modifier.fillMaxSize())
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AnalyticsScoreCard(value = 1f - risk, color = Color(0xFF4CAF50), label = "Safety", title = "Risk Profile", subtitle = "Low Risk", modifier = Modifier.weight(1f))
                AnalyticsScoreCard(value = completion, color = Color(0xFF1976D2), label = "Prob.", title = "Completion", subtitle = "92% Likely", modifier = Modifier.weight(1f))
            }
            
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, null, tint = Color(0xFFF57C00))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Insights", fontWeight = FontWeight.Bold, color = Color(0xFF1D3762))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("â€¢ Your performance in Algorithms is improving.")
                    Text("â€¢ Focus on Database Systems to maintain your GPA.")
                }
            }
        }
}

@Composable
fun AnalyticsScoreCard(value: Float, color: Color, label: String, title: String, subtitle: String, modifier: Modifier) {
    Card(modifier = modifier.height(180.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(70.dp)) {
                CircularProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxSize(), color = Color.LightGray.copy(alpha = 0.3f), strokeWidth = 6.dp)
                CircularProgressIndicator(progress = { value }, modifier = Modifier.fillMaxSize(), color = color, strokeWidth = 6.dp, strokeCap = StrokeCap.Round)
                Text("${(value * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun GPAGraph(trend: List<Float>, modifier: Modifier = Modifier) {
    if (trend.isEmpty()) return
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / (trend.size - 1).coerceAtLeast(1)
        val path = Path()
        
        trend.forEachIndexed { index, gpa ->
            val x = index * stepX
            val y = height - (gpa / 4.0f * height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(x, y))
        }
        
        drawPath(path = path, color = Color.White, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        
        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()
        drawPath(path = path, brush = Brush.verticalGradient(colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)))
    }
}

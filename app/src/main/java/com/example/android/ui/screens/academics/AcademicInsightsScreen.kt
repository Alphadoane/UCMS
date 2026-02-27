package com.example.android.ui.screens.academics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.android.data.repository.AcademicsRepository
import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.LoadingView
import com.example.android.ui.components.ErrorView // Ensure ErrorView exists or standard composable


@Composable
fun AcademicInsightsScreen(
    // userId: String? = FirebaseAuth.getInstance().currentUser?.uid
) {
    val context = LocalContext.current
    val repository = remember { AcademicsRepository(context) }
    var userId by remember { mutableStateOf<String?>(null) }
    var data by remember { mutableStateOf<Map<String, Any>?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val user = repository.getUserProfile("me")
        userId = user?.id
        
        // Fetch "Intense" Data
        data = repository.getInsights(userId ?: "")
        loading = false
    }

    PortalScaffold(title = "Learning Analytics") {
        if (loading) {
            LoadingView()
        } else if (data == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data available") }
        } else {
            val trend = (data!!["gpa_trend"] as? List<*>)?.map { (it as Number).toFloat() } ?: emptyList()
            val risk = (data!!["risk_score"] as Number).toFloat()
            val completion = (data!!["completion_probability"] as Number).toFloat()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. GPA Trend Graph (Intense Visual)
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
                
                // 2. Risk & Completion (Side by Side)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Risk Card
                    Card(modifier = Modifier.weight(1f).height(180.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize().padding(8.dp)) {
                            CircularIndicator(value = 1f - risk, color = if(risk < 0.3) Color(0xFF4CAF50) else Color(0xFFF57C00), label = "Safety")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Risk Profile", fontWeight = FontWeight.Bold)
                            Text(if(risk < 0.3) "Low Risk" else "High Risk", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    
                    // Completion Card
                    Card(modifier = Modifier.weight(1f).height(180.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize().padding(8.dp)) {
                            CircularIndicator(value = completion, color = Color(0xFF1976D2), label = "Prob.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Completion", fontWeight = FontWeight.Bold)
                            Text("${(completion * 100).toInt()}% Likely", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                // 3. AI Recommendations
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFF57C00))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Insights", fontWeight = FontWeight.Bold, color = Color(0xFF1D3762))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val recs = (data!!["recommendations"] as? List<*>) ?: emptyList<Any>()
                        recs.forEach { 
                            Text("• $it", modifier = Modifier.padding(bottom = 4.dp), style = MaterialTheme.typography.bodyMedium) 
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GPAGraph(trend: List<Float>, modifier: Modifier = Modifier) {
    if (trend.isEmpty()) return
    val maxGPA = 4.0f
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / (trend.size - 1).coerceAtLeast(1)
        
        val path = Path()
        
        trend.forEachIndexed { index, gpa ->
            val x = index * stepX
            val y = height - (gpa / maxGPA * height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            
            // Draw Point
            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(x, y))
        }
        
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Gradient Fill area
        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
            )
        )
    }
}

@Composable
fun CircularIndicator(value: Float, color: Color, label: String) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = Color.LightGray.copy(alpha = 0.3f),
            strokeWidth = 8.dp
        )
        CircularProgressIndicator(
            progress = { value },
            modifier = Modifier.fillMaxSize(),
            color = color,
            strokeWidth = 8.dp,
            strokeCap = StrokeCap.Round
        )
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}


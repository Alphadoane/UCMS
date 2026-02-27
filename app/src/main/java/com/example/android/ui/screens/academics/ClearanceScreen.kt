package com.example.android.ui.screens.academics

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.android.data.repository.AcademicsRepository
import com.example.android.ui.components.*


@Composable
fun ClearanceScreen(
    // userId: String? = FirebaseAuth.getInstance().currentUser?.uid
) {
    val context = LocalContext.current
    val repository = remember { AcademicsRepository(context) }
    var userId by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        val user = repository.getUserProfile("me")
        userId = user?.id
    }

    // Mock Steps (In real app, fetch status for each)
    val steps = remember { 
        listOf(
            ClearanceStep("Library", "Return all borrowed books", true),
            ClearanceStep("Sports Department", "Return outcome kits", true),
            ClearanceStep("Finance", "Clear outstanding fee balance", false), // Blocked here
            ClearanceStep("HOD", "Final academic approval", false),
            ClearanceStep("Registrar", "Certificate issuance", false)
        )
    }

    PortalScaffold(title = "Clearance Workflow") {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                "Graduation Clearance 2024",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn {
                itemsIndexed(steps) { index, step ->
                    StepItem(
                        step = step, 
                        isLast = index == steps.size - 1, 
                        isActive = !step.isCleared && (index == 0 || steps[index-1].isCleared)
                    )
                }
            }
        }
    }
}

data class ClearanceStep(val title: String, val description: String, val isCleared: Boolean)

@Composable
fun StepItem(step: ClearanceStep, isLast: Boolean, isActive: Boolean) {
    val color by animateColorAsState(
        if (step.isCleared) Color(0xFF4CAF50) else if (isActive) Color(0xFF1D3762) else Color.Gray
    )

    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        // Timeline Line
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(40.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .background(color, CircleShape)
            ) {
                if (step.isCleared) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                } else if (isActive) {
                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(color.copy(alpha = 0.5f))
                )
            }
        }

        // Content
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(step.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(step.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            
            if (isActive) {
                Button(
                    onClick = { /* Trigger clearance request */ },
                    modifier = Modifier.padding(top = 8.dp).height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3762)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text("Request Clearance", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

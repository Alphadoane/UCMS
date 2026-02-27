package com.example.android.ui.screens.academics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.AcademicsRepository
import com.example.android.ui.components.*

import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicLeaveScreen(
    // userId: String? = FirebaseAuth.getInstance().currentUser?.uid
) {
    val context = LocalContext.current
    val repository = remember { AcademicsRepository(context) }
    var userId by remember { mutableStateOf<String?>(null) }
    var isFormVisible by remember { mutableStateOf(false) }
    val leaveHistory = remember { mutableStateListOf<Map<String, Any>>() }
    val scope = rememberCoroutineScope()
    
    // Mock History
    LaunchedEffect(Unit) {
        val user = repository.getUserProfile("me")
        userId = user?.id

        leaveHistory.add(mapOf(
            "reason" to "Medical Leave",
            "start_date" to "2024-02-10",
            "end_date" to "2024-02-14",
            "status" to "Approved"
        ))
    }

    PortalScaffold(
        title = "Academic Leave",
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isFormVisible = true },
                containerColor = Color(0xFF1D3762),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Request")
            }
        }
    ) {
        if (isFormVisible) {
            LeaveRequestForm(onDismiss = { isFormVisible = false })
        } else {
            if (leaveHistory.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No leave history found.", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item { Text("History", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp)) }
                    items(leaveHistory) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(item["reason"] as String, style = MaterialTheme.typography.titleSmall)
                                    Text("${item["start_date"]} to ${item["end_date"]}", style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                val status = item["status"] as String
                                SuggestionChip(
                                    onClick = {}, 
                                    label = { Text(status) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        labelColor = if(status=="Approved") Color(0xFF2E7D32) else Color(0xFFE65100)
                                    )
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
fun LeaveRequestForm(onDismiss: () -> Unit) {
    var reason by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    
    val reasons = listOf("Medical", "Deferment", "Personal", "Official Duty")
    var selectedReason by remember { mutableStateOf(reasons[0]) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Request Academic Leave", style = MaterialTheme.typography.headlineSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Reason Type")
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            reasons.take(2).forEach { 
                FilterChip(selected = selectedReason == it, onClick = { selectedReason = it }, label = { Text(it) })
            }
        }
        
        OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("Start Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = endDate,
            onValueChange = { endDate = it },
            label = { Text("End Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text("Detailed Explanation") },
            modifier = Modifier.fillMaxWidth().height(120.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("Cancel") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onDismiss, // Mock submit
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3762))
            ) {
                Text("Submit Request")
            }
        }
    }
}

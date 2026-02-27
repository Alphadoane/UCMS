package com.school.studentportal.shared.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.ui.components.DashboardCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBroadcastScreen(
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var targetAudience by remember { mutableStateOf("ALL") } // ALL, STUDENTS, STAFF
    var isSending by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Communication Hub", style = MaterialTheme.typography.headlineSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            
            DashboardCard(title = "Compose Broadcast", icon = Icons.Default.Send, color = Color(0xFFE91E63)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Subject / Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Message Body") },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    maxLines = 10
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Target Audience Selector
                Text("Target Audience:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = targetAudience == "ALL",
                        onClick = { targetAudience = "ALL" },
                        label = { Text("All Users") }
                    )
                    FilterChip(
                        selected = targetAudience == "STUDENT",
                        onClick = { targetAudience = "STUDENT" },
                        label = { Text("Students") }
                    )
                    FilterChip(
                        selected = targetAudience == "STAFF",
                        onClick = { targetAudience = "STAFF" },
                        label = { Text("Staff") }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        if (title.isBlank() || body.isBlank()) return@Button
                        isSending = true
                        scope.launch {
                            delay(1000) // Mock sending
                            isSending = false
                            snackbarHostState.showSnackbar("Broadcast Sent Successfully")
                            title = ""
                            body = ""
                        }
                    },
                    enabled = !isSending,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSending) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Send Broadcast")
                    }
                }
            }
        }
        SnackbarHost(snackbarHostState, modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter))
    }
}

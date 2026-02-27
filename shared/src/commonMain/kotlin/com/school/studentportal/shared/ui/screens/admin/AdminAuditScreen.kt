package com.school.studentportal.shared.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun AdminAuditScreen(
    onNavigateBack: () -> Unit
) {
    var logs by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(600)
        logs = listOf(
            mapOf("action" to "USER_LOGIN", "details" to "User STU001 logged in successfully", "userId" to "STU001"),
            mapOf("action" to "BROADCAST_SENT", "details" to "Broadcast 'Exam Dates' sent to ALL", "userId" to "ADM001"),
            mapOf("action" to "COURSE_ALLOCATION", "details" to "Allocated CS101 to EMP001", "userId" to "ADM001"),
            mapOf("action" to "PAYMENT_VERIFIED", "details" to "Verified payment TXN002", "userId" to "ADM001")
        )
        loading = false
    }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Column {
            Text("System Audit Logs", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            if (loading) {
                 Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (logs.isEmpty()) {
                        item { Text("No audit logs found.") }
                    }

                    items(logs) { log ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ListAlt, null, tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = log["action"] as? String ?: "ACTION",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha=0.2f))
                                Text(log["details"] as? String ?: "", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "User: ${log["userId"]}", 
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

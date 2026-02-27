package com.example.android.ui.screens.support

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.SupportRepository
import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.DashboardCard
import kotlinx.coroutines.launch

@Composable
fun SupportTicketScreen(
    repository: SupportRepository = remember { SupportRepository() },
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var tickets by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var filter by remember { mutableStateOf("Open") } // Open, Resolved

    var adminId by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        val me = repository.getUserProfile("me")
        adminId = me?.id ?: ""
    }

    LaunchedEffect(filter) {
        loading = true
        tickets = repository.getSupportTickets(if (filter == "All") null else filter)
        loading = false
    }

    PortalScaffold(title = "Help Desk Tickets") {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Filter Tabs
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                FilterChip(selected = filter == "Open", onClick = { filter = "Open" }, label = { Text("Open") })
                FilterChip(selected = filter == "Resolved", onClick = { filter = "Resolved" }, label = { Text("Resolved") })
                FilterChip(selected = filter == "All", onClick = { filter = "All" }, label = { Text("All") })
            }

            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (tickets.isEmpty()) {
                        item { Text("No tickets found matching '$filter'.") }
                    }

                    items(tickets) { ticket ->
                        val status = ticket["status"] as? String ?: "Open"
                        val isResolved = status == "Resolved"
                        
                        DashboardCard(
                            title = ticket["title"] as? String ?: "No Subject",
                            icon = if (isResolved) Icons.Default.Check else Icons.Default.Warning,
                            color = if (isResolved) Color(0xFF2E7D32) else Color(0xFFFF9800)
                        ) {
                            Text(ticket["description"] as? String ?: "No description", style = MaterialTheme.typography.bodyMedium)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Text("User: ${ticket["userId"]}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("Date: ${ticket["createdAt"]}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            
                            if (!isResolved) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            repository.updateTicketStatus(ticket["id"] as String, "Resolved")
                                            tickets = repository.getSupportTickets(filter) // Refresh
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                ) {
                                    Text("Mark Resolved")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

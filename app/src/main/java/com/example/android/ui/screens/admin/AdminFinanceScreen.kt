package com.example.android.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.AdminRepository
import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.DashboardCard
import com.example.android.ui.components.InfoRow
import kotlinx.coroutines.launch

@Composable
fun AdminFinanceScreen(
    repository: AdminRepository = remember { AdminRepository() },
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var transactions by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        transactions = repository.getAllTransactions()
        loading = false
    }

    PortalScaffold(title = "Finance Oversight") {
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (transactions.isEmpty()) {
                    item { Text("No recent transactions found.") }
                }

                items(transactions) { txn ->
                    val status = txn["status"] as? String ?: "Pending"
                    val isVerified = status == "Verified"
                    
                    DashboardCard(
                        title = "TXN: ${(txn["amount"] as? Number)?.toDouble() ?: 0.0}",
                        icon = Icons.Default.AttachMoney,
                        color = if (isVerified) Color(0xFF2E7D32) else Color(0xFFF57C00)
                    ) {
                        InfoRow("User ID", txn["userId"] as? String ?: "Unknown")
                        InfoRow("Method", txn["method"] as? String ?: "-")
                        InfoRow("Ref Code", txn["ref"] as? String ?: "-")
                        
                        // Action
                        if (!isVerified) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val id = txn["id"] as? String ?: return@launch
                                        repository.verifyTransaction(id, "admin_current") // TODO: real admin ID
                                        transactions = repository.getAllTransactions() // Refresh
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verify Payment")
                            }
                        } else {
                             Text("Status: Verified", color = Color(0xFF2E7D32), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.padding(top=8.dp))
                        }
                    }
                }
            }
        }
    }
}

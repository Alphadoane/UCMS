package com.example.android.ui.screens.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.FinanceRepository
import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.LoadingView

@Composable
fun ReceiptsScreen(
    repository: FinanceRepository = remember { FinanceRepository() }
) {
    var receipts by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var userId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val me = repository.getUserProfile("me")
        userId = me?.id
    }

    LaunchedEffect(userId) {
        val currentUserId = userId
        if (currentUserId != null) {
            val data = repository.getReceipts(currentUserId)
            receipts = data
        }
        loading = false
    }

    PortalScaffold(title = "Transaction History") {
        if (loading) {
            LoadingView()
        } else if (receipts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions found", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(receipts) { receipt ->
                    ReceiptItem(receipt)
                }
            }
        }
    }
}

@Composable
fun ReceiptItem(receipt: Map<String, Any>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFE3F2FD), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = Color(0xFF1D3762))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(receipt["method"] as String, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(receipt["date"] as String, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("Ref: ${receipt["ref"]}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            
            Text(
                "KES ${String.format("%,.0f", (receipt["amount"] as Number).toDouble())}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
        }
    }
}

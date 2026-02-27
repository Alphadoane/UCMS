package com.school.studentportal.shared.ui.screens.student.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.ui.components.AppScaffold

import com.school.studentportal.shared.data.repository.FinanceRepository
import com.school.studentportal.shared.data.model.FinanceTransaction

@Composable
fun ReceiptsScreen(
    repository: FinanceRepository,
    onBack: () -> Unit = {}
) {
    var transactions by remember { mutableStateOf<List<FinanceTransaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repository.getReceipts().onSuccess {
            transactions = it.transactions
            isLoading = false
        }.onFailure {
            isLoading = false
        }
    }

    AppScaffold(
        title = "Receipt History",
        showTopBar = false,
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
    ) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1D3762))
            }
        } else if (transactions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions found", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(transactions) { transaction ->
                    ReceiptItem(transaction)
                }
            }
        }
    }
}

@Composable
fun ReceiptItem(transaction: FinanceTransaction) {
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
                Text(transaction.payment_method, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(transaction.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("Ref: ${transaction.receipt.ifBlank { transaction.reference }}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            
            Text(
                "KES ${String.format("%.0f", transaction.amount)}",
                fontWeight = FontWeight.Bold,
                color = if (transaction.status == "SUCCESS") Color(0xFF2E7D32) else Color.Red
            )
        }
    }
}

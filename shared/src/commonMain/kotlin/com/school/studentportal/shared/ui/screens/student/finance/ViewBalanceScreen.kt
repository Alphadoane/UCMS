package com.school.studentportal.shared.ui.screens.student.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.ui.components.AppScaffold
import com.school.studentportal.shared.data.repository.FinanceRepository

@Composable
fun ViewBalanceScreen(
    repository: FinanceRepository,
    onBack: () -> Unit = {},
    onPayNow: () -> Unit = {}
) {
    var balance by remember { mutableStateOf(0.0) }
    var billed by remember { mutableStateOf(0.0) }
    var paid by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    
    val MIN_BALANCE_THRESHOLD = 5000.0
    val isAccessGranted = balance <= MIN_BALANCE_THRESHOLD

    LaunchedEffect(Unit) {
        repository.getFeeBalance().onSuccess {
            balance = it.balance
            billed = it.total_billed ?: 0.0
            paid = it.total_paid ?: 0.0
            isLoading = false
        }.onFailure {
            isLoading = false
        }
    }

    AppScaffold(
        title = "Financial Status",
        showTopBar = false,
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
    ) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1D3762))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Balance Card
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3762))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Current Balance", color = Color.White.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "KES ${String.format("%.2f", balance)}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                // Detailed Breakdown
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FinanceDetailCard("Total Billed", billed, Color(0xFF424242), Modifier.weight(1f))
                    FinanceDetailCard("Total Paid", paid, Color(0xFF2E7D32), Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Access Control Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAccessGranted) Color(0xFFE8F5E9) else Color(0xFFECEFF1)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isAccessGranted) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isAccessGranted) Color(0xFF2E7D32) else Color(0xFFF57C00)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Virtual Campus Access",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (isAccessGranted) 
                                "You have full access to library and virtual campus resources." 
                            else 
                                "Access Restricted. Your balance exceeds the allowed limit of KES ${String.format("%.0f", MIN_BALANCE_THRESHOLD)}.",
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                        
                        if (!isAccessGranted) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onPayNow,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64)),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Pay Now")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinanceDetailCard(title: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "KES ${String.format("%.0f", amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

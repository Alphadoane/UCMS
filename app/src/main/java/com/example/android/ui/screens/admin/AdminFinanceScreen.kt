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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch

@Composable
fun AdminFinanceScreen(
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { AdminRepository(context) }
    val scope = rememberCoroutineScope()
    
    var searchQuery by remember { mutableStateOf("") }
    var students by remember { mutableStateOf<List<com.school.studentportal.shared.data.model.StudentFinanceDto>>(emptyList()) }
    var selectedStudentTransactions by remember { mutableStateOf<com.school.studentportal.shared.data.model.StudentTransactionsResponse?>(null) }
    var loading by remember { mutableStateOf(false) }
    var showTxnDialog by remember { mutableStateOf(false) }

    val loadStudents = {
        scope.launch {
            loading = true
            val res = repository.getFinanceStudents(searchQuery.ifBlank { null })
            res.onSuccess { students = it }
            loading = false
        }
    }

    LaunchedEffect(searchQuery) {
        // Debounce search if needed, but for now simple trigger
        loadStudents()
    }

    PortalScaffold(title = "Finance Oversight") {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                placeholder = { Text("Search student by name or admission number") },
                leadingIcon = { Icon(androidx.compose.material.icons.Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp)
            )

            if (loading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (students.isEmpty()) {
                        item { Text("No students found.", modifier = Modifier.padding(16.dp)) }
                    }

                    items(students) { student ->
                        StudentFinanceCard(student) {
                            scope.launch {
                                val txnRes = repository.getStudentTransactions(student.id)
                                txnRes.onSuccess {
                                    selectedStudentTransactions = it
                                    showTxnDialog = true
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showTxnDialog && selectedStudentTransactions != null) {
            TransactionHistoryDialog(
                response = selectedStudentTransactions!!,
                onDismiss = { showTxnDialog = false }
            )
        }
    }
}

@Composable
fun StudentFinanceCard(
    student: com.school.studentportal.shared.data.model.StudentFinanceDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(student.full_name, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(student.admission_number, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(
                    text = "KES ${student.balance}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (student.balance > 0) Color(0xFFC62828) else Color(0xFF2E7D32)
                )
                Text("Outstanding Balance", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun TransactionHistoryDialog(
    response: com.school.studentportal.shared.data.model.StudentTransactionsResponse,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Transactions: ${response.student_name}")
                Text(response.admission_number, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        },
        text = {
            Box(Modifier.heightIn(max = 400.dp)) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (response.transactions.isEmpty()) {
                        item { Text("No transactions found.") }
                    }
                    items(response.transactions) { txn ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("KES ${txn.amount}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    StatusChip(txn.status)
                                }
                                Text("Ref: ${txn.reference}", style = MaterialTheme.typography.bodySmall)
                                Text("Date: ${txn.date}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text("Method: ${txn.payment_method}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun StatusChip(status: String) {
    val (color, bgColor) = when (status.uppercase()) {
        "SUCCESS" -> Color(0xFF2E7D32) to Color(0xFFE8F5E9)
        "PENDING" -> Color(0xFFF57C00) to Color(0xFFFFF3E0)
        "FAILED" -> Color(0xFFC62828) to Color(0xFFFFEBEE)
        else -> Color.Gray to Color(0xFFF5F5F5)
    }
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

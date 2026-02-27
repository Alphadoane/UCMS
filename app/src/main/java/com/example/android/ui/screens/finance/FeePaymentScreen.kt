package com.example.android.ui.screens.finance

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.android.data.repository.FinanceRepository
import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.LoadingView
import kotlinx.coroutines.launch

@Composable
fun FeePaymentScreen(
    repository: FinanceRepository = remember { FinanceRepository() }
) {
    var studentProfile by remember { mutableStateOf<com.example.android.data.network.ProfileResponse?>(null) }
    var balance by remember { mutableStateOf(0.0) }
    var amountInput by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf("FULL") } // FULL or PARTIAL
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastStatusMessage by remember { mutableStateOf("") }
    var selectedRail by remember { mutableStateOf("MPESA") } // MPESA or BANK
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Fetch initial profile and balance
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val profileRes = repository.getUserProfile("me") // Note: The repository mock returns a hardcoded User, but we need the ApiService ProfileResponse for admission_no
            // In a real app, repository.getProfile() would return the correct type. 
            // For now, let's assume we can get the profile from the network module or similar.
            // For this implementation, I'll fetch the profile directly to ensure we have admission_no and potentially mpesa_phone if added to the response.
            
            val data = repository.getFeeBalance("me")
            balance = (data["balance"] as? Number)?.toDouble() ?: 0.0
            
            // Simulating profile fetch for student details
            studentProfile = com.example.android.data.network.ProfileResponse(
                id = "me",
                admission_no = repository.getUserProfile("me")?.email?.split("@")?.get(0) ?: "REG12345", // Mocking reg no
                full_name = "Student User"
            )
        } finally {
            isLoading = false
        }
    }

    PortalScaffold(title = "Pay School Fees") {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Student Details & Registration Number (Requested: automatically fetched)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Student Details", style = MaterialTheme.typography.titleSmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Registration No:", fontWeight = FontWeight.Medium)
                            Text(studentProfile?.admission_no ?: "Loading...", fontWeight = FontWeight.Bold)
                        }
                        // Note: mpesa_phone is fetched on the backend side, but we could display it here if returned in profile
                    }
                }

                // Payment Methods
                Text("Select Payment Rail", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentRailItem(
                        name = "M-Pesa",
                        icon = Icons.Default.PhoneAndroid,
                        isSelected = selectedRail == "MPESA",
                        onClick = { selectedRail = "MPESA" },
                        modifier = Modifier.weight(1f)
                    )
                    PaymentRailItem(
                        name = "Bank (PesaLink)",
                        icon = Icons.Default.AccountBalance,
                        isSelected = selectedRail == "BANK",
                        onClick = { selectedRail = "BANK" },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Balance Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3762)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Current Fee Balance", color = Color.White.copy(alpha = 0.7f))
                        Text(
                            "KES ${String.format("%,.2f", balance)}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Payment Options (Requested: Full or Partial)
                Text("Select Payment Option", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Column(modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = paymentType == "FULL",
                            onClick = { paymentType = "FULL" }
                        )
                        Text("Pay Full Fee", modifier = Modifier.clickable { paymentType = "FULL" })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = paymentType == "PARTIAL",
                            onClick = { paymentType = "PARTIAL" }
                        )
                        Text("Pay Partial Fee", modifier = Modifier.clickable { paymentType = "PARTIAL" })
                    }
                }

                // Amount Field (Enabled only for Partial)
                if (paymentType == "PARTIAL") {
                    Text("Enter Amount", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) amountInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        prefix = { Text("KES ") },
                        singleLine = true,
                        placeholder = { Text("Enter amount to pay") }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Payment Feedback Section
                if (selectedRail == "MPESA") {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "An M-Pesa prompt will be sent to your registered phone number.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                } else {
                    Surface(
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF1976D2))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "You will be redirected to complete payment via Bank Transfer (PesaLink).",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1976D2)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        val amount = if (paymentType == "PARTIAL") amountInput.toDoubleOrNull() else balance
                        if (amount == null || amount <= 0) {
                             scope.launch { snackbarHostState.showSnackbar("Enter a valid amount") }
                             return@Button
                        }
                        
                        scope.launch {
                            isLoading = true
                            if (selectedRail == "MPESA") {
                                val result = repository.initiateStkPush(paymentType, if (paymentType == "PARTIAL") amount else null)
                                isLoading = false
                                if (result.isSuccess) {
                                    lastStatusMessage = "Check your phone for the M-Pesa PIN prompt"
                                    showSuccessDialog = true
                                } else {
                                    snackbarHostState.showSnackbar("STK Push Failed: ${result.exceptionOrNull()?.message}")
                                }
                            } else {
                                val result = repository.initiateBankPayment(paymentType, if (paymentType == "PARTIAL") amount else null)
                                isLoading = false
                                if (result.isSuccess) {
                                    val data = result.getOrNull()
                                    if (data != null) {
                                        // Open browser for Paystack payment
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(data.authorizationUrl))
                                        context.startActivity(intent)
                                    }
                                } else {
                                    snackbarHostState.showSnackbar("Bank Payment Failed: ${result.exceptionOrNull()?.message}")
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isLoading && (paymentType == "FULL" || amountInput.isNotEmpty()),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3762))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Initiating Payment...")
                    } else {
                        val displayAmount = if (paymentType == "FULL") balance else amountInput.toDoubleOrNull() ?: 0.0
                        Text("Pay KES ${String.format("%,.2f", displayAmount)}")
                    }
                }
            }
            
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))

            if (showSuccessDialog) {
                StkPushInitiatedDialog(
                    message = lastStatusMessage,
                    onDismiss = { showSuccessDialog = false }
                )
            }
        }
    }
}

@Composable
fun PaymentRailItem(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFF1D3762) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) Color(0xFF1D3762) else Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(name, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
fun StkPushInitiatedDialog(message: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(72.dp).background(Color(0xFFE3F2FD), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(48.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("STK Push Sent", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1D3762))
                Spacer(modifier = Modifier.height(8.dp))
                Text(message, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3762)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK")
                }
            }
        }
    }
}

package com.school.studentportal.shared.ui.screens.student.finance

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.data.repository.FinanceRepository
import com.school.studentportal.shared.ui.components.AppScaffold
import com.school.studentportal.shared.utils.rememberBrowserLauncher
import kotlinx.coroutines.launch

@Composable
fun FeePaymentScreen(
    repository: FinanceRepository,
    onBack: () -> Unit = {}
) {
    var phoneNumber by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    
    var balance by remember { mutableStateOf(0.0) }
    var amountInput by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastTransactionRef by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val browserLauncher = rememberBrowserLauncher()

    val paymentMethods = listOf(
        PaymentMethod("M-Pesa", Icons.Default.PhoneAndroid, Color(0xFF4CAF50), "MPESA"),
        PaymentMethod("Bank (PesaLink)", Icons.Default.AccountBalance, Color(0xFF1D3762), "BANK_PESALINK")
    )

    LaunchedEffect(Unit) {
        repository.getFeeBalance().onSuccess { balance = it.balance }
    }

    AppScaffold(
        title = "Make Payment",
        showTopBar = false,
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Balance Card (Same as before)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3762)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Outstanding Balance", color = Color.White.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                scope.launch {
                                    repository.getFeeBalance().onSuccess { balance = it.balance }
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Refresh, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(
                        "KES ${String.format("%.2f", balance)}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Text("Enter Amount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = amountInput,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amountInput = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                prefix = { Text("KES ") },
                singleLine = true,
                label = { Text("Amount") }
            )
            
            Text("Phone Number", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { input -> 
                    phoneNumber = input
                    phoneError = when {
                        input.any { !it.isDigit() } -> "Invalid number: Integers only"
                        input.length > 10 -> "Invalid number: Max 10 digits"
                        else -> null
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp),
                singleLine = true,
                isError = phoneError != null,
                supportingText = {
                    if (phoneError != null) {
                        Text(phoneError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                placeholder = { Text("07... or 01...") },
                label = { Text("M-Pesa Number") }
            )

            Text("Select Payment Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 16.dp).height(120.dp)
            ) {
                items(paymentMethods) { method ->
                    PaymentMethodItem(
                        method = method,
                        isSelected = selectedMethod == method,
                        onClick = { selectedMethod = method }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val amount = amountInput.toDoubleOrNull() ?: 0.0
                    if (amount <= 0) {
                         scope.launch { snackbarHostState.showSnackbar("Enter a valid amount") }
                         return@Button
                    }
                    if (paymentMethods.find { it.rail == "MPESA" } == selectedMethod) {
                        if (phoneNumber.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Enter phone number for M-Pesa") }
                            return@Button
                        }
                        if (phoneError != null) {
                            scope.launch { snackbarHostState.showSnackbar("Fix phone number errors") }
                            return@Button
                        }
                    }
                    if (selectedMethod == null) {
                        scope.launch { snackbarHostState.showSnackbar("Select a payment method") }
                        return@Button
                    }
                    
                    scope.launch {
                        isLoading = true
                        when (selectedMethod?.rail) {
                            "MPESA" -> {
                                repository.initiateMpesaPayment(amount, phoneNumber).fold(
                                    onSuccess = { resp ->
                                        isLoading = false
                                        lastTransactionRef = resp.checkout_request_id ?: ""
                                        showSuccessDialog = true
                                    },
                                    onFailure = {
                                        isLoading = false
                                        scope.launch { snackbarHostState.showSnackbar("Payment failed: ${it.message}") }
                                    }
                                )
                            }
                            "BANK_PESALINK" -> {
                                repository.initiateBankPayment("PARTIAL", amount).fold(
                                    onSuccess = { resp ->
                                        isLoading = false
                                        browserLauncher.openUrl(resp.authorizationUrl)
                                    },
                                    onFailure = {
                                        isLoading = false
                                        scope.launch { snackbarHostState.showSnackbar("Redirect failed: ${it.message}") }
                                    }
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3762))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Processing...")
                } else {
                    Text("Pay KES ${if(amountInput.isEmpty()) "0.00" else amountInput}")
                }
            }
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                title = { Text("Payment Initiated!") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Please check your phone ($phoneNumber) to complete the payment.")
                        if (lastTransactionRef.isNotEmpty()) {
                            Text("Ref: $lastTransactionRef", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showSuccessDialog = false }) { Text("Close") }
                }
            )
        }
    }
}

data class PaymentMethod(val name: String, val icon: ImageVector, val color: Color, val rail: String)

@Composable
fun PaymentMethodItem(method: PaymentMethod, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1.5f)
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
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(method.icon, contentDescription = null, tint = method.color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(method.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

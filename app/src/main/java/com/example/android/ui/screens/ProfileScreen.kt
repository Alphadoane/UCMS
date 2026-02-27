package com.example.android.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.ProfileRepository
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val context: Context = LocalContext.current
    val repository = remember(context) { ProfileRepository(context) }
    val scope = rememberCoroutineScope()
    val profile by repository.observeProfile().collectAsState(initial = null)
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading.value = true
        errorMessage.value = null
        val result = repository.refreshProfile()
        errorMessage.value = result.exceptionOrNull()?.message
        isLoading.value = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(text = "Profile")

        if (isLoading.value && profile == null) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }

        errorMessage.value?.let { msg ->
            Text(text = msg, modifier = Modifier.padding(top = 8.dp))
        }

        profile?.let { p ->
            val regLabel = if (p.role == "STAFF") "Employee ID" else "Registration Number"
            Text(text = "$regLabel: ${p.registrationNumber ?: "N/A"}", modifier = Modifier.padding(top = 16.dp))
            Text(text = "Full Name: ${p.fullName ?: "N/A"}")
            Text(text = "Email: ${p.email ?: "N/A"}")
            Text(text = "Role: ${p.role}")
            p.department?.let { 
                val deptLabel = if (p.role == "STAFF") "Department" else "Course"
                Text(text = "$deptLabel: $it") 
            }
        }

        Button(
            onClick = {
                scope.launch {
                    isLoading.value = true
                    errorMessage.value = null
                    val result = repository.refreshProfile()
                    errorMessage.value = result.exceptionOrNull()?.message
                    isLoading.value = false
                }
            },
            modifier = Modifier.padding(top = 16.dp),
            enabled = !isLoading.value
        ) {
            Text(if (isLoading.value) "Refreshing..." else "Refresh Profile")
        }

        // Change Password Logic
        val authRepo = remember(context) { com.example.android.data.repository.AuthRepository(context) }
        var showPasswordDialog by remember { mutableStateOf(false) }
        var oldPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var isChanging by remember { mutableStateOf(false) }
        
        Button(
            onClick = { showPasswordDialog = true },
            modifier = Modifier.padding(top = 16.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF673AB7))
        ) {
            Text("Change Password")
        }

        if (showPasswordDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showPasswordDialog = false },
                title = { Text("Change Password") },
                text = {
                    Column {
                        androidx.compose.material3.OutlinedTextField(
                            value = oldPassword, 
                            onValueChange = { oldPassword = it },
                            label = { Text("Old Password") },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                        )
                        androidx.compose.material3.OutlinedTextField(
                            value = newPassword, 
                            onValueChange = { newPassword = it },
                            label = { Text("New Password") },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isChanging = true
                            scope.launch {
                                val result = authRepo.changePassword(oldPassword, newPassword)
                                isChanging = false
                                if (result.isSuccess) {
                                    android.widget.Toast.makeText(context, "Password Changed Successfully", android.widget.Toast.LENGTH_SHORT).show()
                                    showPasswordDialog = false
                                    oldPassword = ""
                                    newPassword = ""
                                } else {
                                    android.widget.Toast.makeText(context, "Failed: ${result.exceptionOrNull()?.message}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !isChanging && oldPassword.isNotBlank() && newPassword.isNotBlank()
                    ) {
                        if (isChanging) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("Change")
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showPasswordDialog = false }) { Text("Cancel") }
                }
            )
        }

        Button(onClick = onLogout, modifier = Modifier.padding(top = 16.dp)) { 
            Text("Logout") 
        }
    }
}

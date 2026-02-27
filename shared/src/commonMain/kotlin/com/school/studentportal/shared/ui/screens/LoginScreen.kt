package com.school.studentportal.shared.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.school.studentportal.shared.data.model.AdmissionApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import com.school.studentportal.shared.generated.resources.Res
import com.school.studentportal.shared.generated.resources.kca_logo
import com.school.studentportal.shared.generated.resources.login_background

@Composable
fun LoginScreen(
    onLogin: (String, String, (String) -> Unit, () -> Unit) -> Unit,
    onSignUp: (String, String, String, String?, (String) -> Unit, () -> Unit) -> Unit,
    onRequestPasswordReset: (String, (Result<String>) -> Unit) -> Unit,
    onVerifyPasswordReset: (String, String, String, (Result<String>) -> Unit) -> Unit,
    onCheckApplicationStatus: (String, (Result<AdmissionApplication?>) -> Unit) -> Unit
) {
    var isSignUpMode by rememberSaveable { mutableStateOf(false) }
    var showTrackDialog by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var fullName by rememberSaveable { mutableStateOf("") }
    var admissionNo by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var showForgotPasswordDialog by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(Res.drawable.login_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(modifier = Modifier.fillMaxSize()) {}

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Surface(
                color = Color.Transparent, 
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth() 
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Advancing Knowledge, Driving Change",
                        color = MaterialTheme.colorScheme.primary, 
                        fontSize = 22.sp, 
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Surface(
                        color = Color.Transparent, 
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.padding(top = 24.dp)
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.kca_logo),
                            contentDescription = "KCA University Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(180.dp)
                                .padding(16.dp)
                        )
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(
                    bottomStart = 24.dp,
                    bottomEnd = 24.dp,
                    topStart = 0.dp,
                    topEnd = 0.dp
                ),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .widthIn(max = 480.dp) // Constrain width FIRST
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(32.dp)) { 
                    Text(
                        text = if (isSignUpMode) "Create Account" else "User Login",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = {
                                fullName = it
                                errorMessage = ""
                            },
                            label = { Text("Full Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            enabled = !isLoading,
                        )
                        OutlinedTextField(
                            value = admissionNo,
                            onValueChange = {
                                admissionNo = it
                                errorMessage = ""
                            },
                            label = { Text("Admission Number (Optional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            enabled = !isLoading,
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = "" 
                        },
                        label = { Text("Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        enabled = !isLoading,
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = "" 
                        },
                        label = { Text("Password") },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        enabled = !isLoading,
                    )

                    // Error message
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (isSignUpMode) {
                                // Simple validation
                                if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
                                    errorMessage = "Please fill all required fields"
                                }  else if (password.length < 6) {
                                    errorMessage = "Password must be at least 6 characters"
                                } else {
                                    isLoading = true
                                    errorMessage = ""
                                    onSignUp(
                                        email,
                                        password,
                                        fullName,
                                        admissionNo.takeIf { it.isNotBlank() },
                                        { error ->
                                            errorMessage = error
                                            isLoading = false
                                        },
                                        {
                                            isLoading = false
                                        })
                                }
                            } else {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Please enter both email and password"
                                } else {
                                    isLoading = true
                                    errorMessage = ""
                                    onLogin(email, password, { error ->
                                        errorMessage = error
                                        isLoading = false
                                    }, {
                                        isLoading = false
                                    })
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(if (isSignUpMode) "SIGN UP" else "SIGN IN")
                        }
                    }

                    if (!isSignUpMode) {
                        TextButton(onClick = { showForgotPasswordDialog = true }) {
                            Text(
                                "Forgot Password?",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Box(modifier = Modifier.padding(top = 16.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        onLogin(
                                            "APPLY",
                                            "",
                                            { },
                                            { })
                                    }, 
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("New Applicant? Apply Now")
                                }

                                OutlinedButton(
                                    onClick = { showTrackDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Track Application Status")
                                }
                            }
                        }
                    }
                }
            }
            if (showTrackDialog) {
                TrackApplicationDialog(
                    onDismiss = { showTrackDialog = false },
                    onCheckApplicationStatus = onCheckApplicationStatus
                )
            }
        }
        
        if (showForgotPasswordDialog) {
            ForgotPasswordDialog(
                onDismiss = { showForgotPasswordDialog = false },
                onRequestPasswordReset = onRequestPasswordReset,
                onVerifyPasswordReset = onVerifyPasswordReset
            )
        }
    }

}

@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onRequestPasswordReset: (String, (Result<String>) -> Unit) -> Unit,
    onVerifyPasswordReset: (String, String, String, (Result<String>) -> Unit) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Email, 2: OTP, 3: New Password
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(when(step) {
            1 -> "Reset Password"
            2 -> "Enter OTP"
            3 -> "Set New Password"
            else -> "Reset Password"
        })},
        text = {
            Column {
                if (successMessage != null) {
                    Text(successMessage!!, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                }
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(bottom = 8.dp))
                }
                
                when (step) {
                    1 -> {
                        Text("Enter your email address to receive a temporary reset code.")
                        OutlinedTextField(
                            value = email, 
                            onValueChange = { email = it; error = null },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                    2 -> {
                        Text("Enter the 6-digit code sent to your email (Check server logs in demo).")
                        OutlinedTextField(
                            value = otp,
                            onValueChange = { otp = it; error = null },
                            label = { Text("OTP Code") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                    3 -> {
                        Text("Enter your new password.")
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it; error = null },
                            label = { Text("New Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                }
                
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        error = null
                        successMessage = null
                        
                        when (step) {
                            1 -> { // Request OTP
                                if (email.isBlank()) {
                                    error = "Email required"
                                    isLoading = false
                                    return@launch
                                }
                                onRequestPasswordReset(email) { res ->
                                    if (res.isSuccess) {
                                        successMessage = res.getOrNull()
                                        step = 2
                                    } else {
                                        error = res.exceptionOrNull()?.message ?: "Request failed"
                                    }
                                    isLoading = false
                                }
                            }
                            2 -> { 
                                   if (otp.length < 6) {
                                        error = "Invalid OTP length"
                                        isLoading = false
                                        return@launch
                                   }
                                   step = 3
                                   isLoading = false
                            }
                            3 -> { // Reset
                                if (newPassword.length < 6) {
                                    error = "Password too short"
                                    isLoading = false
                                    return@launch
                                }
                                onVerifyPasswordReset(email, otp, newPassword) { res ->
                                    if (res.isSuccess) {
                                        successMessage = "Password reset successfully!"
                                        isLoading = false
                                        // Wait and dismiss? 
                                        // Since we can't easily delay in callback structure without coroutine scope management on caller side or passing scope, 
                                        // lets just rely on user clicking close or we can update UI state.
                                        // ideally we would access the scope here to delay, but the callback is void.
                                        // Actually I'm inside scope.launch block.
                                        // But onVerifyPasswordReset is async callback. I should probably suspend or handle it differently.
                                        // For simplicity, I'll assume the callback is invoked on main thread or I'll launch another coroutine if needed,
                                        // but I can't delay INSIDE the callback here easily unless I wrap it.
                                        // Wait, the callback `(Result<String>) -> Unit` is invoked by the caller.
                                        // I can't delay here *after* the callback returns unless the callback itself blocks (it shouldn't).
                                        // I'll just set success message.
                                    } else {
                                        error = res.exceptionOrNull()?.message ?: "Reset failed"
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    }
                },
                enabled = !isLoading
            ) {
                Text(when(step) {
                    1 -> "Send Code"
                    2 -> "Next"
                    3 -> "Reset Password"
                    else -> "Submit"
                })
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun TrackApplicationDialog(
    onDismiss: () -> Unit,
    onCheckApplicationStatus: (String, (Result<AdmissionApplication?>) -> Unit) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<AdmissionApplication?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showCredentials by remember { mutableStateOf(false) } 
    
    val scope = rememberCoroutineScope()

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(if (showCredentials) "Student Credentials" else "Track Application") 
        },
        text = {
            Column {
                if (showCredentials && result != null) {
                    Text("Congratulations! You are enrolled.", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    Spacer(Modifier.height(8.dp))
                    Text("Reg. Number:", style = MaterialTheme.typography.labelMedium)
                    Text(result!!.student_reg_number ?: "Pending generation", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Student Email:", style = MaterialTheme.typography.labelMedium)
                    Text(result!!.email ?: "Pending", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Default Password:", style = MaterialTheme.typography.labelMedium)
                    Text(result!!.credential_password ?: "N/A", fontWeight = FontWeight.Bold)
                    
                    Spacer(Modifier.height(16.dp))
                    Text("Please save these details to sign in.", fontStyle = FontStyle.Italic, fontSize = 12.sp)

                } else if (result == null) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Enter National ID or App ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != null) Text(error!!, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(top=8.dp))
                    if (isLoading) CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally).padding(top=8.dp))
                } else {
                    Text("Applicant Name:", style = MaterialTheme.typography.labelMedium)
                    Text("${result!!.first_name} ${result!!.last_name}", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Current Status:", style = MaterialTheme.typography.labelMedium)
                    Text(
                        result!!.current_phase,
                        fontWeight = FontWeight.Bold,
                        color = if(result!!.current_phase == "ENROLLED") Color(0xFF2E7D32) else Color.Blue,
                        fontSize = 18.sp
                    )
                    
                    if (result!!.current_phase == "REJECTED" && !result!!.rejection_reason.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Rejection Reason:", color = Color(0xFF8D6E63), fontWeight = FontWeight.Bold)
                        Text(result!!.rejection_reason!!, color = Color(0xFF8D6E63))
                    }
                }
            }
        },
        confirmButton = {
            if (showCredentials) {
                Button(onClick = onDismiss) { Text("Close") }
            } else if (result == null) {
                Button(onClick = {
                    scope.launch {
                        isLoading = true
                        error = null
                        onCheckApplicationStatus(query) { res ->
                            if (res.isSuccess) {
                                result = res.getOrNull()
                            } else {
                                error = "Application not found"
                            }
                            isLoading = false
                        }
                    }
                }) { Text("Check Status") }
            } else {
                Row {
                   if (result!!.current_phase == "ENROLLED") {
                       Button(onClick = { showCredentials = true }) { Text("View Credentials") }
                       Spacer(Modifier.width(8.dp))
                   }
                   Button(onClick = onDismiss) { Text("Close") }
                }
            }
        },
        dismissButton = {
            if (showCredentials) {
                TextButton(onClick = { showCredentials = false }) { Text("Back") }
            } else if (result != null) {
                TextButton(onClick = { result = null; query = "" }) { Text("Check Another") }
            } else {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

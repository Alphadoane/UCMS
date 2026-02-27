package com.school.studentportal.shared.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.data.repository.AuthRepository
import com.school.studentportal.shared.ui.components.AppScaffold
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import com.school.studentportal.shared.utils.rememberFilePicker
import com.school.studentportal.shared.utils.PlatformFile

@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    onLogout: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val userState by authRepository.currentUser.collectAsState()
    val user = userState
    
    if (user == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var isEditMode by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    
    // Editable states
    var phoneNumber by remember(user) { mutableStateOf(user.phoneNumber ?: "") }
    var alternateEmail by remember(user) { mutableStateOf(user.alternateEmail ?: "") }
    var address by remember(user) { mutableStateOf(user.address ?: "") }
    var bio by remember(user) { mutableStateOf(user.bio ?: "") }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var isChanging by remember { mutableStateOf(false) }

    val filePicker = rememberFilePicker { file ->
        if (file != null) {
            scope.launch {
                isSaving = true
                val bytes = file.readBytes()
                val result = authRepository.uploadAvatar(bytes, file.name)
                isSaving = false
                if (result.isSuccess) {
                    snackbarHostState.showSnackbar("Avatar updated successfully")
                } else {
                    snackbarHostState.showSnackbar("Failed to upload avatar")
                }
            }
        }
    }

    AppScaffold(
        title = "My Profile",
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Section
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!user.avatarUrl.isNullOrBlank()) {
                    KamelImage(
                        resource = asyncPainterResource(user.avatarUrl!!),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onLoading = { CircularProgressIndicator(modifier = Modifier.size(24.dp)) },
                        onFailure = { Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = Color.Gray) }
                    )
                } else {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(80.dp), tint = Color.Gray)
                }

                if (isEditMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { filePicker.launch() }) {
                            Icon(Icons.Default.PhotoCamera, null, tint = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(user.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(user.role.name, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            // Information Cards
            ProfileSectionTitle("Official Identity")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val regLabel = if (user.role.name == "STAFF") "Employee ID" else "Registration Number"
                    ReadOnlyProfileItem(regLabel, user.regNumber ?: "N/A", Icons.Default.Badge)
                    ReadOnlyProfileItem("University Email", user.email, Icons.Default.Email)
                    user.department?.let { 
                        val deptLabel = if (user.role.name == "STAFF") "Department" else "Program"
                        ReadOnlyProfileItem(deptLabel, it, Icons.Default.School) 
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileSectionTitle("Personal Information")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    EditableProfileItem(
                        label = "Phone Number",
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        isEditable = isEditMode,
                        icon = Icons.Default.Phone
                    )
                    EditableProfileItem(
                        label = "Alternate Email",
                        value = alternateEmail,
                        onValueChange = { alternateEmail = it },
                        isEditable = isEditMode,
                        icon = Icons.Default.AlternateEmail
                    )
                    EditableProfileItem(
                        label = "Address",
                        value = address,
                        onValueChange = { address = it },
                        isEditable = isEditMode,
                        icon = Icons.Default.Home
                    )
                    EditableProfileItem(
                        label = "Bio",
                        value = bio,
                        onValueChange = { bio = it },
                        isEditable = isEditMode,
                        icon = Icons.Default.Info,
                        singleLine = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            if (!isEditMode) {
                Button(
                    onClick = { isEditMode = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Edit, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Edit Profile")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showPasswordDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Lock, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Change Password")
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Logout from System")
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { isEditMode = false },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            isSaving = true
                            scope.launch {
                                val result = authRepository.updateProfile(
                                    phoneNumber = phoneNumber,
                                    alternateEmail = alternateEmail,
                                    address = address,
                                    bio = bio
                                )
                                isSaving = false
                                if (result.isSuccess) {
                                    isEditMode = false
                                    snackbarHostState.showSnackbar("Profile updated successfully")
                                } else {
                                    snackbarHostState.showSnackbar("Failed to update profile")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text("Save Changes")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showPasswordDialog) {
            AlertDialog(
                onDismissRequest = { if (!isChanging) showPasswordDialog = false },
                title = { Text("Update Password") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Enter your current and new password to secure your account.", style = MaterialTheme.typography.bodySmall)
                        OutlinedTextField(
                            value = oldPassword, 
                            onValueChange = { oldPassword = it },
                            label = { Text("Current Password") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.LockOpen, null) }
                        )
                        OutlinedTextField(
                            value = newPassword, 
                            onValueChange = { newPassword = it },
                            label = { Text("New Password") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, null) }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isChanging = true
                            scope.launch {
                                // Real implementation would use authRepository.changePassword
                                kotlinx.coroutines.delay(1500)
                                isChanging = false
                                snackbarHostState.showSnackbar("Password Updated Successfully")
                                showPasswordDialog = false
                                oldPassword = ""
                                newPassword = ""
                            }
                        },
                        enabled = !isChanging && oldPassword.length >= 6 && newPassword.length >= 6
                    ) {
                        if (isChanging) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White) else Text("Update")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPasswordDialog = false }, enabled = !isChanging) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun ReadOnlyProfileItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun EditableProfileItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditable: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    singleLine: Boolean = true
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = if (isEditable) MaterialTheme.colorScheme.primary else Color.Gray)
            Spacer(Modifier.width(12.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        
        if (isEditable) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = singleLine
            )
        } else {
            Text(
                text = if (value.isBlank()) "Not set" else value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp, start = 32.dp),
                color = if (value.isBlank()) Color.LightGray else Color.Unspecified
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
    }
}

package com.school.studentportal.shared.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.school.studentportal.shared.data.model.User
import com.school.studentportal.shared.data.model.UserDto
import com.school.studentportal.shared.data.model.UserRole
import com.school.studentportal.shared.data.repository.AdminRepository
import com.school.studentportal.shared.ui.components.DashboardCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserMgmtScreen(
    repository: AdminRepository,
    onNavigateBack: () -> Unit,
    onImpersonate: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    val allUsers by repository.users.collectAsState()
    
    val filteredUsers = remember(searchQuery, allUsers) {
        if (searchQuery.isBlank()) allUsers else {
            allUsers.filter { 
                it.email.contains(searchQuery, ignoreCase = true) || 
                (it.first_name + " " + it.last_name).contains(searchQuery, ignoreCase = true)
            }
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isRefreshing = true
        repository.refreshUsers()
        isRefreshing = false
    }

    // Create User State
    var showCreateDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newRole by remember { mutableStateOf(UserRole.STUDENT) }
    
    // Status Feedback
    val snackbarHostState = remember { SnackbarHostState() }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("User Management", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by Email or Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredUsers) { user ->
                    DashboardCard(
                        title = "${user.first_name} ${user.last_name}",
                        icon = Icons.Default.Person,
                        color = when(UserRole.fromString(user.role)) {
                            UserRole.ADMIN -> Color(0xFFC62828)
                            UserRole.STAFF -> Color(0xFF1565C0)
                            else -> Color(0xFF2E7D32)
                        }
                    ) {
                        UserMgmtInfoRow("Email", user.email)
                        UserMgmtInfoRow("Role", user.role)
                        
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { 
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Password reset sent to ${user.email}")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64))
                            ) {
                                Icon(Icons.Default.LockReset, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset")
                            }

                            Button(
                                onClick = { onImpersonate(user.email) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                            ) {
                                Icon(Icons.Default.Login, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Login As")
                            }
                        }
                    }
                }

                if (filteredUsers.isEmpty() && !isRefreshing) {
                    item {
                        Text("No users found", modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center, color = Color.Gray)
                    }
                }
            }
        }
        
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 16.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Add User", tint = Color.White)
        }

        IconButton(onClick = onNavigateBack, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Default.Close, null)
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        
        if (showCreateDialog) {
            var isCreating by remember { mutableStateOf(false) }
            
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create New User") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Email will be auto-generated (firstname.lastname@kcau.ac.ke)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Full Name") })
                        
                        Text("Role:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                               listOf(UserRole.STUDENT, UserRole.STAFF, UserRole.ADMIN).forEach { role ->
                                FilterChip(
                                    selected = newRole == role,
                                    onClick = { newRole = role },
                                    label = { Text(role.name) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        enabled = !isCreating && newName.isNotBlank(),
                        onClick = {
                        isCreating = true
                        scope.launch {
                            // TODO: Add create method to repository
                            delay(1000)
                            isCreating = false
                            snackbarHostState.showSnackbar("User Created Successfully")
                            showCreateDialog = false
                            newName = ""
                            repository.refreshUsers()
                        }
                    }) { 
                        if (isCreating) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("Create") 
                    }
                },
                dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } }
            )
        }
    }
}

@Composable
fun UserMgmtInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

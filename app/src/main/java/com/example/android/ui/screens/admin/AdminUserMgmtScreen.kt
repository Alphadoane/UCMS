package com.example.android.ui.screens.admin

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
import com.example.android.data.model.UserRole
import com.example.android.data.repository.AdminRepository
import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.DashboardCard
import com.example.android.ui.components.InfoRow
import kotlinx.coroutines.launch

@Composable
fun AdminUserMgmtScreen(
    repository: AdminRepository = remember { AdminRepository() },
    onNavigateBack: () -> Unit,
    onImpersonate: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<com.example.android.data.model.User?>(null) }
    var isSearching by remember { mutableStateOf(false) }

    // Create User State
    var showCreateDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newRole by remember { mutableStateOf(UserRole.STUDENT) }
    
    // Status Feedback
    val context = androidx.compose.ui.platform.LocalContext.current
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            android.widget.Toast.makeText(context, feedbackMessage, android.widget.Toast.LENGTH_SHORT).show()
            feedbackMessage = null
        }
    }

    PortalScaffold(
        title = "User Management",
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add User", tint = Color.White)
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by Email or Reg No") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        if (searchQuery.isNotBlank()) {
                            isSearching = true
                            scope.launch {
                                searchResult = repository.searchUser(searchQuery)
                                isSearching = false
                            }
                        }
                    }) {
                        Icon(Icons.Default.Search, null)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isSearching) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Results
            if (searchResult != null) {
                DashboardCard(
                    title = searchResult!!.fullName,
                    icon = Icons.Default.Person,
                    color = Color(0xFF1976D2)
                ) {
                    InfoRow("Email", searchResult!!.email)
                    InfoRow("Role", searchResult!!.role.name)
                    InfoRow("Reg No", searchResult!!.regNumber ?: "N/A")
                    
                    var isResetting by remember { mutableStateOf(false) }
                    Button(
                        onClick = { 
                            isResetting = true
                            scope.launch {
                                val result = repository.resetUserPassword(searchResult!!.id)
                                isResetting = false
                                feedbackMessage = if (result.isSuccess) result.getOrNull() else "Reset Failed: ${result.exceptionOrNull()?.message}"
                            }
                        },
                        enabled = !isResetting,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64))
                    ) {
                        if (isResetting) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White) else Icon(Icons.Default.LockReset, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset Password")
                    }

                    // Impersonate Button
                    Button(
                        onClick = { onImpersonate(searchResult!!.email) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)) // Orange
                    ) {
                        Icon(Icons.Default.Login, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Login As User")
                    }
                }
            } else if (searchQuery.isNotBlank() && !isSearching) {
                Text("No user found - Note: Search backend mock is limited/stubbed.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
        
        if (showCreateDialog) {
            var regNo by remember { mutableStateOf("") }
            var department by remember { mutableStateOf("") }
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
                            UserRole.entries.forEach { role ->
                                FilterChip(
                                    selected = newRole == role,
                                    onClick = { newRole = role },
                                    label = { Text(role.name.take(3)) }
                                )
                            }
                        }
                        
                        // Fields based on role
                        if (newRole == UserRole.STUDENT || newRole == UserRole.STAFF) {
                             val label = if (newRole == UserRole.STUDENT) "Reg Number (Optional)" else "Employee ID (Optional)"
                             OutlinedTextField(value = regNo, onValueChange = { regNo = it }, label = { Text(label) })
                        }
                        
                        if (newRole == UserRole.STAFF) {
                             OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Department") })
                        }
                    }
                },
                confirmButton = {
                    Button(
                        enabled = !isCreating && newName.isNotBlank(),
                        onClick = {
                        isCreating = true
                        scope.launch {
                            val result = repository.createUser(
                                fullName = newName, 
                                email = null, 
                                role = newRole, 
                                regNumber = regNo.ifBlank { null },
                                department = department.ifBlank { null }
                            )
                            isCreating = false
                            if (result.isSuccess) {
                                feedbackMessage = "User Created Successfully"
                                showCreateDialog = false
                                newName = ""
                                regNo = ""
                                department = ""
                            } else {
                                feedbackMessage = result.exceptionOrNull()?.message ?: "Creation Failed"
                            }
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

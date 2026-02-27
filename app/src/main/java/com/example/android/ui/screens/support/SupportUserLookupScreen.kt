package com.example.android.ui.screens.support

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.android.data.model.User
import com.example.android.data.repository.SupportRepository
import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.DashboardCard
import com.example.android.ui.components.InfoRow
import kotlinx.coroutines.launch

@Composable
fun SupportUserLookupScreen(
    repository: SupportRepository = remember { SupportRepository() },
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<User?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    PortalScaffold(title = "User Diagnostics") {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Search Bar
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Reg Number or Email") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        if (query.isNotBlank()) {
                            isSearching = true
                            hasSearched = true
                            scope.launch {
                                searchResult = repository.searchUser(query)
                                isSearching = false
                            }
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isSearching) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (hasSearched && searchResult == null) {
                Text("User not found.", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (searchResult != null) {
                val user = searchResult!!
                DashboardCard(
                    title = user.fullName,
                    icon = Icons.Default.Person,
                    color = Color(0xFF0097A7)
                ) {
                    InfoRow("Role", user.role.toString())
                    InfoRow("Email", user.email)
                    InfoRow("Reg No", user.regNumber ?: "N/A")
                    InfoRow("Course", user.course ?: "N/A")
                    
                    Button(
                        onClick = {
                            // Mock Action
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64)),
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Icon(Icons.Default.LockReset, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset Password (Simulated)")
                    }
                }
            }
        }
    }
}

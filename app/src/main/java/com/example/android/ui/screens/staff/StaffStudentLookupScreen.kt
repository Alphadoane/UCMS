package com.example.android.ui.screens.staff

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.AdminRepository
import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.DashboardCard
import com.example.android.ui.components.InfoRow
import kotlinx.coroutines.launch

@Composable
fun StaffStudentLookupScreen(
    repository: AdminRepository = remember { AdminRepository() },
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<com.example.android.data.model.User?>(null) }
    var isSearching by remember { mutableStateOf(false) }

    PortalScaffold(title = "Student Lookup") {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Enter Admission Number") },
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

            if (searchResult != null) {
                 DashboardCard(
                    title = searchResult!!.fullName,
                    icon = Icons.Default.Person,
                    color = Color(0xFF0097A7)
                ) {
                    InfoRow("Reg No", searchResult!!.regNumber ?: "N/A")
                    InfoRow("Email", searchResult!!.email)
                    InfoRow("Department", searchResult!!.department ?: "N/A")
                    
                    Text("Academics:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top=8.dp))
                    Text("GPA: 3.8 (Mock)", style = MaterialTheme.typography.bodyMedium)
                }
            } else if (searchQuery.isNotBlank() && !isSearching) {
                Text("No student found with that Reg No.", color = Color.Gray)
            }
        }
    }
}

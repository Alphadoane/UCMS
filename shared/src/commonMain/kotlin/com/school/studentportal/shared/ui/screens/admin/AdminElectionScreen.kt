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
import com.school.studentportal.shared.data.model.Election
import com.school.studentportal.shared.data.model.Candidate
import com.school.studentportal.shared.ui.components.AppScaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.school.studentportal.shared.data.repository.AdminRepository

@Composable
fun AdminElectionScreen(
    repository: AdminRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    val elections by repository.elections.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showAddCandidateDialog by remember { mutableStateOf<Election?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isRefreshing = true
        repository.refreshElections()
        isRefreshing = false
    }
    
    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Column {
             Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                 IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) }
                 Text("Election Management", style = MaterialTheme.typography.headlineSmall)
             }
             Spacer(modifier = Modifier.height(16.dp))
             
             if (isRefreshing) {
                 LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                 Spacer(modifier = Modifier.height(16.dp))
             }

             LazyColumn(modifier = Modifier.weight(1f)) {
                items(elections) { election ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                         elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(election.title, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            Text(if (election.is_active) "Active" else "Closed", color = if (election.is_active) Color(0xFF2E7D32) else Color.Gray)
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            Text("Candidates: ${election.candidates.size}")
                            election.candidates.forEach { 
                                Text("- ${it.name} (${it.slogan})")
                            }
                            
                            Button(
                                onClick = { showAddCandidateDialog = election },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Add Candidate")
                            }
                        }
                    }
                }

                if (elections.isEmpty() && !isRefreshing) {
                    item {
                         Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                             Text("No elections found", color = Color.Gray)
                         }
                    }
                }
             }
        }
        
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = Color(0xFFE65100),
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomEnd)
        ) {
            Icon(Icons.Default.Add, null, tint = Color.White)
        }
        
        if (showCreateDialog) {
             var title by remember { mutableStateOf("") }
             var desc by remember { mutableStateOf("") }
             var isSubmitting by remember { mutableStateOf(false) }

             AlertDialog(
                 onDismissRequest = { showCreateDialog = false },
                 title = { Text("Create Election") },
                 text = {
                     Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                         OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                         OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                     }
                 },
                 confirmButton = {
                     Button(
                         enabled = !isSubmitting && title.isNotBlank(),
                         onClick = {
                             isSubmitting = true
                             scope.launch {
                                 repository.createElection(title, desc, "2026-12-31")
                                 repository.refreshElections()
                                 isSubmitting = false
                                 showCreateDialog = false
                             }
                     }) { Text("Create") }
                 },
                 dismissButton = {
                     TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
                 }
             )
        }
        
        if (showAddCandidateDialog != null) {
            var name by remember { mutableStateOf("") }
            var slogan by remember { mutableStateOf("") }
            var isSubmitting by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showAddCandidateDialog = null },
                title = { Text("Add Candidate to ${showAddCandidateDialog!!.title}") },
                text = {
                     Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Candidate Name") })
                        OutlinedTextField(value = slogan, onValueChange = { slogan = it }, label = { Text("Slogan") })
                     }
                },
                confirmButton = {
                     Button(
                         enabled = !isSubmitting && name.isNotBlank(),
                         onClick = {
                            isSubmitting = true
                            scope.launch {
                                repository.addCandidate(showAddCandidateDialog!!.id, name, slogan)
                                repository.refreshElections()
                                isSubmitting = false
                                showAddCandidateDialog = null
                            }
                     }) { Text("Add") }
                },
                dismissButton = {
                     TextButton(onClick = { showAddCandidateDialog = null }) { Text("Cancel") }
                }
            )
        }
    }
}

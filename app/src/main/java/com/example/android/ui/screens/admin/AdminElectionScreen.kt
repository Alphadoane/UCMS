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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.android.data.model.Election
import com.example.android.data.model.ElectionRequest
import com.example.android.data.model.CandidateRequest
import com.example.android.data.repository.VotingRepository
import com.example.android.ui.components.PortalScaffold
import kotlinx.coroutines.launch

@Composable
fun AdminElectionScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { VotingRepository(context) }
    val scope = rememberCoroutineScope()
    
    var elections by remember { mutableStateOf<List<Election>>(emptyList()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showAddCandidateDialog by remember { mutableStateOf<Election?>(null) }
    
    // Refresh Logic
    val refreshElections = {
        scope.launch {
            try {
                val res = repository.getElections()
                if (res.isSuccessful && res.body() != null) elections = res.body()!!.items
            } catch (e: Exception) {
                // error handling
            }
        }
    }
    
    LaunchedEffect(Unit) { refreshElections() }
    
    PortalScaffold(
        title = "Election Management",
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = Color(0xFFE65100)) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(elections) { election ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                     elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(election.title, style = MaterialTheme.typography.titleMedium)
                        Text(if (election.is_active) "Active" else "Closed", color = if (election.is_active) Color.Green else Color.Gray)
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Text("Candidates: ${election.candidates.size}")
                        election.candidates.forEach { 
                            Text("- ${it.name}")
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
        }
        
        if (showCreateDialog) {
             var title by remember { mutableStateOf("") }
             var desc by remember { mutableStateOf("") }
             
             AlertDialog(
                 onDismissRequest = { showCreateDialog = false },
                 title = { Text("Create Election") },
                 text = {
                     Column {
                         OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                         OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                     }
                 },
                 confirmButton = {
                     Button(onClick = {
                         scope.launch {
                             repository.createElection(ElectionRequest(title, desc, "2026-01-22T00:00:00Z")) // Simple date for now
                             refreshElections()
                             showCreateDialog = false
                         }
                     }) { Text("Create") }
                 }
             )
        }
        
        if (showAddCandidateDialog != null) {
            var name by remember { mutableStateOf("") }
            
            AlertDialog(
                onDismissRequest = { showAddCandidateDialog = null },
                title = { Text("Add Candidate to ${showAddCandidateDialog!!.title}") },
                text = {
                     OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Candidate Name") })
                },
                confirmButton = {
                     Button(onClick = {
                         scope.launch {
                             repository.addCandidate(showAddCandidateDialog!!.id, CandidateRequest(name, ""))
                             refreshElections()
                             showAddCandidateDialog = null
                         }
                     }) { Text("Add") }
                }
            )
        }
    }
}

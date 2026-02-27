package com.example.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import com.example.android.data.model.Election
import com.example.android.data.model.Candidate
import com.example.android.data.model.VoteRequest
import com.example.android.data.repository.VotingRepository
import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.DashboardCard
import kotlinx.coroutines.launch

@Composable
fun VotingSystemScreen() {
    val context = LocalContext.current
    val repository = remember { VotingRepository(context) }
    val scope = rememberCoroutineScope()
    
    var elections by remember { mutableStateOf<List<Election>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val result = repository.getElections()
            if (result.isSuccessful && result.body() != null) {
                 elections = result.body()!!.items
            } else {
                error = "Failed to load elections"
            }
        } catch (e: Exception) {
            error = e.message
        }
        isLoading = false
    }

    val snackbarHostState = remember { SnackbarHostState() }

    PortalScaffold(
        title = "Voting System",
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1D3762))
            }
        } else if (error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error!!, color = MaterialTheme.colorScheme.tertiary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(elections) { election ->
                    ElectionCard(election = election, repository = repository, snackbarHostState = snackbarHostState)
                }
            }
        }
    }
}

@Composable
fun ElectionCard(election: Election, repository: VotingRepository, snackbarHostState: SnackbarHostState) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCandidate by remember { mutableStateOf<Int?>(null) }
    var isVoting by remember { mutableStateOf(false) }
    var hasVoted by remember { mutableStateOf(election.has_voted) }
    var showResults by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<Candidate>>(emptyList()) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = election.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Opens: ${election.start_date.take(10)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (showResults) {
                 // Results View
                 Column {
                     Text(
                         text = "Live Results", 
                         style = MaterialTheme.typography.titleMedium,
                         fontWeight = FontWeight.SemiBold,
                         modifier = Modifier.padding(bottom = 8.dp)
                     )
                     results.forEach { cand ->
                         Column(modifier = Modifier.padding(vertical = 8.dp)) {
                             Row(
                                 modifier = Modifier.fillMaxWidth(),
                                 horizontalArrangement = Arrangement.SpaceBetween
                             ) {
                                 Text(cand.name, fontWeight = FontWeight.Medium)
                                 Text("${cand.vote_count} votes", color = MaterialTheme.colorScheme.primary)
                             }
                             Spacer(modifier = Modifier.height(4.dp))
                             LinearProgressIndicator(
                                 progress = if (results.sumOf { it.vote_count } > 0) cand.vote_count.toFloat() / results.sumOf { it.vote_count } else 0f,
                                 modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                 color = MaterialTheme.colorScheme.primary,
                                 trackColor = MaterialTheme.colorScheme.surfaceVariant
                             )
                         }
                     }
                     TextButton(
                         onClick = { showResults = false },
                         modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                     ) { 
                        Text("Close Results") 
                     }
                 }
            } else {
                 // Voting View
                 if (expanded) {
                    if (hasVoted || !election.is_active) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = if (hasVoted) "You have already voted." else "Voting is currently closed.",
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        if (!showResults) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val res = repository.getElectionResults(election.id)
                                        if (res.isSuccessful) {
                                            results = res.body()!!.results
                                            showResults = true
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) { Text("View Results") }
                        }
                    } else {
                        Text(
                            text = "Select a Candidate",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        election.candidates.forEach { candidate ->
                            val isSelected = selectedCandidate == candidate.id
                            Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedCandidate = candidate.id }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedCandidate = candidate.id }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = candidate.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                        
                        Button(
                            onClick = {
                                if (selectedCandidate != null) {
                                    isVoting = true
                                    scope.launch {
                                        try {
                                            val res = repository.castVote(election.id, VoteRequest(selectedCandidate!!))
                                            if (res.isSuccessful) {
                                                hasVoted = true
                                            } else {
                                                // Extract error message
                                                val errorBody = res.errorBody()?.string()
                                                val message = try {
                                                    org.json.JSONObject(errorBody ?: "{}").getString("detail")
                                                } catch (e: Exception) {
                                                    "Failed to cast vote: ${res.code()}"
                                                }
                                                snackbarHostState.showSnackbar(message)
                                            }
                                        } catch(e: Exception) {
                                            snackbarHostState.showSnackbar("Error: ${e.message}")
                                        }
                                        isVoting = false
                                    }
                                }
                            },
                            enabled = selectedCandidate != null && !isVoting,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isVoting) "Casting Vote..." else "Vote")
                        }
                    }
                }
                
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Hide Details" else "Show Details")
                }
            }
        }
    }
}

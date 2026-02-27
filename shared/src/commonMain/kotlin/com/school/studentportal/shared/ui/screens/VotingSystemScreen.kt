package com.school.studentportal.shared.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.ui.components.AppScaffold
import kotlinx.coroutines.launch

import com.school.studentportal.shared.ui.viewmodel.VotingViewModel
import com.school.studentportal.shared.ui.viewmodel.VotingUiState

@Composable
fun VotingSystemScreen(viewModel: VotingViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.voteSuccess, uiState.error) {
        uiState.voteSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatus()
        }
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatus()
        }
    }

    AppScaffold(
        title = "Voting System",
        showTopBar = false,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading && uiState.elections.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null && uiState.elections.isEmpty()) {
                Text(
                    text = uiState.error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp).align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.elections) { election ->
                        var expanded by remember { mutableStateOf(false) }
                        var selectedCandidateId by remember { mutableStateOf<Int?>(null) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(election.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                if (election.description.isNotEmpty()) {
                                    Text(election.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                }
                                
                                if (expanded) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    if (election.has_voted) {
                                        Text("You have already cast your vote.", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        // Results
                                        election.candidates.forEach { cand ->
                                            val total = election.candidates.sumOf { it.vote_count }
                                            val progress = if(total > 0) cand.vote_count.toFloat() / total else 0f
                                            Column(Modifier.padding(vertical = 4.dp)) {
                                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(cand.name)
                                                    Text("${(progress * 100).toInt()}% (${cand.vote_count})")
                                                }
                                                LinearProgressIndicator(
                                                    progress = { progress },
                                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    trackColor = MaterialTheme.colorScheme.primaryContainer
                                                )
                                            }
                                        }
                                    } else {
                                        election.candidates.forEach { candidate ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth().clickable { selectedCandidateId = candidate.id }
                                            ) {
                                                RadioButton(selected = selectedCandidateId == candidate.id, onClick = { selectedCandidateId = candidate.id })
                                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                                    Text(candidate.name, fontWeight = FontWeight.Medium)
                                                    if (candidate.slogan != null) {
                                                        Text(candidate.slogan!!, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                    }
                                                }
                                            }
                                        }
                                        Button(
                                            onClick = {
                                                selectedCandidateId?.let { viewModel.castVote(election.id, it) }
                                            },
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                            enabled = !uiState.isVoting && selectedCandidateId != null
                                        ) {
                                            if (uiState.isVoting) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Text("Submit Vote")
                                            }
                                        }
                                    }
                                }
                                
                                TextButton(onClick = { expanded = !expanded }, modifier = Modifier.align(Alignment.End)) {
                                    Text(if (expanded) "Hide Details" else "Show Details")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

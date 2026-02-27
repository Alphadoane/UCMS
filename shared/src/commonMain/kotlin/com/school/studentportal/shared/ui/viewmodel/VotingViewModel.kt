package com.school.studentportal.shared.ui.viewmodel

import com.school.studentportal.shared.data.model.Election
import com.school.studentportal.shared.data.repository.VotingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class VotingUiState(
    val elections: List<Election> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isVoting: Boolean = false,
    val voteSuccess: String? = null
)

class VotingViewModel(
    private val repository: VotingRepository,
    private val scope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(VotingUiState())
    val uiState: StateFlow<VotingUiState> = _uiState.asStateFlow()

    init {
        loadElections()
    }

    fun loadElections() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getElections()
            if (result.isSuccess) {
                _uiState.update { it.copy(elections = result.getOrNull() ?: emptyList(), isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun castVote(electionId: Int, candidateId: Int) {
        scope.launch {
            _uiState.update { it.copy(isVoting = true, error = null, voteSuccess = null) }
            val result = repository.castVote(electionId, candidateId)
            if (result.isSuccess) {
                _uiState.update { it.copy(isVoting = false, voteSuccess = "Vote cast successfully!") }
                loadElections() // Refresh to update has_voted status
            } else {
                _uiState.update { it.copy(isVoting = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }
    
    fun clearStatus() {
        _uiState.update { it.copy(error = null, voteSuccess = null) }
    }
}

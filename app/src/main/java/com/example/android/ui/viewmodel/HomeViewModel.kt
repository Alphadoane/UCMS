package com.example.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.data.model.User
import com.example.android.data.repository.AcademicsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val user: User? = null,
    val feeData: Map<String, Any>? = null,
    val courses: List<Map<String, Any>> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AcademicsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Triggered by UI or Init
    fun init(user: User) {
        _uiState.update { it.copy(user = user, isLoading = true) }
        loadDashboardData(user.id)
    }

    private fun loadDashboardData(userId: String) {
        viewModelScope.launch {
            try {
                // Parallel flows
                val feeFlow = kotlinx.coroutines.flow.flow { emit(repository.getFeeBalance(userId)) } // TODO: Refactor repo to Flow
                val coursesFlow = repository.observeCourseRegistration(userId)
                
                // Combine flows only if robust, otherwise separate launches
                launch {
                    try {
                        // For fees, we just fetch once for now as it doesn't have an observation flow yet
                        val fees = repository.getFeeBalance(userId)
                        _uiState.update { it.copy(feeData = fees) }
                    } catch (e: Exception) {
                        // Ignore minor fee error or log
                    }
                }
                
                launch {
                    repository.observeCourseRegistration(userId)
                        .catch { e -> _uiState.update { it.copy(errorMessage = e.message) } }
                        .collect { courses ->
                            _uiState.update { it.copy(courses = courses, isLoading = false) }
                            
                            // Passive background refresh if empty
                            if (courses.isEmpty()) {
                                repository.refreshCourseRegistration(userId)
                            }
                        }
                }
                
                // Also trigger refresh immediately to ensure stale-while-revalidate for next time
                launch {
                    repository.refreshCourseRegistration(userId)
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}

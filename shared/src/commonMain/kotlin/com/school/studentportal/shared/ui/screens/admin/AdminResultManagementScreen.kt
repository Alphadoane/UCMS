package com.school.studentportal.shared.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.data.repository.AdminRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminResultManagementScreen(
    repository: AdminRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var semesterId by remember { mutableStateOf("") }
    var courseId by remember { mutableStateOf("") }
    var isPublishing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Result Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Publish Results", style = MaterialTheme.typography.titleLarge)
            Text("Enter Semester ID (and optionally Course ID) to publish results. Publishing will make results visible to students.")
            
            OutlinedTextField(
                value = semesterId,
                onValueChange = { semesterId = it },
                label = { Text("Semester ID") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = courseId,
                onValueChange = { courseId = it },
                label = { Text("Course ID (Optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                onClick = {
                    if (semesterId.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Semester ID is required") }
                        return@Button
                    }
                    
                    val semId = semesterId.toIntOrNull()
                    if (semId == null) {
                         scope.launch { snackbarHostState.showSnackbar("Invalid Semester ID") }
                         return@Button
                    }
                    
                    val cId = if (courseId.isNotBlank()) courseId.toIntOrNull() else null
                    
                    isPublishing = true
                    scope.launch {
                        val result = repository.publishResults(semId, cId)
                        if (result.isSuccess) {
                            snackbarHostState.showSnackbar("Results published successfully")
                            semesterId = ""
                            courseId = ""
                        } else {
                            snackbarHostState.showSnackbar("Error: ${result.exceptionOrNull()?.message}")
                        }
                        isPublishing = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isPublishing
            ) {
                if (isPublishing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Publish Results")
                }
            }
        }
    }
}

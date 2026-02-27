package com.example.android.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.AdmissionRepository
import com.example.android.data.model.AdmissionProgram
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProgramsScreen(onNavigateBack: () -> Unit) {
    val repository = remember { AdmissionRepository() }
    var programs by remember { mutableStateOf<List<AdmissionProgram>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var editingProgram by remember { mutableStateOf<AdmissionProgram?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun refresh() {
        scope.launch {
            isLoading = true
            programs = repository.getPrograms().getOrNull() ?: emptyList()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(
        topBar = { TopAppBar(
            title = { Text("Manage Programmes") },
            navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) } },
            actions = {
                IconButton(onClick = { editingProgram = null; showDialog = true }) {
                    Icon(Icons.Default.Add, "Add Program")
                }
            }
        )},
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { Text("Tap to edit, Press Add to create", style = MaterialTheme.typography.bodySmall) }
                    items(programs) { program ->
                        ProgramListItem(program, 
                            onClick = { editingProgram = program; showDialog = true },
                            onDelete = {
                                scope.launch {
                                    val res = repository.deleteProgram(program.id)
                                    if (res.isSuccess) refresh()
                                    else snackbarHostState.showSnackbar("Delete failed")
                                }
                            }
                        )
                    }
                }
            }
        }
        
        if (showDialog) {
            ProgramDialog(
                program = editingProgram,
                onDismiss = { showDialog = false },
                onSave = { p ->
                    scope.launch {
                        val res = if (p.id == 0) repository.createProgram(p) else repository.updateProgram(p)
                        if (res.isSuccess) {
                            showDialog = false
                            refresh()
                        } else {
                            snackbarHostState.showSnackbar("Detailed Error: ${res.exceptionOrNull()?.message}")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ProgramListItem(program: AdmissionProgram, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(program.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text("${program.category} | ${program.code}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color(0xFF455A64)) }
        }
    }
}

@Composable
fun ProgramDialog(program: AdmissionProgram?, onDismiss: () -> Unit, onSave: (AdmissionProgram) -> Unit) {
    var name by remember { mutableStateOf(program?.name ?: "") }
    var code by remember { mutableStateOf(program?.code ?: "") }
    var category by remember { mutableStateOf(program?.category ?: "Undergraduate") }
    var duration by remember { mutableStateOf(program?.duration_years?.toString() ?: "4") }
    var reqs by remember { mutableStateOf(program?.entry_requirements ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (program == null) "New Programme" else "Edit Programme") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") })
                OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration (Years)") })
                OutlinedTextField(value = reqs, onValueChange = { reqs = it }, label = { Text("Requirements") }, minLines = 3)
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(AdmissionProgram(
                    id = program?.id ?: 0,
                    code = code, name = name,
                    duration_years = duration.toIntOrNull() ?: 4,
                    category = category, entry_requirements = reqs
                ))
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

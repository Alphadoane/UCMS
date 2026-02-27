package com.school.studentportal.shared.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.data.model.AdmissionProgram
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProgramsScreen(onNavigateBack: () -> Unit) {
    // Mock Data State
    var programs by remember { mutableStateOf(listOf(
        AdmissionProgram(1, "CS101", "Computer Science", 4, "Undergraduate", "C+ in KCSE"),
        AdmissionProgram(2, "IT102", "Information Technology", 4, "Undergraduate", "C in KCSE"),
        AdmissionProgram(3, "BBIT103", "Bus. Info. Tech", 4, "Undergraduate", "C in KCSE")
    )) }
    
    var isLoading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var editingProgram by remember { mutableStateOf<AdmissionProgram?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // No redundant Scaffold here, handled by parent
    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
             Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Manage Programmes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                IconButton(onClick = { editingProgram = null; showDialog = true }) {
                    Icon(Icons.Default.Add, "Add Program", tint = MaterialTheme.colorScheme.primary)
                }
            }
            
            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .widthIn(max = 900.dp)
                            .fillMaxWidth()
                    ) {
                        item { Text("Tap to edit, Press + to create", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                        items(programs) { program ->
                            ProgramListItem(program, 
                                onClick = { editingProgram = program; showDialog = true },
                                onDelete = {
                                    scope.launch {
                                        programs = programs.filter { it.id != program.id }
                                        snackbarHostState.showSnackbar("Deleted ${program.name}")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))

        if (showDialog) {
            ProgramDialog(
                program = editingProgram,
                onDismiss = { showDialog = false },
                onSave = { p ->
                    scope.launch {
                        if (p.id == 0) {
                            // Create
                            val newId = (programs.maxOfOrNull { it.id } ?: 0) + 1
                            programs = programs + p.copy(id = newId)
                            snackbarHostState.showSnackbar("Created ${p.name}")
                        } else {
                            // Update
                            programs = programs.map { if (it.id == p.id) p else it }
                            snackbarHostState.showSnackbar("Updated ${p.name}")
                        }
                        showDialog = false
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

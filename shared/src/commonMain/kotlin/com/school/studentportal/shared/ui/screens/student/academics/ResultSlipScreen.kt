package com.school.studentportal.shared.ui.screens.student.academics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.school.studentportal.shared.data.repository.AcademicsRepository
import com.school.studentportal.shared.data.model.ResultSlip
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultSlipScreen(
    repository: AcademicsRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var resultSlip by remember { mutableStateOf<ResultSlip?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isDownloading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            val res = repository.getResultSlip(null) // Fetch latest by default
            if (res.isSuccess) {
                resultSlip = res.getOrNull()
            } else {
                error = res.exceptionOrNull()?.message ?: "Failed to load result slip"
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Result Slip") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (resultSlip != null) {
                        IconButton(onClick = {
                            if (!isDownloading) {
                                scope.launch {
                                    isDownloading = true
                                    // repository.downloadResultSlipPdf(resultSlip!!.semester_id) // TODO: Implement download
                                    // Simulating download for now or need actual download logic which might depend on platform
                                    isDownloading = false
                                }
                            }
                        }) {
                            if (isDownloading) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.Download, contentDescription = "Download PDF")
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                    Button(onClick = {
                        scope.launch {
                            isLoading = true
                            error = null
                            val res = repository.getResultSlip(null)
                            if (res.isSuccess) resultSlip = res.getOrNull()
                            else error = res.exceptionOrNull()?.message
                            isLoading = false
                        }
                    }) {
                        Text("Retry")
                    }
                }
            } else if (resultSlip != null) {
                ResultSlipContent(resultSlip!!)
            } else {
                Text("No results found.", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun ResultSlipContent(slip: ResultSlip) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("OFFICIAL TRANSCRIPT", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(Modifier.height(8.dp))
                    Text("Name: ${slip.student_name}")
                    Text("Admission No: ${slip.admission_number}")
                    Text("Program: ${slip.program}")
                    Text("Semester: ${slip.semester} (${slip.academic_year})")
                }
            }
        }

        item {
            Text("Academic Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(slip.results) { result ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(result.course_code, fontWeight = FontWeight.Bold)
                        Text(result.course_title, style = MaterialTheme.typography.bodyMedium)
                        Text("Credits: ${result.credits}", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(result.grade, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Points: ${result.points}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Credits: ${slip.total_credits}", fontWeight = FontWeight.Bold)
                    Text("GPA: ${slip.gpa}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        
        item {
             Text(
                "Remarks: ${if(slip.gpa >= 2.0) "Pass" else "Fail"}", // Simple logic for display
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                modifier = Modifier.padding(top = 8.dp)
             )
        }
    }
}

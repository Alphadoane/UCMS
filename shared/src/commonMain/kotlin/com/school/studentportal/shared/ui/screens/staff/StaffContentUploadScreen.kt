package com.school.studentportal.shared.ui.screens.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.ui.components.DashboardCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.ArrowBack
import com.school.studentportal.shared.data.repository.StaffRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffContentUploadScreen(
    courseId: String,
    repository: StaffRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    var existingMaterials by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    val courseIdInt = courseId.toIntOrNull() ?: 0

    // Upload Form State
    var showUploadDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("PDF") } 
    var uriOrLink by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        // For now, there's no getMaterials endpoint, so we show an empty list or mock
        // But we implement the upload correctly
        isRefreshing = true
        delay(300)
        isRefreshing = false
    }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Column {
             Row(verticalAlignment = Alignment.CenterVertically) {
                 IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) }
                 Text("Course Materials", style = MaterialTheme.typography.headlineSmall)
             }
            Spacer(modifier = Modifier.height(16.dp))

            if (isRefreshing) {
                 Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                 }
            } else {
                 LazyColumn(
                     modifier = Modifier.weight(1f),
                     verticalArrangement = Arrangement.spacedBy(16.dp)
                 ) {
                     if (existingMaterials.isEmpty()) {
                         item { Text("No materials uploaded yet.") }
                     }
                     items(existingMaterials) { material ->
                         DashboardCard(
                             title = material["title"] as? String ?: "Untitled",
                             icon = if (material["type"] == "Link") Icons.Default.Link else Icons.Default.Description,
                             color = Color(0xFF455A64)
                         ) {
                             Text("Type: ${material["type"]}")
                             Text("Date: ${material["date"] ?: "Recently"}")
                         }
                     }
                 }
            }
        }
        
        FloatingActionButton(
            onClick = { showUploadDialog = true },
            containerColor = Color(0xFFEF6C00),
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 48.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Upload", tint = Color.White)
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))

        if (showUploadDialog) {
            var isUploading by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = { showUploadDialog = false },
                title = { Text("Upload Learning Material") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title (e.g. Week 1 Notes)") }
                        )
                        
                        Text("Material Type:")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = type == "PDF", onClick = { type = "PDF" }, label = { Text("PDF Document") })
                            FilterChip(selected = type == "Link", onClick = { type = "Link" }, label = { Text("Web Link") })
                        }
                        
                        OutlinedTextField(
                            value = uriOrLink,
                            onValueChange = { uriOrLink = it },
                            label = { Text(if (type == "Link") "Paste URL" else "File Path / URI") },
                            placeholder = { Text(if (type == "Link") "https://..." else "Select file...") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        enabled = !isUploading && title.isNotBlank() && uriOrLink.isNotBlank(),
                        onClick = {
                        isUploading = true
                        scope.launch {
                            val request = com.school.studentportal.shared.data.model.ContentUploadRequest(
                                course_id = courseIdInt,
                                title = title,
                                content_type = type,
                                content_url = uriOrLink,
                                description = null
                            )
                            val res = repository.uploadContent(request)
                            if (res.isSuccess) {
                                existingMaterials = existingMaterials + mapOf("title" to title, "type" to type, "date" to "Just Now")
                                snackbarHostState.showSnackbar("Material Uploaded Successfully")
                                showUploadDialog = false
                            } else {
                                snackbarHostState.showSnackbar("Failed to upload material")
                            }
                            isUploading = false
                        }
                    }) {
                        if (isUploading) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("Upload")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUploadDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

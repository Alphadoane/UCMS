package com.example.android.ui.screens.staff

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.AdminRepository
import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.DashboardCard
import kotlinx.coroutines.launch

@Composable
fun StaffContentUploadScreen(
    unitCode: String,
    repository: AdminRepository = remember { AdminRepository() },
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var userId by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
         val me = repository.getUserProfile("me")
         userId = me?.id ?: ""
    }
    
    var existingMaterials by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    
    // Upload Form State
    var showUploadDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("PDF") } // PDF, Link, Video
    var uriOrLink by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        existingMaterials = repository.getCourseMaterials(unitCode)
        loading = false
    }

    PortalScaffold(
        title = "Materials: $unitCode",
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showUploadDialog = true },
                containerColor = Color(0xFFEF6C00)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Upload", tint = Color.White)
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (loading) {
                 CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                 LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

        if (showUploadDialog) {
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
                        if (type == "PDF" && uriOrLink.isEmpty()) {
                             Button(onClick = { uriOrLink = "content://media/external/file/123" }, modifier = Modifier.fillMaxWidth()) {
                                 Text("Pick File (Simulated)")
                             }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        scope.launch {
                            repository.uploadCourseMaterial(unitCode, title, type, uriOrLink, userId)
                            showUploadDialog = false
                            existingMaterials = repository.getCourseMaterials(unitCode)
                            android.widget.Toast.makeText(context, "Uploaded!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Upload")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUploadDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

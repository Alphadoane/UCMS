package com.school.studentportal.shared.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.data.model.AdmissionApplication
import com.school.studentportal.shared.data.model.AdmissionDocument
import com.school.studentportal.shared.data.repository.AdminRepository
import com.school.studentportal.shared.utils.toImageBitmap
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAdmissionScreen(
    repository: AdminRepository,
    onNavigateBack: () -> Unit
) {
    val applications by repository.applications.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    var selectedApp by remember { mutableStateOf<AdmissionApplication?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        isLoading = true
        repository.refreshApplications()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admission Console") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (isLoading && applications.isEmpty()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else if (selectedApp != null) {
                // We find the latest version of the selected app in the list
                val currentApp = applications.find { it.application_id == selectedApp?.application_id } ?: selectedApp!!
                AdmissionDetailView(
                    app = currentApp,
                    repository = repository,
                    onBack = { selectedApp = null },
                    onEnroll = { 
                         scope.launch {
                             val result = repository.enrollStudent(currentApp.application_id)
                             if (result.isSuccess) {
                                 val credentials = result.getOrNull()
                                 snackbarHostState.showSnackbar("Enrolled: ${credentials?.get("email")}")
                                 repository.refreshApplications()
                                 selectedApp = null
                             } else {
                                 val msg = result.exceptionOrNull()?.message ?: "Enrollment failed"
                                 snackbarHostState.showSnackbar(msg)
                             }
                         }
                    },
                    onError = { msg ->
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    },
                    onUpdatePhase = { phase, reason ->
                        scope.launch {
                            val result = repository.updateApplicationPhase(currentApp.application_id, phase, reason)
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar("Updated to $phase")
                                repository.refreshApplications()
                            } else {
                                snackbarHostState.showSnackbar("Update failed")
                            }
                        }
                    }
                )
            } else {
                if (applications.isEmpty() && !isLoading) {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                         Text("No applications found", style = MaterialTheme.typography.bodyLarge)
                         Spacer(Modifier.height(8.dp))
                         Button(onClick = { 
                             scope.launch {
                                 isLoading = true
                                 repository.refreshApplications()
                                 isLoading = false
                             }
                         }) { Text("Reload") }
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(applications) { app ->
                            ApplicationCard(app) { selectedApp = app }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationCard(app: AdmissionApplication, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(app.application_id, fontWeight = FontWeight.Bold)
                Surface(
                    color = when(app.current_phase) {
                        "APPLIED" -> Color(0xFFE3F2FD)
                        "VERIFIED" -> Color(0xFFE8F5E9)
                        "ENROLLED" -> Color(0xFFF1F8E9)
                        "REJECTED" -> Color(0xFFFFEBEE)
                        else -> Color.LightGray
                    },
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(app.current_phase, 
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when(app.current_phase) {
                            "APPLIED" -> Color(0xFF1976D2)
                            "VERIFIED" -> Color(0xFF2E7D32)
                            "ENROLLED" -> Color(0xFF388E3C)
                            "REJECTED" -> Color(0xFFD32F2F)
                            else -> Color.DarkGray
                        }
                    )
                }
            }
            Text("${app.first_name} ${app.last_name}", style = MaterialTheme.typography.titleMedium)
            Text("ID: ${app.national_id}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun AdmissionDetailView(
    app: AdmissionApplication, 
    repository: AdminRepository,
    onBack: () -> Unit, 
    onEnroll: () -> Unit, 
    onError: (String) -> Unit,
    onUpdatePhase: (String, String?) -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }
    var viewingDoc by remember { mutableStateOf<AdmissionDocument?>(null) }
    val scope = rememberCoroutineScope()
    
    if (viewingDoc != null) {
        AdminDocumentReviewDialog(
            doc = viewingDoc!!,
            repository = repository,
            onDismiss = { viewingDoc = null },
            onVerify = { approve, reason ->
                scope.launch {
                    val result = repository.verifyDocument(viewingDoc!!.id, approve, reason)
                    if (result.isSuccess) {
                        repository.refreshApplications()
                        viewingDoc = null
                    } else {
                        onError("Verification failed")
                    }
                }
            }
        )
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Application") },
            text = {
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    label = { Text("Reason for Rejection") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = { 
                        if (rejectReason.isNotBlank()) {
                            onUpdatePhase("REJECTED", rejectReason)
                            showRejectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Reject") }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
    ) {
        TextButton(onClick = onBack) { 
            Icon(Icons.Default.ArrowBack, null)
            Spacer(Modifier.width(8.dp))
            Text("Back to list") 
        }
        
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Applicant: ${app.first_name} ${app.last_name}", style = MaterialTheme.typography.headlineSmall)
                Text("App ID: ${app.application_id}", style = MaterialTheme.typography.bodyMedium)
                Text("National ID: ${app.national_id}", style = MaterialTheme.typography.bodyMedium)
                Text("Current Status: ${app.current_phase}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        if (app.documents.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Verification Checklist", style = MaterialTheme.typography.titleMedium)
            app.documents.forEach { doc ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(doc.document_type.replace("_", " "), fontWeight = FontWeight.Bold)
                            if (doc.is_verified) {
                                Text("Verified", color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall)
                            } else if (doc.rejection_reason?.isNotEmpty() == true) {
                                Text("Rejected: ${doc.rejection_reason}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            } else {
                                Text("Action Required", color = Color(0xFFF57C00), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Button(onClick = { viewingDoc = doc }) {
                            Text("Review")
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Workflow Actions
        Text("Application Workflow", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        
        when (app.current_phase) {
            "APPLIED" -> {
                Button(onClick = { onUpdatePhase("VERIFIED", null) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Approve All Documents & Mark Verified")
                }
            }
            "VERIFIED" -> {
                Button(onClick = { onUpdatePhase("SELECTED", null) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Select for Programme")
                }
            }
            "SELECTED" -> {
                Button(onClick = { onUpdatePhase("OFFERED", null) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Send Letter of Offer")
                }
            }
            "OFFERED" -> {
                Button(
                    onClick = onEnroll,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Enroll as Student")
                }
            }
            "ENROLLED" -> {
                Text("Student already enrolled.", color = Color(0xFF2E7D32), style = MaterialTheme.typography.titleMedium)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        if (app.current_phase != "REJECTED" && app.current_phase != "ENROLLED") {
            OutlinedButton(
                onClick = { showRejectDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reject Application")
            }
        }
    }
}

@Composable
fun AdminDocumentReviewDialog(
    doc: AdmissionDocument,
    repository: AdminRepository,
    onDismiss: () -> Unit,
    onVerify: (Boolean, String) -> Unit
) {
    var rejectReason by remember { mutableStateOf("") }
    var isRejecting by remember { mutableStateOf(false) }
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoadingImage by remember { mutableStateOf(true) }

    LaunchedEffect(doc.id) {
        isLoadingImage = true
        val result = repository.downloadFile(doc.file)
        if (result.isSuccess) {
            imageBitmap = result.getOrNull()?.toImageBitmap()
        }
        isLoadingImage = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review: ${doc.document_type.replace("_", " ")}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Image Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingImage) {
                        CircularProgressIndicator()
                    } else if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap!!,
                            contentDescription = "Document Preview",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("Failed to load preview: ${doc.file}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }

                if (isRejecting) {
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Reason for Rejection") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                } else {
                    Text("Status: ${if (doc.is_verified) "Verified" else "Pending"}")
                    if (doc.rejection_reason?.isNotEmpty() == true) {
                        Text("Past Rejection Reason: ${doc.rejection_reason}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isRejecting) {
                    Button(
                        onClick = { onVerify(false, rejectReason) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Confirm Reject") }
                } else {
                    Button(
                        onClick = { onVerify(true, "") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) { Text("Approve") }
                    
                    OutlinedButton(onClick = { isRejecting = true }) {
                        Text("Reject")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

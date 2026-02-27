package com.school.studentportal.shared.ui.screens.admission

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch
import com.school.studentportal.shared.data.repository.AdmissionRepository
import com.school.studentportal.shared.data.model.AdmissionProgram
import com.school.studentportal.shared.utils.PlatformFile
import com.school.studentportal.shared.utils.rememberFilePicker
import androidx.compose.ui.graphics.ImageBitmap
import com.school.studentportal.shared.utils.toImageBitmap
import androidx.compose.foundation.Image
import com.school.studentportal.shared.data.model.AdmissionDocument
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import kotlin.collections.find

@Composable
fun AdmissionWizardScreen(
    repository: AdmissionRepository,
    onNavigateBack: () -> Unit,
    onSuccess: (String) -> Unit // Returns Application ID
) {
    var currentStep by remember { mutableStateOf(1) } // Changed to mutableStateOf for KMP compatibility if mutableIntStateOf specific to version
    var applicationId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Form State
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var meanGrade by remember { mutableStateOf("") }
    var programCode by remember { mutableStateOf("") }
    var programId by remember { mutableStateOf(1) }
    var programs by remember { mutableStateOf<List<AdmissionProgram>>(emptyList()) }
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        val result = repository.getPrograms()
        if (result.isSuccess) {
            programs = result.getOrNull() ?: emptyList()
        } else {
            error = "Failed to load programs: ${result.exceptionOrNull()?.message}"
        }
    }

    Scaffold(
        topBar = {
            // Check implicit OptIn for SmallTopAppBar or use Box/Row for multiplatform safety
            // Material3 TopAppBar is experimental usually
            // We use simple row/text if issues arise, but assuming M3 is available.
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Admission Application") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 700.dp)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally // Centers internal items
            ) {
                LinearProgressIndicator(
                    progress = currentStep / 5f,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                )
                
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                // Card wrapper for professional look
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (currentStep) {
                            1 -> BioDataStep(
                                fName = firstName, lName = lastName, nId = nationalId,
                                onUpdate = { f, l, n -> firstName = f; lastName = l; nationalId = n },
                                onNext = { currentStep = 2 }
                            )
                            2 -> AcademicStep(
                                grade = meanGrade,
                                onUpdate = { meanGrade = it },
                                onNext = { currentStep = 3 },
                                onBack = { currentStep = 1 }
                            )
                            3 -> ProgramStep(
                                selectedCode = programCode,
                                programs = programs,
                                onSelect = { code, id -> 
                                    programCode = code
                                    programId = id
                                },
                                onNext = { currentStep = 4 },
                                onBack = { currentStep = 2 }
                            )
                            4 -> ReviewStep(
                                summary = "Name: $firstName $lastName\nID: $nationalId\nGrade: $meanGrade\nProgram: $programCode",
                                isSubmitting = isSubmitting,
                                onSubmit = {
                                    scope.launch {
                                        isSubmitting = true
                                        error = null
                                        val result = repository.submitApplication(firstName, lastName, nationalId, meanGrade, programId)
                                        isSubmitting = false
                                        if (result.isSuccess) {
                                            applicationId = result.getOrNull()?.application_id
                                            currentStep = 5 
                                        } else {
                                            error = result.exceptionOrNull()?.message ?: "Submission Failed"
                                        }
                                    }
                                },
                                onBack = { currentStep = 3 }
                            )
                            5 -> DocumentUploadStep(
                                applicationId = applicationId!!,
                                nationalId = nationalId,
                                repository = repository,
                                onFinish = { onSuccess(applicationId!!) }
                            )
                        }
                    }
                }
                
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp))
                }
                
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onNavigateBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                     Text("Cancel Application")
                }
            }
        }
    }
}

@Composable
fun BioDataStep(fName: String, lName: String, nId: String, onUpdate: (String, String, String) -> Unit, onNext: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Step 1: Personal Details", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = fName, onValueChange = { onUpdate(it, lName, nId) }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = lName, onValueChange = { onUpdate(fName, it, nId) }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = nId, onValueChange = { onUpdate(fName, lName, it) }, label = { Text("National ID") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = onNext, modifier = Modifier.align(Alignment.End)) { Text("Next") }
    }
}

@Composable
fun AcademicStep(grade: String, onUpdate: (String) -> Unit, onNext: () -> Unit, onBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Step 2: Academic History", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = grade, onValueChange = onUpdate, label = { Text("KCSE Mean Grade") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Button(onClick = onNext) { Text("Next") }
        }
    }
}

@Composable
fun ProgramStep(
    selectedCode: String,
    programs: List<AdmissionProgram>,
    onSelect: (String, Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Step 3: Choose Programme", style = MaterialTheme.typography.titleMedium)
        
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedCode,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Program") },
                trailingIcon = { 
                    IconButton(onClick = { expanded = true }) { 
                         Icon(Icons.Default.ArrowDropDown, null)
                    }
                },
                modifier = Modifier.fillMaxWidth().clickable { expanded = true }
            )
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth().height(300.dp) // Constrain height
            ) {
                programs.forEach { program ->
                    DropdownMenuItem(
                        text = { Text("${program.code} - ${program.name}") },
                        onClick = {
                            onSelect(program.code, program.id)
                            expanded = false
                        }
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Button(onClick = onNext, enabled = selectedCode.isNotEmpty()) { Text("Next") }
        }
    }
}

@Composable
fun ReviewStep(summary: String, isSubmitting: Boolean, onSubmit: () -> Unit, onBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Step 4: Review Application", style = MaterialTheme.typography.titleMedium)
        Text(summary)
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onBack, enabled = !isSubmitting) { Text("Back") }
            Button(onClick = onSubmit, enabled = !isSubmitting) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Save & Continue")
            }
        }
    }
}

@Composable
fun DocumentReviewDialog(
    type: String,
    title: String,
    status: String,
    selectedFile: PlatformFile?,
    onDismiss: () -> Unit,
    onReplace: () -> Unit,
    onRemove: () -> Unit
) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoadingPreview by remember { mutableStateOf(false) }
    
    LaunchedEffect(selectedFile) {
        if (selectedFile != null) {
            val name = selectedFile.name.lowercase()
            if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")) {
                isLoadingPreview = true
                try {
                    val bytes = selectedFile.readBytes()
                    imageBitmap = bytes.toImageBitmap()
                } catch (e: Exception) {
                    println("DEBUG: Failed to load preview: ${e.message}")
                }
                isLoadingPreview = false
            }
        } else {
            imageBitmap = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Document Review: $title") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Type: $type", style = MaterialTheme.typography.bodySmall)
                Text("Current Status: $status", style = MaterialTheme.typography.bodyMedium, color = if (status == "Uploaded") Color(0xFF2E7D32) else Color.Gray)
                if (selectedFile != null) {
                    Text("Selected File: ${selectedFile.name}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    
                    if (isLoadingPreview) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap!!,
                            contentDescription = "Preview",
                            modifier = Modifier.fillMaxWidth().height(200.dp).padding(vertical = 8.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    } else {
                         Text("No preview available for this file type.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }

                    Text("Ready for upload. Please click 'Upload & Continue' on the main screen to save this document.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                } else {
                    Text("No file selected locally.", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
                }
                
                if (status == "Uploaded") {
                    Text("Note: This document is already on the server. You can still replace it by selecting a new file.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onReplace) {
                Text(if (status == "Pending") "Select File" else "Replace File")
            }
        },
        dismissButton = {
            Row {
                if (selectedFile != null && status != "Uploaded") {
                    TextButton(onClick = onRemove, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)) {
                        Text("Remove Selection")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
fun DocumentUploadStep(
    applicationId: String,
    nationalId: String,
    repository: AdmissionRepository,
    onFinish: () -> Unit
) {
    var missingDocs by remember { mutableStateOf<List<String>>(emptyList()) }
    var uploadedDocs by remember { mutableStateOf<List<String>>(emptyList()) }
    val selectedFiles = remember { mutableStateMapOf<String, PlatformFile>() }
    
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    var currentPhase by remember { mutableStateOf<String?>(null) }
    var admEmail by remember { mutableStateOf<String?>(null) }
    var admPassword by remember { mutableStateOf<String?>(null) }
    
    val docNames = mapOf(
        "NATIONAL_ID" to "National ID / Passport",
        "BIRTH_CERT" to "Birth Certificate",
        "RESULT_SLIP" to "KCSE Result Slip",
        "LEAVING_CERT" to "Leaving Certificate",
        "TRANSCRIPT" to "Academic Transcript",
        "PASSPORT_PHOTO" to "Passport Size Photo",
        "MEDICAL_REPORT" to "Medical Report"
    )

    val docGroups = mapOf(
        "Identity & Personal" to listOf("NATIONAL_ID", "BIRTH_CERT", "PASSPORT_PHOTO"),
        "Academic Records" to listOf("RESULT_SLIP", "LEAVING_CERT", "TRANSCRIPT"),
        "Other" to listOf("MEDICAL_REPORT")
    )

    var serverDocObjects by remember { mutableStateOf<List<AdmissionDocument>>(emptyList()) }
    
    fun refresh() {
        scope.launch {
            isLoading = true
            val res = repository.checkStatus(applicationId)
            isLoading = false
            if (res.isSuccess) {
                val app = res.getOrNull()
                missingDocs = app?.missing_documents ?: emptyList()
                uploadedDocs = app?.documents?.map { it.document_type } ?: emptyList()
                serverDocObjects = app?.documents ?: emptyList()
                currentPhase = app?.current_phase
                admEmail = app?.email
                admPassword = app?.credential_password
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }
    
    var activeUploadType by remember { mutableStateOf<String?>(null) }
    val picker = rememberFilePicker { file ->
        if (file != null && activeUploadType != null) {
            selectedFiles[activeUploadType!!] = file
        }
        activeUploadType = null
    }

    var reviewDocType by remember { mutableStateOf<String?>(null) }

    if (reviewDocType != null) {
        val type = reviewDocType!!
        val docObj = serverDocObjects.find { it.document_type == type }
        val status = when {
            selectedFiles.containsKey(type) -> "Selected"
            uploadedDocs.contains(type) -> if (docObj?.is_verified == true) "Verified" else "Uploaded"
            else -> "Pending"
        }
        
        DocumentReviewDialog(
            type = type,
            title = docNames[type] ?: type,
            status = status,
            selectedFile = selectedFiles[type],
            onDismiss = { reviewDocType = null },
            onReplace = { 
                activeUploadType = type
                picker.launch()
                reviewDocType = null
            },
            onRemove = {
                selectedFiles.remove(type)
                reviewDocType = null
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (currentPhase == "ENROLLED" && admEmail != null) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Admission Successful!", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Your Student Credentials:", style = MaterialTheme.typography.titleMedium)
                    Text("Email: $admEmail", style = MaterialTheme.typography.bodyLarge)
                    Text("Password: $admPassword", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Please save these credentials to login.", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
             Text("Step 5: Document Upload", style = MaterialTheme.typography.headlineSmall)
             Text("Select files for all required documents. Click on a card to review or replace the document.", style = MaterialTheme.typography.bodySmall)
        }
        
        if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        if (error != null) {
             Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                 Text(error!!, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall)
             }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            docGroups.forEach { (groupName, types) ->
                Text(groupName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                
                types.forEach { type ->
                    val docObj = serverDocObjects.find { it.document_type == type }
                    val status = when {
                        selectedFiles.containsKey(type) -> "Selected"
                        uploadedDocs.contains(type) -> {
                            if (docObj?.is_verified == true) "Verified" 
                            else if (docObj?.rejection_reason?.isNotEmpty() == true) "Rejected"
                            else "Uploaded"
                        }
                        else -> "Pending"
                    }
                    
                    DocumentCard(
                        title = docNames[type] ?: type,
                        status = status,
                        fileName = selectedFiles[type]?.name,
                        rejectionReason = docObj?.rejection_reason,
                        onClick = { reviewDocType = type }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val hasPending = missingDocs.any { !selectedFiles.containsKey(it) && !uploadedDocs.contains(it) }
        val rejectedDocs = serverDocObjects.filter { it.rejection_reason?.isNotEmpty() == true && it.document_type in uploadedDocs }
        val canSubmit = (selectedFiles.isNotEmpty() || (!hasPending && currentPhase == "DRAFT")) && !isLoading

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    error = null
                    try {
                        var uploadsSuccessful = true
                        val failed = mutableListOf<String>()
                        
                        selectedFiles.forEach { (type, file) ->
                             val res = repository.uploadDocument(applicationId, nationalId, type, file)
                             if (!res.isSuccess) {
                                 uploadsSuccessful = false
                                 failed.add(docNames[type] ?: type)
                                 error = res.exceptionOrNull()?.message
                             }
                        }
                        
                        if (uploadsSuccessful) {
                            selectedFiles.clear()
                            val refreshRes = repository.checkStatus(applicationId)
                            val app = refreshRes.getOrNull()
                            if (app?.missing_documents?.isEmpty() == true) {
                                val phaseRes = repository.submitFinalApplication(applicationId)
                                if (phaseRes.isSuccess) {
                                    onFinish()
                                } else {
                                    error = "Submission failed. Please check required documents."
                                }
                            } else {
                                error = "Some documents are still missing according to the server."
                                refresh()
                            }
                        } else {
                            if (error == null) error = "Failed to upload: ${failed.joinToString(", ")}"
                            refresh()
                        }
                    } catch (e: Exception) {
                        error = "Error: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            }, 
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = canSubmit
        ) {
            val buttonText = if (selectedFiles.isNotEmpty()) "Upload & Continue" else "Finish & Submit"
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White) 
            else Text(buttonText)
        }
    }
}

@Composable
fun DocumentCard(title: String, status: String, fileName: String? = null, rejectionReason: String? = null, onClick: () -> Unit) {
    val containerColor = when(status) {
        "Verified" -> Color(0xFFE8F5E9)
        "Uploaded" -> Color(0xFFF1F8E9)
        "Selected" -> Color(0xFFE3F2FD)
        "Rejected" -> Color(0xFFFFEBEE)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val statusColor = when(status) {
        "Verified" -> Color(0xFF2E7D32)
        "Uploaded" -> Color(0xFF388E3C)
        "Selected" -> MaterialTheme.colorScheme.primary
        "Rejected" -> Color(0xFFD32F2F)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                if (fileName != null) {
                    Text(text = "New File: $fileName", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(text = status, style = MaterialTheme.typography.bodySmall, color = statusColor, fontWeight = FontWeight.Bold)
                if (status == "Rejected" && rejectionReason != null) {
                    Text(text = "Reason: $rejectionReason", style = MaterialTheme.typography.labelSmall, color = statusColor)
                }
            }
            
            when(status) {
                "Verified" -> Icon(Icons.Filled.CheckCircle, "Verified", tint = statusColor)
                "Uploaded" -> Icon(Icons.Filled.Check, "Uploaded", tint = statusColor)
                "Selected" -> Icon(Icons.Filled.FileUpload, "Selected", tint = statusColor)
                "Rejected" -> Icon(Icons.Filled.Error, "Rejected", tint = statusColor)
                else -> Icon(Icons.Filled.FileUpload, "Pending", tint = Color.Gray)
            }
        }
    }
}

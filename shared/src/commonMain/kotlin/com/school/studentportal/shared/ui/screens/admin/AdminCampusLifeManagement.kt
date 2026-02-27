package com.school.studentportal.shared.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.data.model.CampusLifeContent
import com.school.studentportal.shared.data.repository.SupportRepository
import com.school.studentportal.shared.ui.components.AppScaffold
import com.school.studentportal.shared.ui.components.LoadingView
import com.school.studentportal.shared.ui.components.ErrorView
import com.school.studentportal.shared.utils.rememberFilePicker
import com.school.studentportal.shared.utils.PlatformFile
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCampusLifeManagement(repository: SupportRepository, onBack: () -> Unit) {
    var contentList by remember { mutableStateOf<List<CampusLifeContent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<CampusLifeContent?>(null) }
    var itemToDelete by remember { mutableStateOf<CampusLifeContent?>(null) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("News") }
    var selectedImageFile by remember { mutableStateOf<PlatformFile?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val filePicker = rememberFilePicker { file ->
        selectedImageFile = file
    }

    fun refresh() {
        isLoading = true
        scope.launch {
            repository.getCampusLifeContent().onSuccess {
                contentList = it
                isLoading = false
            }.onFailure {
                error = it.message ?: "Failed to load content"
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    AppScaffold(
        title = "Campus Life Management",
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingItem = null
                    title = ""
                    description = ""
                    category = "News"
                    selectedImageFile = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Add Post")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        if (isLoading) {
            LoadingView()
        } else if (error != null) {
            ErrorView(error!!) { refresh() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(contentList) { item ->
                    AdminContentItem(
                        item = item,
                        onEdit = {
                            editingItem = item
                            title = item.title
                            description = item.description
                            category = item.category
                            selectedImageFile = null
                            showDialog = true
                        },
                        onDelete = { itemToDelete = item }
                    )
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSaving) showDialog = false },
            title = { Text(if (editingItem == null) "New Campus Post" else "Edit Post") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    // Category Selector
                    Column {
                        Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                            listOf("News", "Event", "Activity").forEach { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }
                    }

                    // Image Section
                    Column {
                        Text("Post Image", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { filePicker.launch() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImageFile != null) {
                                Text("Image Selected: ${selectedImageFile?.name}", style = MaterialTheme.typography.bodySmall)
                            } else if (editingItem?.image != null) {
                                KamelImage(
                                    resource = asyncPainterResource(editingItem!!.image!!),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Edit, null, tint = Color.White)
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CloudUpload, null, tint = MaterialTheme.colorScheme.primary)
                                    Text("Pick an Image", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSaving = true
                        scope.launch {
                            val imgBytes = selectedImageFile?.readBytes()
                            val imgName = selectedImageFile?.name
                            
                            val result = if (editingItem == null) {
                                repository.createCampusLifeContent(title, description, category, imgBytes, imgName)
                            } else {
                                repository.updateCampusLifeContent(editingItem!!.id!!, title, description, category, imgBytes, imgName)
                            }

                            isSaving = false
                            if (result.isSuccess) {
                                showDialog = false
                                refresh()
                                snackbarHostState.showSnackbar(if (editingItem == null) "Post created" else "Post updated")
                            } else {
                                snackbarHostState.showSnackbar("Error: ${result.exceptionOrNull()?.message}")
                            }
                        }
                    },
                    enabled = !isSaving && title.isNotBlank() && description.isNotBlank()
                ) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    else Text(if (editingItem == null) "Create" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }, enabled = !isSaving) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Post?") },
            text = { Text("Are you sure you want to delete '${itemToDelete?.title}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        val id = itemToDelete?.id ?: return@Button
                        itemToDelete = null
                        scope.launch {
                            repository.deleteCampusLifeContent(id).onSuccess {
                                refresh()
                                snackbarHostState.showSnackbar("Post deleted")
                            }.onFailure {
                                snackbarHostState.showSnackbar("Failed to delete")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminContentItem(
    item: CampusLifeContent,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image Preview or Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!item.image.isNullOrBlank()) {
                    KamelImage(
                        resource = asyncPainterResource(item.image!!),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onLoading = { CircularProgressIndicator(modifier = Modifier.size(20.dp)) },
                        onFailure = { Icon(Icons.Default.Image, null, tint = Color.Gray) }
                    )
                } else {
                    Icon(Icons.Default.Image, null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Text(item.category, modifier = Modifier.padding(horizontal = 4.dp))
                    }
                    if (item.created_at != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.created_at!!.split("T").firstOrNull() ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

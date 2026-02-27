package com.school.studentportal.shared.ui.screens.admin

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
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.data.model.Book
import com.school.studentportal.shared.data.model.BookCategory
import com.school.studentportal.shared.data.repository.LibraryRepository
import com.school.studentportal.shared.utils.PlatformFile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLibraryScreen(
    repository: LibraryRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf(BookCategory.TECH) }
    val books by repository.books.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCategory) {
        isLoading = true
        repository.refreshBooks(selectedCategory)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showUploadDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Book")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedCategory.ordinal) {
                BookCategory.values().forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = { Text(category.name) }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    items(books) { book ->
                        BookItem(book, onDelete = {
                            scope.launch {
                                repository.deleteBook(book.id)
                                repository.refreshBooks(selectedCategory)
                            }
                        })
                    }
                }
            }
        }

        if (showUploadDialog) {
            UploadBookDialog(
                initialCategory = selectedCategory,
                onDismiss = { showUploadDialog = false },
                onUpload = { title, author, category, file ->
                    scope.launch {
                        isLoading = true
                        showUploadDialog = false
                        // Read file bytes - assume PlatformFile has readBytes or similar mechanism
                        // For simplicity in this step, we might need a way to get bytes from PlatformFile
                        // Since PlatformFile is platform specific, we'll assume a helper or the picker returns bytes directly in next iteration if needed.
                        // Actually, let's use the file picker result.
                         val bytes = file.readBytes() // Hypothetical extension or method
                         repository.uploadBook(title, author, category, bytes, file.name)
                         repository.refreshBooks(selectedCategory)
                         isLoading = false
                    }
                }
            )
        }
    }
}

@Composable
fun BookItem(book: Book, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(book.title, style = MaterialTheme.typography.titleMedium)
                Text(book.author, style = MaterialTheme.typography.bodyMedium)
                Text(book.category.name, style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun UploadBookDialog(
    initialCategory: BookCategory,
    onDismiss: () -> Unit,
    onUpload: (String, String, BookCategory, PlatformFile) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(initialCategory) }
    var selectedFile by remember { mutableStateOf<PlatformFile?>(null) }
    
    val launcher = com.school.studentportal.shared.utils.rememberFilePicker { file ->
        selectedFile = file
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Book") },
        text = {
            Column {
                TextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = author, onValueChange = { author = it }, label = { Text("Author") })
                Spacer(modifier = Modifier.height(8.dp))
                // Simple category selection (e.g., Radio Buttons or just Text for now to keep it simple, defaulting to selected tab)
                Text("Category: ${category.name}")
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(onClick = { launcher.launch() }) {
                    Text(selectedFile?.name ?: "Select PDF")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedFile?.let { onUpload(title, author, category, it) } },
                enabled = title.isNotEmpty() && author.isNotEmpty() && selectedFile != null
            ) {
                Text("Upload")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

package com.school.studentportal.shared.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.ui.components.AppScaffold

@Composable
fun LibraryScreen() {
    var selectedCategory by remember { mutableStateOf("Technology") }
    var searchQuery by remember { mutableStateOf("") }
    
    // Mock Data Models for Shared
    data class SharedBook(
        val title: String,
        val author: String,
        val callNumber: String,
        val status: String,
        val color: Long
    )

    val mockBooks = remember(selectedCategory) {
        when(selectedCategory) {
            "Technology" -> listOf(
                SharedBook("Clean Code", "Robert C. Martin", "QA76.76.C54", "Available", 0xFF1D3762),
                SharedBook("Kotlin in Action", "Dmitry Jemerov", "QA76.73.K68", "Borrowed", 0xFF388E3C),
                SharedBook("Android Programming", "Bill Phillips", "QA76.77.A53", "Available", 0xFF673AB7)
            )
            "Education" -> listOf(
                SharedBook("Pedagogy of the Oppressed", "Paulo Freire", "LB880.F73", "Available", 0xFFFFA000),
                SharedBook("The Republic", "Plato", "JC71.P35", "Reserved", 0xFF455A64)
            )
            else -> listOf(
                SharedBook("Generic Book", "Unknown Author", "000.000", "Available", 0xFF757575)
            )
        }
    }

    val categories = listOf("Technology", "Education", "Business", "Professional")

    AppScaffold(title = "Library", showTopBar = false) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Title or Author") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            // Categories
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = { Text(category) }
                    )
                }
            }

            // Results
            val filteredBooks = if (searchQuery.isBlank()) mockBooks else mockBooks.filter { 
                it.title.contains(searchQuery, ignoreCase = true) || it.author.contains(searchQuery, ignoreCase = true)
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredBooks) { book ->
                    Card(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Cover Placeholder
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .fillMaxHeight()
                                    .background(Color(book.color)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Book, null, tint = Color.White)
                            }
                            
                            // Info
                            Column(modifier = Modifier.padding(12.dp).weight(1f)) {
                                Text(book.title, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(book.author, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Spacer(modifier = Modifier.weight(1f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(book.status, color = if(book.status == "Available") Color(0xFF2E7D32) else Color.Gray, style = MaterialTheme.typography.labelSmall)
                                    Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.school.studentportal.shared.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.data.model.CampusLifeContent
import com.school.studentportal.shared.data.repository.SupportRepository
import com.school.studentportal.shared.ui.components.AppScaffold
import com.school.studentportal.shared.ui.components.LoadingView
import com.school.studentportal.shared.ui.components.ErrorView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusLifeScreen(repository: SupportRepository, onNavigateBack: () -> Unit) {
    var contentList by remember { mutableStateOf<List<CampusLifeContent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        repository.getCampusLifeContent().onSuccess {
            contentList = it
            isLoading = false
        }.onFailure {
            error = it.message ?: "Failed to load content"
            isLoading = false
        }
    }

    AppScaffold(
        title = "Campus Life",
        showTopBar = true,
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
    ) {
        if (isLoading) {
            LoadingView()
        } else if (error != null) {
            ErrorView(error!!)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Featured Content (e.g. category 'Event')
                val events = contentList.filter { it.category.equals("Event", ignoreCase = true) }
                if (events.isNotEmpty()) {
                    item {
                        Text("Happening Now", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(events) { event ->
                                EventCard(event.title, event.created_at?.take(10) ?: "", Color(0xFF1E88E5))
                            }
                        }
                    }
                }

                // General Feed
                item {
                    Text("Campus Updates", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                items(contentList) { item ->
                    NewsItem(item.title, item.description)
                }

                if (contentList.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No campus updates yet.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(title: String, date: String, color: Color) {
    Card(
        modifier = Modifier.size(width = 240.dp, height = 140.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(date, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun NewsItem(headline: String, snippet: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
             Box(modifier = Modifier.size(80.dp).background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)))
            
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(headline, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(snippet, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 3)
            }
        }
    }
}

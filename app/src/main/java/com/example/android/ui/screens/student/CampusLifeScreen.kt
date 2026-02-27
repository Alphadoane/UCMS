package com.example.android.ui.screens.student

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android.ui.components.PortalScaffold

@Composable
fun CampusLifeScreen() {
    PortalScaffold(title = "Campus Life") {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            // Events Carousel
            item {
                Text("Happening Now", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { EventCard("Tech Week", "12 Oct", Color(0xFF1E88E5)) }
                    item { EventCard("Cultural Day", "15 Oct", Color(0xFFE91E63)) }
                    item { EventCard("Sports Gala", "20 Oct", Color(0xFF43A047)) }
                }
            }

            // News Feed
            item {
                Text("Campus News", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    NewsItem("Library Hours Extended", "The library will now open until 10 PM...")
                    NewsItem("Exam Schedule Released", "Check your student portal for draft timetable...")
                    NewsItem("New Cafeteria Opening", "The West Wing cafeteria opens on Monday...")
                }
            }
            
            // Clubs
             item {
                Text("My Clubs", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                 Spacer(modifier = Modifier.height(12.dp))
                 Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                     FilterChip(selected = true, onClick = {}, label = { Text("Robotics") })
                     FilterChip(selected = true, onClick = {}, label = { Text("Debate") })
                     FilterChip(selected = false, onClick = {}, label = { Text("Music +") })
                 }
            }
        }
    }
}

@Composable
fun EventCard(title: String, date: String, color: Color) {
    Card(
        modifier = Modifier.size(width = 200.dp, height = 120.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(date, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun NewsItem(headline: String, snippet: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(60.dp).background(Color.LightGray, RoundedCornerShape(8.dp)))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(headline, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(snippet, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 2)
        }
    }
}

package com.example.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * eFootball Style Player Card for Student Profile
 */
@Composable
fun GamerProfileCard(
    name: String,
    regNo: String,
    course: String,
    gpa: Double // 4.0 Scale
) {
    // eFootball Colors
    val deepBlue = Color(0xFF1A1F38)
    val vibrantPurple = Color(0xFF6200EA)
    val neonPink = Color(0xFFF50057)
    val gold = Color(0xFFFFD700)
    
    // Calculate "OVR" (Overall Rating)
    val ovr = (gpa * 25).toInt().coerceIn(0, 99)
    val cardColor = if (ovr >= 90) gold else if (ovr >= 80) Color(0xFFC0C0C0) else Color(0xFFCD7F32)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(deepBlue, vibrantPurple),
                    tileMode = TileMode.Clamp
                ),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // Decorative Slanted Background overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 120.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.2f),
                    shape = CutCornerShape(topStart = 80.dp) 
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: OVR Badge & Photo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(100.dp)
            ) {
                // OVR BADGE
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(cardColor, CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$ovr",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black.copy(alpha = 0.8f),
                            lineHeight = 32.sp
                        )
                        Text(
                            text = "OVR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.6f),
                            fontSize = 8.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Photo Placeholder
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person, 
                        contentDescription = null, 
                        tint = Color.White, 
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // RIGHT: Info & Stats
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = course,
                    style = MaterialTheme.typography.bodyMedium,
                    color = gold,
                    fontWeight = FontWeight.Medium
                )
                 Text(
                    text = regNo,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))
                
                // STATS ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem("ATT", "94", neonPink)
                    StatItem("ASG", "88", Color.Cyan)
                    StatItem("EXM", "91", Color.Green)
                    StatItem("DIS", "99", Color.White)
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 10.sp
        )
    }
}

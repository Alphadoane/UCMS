package com.school.studentportal.shared.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RollingWheelMenu(
    items: List<Pair<String, String>>,
    currentRoute: String,
    onItemClick: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    Box(modifier = Modifier.fillMaxWidth()) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = 150.dp), // Padding to allow scrolling top/bottom to center
            modifier = Modifier.height(600.dp) // Fixed height for the "wheel" view window
        ) {
            itemsIndexed(items) { index, item ->
                val (title, route) = item
                val isSelected = currentRoute == route

                // Calculate layout attributes based on scroll position
                val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }
                val visibleItemsInfo = layoutInfo.visibleItemsInfo
                val itemInfo = visibleItemsInfo.find { it.index == index }
                
                var scale = 0.7f
                var alpha = 0.5f
                var rotationX = 0f
                
                if (itemInfo != null) {
                    val centerOffset = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                    val itemCenter = itemInfo.offset + itemInfo.size / 2
                    val distanceFromCenter = abs(centerOffset - itemCenter)
                    val viewportSize = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
                    
                    // Normalize distance: 0 at center, 1 at edges
                    val normalizedDistance = (distanceFromCenter.toFloat() / (viewportSize / 2)).coerceIn(0f, 1f)
                    
                    scale = 0.7f + (0.3f * (1 - normalizedDistance)) // 1.0 at center, 0.7 at edges
                    alpha = 0.5f + (0.5f * (1 - normalizedDistance)) // 1.0 at center, 0.5 at edges
                    // rotationX = normalizedDistance * 20f * (if (itemCenter < centerOffset) 1 else -1) // Simplify 3D effect for potential desktop issues
                }

                Card(
                    onClick = { onItemClick(route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            // this.rotationX = rotationX 
                            this.alpha = alpha
                            // cameraDistance = 12 * density
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF1D3762) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 8.dp else 2.dp
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) Color.White else Color(0xFF1D3762),
                            fontSize = 18.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

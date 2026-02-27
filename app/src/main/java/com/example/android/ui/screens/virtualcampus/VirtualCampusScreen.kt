package com.example.android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VirtualCampusScreen(user: com.example.android.data.model.User? = null, onNavigate: (String) -> Unit) {
    val items = mutableListOf(
        "Dashboard" to "vc_dashboard"
    )
    
    // "My Courses" is for students. Staff have "Lectures" and "Zoom Rooms"
    if (user?.role != com.example.android.data.model.UserRole.STAFF) {
        items.add("My Courses" to "vc_my_courses")
    }
    
    items.add("Lectures" to "vc_lectures")
    items.add("Zoom Rooms" to "vc_zoom_rooms")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        items.forEach { (label, route) ->
            NavigationDrawerItem(
                label = { Text(label) },
                selected = false,
                onClick = { onNavigate(route) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}

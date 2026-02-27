package com.school.studentportal.shared.ui.screens.student.virtualcampus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.data.model.User
import com.school.studentportal.shared.data.model.UserRole
import com.school.studentportal.shared.ui.Routes
import com.school.studentportal.shared.ui.components.AppScaffold

@Composable
fun VirtualCampusScreen(user: User?, onNavigate: (String) -> Unit) {
    val items = mutableListOf(
        "Dashboard" to Routes.VC_DASHBOARD
    )
    
    if (user?.role != UserRole.STAFF && user?.role != UserRole.LECTURER) {
        items.add("My Courses" to Routes.VC_MY_COURSES)
    }
    
    items.add("Lectures" to Routes.VC_LECTURES)
    items.add("Zoom Rooms" to Routes.VC_ZOOM_ROOMS)
    
    AppScaffold(title = "Virtual Campus", showTopBar = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
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
}

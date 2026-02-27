package com.school.studentportal.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.data.model.User
import com.school.studentportal.shared.data.model.UserRole
import com.school.studentportal.shared.ui.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffoldWithDrawer(
    user: User,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    title: String? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val role = user.role
    
    val topLevelItems = when (role) {
        UserRole.STAFF -> listOf(
            "Home" to Routes.HOME,
            "Profile" to Routes.PROFILE,
            "Virtual Campus" to Routes.VIRTUAL_CAMPUS,
            "Library" to Routes.LIBRARY,
            "Complaints" to Routes.COMPLAINTS
        )
        UserRole.ADMIN -> listOf(
            "Home" to Routes.HOME,
            "My Profile" to Routes.PROFILE,
            "Admissions" to "admin_admissions",
            "Programs" to "admin_programs",
            "Finance" to "admin_finance",
            "Users" to "admin_create_user",
            "Broadcasts" to "admin_broadcast",
            "Library" to Routes.LIBRARY
        )
        else -> listOf(
            "Home" to Routes.HOME,
            "Profile" to Routes.PROFILE,
            "Academics" to Routes.ACADEMICS,
            "Finance" to Routes.FINANCE,
            "Campus Life" to "student_campus_life",
            "Virtual Campus" to Routes.VIRTUAL_CAMPUS,
            "Library" to Routes.LIBRARY,
            "Complaints" to Routes.COMPLAINTS,
            "Voting System" to Routes.VOTING
        )
    }

    val isTopLevel = topLevelItems.any { it.second == currentRoute }
    val displayTitle = title ?: topLevelItems.firstOrNull { it.second == currentRoute }?.first ?: currentRoute.substringAfter("_").replace("_", " ").capitalizeWords()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Student Portal",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp, start = 16.dp)
                    )
                    RollingWheelMenu(
                        items = topLevelItems,
                        currentRoute = currentRoute,
                        onItemClick = { route ->
                            scope.launch { drawerState.close() }
                            onNavigate(route)
                        }
                    )
                }
            }
        },
        gesturesEnabled = isTopLevel
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = displayTitle, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        if (isTopLevel) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        } else {
                            IconButton(onClick = { onBack?.invoke() ?: onNavigate(Routes.HOME) }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            floatingActionButton = {
                if (isTopLevel) {
                    FloatingActionButton(
                        onClick = { /* AI Chat logic */ },
                        containerColor = Color(0xFF1D3762),
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Chat")
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    )
                    .padding(innerPadding)
            ) {
                content(innerPadding)
            }
        }
    }
}



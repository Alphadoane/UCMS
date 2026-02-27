package com.example.android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch
import com.example.android.Routes
import com.example.android.data.model.User
import com.example.android.data.model.UserRole
import com.example.android.ui.navigation.AppNavHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffoldWithDrawer(navController: NavHostController, initialUser: User? = null, onLogout: () -> Unit, onImpersonate: (String) -> Unit) {
    // Show loading if user data is missing but expected
    if (initialUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1D3762))
        }
        return
    }
    
    val role = initialUser.role ?: UserRole.STUDENT
    
    // For Admin, remove the drawer/outer scaffold completely
    if (role == UserRole.ADMIN) {
        AppNavHost(
            navController = navController,
            initialUser = initialUser,
            onLogout = onLogout,
            onImpersonate = onImpersonate,
            modifier = Modifier.fillMaxSize()
        )
        return
    }
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val items = when (role) {
        UserRole.STAFF, UserRole.LECTURER -> listOf(
            "Home" to Routes.HOME,
            "Profile" to Routes.PROFILE,
            "My Courses" to "staff_courses",
            "Virtual Campus" to Routes.VIRTUAL_CAMPUS,
            "Library" to Routes.LIBRARY,
            "Complaints" to Routes.COMPLAINTS
        )
        else -> listOf( // Student and others
            "Home" to Routes.HOME,
            "Profile" to Routes.PROFILE,
            "Academics" to Routes.ACADEMICS,
            "Finance" to Routes.FINANCE,
            "Virtual Campus" to Routes.VIRTUAL_CAMPUS,
            "Library" to Routes.LIBRARY,
            "Complaints" to Routes.COMPLAINTS,
            "Voting System" to Routes.VOTING
        )
    }

    var isChatOpen by rememberSaveable { mutableStateOf(false) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.HOME

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Student Portal",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp, start = 16.dp)
                )
                RollingWheelMenu(
                    items = items,
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        scope.launch { drawerState.close() }
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            // Removed fixed top bar and nested scroll behavior for a truly immersive experience
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { isChatOpen = !isChatOpen },
                    containerColor = Color(0xFF1D3762),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Chat")
                }
            }
        ) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                AppNavHost(
                    navController = navController,
                    initialUser = initialUser,
                    onLogout = onLogout,
                    onImpersonate = onImpersonate,
                    modifier = Modifier.fillMaxSize(),
                    // Pass drawer toggle so screens can add it to their immersive headers
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
                
                ChatOverlay(isVisible = isChatOpen, onDismiss = { isChatOpen = false })
            }
        }
    }
}

/**
 * A minimal, integrated menu toggle that fits into professional immersive headers.
 */
@Composable
fun IntegratedMenuToggle(onOpenDrawer: () -> Unit) {
    IconButton(
        onClick = onOpenDrawer,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Open Menu",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

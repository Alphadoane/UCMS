package com.example.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.example.android.ui.theme.AndroidTheme
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

// Data Layers
import com.example.android.data.repository.AuthRepository
import com.example.android.data.model.User
import com.example.android.data.model.UserRole
import timber.log.Timber

// Screens used in Main Entry logic
import com.example.android.ui.screens.LoginScreen
import com.school.studentportal.shared.ui.screens.admission.AdmissionWizardScreen as SharedAdmissionWizardScreen
import com.school.studentportal.shared.data.repository.AdmissionRepository as SharedAdmissionRepository
import com.school.studentportal.shared.data.network.SharedApiService
import com.school.studentportal.shared.data.network.TokenManager

// Expanded Components
import com.example.android.ui.components.AppScaffoldWithDrawer
import com.example.android.ui.navigation.AppNavHost 
// Note: AppScaffoldWithDrawer is in ui/components, AppNavHost in ui/navigation. 
// However, AppScaffoldWithDrawer might not be exported publicly? Kotlin is public by default.

@dagger.hilt.android.AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.i("Timber initialized in debug build")
        }
        enableEdgeToEdge()
        // Initialize API immediately
        com.example.android.data.network.NetworkModule.createApiService(this)
        
        // Schedule Background Sync
        val syncRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.android.worker.SyncWorker>(
            15, java.util.concurrent.TimeUnit.MINUTES
        )
            .setConstraints(
                androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                    .build()
            )
            .build()
            
        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "AcademicsSync",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
        
        setContent {
            AndroidTheme {
                var isAuthenticated by rememberSaveable { mutableStateOf(false) }
                var isApplying by rememberSaveable { mutableStateOf(false) }
                var initialUser by remember { mutableStateOf<User?>(null) }
                val authRepository = AuthRepository(this)
                val tokenManager = remember { TokenManager() }
                val sharedApi = remember { SharedApiService(tokenManager) }
                val sharedAdmissionRepo = remember { SharedAdmissionRepository(sharedApi) }
                // Repositories are now instantiated in screens as needed (Modular architecture)
                
                // Check if user is already logged in
                LaunchedEffect(Unit) {
                    val loggedIn = authRepository.isLoggedIn()
                    if (loggedIn) {
                        isAuthenticated = true // Optimistic update
                        val token = authRepository.getCurrentToken()
                        if (token != null) {
                            tokenManager.saveTokens(token, "") 
                        }
                        authRepository.getProfile().onSuccess { p ->
                             initialUser = User(
                                id = p.id,
                                email = p.email ?: "",
                                firstName = p.full_name?.split(" ")?.firstOrNull() ?: "",
                                lastName = p.full_name?.split(" ")?.drop(1)?.joinToString(" ") ?: "",
                                role = UserRole.fromString(p.role),
                                regNumber = p.admission_no,
                                course = p.course
                            )
                        }.onFailure {
                            // If profile fetch fails (e.g. token expired), logout
                            authRepository.logout()
                            isAuthenticated = false
                        }
                    }
                }
                
                if (isApplying) {
                    SharedAdmissionWizardScreen(
                        repository = sharedAdmissionRepo,
                        onNavigateBack = { isApplying = false },
                        onSuccess = { appId -> 
                            isApplying = false
                            // Optional: Show success dialog/toast logic here
                        }
                    )
                } else if (!isAuthenticated) {
                    LoginScreen(
                        onLogin = { email, password, onError, onComplete ->
                            if (email == "APPLY") {
                                isApplying = true
                                onComplete()
                            } else {
                                lifecycleScope.launch {
                                authRepository.login(email, password)
                                    .onSuccess { result -> 
                                        // result is Pair<String, ProfileResponse?>
                                        isAuthenticated = true 
                                        tokenManager.saveTokens(result.first, "")
                                        result.second?.let { p ->
                                            initialUser = User(
                                                id = p.id,
                                                email = p.email ?: "",
                                                firstName = p.full_name?.split(" ")?.firstOrNull() ?: "",
                                                lastName = p.full_name?.split(" ")?.drop(1)?.joinToString(" ") ?: "",
                                                role = UserRole.fromString(p.role),
                                                regNumber = p.admission_no,
                                                course = p.course
                                            )
                                        }
                                    }
                                    .onFailure { exception ->
                                        onError(exception.message ?: "Login failed")
                                    }
                                onComplete()
                            }
                        }
                    },
                        onSignUp = { email, password, fullName, admissionNo, onError, onComplete ->
                           // ... keep existing
                            lifecycleScope.launch {
                                authRepository.signUp(email, password, fullName, admissionNo)
                                    .onSuccess { isAuthenticated = true }
                                    .onFailure { exception ->
                                        onError(exception.message ?: "Sign up failed")
                                    }
                                onComplete()
                            }
                        }
                    )
                } else {
                    val navController = rememberNavController()
                    AppScaffoldWithDrawer(
                        navController = navController,
                        initialUser = initialUser,
                        onLogout = { 
                            lifecycleScope.launch {
                                authRepository.logout()
                                isAuthenticated = false
                                initialUser = null
                            }
                        },
                        onImpersonate = { targetEmail ->
                            lifecycleScope.launch {
                                authRepository.impersonateUser(targetEmail)
                                    .onSuccess { result ->
                                        // result.first is token, second is profile
                                        // Update state with new user
                                        result.second?.let { p ->
                                            initialUser = User(
                                                id = p.id,
                                                email = p.email ?: "",
                                                firstName = p.full_name?.split(" ")?.firstOrNull() ?: "",
                                                lastName = p.full_name?.split(" ")?.drop(1)?.joinToString(" ") ?: "",
                                                role = UserRole.fromString(p.role),
                                                regNumber = p.admission_no,
                                                course = p.course
                                            )
                                            // Reset Navigation to Home (now as the new user)
                                            navController.navigate(Routes.HOME) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    }
                                    .onFailure { e ->
                                        android.util.Log.e("Impersonate", "Failed", e)
                                        // Optional: Show toast or snackbar
                                    }
                            }
                        }
                    )
                }
            }
        }
    }
}

package com.school.studentportal.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.school.studentportal.shared.ui.screens.LoginScreen
import com.school.studentportal.shared.ui.screens.HomeScreen
import com.school.studentportal.shared.ui.viewmodel.DashboardUiState
import com.school.studentportal.shared.data.model.User
import com.school.studentportal.shared.data.model.UserRole
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import com.school.studentportal.shared.ui.Routes
import kotlinx.coroutines.launch
import com.school.studentportal.shared.data.network.TokenManager
import com.school.studentportal.shared.data.network.SharedApiService
import com.school.studentportal.shared.data.repository.SupportRepository
import com.school.studentportal.shared.data.repository.AuthRepository
import com.school.studentportal.shared.data.repository.AcademicsRepository
import com.school.studentportal.shared.data.repository.AdminRepository
import com.school.studentportal.shared.data.repository.StaffRepository
import com.school.studentportal.shared.data.repository.FinanceRepository
import com.school.studentportal.shared.data.repository.VotingRepository
import com.school.studentportal.shared.data.repository.AnalyticsRepository
import com.school.studentportal.shared.ui.viewmodel.VotingViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import com.school.studentportal.shared.ui.screens.admin.AdminElectionScreen
import com.school.studentportal.shared.ui.screens.admin.AdminAuditScreen
import com.school.studentportal.shared.ui.screens.admin.AdminCampusLifeManagement
import com.school.studentportal.shared.ui.screens.admin.AdminHealthManagement
import com.school.studentportal.shared.ui.screens.admin.AdminReportsScreen
import com.school.studentportal.shared.ui.screens.ProfileScreen
import com.school.studentportal.shared.ui.screens.AcademicsScreen
import com.school.studentportal.shared.ui.screens.LibraryScreen
import com.school.studentportal.shared.ui.screens.VotingSystemScreen
import com.school.studentportal.shared.ui.screens.ComplaintScreen
import com.school.studentportal.shared.ui.screens.student.health.HealthScreen
import com.school.studentportal.shared.ui.screens.student.StudentAnalyticsScreen
import com.school.studentportal.shared.ui.screens.student.finance.FinanceScreen
import com.school.studentportal.shared.ui.screens.student.finance.FeePaymentScreen
import com.school.studentportal.shared.ui.screens.student.finance.ViewBalanceScreen
import com.school.studentportal.shared.ui.screens.student.finance.ReceiptsScreen
import com.school.studentportal.shared.ui.screens.student.virtualcampus.VirtualCampusScreen
import com.school.studentportal.shared.ui.screens.student.virtualcampus.VCDashboardScreen
import com.school.studentportal.shared.ui.screens.student.virtualcampus.VCMyCoursesScreen
import com.school.studentportal.shared.ui.screens.student.virtualcampus.MeetingScreen
import com.school.studentportal.shared.ui.screens.student.virtualcampus.ZoomRoomsScreen
import com.school.studentportal.shared.ui.screens.student.virtualcampus.UnitContentScreen
import com.school.studentportal.shared.ui.screens.student.academics.CourseRegistrationScreen
import com.school.studentportal.shared.ui.screens.student.academics.ExamResultScreen
import com.school.studentportal.shared.ui.screens.student.academics.ExamCardScreen
import com.school.studentportal.shared.ui.screens.student.academics.AcademicInsightsScreen
import com.school.studentportal.shared.ui.screens.PlaceholderScreen
import com.school.studentportal.shared.ui.screens.admin.AdminDashboardScreen
import com.school.studentportal.shared.ui.screens.staff.StaffDashboardScreen
import com.school.studentportal.shared.ui.screens.staff.StaffCoursesScreen
import com.school.studentportal.shared.ui.screens.staff.StaffGradingScreen
import com.school.studentportal.shared.ui.screens.staff.StaffStudentLookupScreen
import com.school.studentportal.shared.ui.screens.staff.StaffContentUploadScreen
import com.school.studentportal.shared.ui.screens.staff.StaffLecturesScreen
import com.school.studentportal.shared.ui.screens.student.CampusLifeScreen
import com.school.studentportal.shared.ui.components.AppScaffoldWithDrawer

@Composable
fun App() {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        
        // Dependencies (Manually injected for now)
        val tokenManager = remember { TokenManager() }
        val apiService = remember { SharedApiService(tokenManager) }
        val authRepository = remember { AuthRepository(apiService, tokenManager) }
        val academicsRepository = remember { AcademicsRepository(apiService) }
        val adminRepository = remember { AdminRepository(apiService) }
        val staffRepository = remember { StaffRepository(apiService) }
        val financeRepository = remember { FinanceRepository(apiService) }
        val votingRepository = remember { VotingRepository(apiService) }
        val supportRepository = remember { SupportRepository(apiService) }
        val analyticsRepository = remember { AnalyticsRepository(apiService) }
        val virtualCampusRepository = remember { com.school.studentportal.shared.data.repository.VirtualCampusRepository(apiService) }

        var currentScreen by remember { mutableStateOf("login") }
        
        // Observe user state from repository
        val user by authRepository.currentUser.collectAsState()
        
        // Check session on startup
        LaunchedEffect(Unit) {
            if (authRepository.checkSession()) {
                currentScreen = Routes.HOME
            }
        }
        
        when (currentScreen) {
            "login" -> {
                LoginScreen(
                    onLogin = { email, password, onError, onSuccess ->
                        if (email == "APPLY") {
                            currentScreen = "admission_wizard"
                        } else {
                            scope.launch {
                                val result = authRepository.login(email, password)
                                if (result.isSuccess) {
                                    onSuccess()
                                    currentScreen = Routes.HOME
                                } else {
                                    onError(result.exceptionOrNull()?.message ?: "Login failed")
                                }
                            }
                        }
                    },
                    onSignUp = { _, _, _, _, onError, onSuccess ->
                         currentScreen = "admission_wizard"
                    },
                    onRequestPasswordReset = { _, cb -> cb(Result.success("Code sent")) },
                    onVerifyPasswordReset = { _, _, _, cb -> cb(Result.success("Reset success")) },
                    onCheckApplicationStatus = { _, cb -> cb(Result.failure(Exception("Not impl"))) }
                )
            }
            "admission_wizard" -> {
                val admissionRepository = remember { com.school.studentportal.shared.data.repository.AdmissionRepository(apiService) }
                com.school.studentportal.shared.ui.screens.admission.AdmissionWizardScreen(
                    repository = admissionRepository,
                    onNavigateBack = { currentScreen = "login" },
                    onSuccess = { appId -> currentScreen = "login" }
                )
            }

            else -> {
                val currentUser = user
                if (currentUser != null) {
                    val backRoute: (() -> Unit)? = when {
                        currentScreen.startsWith(Routes.FINANCE) && currentScreen != Routes.FINANCE -> {{ currentScreen = Routes.FINANCE }}
                        currentScreen.startsWith("vc_") && currentScreen != Routes.VIRTUAL_CAMPUS -> {{ currentScreen = Routes.VIRTUAL_CAMPUS }}
                        currentScreen.startsWith(Routes.ACADEMICS) && currentScreen != Routes.ACADEMICS -> {{ currentScreen = Routes.ACADEMICS }}
                        currentScreen != Routes.HOME -> {{ currentScreen = Routes.HOME }}
                        else -> null
                    }

                    AppScaffoldWithDrawer(
                        user = currentUser,
                        currentRoute = currentScreen,
                        onNavigate = { dest -> currentScreen = dest },
                        onBack = backRoute
                    ) { paddingValues ->
                        when {
                            currentScreen == Routes.HOME -> {
                                if (currentUser.role == UserRole.STAFF) {
                                    StaffDashboardScreen(user = currentUser, repository = staffRepository, onNavigate = { currentScreen = it })
                                } else if (currentUser.role == UserRole.ADMIN) {
                                    AdminDashboardScreen(user = currentUser, repository = adminRepository, onNavigate = { currentScreen = it })
                                } else {
                                    HomeScreen(uiState = DashboardUiState(user = currentUser), onNavigate = { currentScreen = it })
                                }
                            }
                            currentScreen == Routes.PROFILE -> ProfileScreen(
                                authRepository = authRepository,
                                onBack = { currentScreen = Routes.HOME },
                                onLogout = { scope.launch { authRepository.logout(); currentScreen = "login" } }
                            )
                            currentScreen == Routes.ACADEMICS -> AcademicsScreen(onNavigate = { currentScreen = it })
                            currentScreen == Routes.ACADEMICS_COURSE_REG -> CourseRegistrationScreen(repository = academicsRepository, onBack = { currentScreen = Routes.ACADEMICS })
                            currentScreen == Routes.ACADEMICS_EXAM_RESULT -> ExamResultScreen(repository = academicsRepository, onBack = { currentScreen = Routes.ACADEMICS })
                            currentScreen == Routes.ACADEMICS_EXAM_CARD -> ExamCardScreen(onBack = { currentScreen = Routes.ACADEMICS })
                            currentScreen == Routes.ACADEMICS_INSIGHTS -> AcademicInsightsScreen(onBack = { currentScreen = Routes.ACADEMICS })
                            currentScreen == Routes.ACADEMICS_RESULT_SLIP -> com.school.studentportal.shared.ui.screens.student.academics.ResultSlipScreen(repository = academicsRepository, onBack = { currentScreen = Routes.ACADEMICS })
                            currentScreen == Routes.LIBRARY -> LibraryScreen()
                            currentScreen == Routes.VOTING -> {
                                val votingViewModel = remember { VotingViewModel(votingRepository, scope) }
                                VotingSystemScreen(viewModel = votingViewModel)
                            }
                            currentScreen == Routes.HEALTH -> HealthScreen(repository = supportRepository, onBack = { currentScreen = Routes.HOME })
                            currentScreen == Routes.STUDENT_ANALYTICS -> StudentAnalyticsScreen(repository = analyticsRepository, onBack = { currentScreen = Routes.HOME })
                            currentScreen == "student_campus_life" -> CampusLifeScreen(repository = supportRepository, onNavigateBack = { currentScreen = Routes.HOME })
                            currentScreen == Routes.COMPLAINTS -> ComplaintScreen(user = currentUser, supportRepository = supportRepository, academicsRepository = academicsRepository, onBack = { currentScreen = Routes.HOME })
                            currentScreen == Routes.ADMIN_CAMPUS_LIFE -> AdminCampusLifeManagement(repository = supportRepository, onBack = { currentScreen = Routes.HOME })
                            currentScreen == Routes.ADMIN_HEALTH -> AdminHealthManagement(repository = supportRepository, onBack = { currentScreen = Routes.HOME })
                            currentScreen == Routes.ADMIN_REPORTS -> AdminReportsScreen(repository = analyticsRepository, onBack = { currentScreen = Routes.HOME })
                            currentScreen == Routes.ADMIN_RESULT_MGMT -> com.school.studentportal.shared.ui.screens.admin.AdminResultManagementScreen(repository = adminRepository, onBack = { currentScreen = Routes.HOME })
                            currentScreen == Routes.FINANCE -> FinanceScreen(onNavigate = { currentScreen = it })
                            currentScreen == Routes.FINANCE_FEE_PAYMENT -> FeePaymentScreen(repository = financeRepository, onBack = { currentScreen = Routes.FINANCE })
                            currentScreen == Routes.FINANCE_VIEW_BALANCE -> ViewBalanceScreen(repository = financeRepository, onBack = { currentScreen = Routes.FINANCE }, onPayNow = { currentScreen = Routes.FINANCE_FEE_PAYMENT })
                            currentScreen == Routes.FINANCE_RECEIPTS -> ReceiptsScreen(repository = financeRepository, onBack = { currentScreen = Routes.FINANCE })
                            currentScreen == Routes.VIRTUAL_CAMPUS -> VirtualCampusScreen(user = currentUser, onNavigate = { currentScreen = it })
                            currentScreen == Routes.VC_DASHBOARD -> VCDashboardScreen(user = currentUser, onBack = { currentScreen = Routes.VIRTUAL_CAMPUS })
                            currentScreen == Routes.VC_MY_COURSES -> VCMyCoursesScreen(onCourseClick = { id -> currentScreen = "${Routes.VC_UNIT_CONTENT.replace("{courseId}", id)}" }, onBack = { currentScreen = Routes.VIRTUAL_CAMPUS })
                            currentScreen == Routes.VC_ZOOM_ROOMS || currentScreen == Routes.VC_LECTURES -> ZoomRoomsScreen(
                                userRole = currentUser.role, 
                                repository = virtualCampusRepository,
                                staffRepository = staffRepository,
                                onBack = { currentScreen = Routes.VIRTUAL_CAMPUS }, 
                                onJoinRoom = { id -> currentScreen = "${Routes.VC_MEETING_ROOM.replace("{roomId}", id)}" }
                            )
                            currentScreen.startsWith("vc_unit_content/") -> UnitContentScreen(courseCode = currentScreen.substringAfter("vc_unit_content/"), onBack = { currentScreen = Routes.VC_MY_COURSES })
                            currentScreen.startsWith("vc_meeting_room/") -> MeetingScreen(meetingId = currentScreen.substringAfter("vc_meeting_room/"), onLeave = { currentScreen = Routes.VC_ZOOM_ROOMS })
                            currentScreen.startsWith("staff_grading/") -> StaffGradingScreen(courseId = currentScreen.substringAfter("staff_grading/"), repository = staffRepository, onNavigateBack = { currentScreen = "staff_courses" })
                            currentScreen.startsWith("staff_content/") -> StaffContentUploadScreen(courseId = currentScreen.substringAfter("staff_content/"), repository = staffRepository, onNavigateBack = { currentScreen = "staff_courses" })
                            currentScreen == "staff_courses" -> StaffCoursesScreen(repository = staffRepository, onBack = { currentScreen = Routes.HOME })
                            currentScreen == "staff_student_lookup" -> StaffStudentLookupScreen(onBack = { currentScreen = Routes.HOME })
                            currentScreen == "staff_lectures" -> StaffLecturesScreen(onNavigateBack = { currentScreen = Routes.HOME })
                            else -> PlaceholderScreen(route = currentScreen, onBack = { currentScreen = Routes.HOME })
                        }
                    }
                } else {
                    currentScreen = "login"
                }
            }
        }
    }
}

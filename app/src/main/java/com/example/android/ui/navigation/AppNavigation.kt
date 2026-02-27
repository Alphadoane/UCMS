package com.example.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.android.Routes
import com.example.android.data.model.User
import com.example.android.data.model.UserRole
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

// Import all screens
import com.example.android.ui.screens.LoginScreen
import com.example.android.ui.screens.HomeScreen
import com.example.android.ui.screens.HealthScreen
import com.example.android.ui.screens.ProfileScreen
import com.example.android.ui.screens.AcademicsScreen
import com.example.android.ui.screens.FinanceScreen
import com.example.android.ui.screens.VirtualCampusScreen
import com.example.android.ui.screens.SimpleScreen
import com.example.android.ui.screens.VotingSystemScreen
import com.example.android.ui.screens.academics.*
import com.example.android.ui.screens.virtualcampus.*
import com.example.android.ui.screens.admin.*
import com.example.android.ui.screens.staff.*
import com.example.android.ui.screens.student.*
import com.example.android.ui.screens.finance.*
import com.example.android.ui.screens.support.*
import com.school.studentportal.shared.ui.screens.ComplaintScreen as SharedComplaintScreen
import com.school.studentportal.shared.ui.screens.ProfileScreen as SharedProfileScreen
import com.school.studentportal.shared.data.repository.SupportRepository as SharedSupportRepository
import com.school.studentportal.shared.data.repository.AcademicsRepository as SharedAcademicsRepository
import com.school.studentportal.shared.data.repository.AuthRepository as SharedAuthRepository

// Shared Imports
import com.school.studentportal.shared.data.network.TokenManager
import com.school.studentportal.shared.data.network.SharedApiService
import com.school.studentportal.shared.data.repository.FinanceRepository as SharedFinanceRepository
import com.school.studentportal.shared.data.repository.VirtualCampusRepository as SharedVirtualCampusRepository
import com.school.studentportal.shared.ui.screens.student.finance.FeePaymentScreen as SharedFeePaymentScreen
import androidx.compose.ui.platform.LocalContext
import com.example.android.data.prefs.UserPrefs
import com.school.studentportal.shared.data.repository.AdminRepository as SharedAdminRepository
import com.school.studentportal.shared.ui.screens.admin.AdminAdmissionScreen as SharedAdminAdmissionScreen
import com.school.studentportal.shared.ui.screens.admin.AdminDashboardScreen as SharedAdminDashboardScreen
import com.school.studentportal.shared.ui.screens.admin.AdminAllocationScreen as SharedAdminAllocationScreen
import com.school.studentportal.shared.ui.screens.admin.AdminUserMgmtScreen as SharedAdminUserMgmtScreen
import com.school.studentportal.shared.data.repository.LibraryRepository
import com.school.studentportal.shared.ui.screens.admin.AdminLibraryScreen
import com.school.studentportal.shared.ui.screens.student.StudentLibraryScreen
import com.school.studentportal.shared.ui.screens.admin.AdminZoomMgmtScreen as SharedAdminZoomMgmtScreen
import com.school.studentportal.shared.ui.screens.admin.AdminElectionScreen as SharedAdminElectionScreen
import com.school.studentportal.shared.ui.screens.admin.AdminProgramsScreen as SharedAdminProgramsScreen
import com.school.studentportal.shared.data.model.User as SharedUser
import com.school.studentportal.shared.data.model.UserRole as SharedUserRole
import com.school.studentportal.shared.data.repository.AnalyticsRepository as SharedAnalyticsRepository
import com.school.studentportal.shared.ui.screens.admin.AdminReportsScreen as SharedAdminReportsScreen
import com.school.studentportal.shared.ui.screens.admin.AdminHealthManagement as SharedAdminHealthManagement
import com.school.studentportal.shared.ui.screens.admin.AdminCampusLifeManagement as SharedAdminCampusLifeManagement
import com.school.studentportal.shared.ui.screens.student.StudentAnalyticsScreen as SharedStudentAnalyticsScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue

@Composable
fun AppNavHost(
    navController: NavHostController, 
    initialUser: User? = null, 
    onLogout: () -> Unit, 
    onImpersonate: (String) -> Unit, 
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = Routes.HOME, modifier = modifier) {
        composable(Routes.HOME) { 
             when (initialUser?.role) {
                 UserRole.ADMIN -> {
                     val context = LocalContext.current
                     val userPrefs = remember { UserPrefs(context) }
                     val token by userPrefs.authToken.collectAsState(initial = null)
                     val tokenManager = remember { TokenManager() }
                     val sharedApiService = remember { SharedApiService(tokenManager) }
                     val adminRepo = remember { SharedAdminRepository(sharedApiService) }

                     LaunchedEffect(token) {
                         if (token != null) {
                             tokenManager.saveTokens(token!!, "")
                             adminRepo.refreshStats()
                         }
                     }

                     // Map local User to SharedUser
                     val sharedUser = remember(initialUser) {
                        SharedUser(
                            id = initialUser?.id ?: "",
                            firstName = initialUser?.firstName ?: "",
                            lastName = initialUser?.lastName ?: "",
                            email = initialUser?.email ?: "",
                            role = SharedUserRole.ADMIN
                        )
                     }

                     SharedAdminDashboardScreen(
                         user = sharedUser,
                         repository = adminRepo,
                         onNavigate = { 
                             if (it == "logout") {
                                 onLogout()
                             } else {
                                 navController.navigate(it) 
                             }
                         },
                         onLogout = onLogout,
                         onOpenDrawer = onOpenDrawer
                     )
                 }
                 UserRole.STAFF, UserRole.LECTURER -> {
                     StaffDashboardScreen(
                         user = initialUser,
                         onNavigate = { navController.navigate(it) },
                         onOpenDrawer = onOpenDrawer
                     )
                 }
                 else -> {
                     HomeScreen(
                         initialUser = initialUser, 
                         onLogout = onLogout, 
                         onNavigate = { navController.navigate(it) },
                         onOpenDrawer = onOpenDrawer
                     ) 
                 }
             }
        }
        composable(Routes.PROFILE) { 
             val context = LocalContext.current
             val userPrefs = remember { UserPrefs(context) }
             val token by userPrefs.authToken.collectAsState(initial = null)
             val tokenManager = remember { TokenManager() }
             val sharedApiService = remember { SharedApiService(tokenManager) }
             val authRepo = remember { SharedAuthRepository(sharedApiService, tokenManager) }

             LaunchedEffect(token) {
                 if (token != null) {
                     tokenManager.saveTokens(token!!, "")
                 }
                 authRepo.checkSession()
             }

             SharedProfileScreen(
                 authRepository = authRepo,
                 onLogout = onLogout,
                 onBack = { navController.popBackStack() }
             )
        }
        composable(Routes.ACADEMICS) { AcademicsScreen(onNavigate = { navController.navigate(it) }) }
        composable(Routes.FINANCE) { FinanceScreen(onNavigate = { navController.navigate(it) }) }
        composable(Routes.VIRTUAL_CAMPUS) { 
            val sharedUser = remember(initialUser) {
                if (initialUser != null) {
                    SharedUser(
                        id = initialUser.id,
                        firstName = initialUser.firstName,
                        lastName = initialUser.lastName,
                        email = initialUser.email,
                        role = when (initialUser.role) {
                            UserRole.ADMIN -> SharedUserRole.ADMIN
                            UserRole.STAFF -> SharedUserRole.STAFF
                            UserRole.LECTURER -> SharedUserRole.LECTURER
                            else -> SharedUserRole.STUDENT
                        }
                    )
                } else null
            }
            com.school.studentportal.shared.ui.screens.student.virtualcampus.VirtualCampusScreen(user = sharedUser, onNavigate = { navController.navigate(it) }) 
        }
        composable(Routes.VC_DASHBOARD) { VCDashboardScreen() }
        composable(Routes.VC_MY_COURSES) { com.school.studentportal.shared.ui.screens.student.virtualcampus.VCMyCoursesScreen(onCourseClick = { navController.navigate(Routes.VC_UNIT_CONTENT.replace("{courseId}", it)) }, onBack = { navController.popBackStack() }) }
        composable(Routes.VC_LECTURES) { 
             if (initialUser?.role == UserRole.STAFF) {
                 StaffLecturesScreen(onNavigateBack = { navController.popBackStack() })
             } else {
                 SimpleScreen("Lectures") 
             }
        }
        composable(Routes.VC_ZOOM_ROOMS) {
            val context = LocalContext.current
            val userPrefs = remember { UserPrefs(context) }
            val token by userPrefs.authToken.collectAsState(initial = null)
            val tokenManager = remember { TokenManager() }
            val sharedApiService = remember { SharedApiService(tokenManager) }
            val vcRepo = remember { SharedVirtualCampusRepository(sharedApiService) }
            val staffRepo = remember { com.school.studentportal.shared.data.repository.StaffRepository(sharedApiService) }

            LaunchedEffect(token) {
                if (token != null) tokenManager.saveTokens(token!!, "")
            }

            val sharedRole = when (initialUser?.role) {
                UserRole.ADMIN -> SharedUserRole.ADMIN
                UserRole.STAFF -> SharedUserRole.STAFF
                UserRole.LECTURER -> SharedUserRole.LECTURER
                else -> SharedUserRole.STUDENT
            }

            com.school.studentportal.shared.ui.screens.student.virtualcampus.ZoomRoomsScreen(
                userRole = sharedRole,
                repository = vcRepo,
                staffRepository = staffRepo,
                onBack = { navController.popBackStack() },
                onJoinRoom = { navController.navigate(Routes.VC_MEETING_ROOM.replace("{roomId}", it)) }
            )
        }
        composable(
            route = Routes.VC_MEETING_ROOM,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            MeetingScreen(
                meetingId = roomId,
                onLeave = { navController.popBackStack() }
            )
        }
        composable(Routes.LIBRARY) { 
            val apiService = remember { SharedApiService(TokenManager()) }
            val repo = remember { LibraryRepository(apiService) }
            StudentLibraryScreen(
                repository = repo,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.VOTING) { VotingSystemScreen() }

        // Academics sub-destinations
        composable(Routes.ACADEMICS_COURSE_REG) { CourseRegistrationScreen() }
        composable(Routes.ACADEMICS_COURSE_WORK) { CourseWorkScreen() }
        composable(Routes.ACADEMICS_EXAM_CARD) { ExamCardScreen() }
        composable(Routes.ACADEMICS_EXAM_AUDIT) { ExamAuditScreen() }
        composable(Routes.ACADEMICS_EXAM_RESULT) { ExamResultScreen() }
        composable(Routes.ACADEMICS_ACADEMIC_LEAVE) { AcademicLeaveScreen() }
        composable(Routes.ACADEMICS_CLEARANCE) { ClearanceScreen() }
        composable(Routes.ACADEMICS_INSIGHTS) { AcademicInsightsScreen() }
        composable(Routes.ACADEMICS_RESULT_SLIP) {
            val context = LocalContext.current
            val userPrefs = remember { UserPrefs(context) }
            val token by userPrefs.authToken.collectAsState(initial = null)
            val tokenManager = remember { TokenManager() }
            val sharedApiService = remember { SharedApiService(tokenManager) }
            val academicsRepo = remember { SharedAcademicsRepository(sharedApiService) }

            LaunchedEffect(token) {
                if (token != null) tokenManager.saveTokens(token!!, "")
            }

            com.school.studentportal.shared.ui.screens.student.academics.ResultSlipScreen(
                repository = academicsRepo,
                onBack = { navController.popBackStack() }
            )
        }

        // Finance sub-destinations
        composable(Routes.FINANCE_FEE_PAYMENT) { 
            val context = LocalContext.current
            val userPrefs = remember { UserPrefs(context) }
            val token by userPrefs.authToken.collectAsState(initial = null)
            val tokenManager = remember { TokenManager() }
            
            // Sync token from Android DataStore to Shared TokenManager
            LaunchedEffect(token) {
                if (token != null) {
                    tokenManager.saveTokens(token!!, "")
                }
            }
            
            val sharedApiService = remember { SharedApiService(tokenManager) }
            val financeRepo = remember { SharedFinanceRepository(sharedApiService) }
            
            SharedFeePaymentScreen(
                repository = financeRepo,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.FINANCE_VIEW_BALANCE) { ViewBalanceScreen(onPayNow = { navController.navigate(Routes.FINANCE_FEE_PAYMENT) }) }
        composable(Routes.FINANCE_RECEIPTS) { ReceiptsScreen() }

        // Virtual Campus sub-destinations
        composable(Routes.VC_DASHBOARD) { VCDashboardScreen() }
        composable(Routes.VC_MY_COURSES) { 
            VCMyCoursesScreen(
                onCourseClick = { courseId -> navController.navigate("student_course_detail/$courseId") }
            ) 
        }
        composable(
            route = Routes.VC_UNIT_CONTENT,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            UnitContentScreen(
                courseCode = courseId, 
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.VC_LECTURES) { SimpleScreen("Lectures") }

        // Student 360 Routes
        composable("student_timetable") { TimetableScreen() }
        composable("student_campus_life") { CampusLifeScreen() }
        composable("student_essentials") { StudentEssentialsScreen() }
        composable(
            route = "student_course_detail/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            CourseDetailScreen(
                courseId = courseId, 
                onBack = { navController.popBackStack() }
            )
        }

        // Complaint Route
        composable(Routes.COMPLAINTS) {
             val context = LocalContext.current
             val userPrefs = remember { UserPrefs(context) }
             val token by userPrefs.authToken.collectAsState(initial = null)
             val tokenManager = remember { TokenManager() }
             val sharedApiService = remember { SharedApiService(tokenManager) }
             
             // Shared User mapping
             val sharedUser = remember(initialUser) {
                SharedUser(
                    id = initialUser?.id ?: "",
                    firstName = initialUser?.firstName ?: "",
                    lastName = initialUser?.lastName ?: "",
                    email = initialUser?.email ?: "",
                    role = when(initialUser?.role) {
                        UserRole.ADMIN -> SharedUserRole.ADMIN
                        UserRole.STAFF -> SharedUserRole.STAFF
                        UserRole.LECTURER -> SharedUserRole.LECTURER
                        else -> SharedUserRole.STUDENT
                    }
                )
             }

             val supportRepo = remember { SharedSupportRepository(sharedApiService) }
             val academicsRepo = remember { SharedAcademicsRepository(sharedApiService) }

             LaunchedEffect(token) {
                 if (token != null) {
                     tokenManager.saveTokens(token!!, "")
                 }
             }

             SharedComplaintScreen(
                 user = sharedUser,
                 supportRepository = supportRepo,
                 academicsRepository = academicsRepo,
                 onBack = { navController.popBackStack() }
             )
        }
        
        // Health Route
        composable(Routes.HEALTH) { 
            HealthScreen(onNavigateBack = { navController.popBackStack() }) 
        }

        composable(Routes.STUDENT_ANALYTICS) {
            val context = LocalContext.current
            val userPrefs = remember { UserPrefs(context) }
            val token by userPrefs.authToken.collectAsState(initial = null)
            val tokenManager = remember { TokenManager() }
            val sharedApiService = remember { SharedApiService(tokenManager) }
            val analyticsRepo = remember { SharedAnalyticsRepository(sharedApiService) }

            LaunchedEffect(token) {
                if (token != null) tokenManager.saveTokens(token!!, "")
            }

            SharedStudentAnalyticsScreen(
                repository = analyticsRepo,
                onBack = { navController.popBackStack() }
            )
        }

        // Admin Routes
        composable("admin_semesters") { 
            AdminSemesterScreen(
                onNavigateBack = { navController.popBackStack() }
            ) 
        }
        composable("admin_units") { 
            AdminCourseMgmtScreen(
                onNavigateBack = { navController.popBackStack() }
            ) 
        }
        composable("admin_broadcast") { 
            AdminBroadcastScreen(
                onNavigateBack = { navController.popBackStack() }
            ) 
        }
        composable("admin_finance") { 
            AdminFinanceScreen(
                onNavigateBack = { navController.popBackStack() }
            ) 
        }
        composable("admin_audit") { 
            AdminAuditScreen(
                onNavigateBack = { navController.popBackStack() }
            ) 
        }

        composable(Routes.ADMIN_REPORTS) {
            val context = LocalContext.current
            val userPrefs = remember { UserPrefs(context) }
            val token by userPrefs.authToken.collectAsState(initial = null)
            val tokenManager = remember { TokenManager() }
            val sharedApiService = remember { SharedApiService(tokenManager) }
            val analyticsRepo = remember { SharedAnalyticsRepository(sharedApiService) }

            LaunchedEffect(token) {
                if (token != null) tokenManager.saveTokens(token!!, "")
            }

            SharedAdminReportsScreen(
                repository = analyticsRepo,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_HEALTH) {
            val context = LocalContext.current
            val userPrefs = remember { UserPrefs(context) }
            val token by userPrefs.authToken.collectAsState(initial = null)
            val tokenManager = remember { TokenManager() }
            val sharedApiService = remember { SharedApiService(tokenManager) }
            val supportRepo = remember { SharedSupportRepository(sharedApiService) }

            LaunchedEffect(token) {
                if (token != null) tokenManager.saveTokens(token!!, "")
            }

            SharedAdminHealthManagement(
                repository = supportRepo,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_CAMPUS_LIFE) {
            val context = LocalContext.current
            val userPrefs = remember { UserPrefs(context) }
            val token by userPrefs.authToken.collectAsState(initial = null)
            val tokenManager = remember { TokenManager() }
            val sharedApiService = remember { SharedApiService(tokenManager) }
            val supportRepo = remember { SharedSupportRepository(sharedApiService) }

            LaunchedEffect(token) {
                if (token != null) tokenManager.saveTokens(token!!, "")
            }

            SharedAdminCampusLifeManagement(
                repository = supportRepo,
                onBack = { navController.popBackStack() }
            )
        }

        // Staff Routes

        composable(
            route = "staff_grading/{unitCode}",
            arguments = listOf(navArgument("unitCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val unitCode = backStackEntry.arguments?.getString("unitCode") ?: ""
            StaffGradingScreen(
                unitCode = unitCode,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "staff_content/{unitCode}",
            arguments = listOf(navArgument("unitCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val unitCode = backStackEntry.arguments?.getString("unitCode") ?: ""
            StaffContentUploadScreen(
                unitCode = unitCode,
                onNavigateBack = { navController.popBackStack() }
            )
        }


        // Admin Routes (Extended)
        composable("admin_allocation") {
            SharedAdminAllocationScreen(
                repository = remember { SharedAdminRepository(SharedApiService(TokenManager())) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

         composable("admin_user_mgmt") {
             SharedAdminUserMgmtScreen(
                 repository = remember { SharedAdminRepository(SharedApiService(TokenManager())) },
                 onNavigateBack = { navController.popBackStack() },
                 onImpersonate = onImpersonate
             )
         }
         // Removed redundant admin_manage_roles as it points to the same screen
         
         composable("admin_zoom") {
             SharedAdminZoomMgmtScreen(
                repository = remember { SharedVirtualCampusRepository(SharedApiService(TokenManager())) },
                onNavigateBack = { navController.popBackStack() }
             )
         }
         composable(Routes.ADMIN_ELECTION) { 
              SharedAdminElectionScreen(
                repository = remember { SharedAdminRepository(SharedApiService(TokenManager())) },
                onNavigateBack = { navController.popBackStack() }
              )
         }
         composable("admin_programs") { 
              SharedAdminProgramsScreen(onNavigateBack = { navController.popBackStack() })
         }
         
         composable(Routes.ADMIN_RESULT_MGMT) {
             val context = LocalContext.current
             val userPrefs = remember { UserPrefs(context) }
             val token by userPrefs.authToken.collectAsState(initial = null)
             val tokenManager = remember { TokenManager() }
             val sharedApiService = remember { SharedApiService(tokenManager) }
             val adminRepo = remember { SharedAdminRepository(sharedApiService) }

             LaunchedEffect(token) {
                 if (token != null) tokenManager.saveTokens(token!!, "")
             }

             com.school.studentportal.shared.ui.screens.admin.AdminResultManagementScreen(
                 repository = adminRepo,
                 onBack = { navController.popBackStack() }
             )
         }
        composable(Routes.ADMIN_ELECTION) {
             val context = LocalContext.current
             val userPrefs = remember { UserPrefs(context) }
             val token by userPrefs.authToken.collectAsState(initial = null)
             val tokenManager = remember { TokenManager() }
             val sharedApiService = remember { SharedApiService(tokenManager) }
             val adminRepo = remember { SharedAdminRepository(sharedApiService) }

             LaunchedEffect(token) {
                 if (token != null) tokenManager.saveTokens(token!!, "")
             }

             SharedAdminElectionScreen(
                 repository = adminRepo,
                 onNavigateBack = { navController.popBackStack() }
             )
        }

        // Staff Routes
        composable(Routes.STAFF_COURSES) {
             val context = LocalContext.current
             val userPrefs = remember { UserPrefs(context) }
             val token by userPrefs.authToken.collectAsState(initial = null)
             val tokenManager = remember { TokenManager() }
             val sharedApiService = remember { SharedApiService(tokenManager) }
             val staffRepo = remember { com.school.studentportal.shared.data.repository.StaffRepository(sharedApiService) }

             LaunchedEffect(token) {
                 if (token != null) tokenManager.saveTokens(token!!, "")
             }

             com.school.studentportal.shared.ui.screens.staff.StaffCoursesScreen(
                 repository = staffRepo,
                 onBack = { navController.popBackStack() }
             )
        }

        composable(Routes.STAFF_STUDENT_LOOKUP) {
             com.school.studentportal.shared.ui.screens.staff.StaffStudentLookupScreen(
                 onBack = { navController.popBackStack() }
             )
        }
        composable("admin_admissions") {
             val context = LocalContext.current
             val userPrefs = remember { UserPrefs(context) }
             val token by userPrefs.authToken.collectAsState(initial = null)
             val tokenManager = remember { TokenManager() }
             
             val sharedApiService = remember { SharedApiService(tokenManager) }
             val adminRepo = remember { SharedAdminRepository(sharedApiService) }

             LaunchedEffect(token) {
                 if (token != null) {
                     tokenManager.saveTokens(token!!, "")
                     // FIX: Refresh applications once token is available and saved
                     adminRepo.refreshApplications()
                 }
             }

             SharedAdminAdmissionScreen(
                 repository = adminRepo,
                 onNavigateBack = { navController.popBackStack() }
             )
        }

         // Support Routes (Legacy - Redirecting to UCMS)
         composable("support_tickets") {
             navController.navigate(Routes.COMPLAINTS) {
                 popUpTo("support_tickets") { inclusive = true }
             }
         }
         composable("support_user_lookup") {
             SupportUserLookupScreen(onNavigateBack = { navController.popBackStack() })
         }

         // Library Routes
         composable("admin_library") {
             val apiService = remember { SharedApiService(TokenManager()) }
             val repo = remember { LibraryRepository(apiService) }
             AdminLibraryScreen(
                 repository = repo,
                 onNavigateBack = { navController.popBackStack() }
             )
         }
         composable("student_library") {
             val apiService = remember { SharedApiService(TokenManager()) }
             val repo = remember { LibraryRepository(apiService) }
             StudentLibraryScreen(
                 repository = repo,
                 onNavigateBack = { navController.popBackStack() }
             )
         }
    }
}

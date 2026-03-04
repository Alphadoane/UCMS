package com.example.android.data.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Contextual
import kotlinx.serialization.json.JsonElement
import retrofit2.Response
import retrofit2.http.*

@Serializable
data class LoginRequest(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String
) {
    override fun toString(): String = "LoginRequest(username='$username')"
}

@Serializable
data class ImpersonationRequest(
    @SerialName("email") val email: String
)

@Serializable
data class LoginResponse(
    val access: String,
    val refresh: String,
    val user: ProfileResponse? = null
) {
    override fun toString(): String = "LoginResponse(access=***, refresh=***, user=$user)"
}

@Serializable
data class ProfileResponse(
    val id: String,
    val admission_no: String? = null,
    val full_name: String? = null,
    val email: String? = null,
    val role: String? = null,
    val course: String? = null
)

@Serializable
data class UserDto(val id: String, val name: String, val email: String)

@Serializable
data class Lecture(
    val id: Int,
    val course_code: String,
    val course_title: String,
    val day: String,
    val start_time: String,
    val end_time: String,
    val venue: String
)

@Serializable
data class TimetableResponse(val items: List<Lecture>)

@Serializable
data class CreateZoomRoomRequest(
    @SerialName("course_code") val courseCode: String,
    @SerialName("title") val title: String,
    @SerialName("start_time") val startTime: String
)

@Serializable
data class ZoomRoomResponse(
    val id: String,
    @SerialName("join_url") val joinUrl: String,
    @SerialName("host_url") val hostUrl: String? = null
)

@Serializable
data class ZoomRoom(
    val id: String,
    val course_code: String,
    val course_title: String,
    val start_time: String,
    val join_url: String,
    val host_url: String? = null,
    val is_host: Boolean = false
)

@Serializable
data class ZoomRoomsResponse(val rooms: List<ZoomRoom>)

@Serializable
data class MarkAttendanceRequest(val course_id: String? = null)

@Serializable
data class AttendanceSummary(val present: Int, val enrolled: Int)

@Serializable
data class CourseRegistrationItem(
    val course_code: String,
    val course_name: String,
    val credits: Int,
    val semester: String,
    val status: String
)

@Serializable
data class CourseRegistrationResponse(val items: List<CourseRegistrationItem>)

@Serializable
data class CourseWorkItem(
    val course_code: String,
    val assignment: String,
    val marks: Double,
    val max_marks: Double,
    val due_date: String,
    val status: String
)

@Serializable
data class CourseWorkResponse(val items: List<CourseWorkItem>)

@Serializable
data class ExamCardItem(
    val id: String? = null,
    val course_code: String,
    val course_name: String,
    val date: String? = null,
    val start_time: String? = null,
    val end_time: String? = null,
    val venue: String? = null
)

@Serializable
data class ExamCardResponse(val items: List<ExamCardItem>)

@Serializable
data class ExamAuditItem(
    val course_code: String,
    val audit_note: String,
    val status: String
)

@Serializable
data class ExamAuditResponse(val items: List<ExamAuditItem>)

@Serializable
data class ExamResultItem(
    val course_code: String,
    val course_name: String,
    val score: Double,
    val grade_letter: String,
    val approved: Boolean
)

@Serializable
data class ExamResultResponse(val items: List<ExamResultItem>)

@Serializable
data class AcademicLeaveItem(
    val request_id: String? = null,
    val start_date: String? = null,
    val end_date: String? = null,
    val status: String? = null
)

@Serializable
data class AcademicLeaveResponse(val items: List<AcademicLeaveItem>)

@Serializable
data class ClearanceItem(
    val department: String? = null,
    val status: String? = null,
    val remark: String? = null
)

@Serializable
data class ClearanceResponse(val items: List<ClearanceItem>)

@Serializable
data class SupportTicketItem(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val status: String,
    val priority: String,
    val created_at: String
)

@Serializable
data class SupportTicketsResponse(val items: List<SupportTicketItem>)

@Serializable
data class CreateTicketRequest(
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val course_id: Int? = null
)

@Serializable
data class AdminStatsResponse(
    val total_users: Int,
    val students: Int,
    val staff: Int,
    val active_elections: Int,
    val pending_tickets: Int,
    val db_status: String
)

@Serializable
data class AllocationOptionsResponse(
    val courses: List<CourseOption>,
    val lecturers: List<LecturerOption>
)

@Serializable
data class CourseOption(val id: Int, val code: String, val name: String)

@Serializable
data class LecturerOption(val user_id: String, val employee_id: String?, val name: String)

@Serializable
data class AllocateLectureRequest(
    @SerialName("course_id") val course_id: Int, 
    val employee_id: String,
    val day: String,
    val start_time: String,
    val end_time: String,
    val venue: String,
    val student_ids: List<String> = emptyList()
)

@Serializable
data class CourseWork(
    val id: Int,
    val course_name: String,
    val lecturer_name: String,
    val title: String,
    val description: String,
    val max_marks: Double,
    val due_date: String
)

@Serializable
data class CourseMaterialRequest(
    val title: String,
    val description: String,
    val link: String? = null
)

@Serializable
data class CourseWorkRequest(
    val title: String,
    val description: String,
    val max_marks: Double,
    val due_date: String,
    val category: String
)

@Serializable
data class CourseStudentResponse(
    val admission_number: String,
    val full_name: String,
    val program: String,
    val semester: String,
    val registered_at: String
)

@Serializable
data class LearningMaterialResponse(
    val id: Int,
    val title: String,
    val description: String? = null,
    val link: String? = null,
    val file: String? = null,
    val lecturer_name: String? = null,
    val created_at: String? = null
)

@Serializable
data class StaffCourseWorkResponse(
    val id: Int,
    val title: String,
    val description: String? = null,
    val category: String, // assignment or cat
    val max_marks: Double? = null,
    val due_date: String? = null,
    val created_at: String? = null
)

@Serializable
data class StaffCourseResponse(
    val id: Int,
    val code: String,
    val title: String,
    val department: String? = null,
    val student_count: Int = 0
)

@Serializable
data class BroadcastRequest(
    val title: String,
    val message: String,
    val target_audience: String
)

@Serializable
data class ChangePasswordRequest(
    val old_password: String,
    val new_password: String
)

@Serializable
data class PasswordResetRequest(val email: String)

@Serializable
data class PasswordResetVerify(val email: String, val otp_code: String, val new_password: String)

@Serializable
data class HealthTip(
    val id: Int,
    val title: String,
    val description: String,
    val image: String? = null,
    val category: String
)

@Serializable
data class AppointmentRequest(
    val appointment_type: String,
    val reason: String,
    val appointment_date: String
)

@Serializable
data class AppointmentResponse(
    val id: Int,
    val student_name: String,
    val appointment_type: String,
    val reason: String,
    val appointment_date: String,
    val status: String,
    val admin_notes: String? = null
)

@Serializable
data class EmergencyAlertRequest(
    val latitude: Double,
    val longitude: Double,
    val message: String
)

@Serializable
data class EmergencyAlertResponse(
    val id: Int,
    val student_name: String,
    val latitude: Double,
    val longitude: Double,
    val message: String,
    val status: String,
    val created_at: String
)

@Serializable
data class AdminHealthStatsResponse(
    val active_alerts_count: Int,
    val pending_appointments_count: Int,
    val active_alerts: List<EmergencyAlertResponse>,
    val pending_appointments: List<AppointmentResponse>
)

@Serializable
data class ApiResponse(val detail: String) // Generic response wrapper


interface ApiService {
    @POST("auth/login")
    @Headers("Content-Type: application/json", "Accept: application/json")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/impersonate")
    @Headers("Content-Type: application/json", "Accept: application/json")
    suspend fun impersonate(@Body request: ImpersonationRequest): Response<LoginResponse>
    
    @GET("profile/me")
    suspend fun getProfile(): Response<ProfileResponse>

    @GET("users")
    suspend fun getUsers(): Response<List<UserDto>>
    
    @GET("admin/stats")
    suspend fun getAdminStats(): Response<AdminStatsResponse>

    @POST("admin/broadcast/send")
    suspend fun sendBroadcast(@Body request: BroadcastRequest): Response<Unit>

    @POST("auth/register")
    suspend fun createUser(@Body request: com.example.android.data.model.CreateUserRequest): Response<com.example.android.data.model.CreateUserResponse>

    @GET("voting/elections")
    suspend fun getElections(): Response<com.example.android.data.model.ElectionListResponse>

    @POST("voting/elections")
    suspend fun createElection(@Body request: com.example.android.data.model.ElectionRequest): Response<com.example.android.data.model.Election>

    @POST("voting/elections/{id}/candidates")
    suspend fun addCandidate(@Path("id") electionId: Int, @Body request: com.example.android.data.model.CandidateRequest): Response<com.example.android.data.model.Candidate>

    @POST("voting/elections/{id}/vote")
    suspend fun castVote(@Path("id") electionId: Int, @Body request: com.example.android.data.model.VoteRequest): Response<Unit>
    
    @GET("voting/elections/{id}/results")
    suspend fun getElectionResults(@Path("id") electionId: Int): Response<com.example.android.data.model.ElectionResultsResponse>

    @GET("academics/timetable")
    suspend fun getTimetable(): Response<List<Lecture>>
    
    @POST("virtual/zoom-rooms")
    suspend fun createZoomRoom(@Body request: CreateZoomRoomRequest): Response<ZoomRoomResponse>
    
    @POST("auth/password/change")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    @POST("auth/password/reset-request")
    suspend fun requestPasswordReset(@Body request: PasswordResetRequest): Response<ApiResponse>

    @POST("auth/password/reset-confirm")
    suspend fun verifyPasswordReset(@Body request: PasswordResetVerify): Response<ApiResponse>


    @POST("admin/users/{userId}/reset-password")
    suspend fun resetUserPassword(@Path("userId") userId: String): Response<Map<String, String>>

    @GET("virtual/zoom-rooms")
    suspend fun getZoomRooms(): Response<ZoomRoomsResponse>

    @POST("virtual/zoom-rooms/{roomId}/attendance/mark")
    suspend fun markAttendance(@Path("roomId") roomId: String, @Body request: MarkAttendanceRequest = MarkAttendanceRequest()): Response<Map<String, String>>

    @GET("virtual/zoom-rooms/{roomId}/attendance/summary")
    suspend fun getAttendanceSummary(@Path("roomId") roomId: String, @retrofit2.http.Query("course_id") courseId: String? = null, @retrofit2.http.Query("date") date: String? = null): Response<AttendanceSummary>

    @GET("academics/course-registration")
    suspend fun getCourseRegistration(): Response<CourseRegistrationResponse>

    @GET("academics/course-work")
    suspend fun getCourseWork(): Response<CourseWorkResponse>

    @GET("academics/exam-card")
    suspend fun getExamCard(): Response<ExamCardResponse>

    @GET("academics/exam-audit")
    suspend fun getExamAudit(): Response<ExamAuditResponse>

    @GET("academics/result")
    suspend fun getExamResult(): Response<ExamResultResponse>

    @GET("academics/academic-leave")
    suspend fun getAcademicLeave(): Response<AcademicLeaveResponse>

    @GET("academics/clearance")
    suspend fun getClearance(): Response<ClearanceResponse>

    @GET("support/tickets")
    suspend fun getSupportTickets(): Response<SupportTicketsResponse>
    
    @GET("support/tickets/{id}")
    suspend fun getTicketDetails(@Path("id") id: String): Response<com.example.android.data.model.TicketDetailsResponse>

    @POST("support/tickets")
    suspend fun createSupportTicket(@Body request: CreateTicketRequest): Response<SupportTicketItem>
    
    @POST("support/tickets/{id}")
    suspend fun replyToTicket(@Path("id") id: String, @Body request: com.example.android.data.model.TicketReplyRequest): Response<com.example.android.data.model.TicketMessage>
    
    @POST("support/tickets/{id}/status")
    suspend fun updateTicketStatus(@Path("id") id: String, @Body request: com.example.android.data.model.UpdateTicketStatusRequest): Response<com.example.android.data.model.Ticket>

    @GET("admission/programs")
    suspend fun getPrograms(): Response<List<com.example.android.data.model.AdmissionProgram>>

    @POST("admission/apply")
    suspend fun applyAdmission(@Body request: com.example.android.data.model.ApplyRequest): Response<com.example.android.data.model.AdmissionApplication>

    @GET("admission/status")
    suspend fun checkStatus(@retrofit2.http.Query("q") query: String): Response<com.example.android.data.model.AdmissionApplication>

    @GET("admission/list")
    suspend fun getApplications(@retrofit2.http.Query("phase") phase: String? = null): Response<List<com.example.android.data.model.AdmissionApplication>>
    
    @POST("admission/programs/create")
    suspend fun createProgram(@Body program: com.example.android.data.model.AdmissionProgram): Response<com.example.android.data.model.AdmissionProgram>
    
    @PUT("admission/programs/{id}")
    suspend fun updateProgram(@Path("id") id: Int, @Body program: com.example.android.data.model.AdmissionProgram): Response<com.example.android.data.model.AdmissionProgram>
    
    @DELETE("admission/programs/{id}")
    suspend fun deleteProgram(@Path("id") id: Int): Response<Unit>

    @POST("admission/enroll/{id}")
    suspend fun enrollStudent(@Path("id") id: String): Response<Map<String, String>>

    @POST("admission/phase/{id}")
    suspend fun updatePhase(@Path("id") id: String, @Body body: Map<String, String>): Response<com.example.android.data.model.AdmissionApplication>

    @POST("admission/submit/{id}")
    suspend fun submitApplication(@Path("id") id: String): Response<Map<String, String>>

    @retrofit2.http.Multipart
    @POST("admission/upload/{id}")
    suspend fun uploadDocument(
        @Path("id") id: String,
        @retrofit2.http.Part file: okhttp3.MultipartBody.Part,
        @retrofit2.http.Part("document_type") type: okhttp3.RequestBody,
        @retrofit2.http.Part("national_id") nationalId: okhttp3.RequestBody
    ): Response<Map<String, String>>
    
    @GET("admin/academics/allocate/options")
    suspend fun getAllocationOptions(): Response<AllocationOptionsResponse>

    @POST("admin/academics/allocate")
    suspend fun allocateLecture(@Body request: AllocateLectureRequest): Response<Unit>

    @GET("academics/assignments")
    suspend fun getAssignments(): Response<List<CourseWork>>

    @GET("staff/courses")
    suspend fun getStaffCourses(): Response<List<StaffCourseResponse>>

    @GET("staff/courses/{courseId}/students")
    suspend fun getCourseStudents(@Path("courseId") courseId: Int): Response<List<CourseStudentResponse>>

    @GET("staff/courses/{courseId}/materials")
    suspend fun getCourseMaterials(@Path("courseId") courseId: Int): Response<List<LearningMaterialResponse>>

    @GET("staff/courses/{courseId}/work")
    suspend fun getStaffCourseWork(@Path("courseId") courseId: Int): Response<List<StaffCourseWorkResponse>>

    @POST("staff/courses/{courseId}/materials")
    suspend fun postCourseMaterial(@Path("courseId") courseId: Int, @Body body: CourseMaterialRequest): Response<LearningMaterialResponse>

    @POST("staff/courses/{courseId}/work")
    suspend fun postCourseWork(@Path("courseId") courseId: Int, @Body body: CourseWorkRequest): Response<StaffCourseWorkResponse>

    @GET("admin/finance/students")
    suspend fun getFinanceStudents(@Query("q") query: String? = null): Response<List<com.school.studentportal.shared.data.model.StudentFinanceDto>>

    @GET("admin/finance/transactions/{studentId}")
    suspend fun getStudentTransactions(@Path("studentId") studentId: String): Response<com.school.studentportal.shared.data.model.StudentTransactionsResponse>

    // Health & Emergency
    @GET("support/campus-life/health_tips")
    suspend fun getHealthTips(): Response<List<HealthTip>>

    @POST("support/appointments/")
    suspend fun bookAppointment(@Body request: AppointmentRequest): Response<AppointmentResponse>

    @POST("support/emergency/")
    suspend fun sendEmergencyAlert(@Body request: EmergencyAlertRequest): Response<EmergencyAlertResponse>

    @GET("admin/health/stats")
    suspend fun getAdminHealthStats(): Response<AdminHealthStatsResponse>

    @POST("support/appointments/{id}/confirm/")
    suspend fun confirmAppointment(@Path("id") id: Int, @Body body: Map<String, String>): Response<Unit>

    @POST("support/emergency/{id}/resolve/")
    suspend fun resolveAlert(@Path("id") id: Int): Response<Unit>
}

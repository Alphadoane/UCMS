package com.school.studentportal.shared.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class LoginRequest(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String
)

@Serializable
data class LoginResponse(
    val access: String,
    val refresh: String,
    val user: ProfileResponse? = null
)

@Serializable
data class ProfileResponse(
    val id: String,
    val admission_no: String? = null,
    val full_name: String? = null,
    val email: String? = null,
    val role: String? = null,
    val course: String? = null,
    val phone_number: String? = null,
    val alternate_email: String? = null,
    val address: String? = null,
    val bio: String? = null,
    val avatar: String? = null
)

@Serializable
data class UpdateProfileRequest(
    val phone_number: String? = null,
    val alternate_email: String? = null,
    val address: String? = null,
    val bio: String? = null
)

@Serializable
data class AvatarUploadResponse(
    val avatar: String?
)

@Serializable
data class ImpersonationRequest(
    @SerialName("email") val email: String
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
data class ApiResponse(val detail: String)

@Serializable
data class UserDto(
    val id: String,
    val first_name: String,
    val last_name: String,
    val email: String,
    val role: String
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
data class ElectionListResponse(val elections: List<Election>)

@Serializable
data class Election(
    val id: Int,
    val title: String,
    val description: String,
    val start_date: String,
    val end_date: String,
    val is_active: Boolean,
    val candidates: List<Candidate> = emptyList(),
    val has_voted: Boolean = false
)

@Serializable
data class Candidate(
    val id: Int,
    val name: String,
    val position: String,
    val bio: String,
    val vote_count: Int = 0,
    val slogan: String? = null
)

@Serializable
data class ElectionRequest(
    val title: String,
    val description: String,
    val end_date: String
)

@Serializable
data class CandidateRequest(
    val name: String,
    val slogan: String
)

@Serializable
data class VoteRequest(
    val candidate_id: Int
)

@Serializable
data class VoteResponse(
    val status: String,
    val election_id: Int,
    val candidate: String
)

@Serializable
data class ElectionResultsResponse(
    val election: String,
    val results: List<Candidate>,
    val total_votes: Int
)

@Serializable
data class CourseOption(
    val id: Int,
    val code: String,
    val name: String
)

@Serializable
data class LecturerOption(
    val user_id: String? = null, 
    val employee_id: String? = null,
    val name: String
)

@Serializable
data class RoomOption(
    val id: String,
    val name: String
)

@Serializable
data class StudentOption(
    val admission_number: String,
    val name: String
)

@Serializable
data class AllocateLectureRequest(
    @SerialName("course_id") val course_id: Int,
    @SerialName("employee_id") val employee_id: String,
    @SerialName("day") val day: String,
    @SerialName("start_time") val start_time: String,
    @SerialName("end_time") val end_time: String,
    @SerialName("venue") val venue: String,
    @SerialName("student_ids") val student_ids: List<String> = emptyList()
)

@Serializable
data class AllocationOptionsResponse(
    val courses: List<CourseOption>,
    val lecturers: List<LecturerOption>,
    val rooms: List<RoomOption> = emptyList(),
    val students: List<StudentOption> = emptyList()
)

@Serializable
data class ComplaintAttachment(
    val id: Int,
    val file: String,
    val file_type: String,
    val uploaded_at: String
)

@Serializable
data class ComplaintComment(
    val id: Int,
    val user_name: String,
    val message: String,
    val created_at: String
)

@Serializable
data class ComplaintTimeline(
    val id: Int,
    val event_type: String,
    val description: String,
    val user_name: String,
    val created_at: String
)

@Serializable
data class ComplaintItem(
    val id: Int,
    val student_name: String,
    val course_name: String,
    val description: String,
    val status: String,
    val priority: String,
    val created_at: String,
    val attachments: List<ComplaintAttachment> = emptyList(),
    val comments: List<ComplaintComment> = emptyList(),
    val timeline: List<ComplaintTimeline> = emptyList()
)

@Serializable
data class ComplaintListResponse(val items: List<ComplaintItem>)

@Serializable
data class CreateComplaintRequest(
    val course_id: Int,
    val description: String,
    val priority: String = "medium"
)

@Serializable
data class UpdateComplaintStatusRequest(val status: String)

@Serializable
data class AcademicCourse(
    val id: Int,
    val code: String,
    val title: String,
    val description: String? = null,
    val credits: Int,
    val is_enrolled: Boolean = false,
    val semester: String? = null
)

@Serializable
data class EnrollmentResponse(
    val enrollment_id: Int,
    val status: String,
    val date: String
)

@Serializable
data class ExamResult(
    val id: Int,
    val course_code: String,
    val course_title: String,
    val grade: String,
    val score: Int,
    val semester: String,
    val academic_year: String
)

@Serializable
data class TimetableEntry(
    val id: Int,
    val course_code: String,
    val course_title: String,
    val day: String,
    val start_time: String,
    val end_time: String,
    val venue: String,
    val lecturer: String? = null
)

@Serializable
data class CourseRegistrationRequest(
    val course_ids: List<Int>
)

// Staff Models
@Serializable
data class StaffCourse(
    val id: Int,
    val code: String,
    val title: String,
    val student_count: Int = 0,
    val semester: String? = null
)

@Serializable
data class StudentGrade(
    val student_id: String,
    val student_name: String,
    val course_code: String,
    val grade: String? = null,
    val score: Int? = null
)

@Serializable
data class SubmitGradesRequest(
    val course_id: Int,
    val grades: List<StudentGradeUpdate>
)

@Serializable
data class StudentGradeUpdate(
    val student_id: String,
    val score: Int
)

@Serializable
data class ContentUploadRequest(
    val course_id: Int,
    val title: String,
    val content_type: String, // e.g., "PDF", "LINK", "VIDEO"
    val content_url: String? = null,
    val description: String? = null
)

@Serializable
data class CourseWork(
    val id: Int = 0,
    val course: Int,
    val course_name: String? = null,
    val lecturer: Int? = null,
    val lecturer_name: String? = null,
    val title: String,
    val description: String? = null,
    val max_marks: Double,
    val due_date: String,
    val created_at: String? = null
)

// Virtual Campus / Zoom
@Serializable
data class ZoomRoom(
    val id: String,
    val course_code: String,
    val course_title: String,
    val start_time: String?,
    val join_url: String,
    val host_url: String? = null,
    val is_host: Boolean = false
)

@Serializable
data class ZoomRoomListResponse(val rooms: List<ZoomRoom>)

@Serializable
data class CreateZoomRoomRequest(
    val course_code: String,
    val title: String,
    val start_time: String
)
@Serializable
data class StkPushRequest(
    val amount: Double? = null,
    val payment_type: String = "FULL"
)

@Serializable
data class MpesaPaymentRequest(
    val phone_number: String,
    val amount: Double
)

@Serializable
data class MpesaResponse(
    val status: String,
    val message: String,
    val checkout_request_id: String? = null
)

@Serializable
data class PaystackInitResponse(
    @SerialName("authorization_url") val authorizationUrl: String,
    @SerialName("access_code") val accessCode: String,
    val reference: String
)

@Serializable
data class FeeBalanceResponse(
    val balance: Double,
    val total_billed: Double? = null,
    val total_paid: Double? = null
)

@Serializable
data class FinanceStatementResponse(
    val transactions: List<FinanceTransaction>
)

@Serializable
data class FinanceTransaction(
    val id: Int,
    val student_name: String,
    val admission_number: String,
    val amount: Double,
    val status: String,
    val reference: String,
    val payment_method: String,
    val date: String,
    val receipt: String
)

@Serializable
data class AdminTransactionsResponse(
    val transactions: List<FinanceTransaction>
)

@Serializable
data class CampusLifeContent(
    val id: Int? = null,
    val title: String,
    val image: String? = null,
    val description: String,
    val category: String = "General",
    val created_at: String? = null
)

@Serializable
data class Appointment(
    val id: Int? = null,
    val student_id: String? = null,
    val student_name: String? = null,
    val appointment_type: String,
    val reason: String,
    val appointment_date: String,
    val status: String? = "PENDING",
    val admin_notes: String? = null,
    val created_at: String? = null
)

@Serializable
data class EmergencyAlert(
    val id: Int? = null,
    val student_name: String? = null,
    val latitude: Double,
    val longitude: Double,
    val status: String? = "ACTIVE",
    val created_at: String? = null
)

@Serializable
data class ResultSlip(
    val student_name: String,
    val admission_number: String,
    val program: String,
    val semester: String,
    val semester_id: Int? = null,
    val academic_year: String,
    val results: List<ResultSlipItem>,
    val total_credits: Double,
    val gpa: Double
)

@Serializable
data class ResultSlipItem(
    val course_code: String,
    val course_title: String,
    val credits: Int,
    val grade: String,
    val score: Double,
    val points: Double
)

@Serializable
data class PublishResultsRequest(
    @SerialName("semester_id") val semesterId: Int,
    @SerialName("course_id") val courseId: Int? = null
)

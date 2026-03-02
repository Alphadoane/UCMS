package com.school.studentportal.shared.data.network

import com.school.studentportal.shared.data.model.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*

class SharedApiService(private val tokenManager: TokenManager) {
    private val client = KtorClient.client

    private suspend fun authHeader(): String? {
        return tokenManager.getAccessToken()?.let { "Bearer $it" }
    }

    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = client.post("auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Login failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(): Result<ProfileResponse> {
        return try {
            val response = client.get("profile/me") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun impersonate(email: String): Result<LoginResponse> {
        return try {
             val response = client.post("auth/impersonate") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(ImpersonationRequest(email))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Impersonation failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsers(): Result<List<UserDto>> {
        return try {
            val response = client.get("users") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch users"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminStats(): Result<AdminStatsResponse> {
        return try {
            val response = client.get("admin/stats") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch stats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminGlobalReports(): Result<AdminReportSummary> {
        return try {
            val response = client.get("admin/reports") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch admin reports"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStudentPerformanceAnalytics(): Result<StudentAnalytics> {
        return try {
            val response = client.get("student/analytics") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch student analytics"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // UCMS (Complaints)
    suspend fun getComplaints(): Result<ComplaintListResponse> {
        return try {
            val response = client.get("support/tickets") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch complaints"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun submitComplaint(courseId: Int, description: String, priority: String): Result<ComplaintItem> {
         return try {
            val response = client.post("support/tickets") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(CreateComplaintRequest(courseId, description, priority))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to submit complaint"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateComplaintStatus(complaintId: Int, newStatus: String): Result<ApiResponse> {
        return try {
            val response = client.post("support/tickets/$complaintId/status") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(UpdateComplaintStatusRequest(newStatus))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to update status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Academics
    suspend fun getAvailableCourses(): Result<List<AcademicCourse>> {
        return try {
            val response = client.get("academics/courses/available") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch courses"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun enrollCourses(courseIds: List<Int>): Result<EnrollmentResponse> {
        return try {
            val response = client.post("academics/enroll") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(CourseRegistrationRequest(courseIds))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Enrollment failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExamResults(): Result<List<ExamResult>> {
        return try {
            val response = client.get("academics/result") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch results"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTimetable(): Result<List<TimetableEntry>> {
        return try {
            val response = client.get("academics/timetable") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch timetable"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getResultSlip(semesterId: Int? = null): Result<ResultSlip> {
        return try {
            val response = client.get("academics/result-slip") {
                authHeader()?.let { header("Authorization", it) }
                semesterId?.let { parameter("semester_id", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch result slip"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadResultSlipPdf(semesterId: Int? = null): Result<ByteArray> {
        return try {
            val response = client.get("academics/result-slip/download") {
                authHeader()?.let { header("Authorization", it) }
                semesterId?.let { parameter("semester_id", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.readBytes())
            } else {
                Result.failure(Exception("Failed to download result slip PDF"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun publishResults(request: PublishResultsRequest): Result<ApiResponse> {
        return try {
            val response = client.post("academics/publish") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to publish results"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Voting
    suspend fun getElections(): Result<List<Election>> {
        return try {
            val response = client.get("voting/elections") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                val listResp: ElectionListResponse = response.body()
                Result.success(listResp.elections)
            } else {
                Result.failure(Exception("Failed to fetch elections"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun castVote(electionId: Int, candidateId: Int): Result<VoteResponse> {
        return try {
            val response = client.post("voting/elections/$electionId/vote") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(VoteRequest(candidateId))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to cast vote: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getElectionResults(electionId: Int): Result<ElectionResultsResponse> {
        return try {
            val response = client.get("voting/elections/$electionId/results") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch results"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Admin - Elections
    suspend fun getAdminElections(): Result<List<Election>> {
        return try {
            val response = client.get("admin/elections") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch elections"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createElection(request: ElectionRequest): Result<Election> {
        return try {
            val response = client.post("admin/elections") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to create election"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCandidate(electionId: Int, request: CandidateRequest): Result<Candidate> {
        return try {
            val response = client.post("admin/elections/$electionId/candidates") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to add candidate"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Staff
    suspend fun getStaffCourses(): Result<List<StaffCourse>> {
        return try {
            val response = client.get("staff/courses") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch staff courses"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCourseStudents(courseId: Int): Result<List<StudentGrade>> {
        return try {
            val response = client.get("staff/courses/$courseId/students") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch students"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitGrades(request: SubmitGradesRequest): Result<ApiResponse> {
        return try {
            val response = client.post("staff/grades/submit") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to submit grades"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadContent(request: ContentUploadRequest): Result<ApiResponse> {
        return try {
            val response = client.post("staff/content/upload") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to upload content"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Admission
    suspend fun getPrograms(): Result<List<AdmissionProgram>> {
        return try {
            val response = client.get("admission/programs") // Check URL correctness
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch programs"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitApplication(request: ApplyRequest): Result<AdmissionApplication> {
        return try {
            val response = client.post("admission/apply") {
                 contentType(ContentType.Application.Json)
                 setBody(request)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                 val errorBody = response.bodyAsText()
                 Result.failure(Exception("Submission failed: ${response.status} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkStatus(query: String): Result<AdmissionApplication> {
        return try {
            val response = client.get("admission/status") {
                parameter("q", query)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Application not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun finalizeApplication(appId: String): Result<Unit> {
         return try {
            val response = client.post("admission/submit/$appId")
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Final submission failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun uploadDocument(appId: String, nationalId: String, type: String, fileBytes: ByteArray, fileName: String): Result<Unit> {
        return try {
               println("DIAGNOSTIC: Uploading $type for $appId, size=${fileBytes.size}")
              val response = client.submitFormWithBinaryData(
                url = "admission/upload/$appId",
                formData = io.ktor.client.request.forms.formData {
                    append("document_type", type)
                    append("type", type)
                    append("national_id", nationalId)
                    append("file", fileBytes, io.ktor.http.Headers.build {
                        append(io.ktor.http.HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"$fileName\"")
                        append(io.ktor.http.HttpHeaders.ContentType, "application/octet-stream")
                    })
                }
            )
            println("DIAGNOSTIC: Upload response code=${response.status}")
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val errorBody = response.body<String>()
                println("DIAGNOSTIC: Upload error details=$errorBody")
                Result.failure(Exception("Upload failed: ${response.status} - $errorBody"))
            }
        } catch (e: Exception) {
            println("DIAGNOSTIC: Upload exception=${e.message}")
            Result.failure(e)
        }
    }

    // Virtual Campus
    suspend fun getZoomRooms(): Result<ZoomRoomListResponse> {
        return try {
            val response = client.get("virtual/zoom-rooms") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch zoom rooms"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createZoomRoom(request: CreateZoomRoomRequest): Result<ZoomRoom> {
        return try {
            val response = client.post("virtual/zoom-rooms") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(Exception("Failed to create zoom room: ${response.status} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Finance
    suspend fun getFeeBalance(): Result<FeeBalanceResponse> {
        return try {
            val response = client.get("finance/view-balance") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch balance"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReceipts(): Result<FinanceStatementResponse> {
        return try {
            val response = client.get("finance/receipts") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch receipts"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun initiateStkPush(request: StkPushRequest): Result<ApiResponse> {
        return try {
            val response = client.post("finance/stk-push") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("STK Push failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun initiateMpesaPayment(amount: Double, phoneNumber: String): Result<MpesaResponse> {
        return try {
            val response = client.post("finance/mpesa/stk-push") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(MpesaPaymentRequest(phoneNumber, amount))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("M-Pesa payment initiation failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun initiatePaystackPayment(request: StkPushRequest): Result<PaystackInitResponse> {
        return try {
            val response = client.post("finance/paystack-initialize") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Bank Payment initialization failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminTransactions(): Result<AdminTransactionsResponse> {
        return try {
            val response = client.get("admin/finance/transactions") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch transactions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminApplications(phase: String? = null): Result<List<AdmissionApplication>> {
        return try {
            val response = client.get("admission/list") {
                authHeader()?.let { header("Authorization", it) }
                phase?.let { parameter("phase", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch applications"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Verify a specific document
    suspend fun verifyDocument(docId: Int, action: String, reason: String): Result<AdmissionDocument> {
        return try {
            val response = client.post("admission/verify/$docId") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(VerifyDocumentRequest(action, reason))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to verify document"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateApplicationPhase(appId: String, phase: String, reason: String? = null): Result<AdmissionApplication> {
        return try {
            val response = client.post("admission/phase/$appId") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(mapOf("phase" to phase, "reason" to reason))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to update phase"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun enrollStudent(appId: String): Result<Map<String, String>> {
        return try {
            val response = client.post("admission/enroll/$appId") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val error = response.bodyAsText()
                Result.failure(Exception("Enrollment failed: $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadFile(url: String): Result<ByteArray> {
        return try {
            val response = client.get(url) {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.readBytes())
            } else {
                Result.failure(Exception("Download failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminAllocationOptions(): Result<AllocationOptionsResponse> {
        return try {
            val response = client.get("admin/academics/allocate/options") {
                 authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch allocation options"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun allocateLecture(request: AllocateLectureRequest): Result<TimetableEntry> {
        return try {
            val response = client.post("admin/academics/allocate") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val errorBody = try { response.bodyAsText() } catch (e: Exception) { "" }
                Result.failure(Exception(errorBody.ifBlank { "Failed to allocate lecture: ${response.status}" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Library
    suspend fun getBooks(category: String? = null): Result<List<Book>> {
        return try {
             val response = client.get("student/books") {
                authHeader()?.let { header("Authorization", it) }
                category?.let { parameter("category", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch books"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadBook(title: String, author: String, category: String, pdfBytes: ByteArray, fileName: String, coverBytes: ByteArray? = null, coverName: String? = null): Result<Unit> {
        return try {
            val response = client.submitFormWithBinaryData(
                url = "admin/books",
                formData = formData {
                    append("title", title)
                    append("author", author)
                    append("category", category)
                    append("pdf_file", pdfBytes, Headers.build {
                        append(HttpHeaders.ContentDisposition, "form-data; name=\"pdf_file\"; filename=\"$fileName\"")
                         append(HttpHeaders.ContentType, "application/pdf")
                    })
                    if (coverBytes != null && coverName != null) {
                        append("cover_image", coverBytes, Headers.build {
                            append(HttpHeaders.ContentDisposition, "form-data; name=\"cover_image\"; filename=\"$coverName\"")
                            append(HttpHeaders.ContentType, "image/*")
                        })
                    }
                }
            ) {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                 val error = response.bodyAsText()
                Result.failure(Exception("Failed to upload book: $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBook(id: Int): Result<Unit> {
        return try {
            val response = client.delete("admin/books/$id/") {
                 authHeader()?.let { header("Authorization", it) }
            }
             if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete book"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Staff / Lecturer
    suspend fun getLecturerCourseWork(): Result<List<CourseWork>> {
        return try {
            val response = client.get("academics/lecturer-course-work") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                 val result = response.body<Map<String, List<CourseWork>>>()
                 Result.success(result["assignments"] ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch course work"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }




    suspend fun createCourseWork(courseAnalysis: CourseWork): Result<CourseWork> {
        return try {
            val response = client.post("academics/lecturer-course-work") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(courseAnalysis)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to create course work"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Result<UpdateProfileRequest> {
        return try {
            val response = client.patch("profile/update") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to update profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAvatar(fileBytes: ByteArray, fileName: String): Result<AvatarUploadResponse> {
        return try {
            val response = client.submitFormWithBinaryData(
                url = "profile/avatar",
                formData = formData {
                    append("avatar", fileBytes, Headers.build {
                        append(HttpHeaders.ContentDisposition, "form-data; name=\"avatar\"; filename=\"$fileName\"")
                        append(HttpHeaders.ContentType, "image/*")
                    })
                }
            ) {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Avatar upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Support - Campus Life
    suspend fun getCampusLifeContent(): Result<List<CampusLifeContent>> {
        return try {
            val response = client.get("support/campus-life") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch campus life content"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCampusLifeContent(title: String, description: String, category: String, imageBytes: ByteArray?, imageFileName: String?): Result<CampusLifeContent> {
        return try {
            val response = client.submitFormWithBinaryData(
                url = "support/campus-life/",
                formData = formData {
                    append("title", title)
                    append("description", description)
                    append("category", category)
                    if (imageBytes != null && imageFileName != null) {
                        append("image", imageBytes, Headers.build {
                            append(HttpHeaders.ContentDisposition, "form-data; name=\"image\"; filename=\"$imageFileName\"")
                            append(HttpHeaders.ContentType, "image/*")
                        })
                    }
                }
            ) {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val error = response.bodyAsText()
                Result.failure(Exception("Failed to create campus life content: $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCampusLifeContent(id: Int, title: String?, description: String?, category: String?, imageBytes: ByteArray?, imageFileName: String?): Result<CampusLifeContent> {
        return try {
            val response = client.submitFormWithBinaryData(
                url = "support/campus-life/$id/",
                formData = formData {
                    title?.let { append("title", it) }
                    description?.let { append("description", it) }
                    category?.let { append("category", it) }
                    if (imageBytes != null && imageFileName != null) {
                        append("image", imageBytes, Headers.build {
                            append(HttpHeaders.ContentDisposition, "form-data; name=\"image\"; filename=\"$imageFileName\"")
                            append(HttpHeaders.ContentType, "image/*")
                        })
                    }
                }
            ) {
                method = HttpMethod.Patch
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val error = response.bodyAsText()
                Result.failure(Exception("Failed to update campus life content: $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCampusLifeContent(id: Int): Result<Unit> {
        return try {
            val response = client.delete("support/campus-life/$id/") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete campus life content"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Support - Appointments
    suspend fun getAppointments(): Result<List<Appointment>> {
        return try {
            val response = client.get("support/appointments") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch appointments"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun bookAppointment(appointment: Appointment): Result<Appointment> {
        return try {
            val response = client.post("support/appointments/") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(appointment)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to book appointment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Support - Emergency
    suspend fun sendEmergencyAlert(latitude: Double, longitude: Double): Result<EmergencyAlert> {
        return try {
            val response = client.post("support/emergency/") {
                contentType(ContentType.Application.Json)
                authHeader()?.let { header("Authorization", it) }
                setBody(mapOf("latitude" to latitude, "longitude" to longitude))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to send emergency alert"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEmergencyAlerts(): Result<List<EmergencyAlert>> {
        return try {
            val response = client.get("support/emergency") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch emergency alerts"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteZoomRoom(roomId: String): Result<ApiResponse> {
        return try {
            val response = client.delete("virtual/zoom-rooms/$roomId") {
                authHeader()?.let { header("Authorization", it) }
            }
            if (response.status.isSuccess()) {
                Result.success(ApiResponse("Room deleted"))
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(Exception("Failed to delete room: ${response.status} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.example.android.data.repository

import android.content.Context
import com.example.android.data.network.ApiService
import com.example.android.data.network.Lecture
import com.example.android.data.network.ProfileResponse
import com.school.studentportal.shared.data.network.SharedApiService
import com.school.studentportal.shared.data.network.TokenManager
import com.school.studentportal.shared.data.model.CreateZoomRoomRequest
import com.school.studentportal.shared.data.model.StaffCourse
import com.school.studentportal.shared.data.model.TimetableEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class VirtualCampusRepository(context: Context? = null) {
    private val apiService = SharedApiService(TokenManager())

    fun getZoomRooms(): Flow<List<Map<String, Any>>> = flow {
        val result = apiService.getZoomRooms()
        if (result.isSuccess) {
            val rooms = result.getOrNull()?.rooms ?: emptyList()
            // Map to Map<String, Any> to match existing UI expectation for now
            val mapped = rooms.map { room ->
                mapOf(
                    "id" to room.id,
                    "course_code" to room.course_code,
                    "course_title" to room.course_title,
                    "start_time" to (room.start_time ?: ""),
                    "join_url" to room.join_url,
                    "host_url" to (room.host_url ?: ""),
                    "is_host" to room.is_host
                )
            }
            emit(mapped)
        } else {
            emit(emptyList())
        }
    }

    suspend fun createZoomRoom(courseCode: String, title: String, startTime: String): Result<String> {
        val request = CreateZoomRoomRequest(courseCode, title, startTime)
        val result = apiService.createZoomRoom(request)
        return if (result.isSuccess) {
            Result.success(result.getOrNull()?.join_url ?: "")
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
        }
    }

    suspend fun getUserProfile(userId: String): ProfileResponse? {
        // We only support "me" for now via shared API
        val result = apiService.getProfile()
        return result.getOrNull()?.let { sharedProfile ->
            ProfileResponse(
                id = sharedProfile.id,
                admission_no = sharedProfile.admission_no,
                full_name = sharedProfile.full_name,
                email = sharedProfile.email,
                role = sharedProfile.role,
                course = sharedProfile.course
            )
        }
    }

    suspend fun getLectures(): List<Lecture> {
        // Try getting staff courses first
        val result = apiService.getStaffCourses()
        if (result.isSuccess) {
            return result.getOrNull()?.map { course ->
                Lecture(
                    id = course.id,
                    course_code = course.code,
                    course_title = course.title,
                    day = "Monday", // Placeholder
                    start_time = "08:00",
                    end_time = "10:00",
                    venue = "Hall A"
                )
            } ?: emptyList()
        }
        
        // Fallback to student timetable
        val timetableResult = apiService.getTimetable()
        if (timetableResult.isSuccess) {
             return timetableResult.getOrNull()?.map { entry ->
                 Lecture(
                     id = 0,
                     course_code = entry.course_code,
                     course_title = entry.course_title,
                     day = entry.day,
                     start_time = entry.start_time,
                     end_time = entry.end_time,
                     venue = entry.venue
                 )
             } ?: emptyList()
        }

        return emptyList()
    }

    suspend fun getUpcomingActivities(userId: String): Map<String, List<Map<String, Any>>> {
        val activities = mutableMapOf<String, List<Map<String, Any>>>()
        
        // Classes from Timetable
        val timetableResult = apiService.getTimetable()
        val classes = timetableResult.getOrNull()?.map { entry ->
            mapOf(
                "title" to entry.course_title, // Corrected
                "course" to entry.course_code, // Corrected
                "time" to "${entry.start_time} - ${entry.end_time}",
                "venue" to entry.venue, // Corrected
                "day" to entry.day // Corrected
            )
        } ?: emptyList()
        activities["classes"] = classes

        // Assignments (Mock or fetch if available)
        activities["assignments"] = emptyList()

        return activities
    }
    
    suspend fun getEnrolledCoursesDetails(userId: String): List<Map<String, Any>> {
         // Derive from timetable for now
         val timetableResult = apiService.getTimetable()
         val uniqueCourses = timetableResult.getOrNull()?.distinctBy { it.course_code } ?: emptyList() // Corrected
         
         return uniqueCourses.map { entry ->
             mapOf(
                 "code" to entry.course_code, // Corrected
                 "name" to entry.course_title, // Corrected
                 "lecturer" to (entry.lecturer ?: "Unknown"),
                 "time" to "${entry.day} ${entry.start_time}", // Corrected
                 "venue" to entry.venue, // Corrected
                 "color" to 0xFF1976D2.toLong()
             )
         }
    }

    suspend fun getUnitContent(courseCode: String): Map<String, Any>? {
        // Mock content for now
        return mapOf(
            "title" to "$courseCode Content",
            "announcements" to listOf("Welcome to the course!", "Mid-semester exam next week."),
            "modules" to listOf(
                mapOf(
                    "title" to "Module 1: Introduction",
                    "resources" to listOf("Lecture Notes.pdf", "Intro Video.mp4")
                ),
                mapOf(
                    "title" to "Module 2: Advanced Topics",
                    "resources" to listOf("Deep Dive.pdf")
                )
            )
        )
    }

    fun observeParticipants(meetingId: String): Flow<List<Map<String, Any>>> = flow {
        // Mock participants
        val participants = listOf(
            mapOf(
                "id" to "1",
                "name" to "Alice",
                "video" to true,
                "color" to 0xFF4CAF50.toLong(),
                "isMe" to false,
                "agoraUid" to 101
            )
        )
        emit(participants)
    }
}

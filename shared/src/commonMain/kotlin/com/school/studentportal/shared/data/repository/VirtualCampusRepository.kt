package com.school.studentportal.shared.data.repository

import com.school.studentportal.shared.data.model.CreateZoomRoomRequest
import com.school.studentportal.shared.data.model.ZoomRoom
import com.school.studentportal.shared.data.network.SharedApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class VirtualCampusRepository(private val api: SharedApiService) {

    fun getZoomRooms(): Flow<List<ZoomRoom>> = flow {
        val result = api.getZoomRooms()
        if (result.isSuccess) {
            emit(result.getOrNull()?.rooms ?: emptyList())
        } else {
            emit(emptyList()) // Emit empty or error state
        }
    }

    suspend fun createZoomRoom(courseCode: String, title: String, startTime: String): Result<ZoomRoom> {
        val request = CreateZoomRoomRequest(courseCode, title, startTime)
        return api.createZoomRoom(request)
    }
}

package com.school.studentportal.shared.data.repository

import com.school.studentportal.shared.data.model.*
import com.school.studentportal.shared.data.network.SharedApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SupportRepository(private val apiService: SharedApiService) {

    private val _complaints = MutableStateFlow<List<ComplaintItem>>(emptyList())
    val complaints = _complaints.asStateFlow()

    suspend fun refreshComplaints() {
        apiService.getComplaints().onSuccess {
            _complaints.value = it.items
        }
    }

    suspend fun lodgeComplaint(courseId: Int, description: String, priority: String): Result<ComplaintItem> {
        return apiService.submitComplaint(courseId, description, priority)
    }

    suspend fun updateStatus(complaintId: Int, newStatus: String): Result<ApiResponse> {
        return apiService.updateComplaintStatus(complaintId, newStatus)
    }

    // Health & Campus Life functions
    suspend fun getCampusLifeContent(): Result<List<CampusLifeContent>> = apiService.getCampusLifeContent()
    
    suspend fun createCampusLifeContent(
        title: String, 
        description: String, 
        category: String, 
        imageBytes: ByteArray? = null, 
        imageFileName: String? = null
    ): Result<CampusLifeContent> = apiService.createCampusLifeContent(title, description, category, imageBytes, imageFileName)

    suspend fun updateCampusLifeContent(
        id: Int,
        title: String? = null,
        description: String? = null,
        category: String? = null,
        imageBytes: ByteArray? = null,
        imageFileName: String? = null
    ): Result<CampusLifeContent> = apiService.updateCampusLifeContent(id, title, description, category, imageBytes, imageFileName)

    suspend fun deleteCampusLifeContent(id: Int): Result<Unit> = apiService.deleteCampusLifeContent(id)

    suspend fun getAppointments(): Result<List<Appointment>> = apiService.getAppointments()
    
    suspend fun bookAppointment(appointment: Appointment): Result<Appointment> = apiService.bookAppointment(appointment)
    
    suspend fun sendEmergencyAlert(latitude: Double, longitude: Double): Result<EmergencyAlert> = 
        apiService.sendEmergencyAlert(latitude, longitude)

    suspend fun getEmergencyAlerts(): Result<List<EmergencyAlert>> = apiService.getEmergencyAlerts()
}

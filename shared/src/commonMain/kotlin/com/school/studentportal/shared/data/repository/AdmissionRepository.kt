package com.school.studentportal.shared.data.repository

import com.school.studentportal.shared.data.model.*
import com.school.studentportal.shared.data.network.SharedApiService
import com.school.studentportal.shared.utils.PlatformFile

class AdmissionRepository(private val api: SharedApiService) {

    suspend fun getPrograms(): Result<List<AdmissionProgram>> {
        return api.getPrograms()
    }

    suspend fun submitApplication(
        firstName: String, lastName: String, nationalId: String, 
        meanGrade: String, programId: Int
    ): Result<AdmissionApplication> {
        val request = ApplyRequest(
            first_name = firstName, last_name = lastName,
            national_id = nationalId, mean_grade = meanGrade,
            program_choice = programId
        )
        return api.submitApplication(request)
    }

    suspend fun checkStatus(query: String): Result<AdmissionApplication> {
        return api.checkStatus(query)
    }

    suspend fun uploadDocument(appId: String, nationalId: String, type: String, file: PlatformFile): Result<Unit> {
        return try {
            val bytes = file.readBytes()
            api.uploadDocument(appId, nationalId, type, bytes, file.name)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitFinalApplication(appId: String): Result<Unit> {
        return api.finalizeApplication(appId)
    }
}

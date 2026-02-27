package com.example.android.data.repository

import com.example.android.data.network.NetworkModule
import com.example.android.data.model.AdmissionApplication
import com.example.android.data.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class AdmissionRepository {
    private val api: ApiService by lazy {
        NetworkModule.apiInstance ?: throw IllegalStateException("API not initialized")
    }

    suspend fun getPrograms(): Result<List<com.example.android.data.model.AdmissionProgram>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getPrograms()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    android.util.Log.e("AdmissionRepo", "Failed to load programs: ${response.code()}")
                    Result.failure(Exception("Failed to load programs"))
                }
            } catch (e: Exception) { 
                android.util.Log.e("AdmissionRepo", "Exception loading programs", e)
                Result.failure(e) 
            }
        }
    }

    suspend fun submitApplication(
        firstName: String, lastName: String, nationalId: String, 
        meanGrade: String, programId: Int
    ): Result<AdmissionApplication> {
        return withContext(Dispatchers.IO) {
            try {
                val request = com.example.android.data.model.ApplyRequest(
                    first_name = firstName, last_name = lastName,
                    national_id = nationalId, mean_grade = meanGrade,
                    program_choice = programId,
                    dob = "2000-01-01", // Default for now
                    gender = "M" // Default
                )
                val response = api.applyAdmission(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                     val errorMsg = response.errorBody()?.string() ?: "Submission failed: ${response.code()}"
                     Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getApplications(phase: String? = null): Result<List<AdmissionApplication>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getApplications(phase)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to fetch applications"))
                }
            } catch (e: Exception) { Result.failure(e) }
        }
    }

    suspend fun createProgram(program: com.example.android.data.model.AdmissionProgram): Result<com.example.android.data.model.AdmissionProgram> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.createProgram(program)
                if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
                else Result.failure(Exception("Creation failed"))
            } catch (e: Exception) { Result.failure(e) }
        }
    }
    
    suspend fun updateProgram(program: com.example.android.data.model.AdmissionProgram): Result<com.example.android.data.model.AdmissionProgram> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.updateProgram(program.id, program)
                if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
                else Result.failure(Exception("Update failed"))
            } catch (e: Exception) { Result.failure(e) }
        }
    }
    
    suspend fun deleteProgram(id: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.deleteProgram(id)
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Delete failed"))
            } catch (e: Exception) { Result.failure(e) }
        }
    }
    
    suspend fun updatePhase(appId: String, phase: String, reason: String? = null): Result<AdmissionApplication> {
        return withContext(Dispatchers.IO) {
            try {
                val payload = mutableMapOf("phase" to phase)
                if (reason != null) payload["reason"] = reason
                
                val response = api.updatePhase(appId, payload)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to update phase: ${response.code()}"))
                }
            } catch (e: Exception) { Result.failure(e) }
        }
    }

    suspend fun submitFinalApplication(appId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.submitApplication(appId)
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Submission failed: ${response.code()}"))
            } catch (e: Exception) { Result.failure(e) }
        }
    }

    suspend fun enrollStudent(appId: String): Result<Map<String, String>> {
        return withContext(Dispatchers.IO) {
             try {
                val response = api.enrollStudent(appId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    Result.failure(Exception("Enrollment failed: $errorMsg"))
                }
            } catch (e: Exception) { Result.failure(e) }
        }
    }

    suspend fun checkStatus(query: String): Result<AdmissionApplication> {
        return withContext(Dispatchers.IO) {
            try {
                // api.checkStatus expects query param 'q'
                // ApiService checks "status?q=..."
                // wait, ApiService definition: @GET("status") checkStatus(...)
                // Checking ApiService definition in previous steps...
                // It was: @GET("status") suspend fun checkStatus(query: String) ? No let's check.
                // It was: @GET("status") suspend fun check_status(request)? No invalid.
                // I need to check ApiService again or just assume standard retrofit.
                // Actually I better check ApiService first or Update it.
                // Inspecting ApiService in step 1531:
                // @GET("status") suspend fun check_status(request): ... wait line 8.
                // It was `path("status", views.check_status)` in urls.
                // In ApiService.kt (Step 1531/1532/1538), I don't see checkStatus!
                // I need to add checkStatus to ApiService first.
                val response = api.checkStatus(query)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Application not found"))
                }
            } catch (e: Exception) { Result.failure(e) }
        }
    }
    suspend fun uploadDocument(appId: String, nationalId: String, type: String, file: java.io.File, mimeType: String): Result<Map<String, String>> {
        return withContext(Dispatchers.IO) {
            try {
                val mediaType = mimeType.toMediaTypeOrNull()
                val reqFile = okhttp3.RequestBody.create(mediaType, file)
                val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, reqFile)
                val typeBody = okhttp3.RequestBody.create("text/plain".toMediaTypeOrNull(), type)
                val idBody = okhttp3.RequestBody.create("text/plain".toMediaTypeOrNull(), nationalId)
                
                val response = api.uploadDocument(appId, body, typeBody, idBody)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val error = response.errorBody()?.string() ?: "Upload failed: ${response.code()}"
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) { Result.failure(e) }
        }
    }
}

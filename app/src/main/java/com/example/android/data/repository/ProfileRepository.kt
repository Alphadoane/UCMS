package com.example.android.data.repository

import android.content.Context
import com.example.android.data.local.DatabaseModule
import com.example.android.data.local.ProfileEntity
import com.example.android.data.network.NetworkModule
import kotlinx.coroutines.flow.Flow

class ProfileRepository(context: Context) {
    private val db = DatabaseModule.database(context)
    private val profileDao = db.profileDao()
    private val api = NetworkModule.createApiService(context)

    fun observeProfile(): Flow<ProfileEntity?> = profileDao.observeProfile()

    suspend fun refreshProfile(): Result<Unit> {
        return try {
            val response = api.getProfile()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val entity = ProfileEntity(
                        id = body.id,
                        registrationNumber = body.admission_no,
                        fullName = body.full_name ?: "Unknown User",
                        email = body.email ?: "",
                        role = body.role ?: "STUDENT",
                        department = body.course
                    )
                    profileDao.upsert(entity)
                    Result.success(Unit)
                } else {
                    Result.failure(IllegalStateException("Empty profile response"))
                }
            } else {
                Result.failure(IllegalStateException("Profile request failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}



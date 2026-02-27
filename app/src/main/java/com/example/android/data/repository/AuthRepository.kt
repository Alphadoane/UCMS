package com.example.android.data.repository

import android.content.Context
import com.example.android.data.network.ApiService
import com.example.android.data.network.LoginRequest
import com.example.android.data.network.NetworkModule
import com.example.android.data.prefs.UserPrefs

import kotlinx.coroutines.flow.first

class AuthRepository(private val context: Context) {
    private val api: ApiService by lazy { NetworkModule.createApiService(context) }
    private val userPrefs: UserPrefs = UserPrefs(context)
    
    suspend fun signUp(email: String, password: String, fullName: String, admissionNo: String? = null): Result<Any?> {
         return Result.failure(Exception("Self-registration is disabled. Please contact the administrator."))
    }
    
    suspend fun login(email: String, password: String): Result<Pair<String, com.example.android.data.network.ProfileResponse?>> {
        return try {
            val response = api.login(LoginRequest(username = email, password = password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Save access and refresh tokens
                    userPrefs.setAuthToken(body.access, body.refresh)
                    Result.success(Pair(body.access, body.user))
                } else {
                    Result.failure(Exception("Login failed: Empty response body"))
                }
            } else {
                Result.failure(Exception("Login failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
             val errorMessage = when {
                e.message?.contains("ConnectException") == true -> "Network error. Check connection."
                else -> e.message ?: "Login failed"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun getProfile(): Result<com.example.android.data.network.ProfileResponse> {
        return try {
            val response = api.getProfile()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun impersonateUser(email: String): Result<Pair<String, com.example.android.data.network.ProfileResponse?>> {
         return try {
            val response = api.impersonate(com.example.android.data.network.ImpersonationRequest(email))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Save NEW access token (overwriting the admin's token)
                    userPrefs.setAuthToken(body.access, body.refresh)
                    Result.success(Pair(body.access, body.user))
                } else {
                    Result.failure(Exception("Impersonation failed: Empty body"))
                }
            } else {
                Result.failure(Exception("Impersonation failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout() {
        userPrefs.setAuthToken(null, null)
        // Clear local profile data to prevent bleeding
        com.example.android.data.local.DatabaseModule.database(context).profileDao().clear()
    }
    
    suspend fun getCurrentToken(): String? {
         return userPrefs.authToken.first().takeIf { !it.isNullOrBlank() }
    }
    
    suspend fun isLoggedIn(): Boolean {
        return getCurrentToken() != null
    }

    suspend fun changePassword(old: String, new: String): Result<Unit> {
        return try {
            val response = api.changePassword(com.example.android.data.network.ChangePasswordRequest(old, new))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun requestPasswordReset(email: String): Result<String> {
        return try {
            val response = api.requestPasswordReset(com.example.android.data.network.PasswordResetRequest(email))
            if (response.isSuccessful) {
                Result.success(response.body()?.detail ?: "Code sent if account exists")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Request failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyPasswordReset(email: String, otp: String, newPass: String): Result<String> {
        return try {
            val response = api.verifyPasswordReset(com.example.android.data.network.PasswordResetVerify(email, otp, newPass))
            if (response.isSuccessful) {
                Result.success(response.body()?.detail ?: "Password reset successfully")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Reset failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

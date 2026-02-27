package com.school.studentportal.shared.data.repository

import com.school.studentportal.shared.data.model.*
import com.school.studentportal.shared.data.network.SharedApiService
import com.school.studentportal.shared.data.network.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(
    private val api: SharedApiService,
    private val tokenManager: TokenManager
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    suspend fun login(email: String, pass: String): Result<User> {
        val result = api.login(LoginRequest(email, pass))
        return if (result.isSuccess) {
            val response = result.getOrNull()!!
            tokenManager.saveTokens(response.access, response.refresh)
            val profile = response.user
            val user = profile?.toUser() ?: User(
                id = "unknown",
                email = email,
                firstName = "User",
                lastName = "",
                role = UserRole.STUDENT
            )
            _currentUser.value = user
            Result.success(user)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Login failed"))
        }
    }

    suspend fun logout() {
        tokenManager.clearTokens()
        _currentUser.value = null
    }

    suspend fun updateProfile(phoneNumber: String?, alternateEmail: String?, address: String?, bio: String?): Result<Unit> {
        val request = UpdateProfileRequest(phoneNumber, alternateEmail, address, bio)
        val result = api.updateProfile(request)
        return if (result.isSuccess) {
            val user = _currentUser.value
            if (user != null) {
                _currentUser.value = user.copy(
                    phoneNumber = phoneNumber,
                    alternateEmail = alternateEmail,
                    address = address,
                    bio = bio
                )
            }
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Update failed"))
        }
    }

    suspend fun uploadAvatar(fileBytes: ByteArray, fileName: String): Result<Unit> {
        val result = api.uploadAvatar(fileBytes, fileName)
        return if (result.isSuccess) {
            val response = result.getOrNull()
            val user = _currentUser.value
            if (user != null && response != null) {
                _currentUser.value = user.copy(avatarUrl = response.avatar)
            }
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Avatar upload failed"))
        }
    }

    suspend fun checkSession(): Boolean {
        if (tokenManager.getAccessToken() != null) {
            val result = api.getProfile()
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()?.toUser()
                return true
            }
        }
        return false
    }

    private fun ProfileResponse.toUser(): User {
        val roleEnum = UserRole.fromString(role)
        val names = full_name?.split(" ") ?: listOf("User")
        val first = names.firstOrNull() ?: ""
        val last = names.drop(1).joinToString(" ")
        
        return User(
            id = id,
            email = email ?: "",
            firstName = first,
            lastName = last,
            role = roleEnum,
            regNumber = admission_no,
            department = course,
            phoneNumber = phone_number,
            alternateEmail = alternate_email,
            address = address,
            bio = bio,
            avatarUrl = avatar
        )
    }
}

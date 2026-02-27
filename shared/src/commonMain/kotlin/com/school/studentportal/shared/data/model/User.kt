package com.school.studentportal.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    STUDENT,
    STAFF,
    LECTURER,
    ADMIN,
    TECHNICAL_SUPPORT;

    companion object {
        fun fromString(role: String?): UserRole {
            val normalized = role?.uppercase() ?: return STUDENT
            return when (normalized) {
                "STUDENT" -> STUDENT
                "STAFF" -> STAFF
                "LECTURER" -> LECTURER
                "ADMIN" -> ADMIN
                "SUPPORT", "TECHNICAL_SUPPORT", "TECH_SUPPORT" -> TECHNICAL_SUPPORT
                else -> {
                    try {
                        valueOf(normalized)
                    } catch (e: IllegalArgumentException) {
                        STUDENT
                    }
                }
            }
        }
    }
}

@Serializable
data class User(
    val id: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val role: UserRole = UserRole.STUDENT,
    val regNumber: String? = null, 
    val course: String? = null,    
    val department: String? = null,
    val phoneNumber: String? = null,
    val alternateEmail: String? = null,
    val address: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}

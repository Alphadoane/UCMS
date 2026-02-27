package com.example.android.data.model

enum class UserRole {
    STUDENT,
    STAFF,
    LECTURER,
    ADMIN,
    TECHNICAL_SUPPORT;

    companion object {
        fun fromString(role: String?): UserRole {
            return try {
                if (role.isNullOrBlank()) {
                    android.util.Log.w("UserRole", "Role is null or blank, defaulting to STUDENT")
                    STUDENT
                } else {
                    val normalized = role.uppercase()
                    android.util.Log.d("UserRole", "Parsing role: '$role' -> '$normalized'")
                    valueOf(normalized)
                }
            } catch (e: IllegalArgumentException) {
                android.util.Log.e("UserRole", "Failed to parse role '$role', defaulting to STUDENT", e)
                STUDENT
            }
        }
    }
}

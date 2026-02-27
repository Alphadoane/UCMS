package com.example.android.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val role: UserRole = UserRole.STUDENT,
    val regNumber: String? = null, // Admission Number for students, Employee ID for others
    val course: String? = null,    // For students
    val department: String? = null // For staff
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}

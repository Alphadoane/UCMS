package com.example.android.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CreateUserRequest(
    @SerialName("full_name") val fullName: String,
    @SerialName("email") val email: String? = null,
    @SerialName("role") val role: String,
    @SerialName("reg_number") val regNumber: String? = null,
    @SerialName("department") val department: String? = null
)

@Serializable
data class CreateUserResponse(
    val id: String,
    val email: String,
    val role: String
)

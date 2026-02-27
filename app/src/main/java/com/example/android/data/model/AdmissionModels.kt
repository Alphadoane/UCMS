package com.example.android.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AdmissionProgram(
    val id: Int,
    val code: String,
    val name: String,
    val duration_years: Int,
    val category: String = "Undergraduate",
    val entry_requirements: String = ""
)

@Serializable
data class AdmissionApplication(
    val id: String = "", // UUID
    val application_id: String,
    val first_name: String = "",
    val last_name: String = "",
    val national_id: String = "",
    val current_phase: String = "APPLIED",
    val tracking_id: String = "", // Keeping for compat
    val documents: List<AdmissionDocument> = emptyList(),
    val missing_documents: List<String> = emptyList(),
    val rejection_reason: String? = null,
    val email: String? = null,
    val credential_password: String? = null,
    val student_reg_number: String? = null
)

@Serializable
data class AdmissionDocument(
    val id: Int,
    val document_type: String,
    val file: String, // URL
    val is_verified: Boolean = false,
    val rejection_reason: String? = null
)

@Serializable
data class ApplyRequest(
    val first_name: String,
    val last_name: String,
    val national_id: String,
    val mean_grade: String,
    val program_choice: Int = 1, // Defaulting ID for now until we have fetching
    val intake: String = "SEPT-2026",
    val mode_of_study: String = "FULL_TIME",
    val phone: String = "0700000000",
    val address: String = "Nairobi",
    val previous_institution: String = "High School",
    val index_number: String = "12345678",
    val dob: String = "2000-01-01",
    val gender: String = "M"
)

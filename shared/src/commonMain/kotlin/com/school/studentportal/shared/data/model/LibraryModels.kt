package com.school.studentportal.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class BookCategory {
    TECH, EDU, BIZ
}

@Serializable
data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val category: BookCategory,
    val pdf_file: String, // URL
    val cover_image: String? = null, // URL
    val uploaded_at: String
)

@Serializable
data class BookUploadRequest(
    val title: String,
    val author: String,
    val category: String
)

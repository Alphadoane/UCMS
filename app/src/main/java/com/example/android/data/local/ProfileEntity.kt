package com.example.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val registrationNumber: String?,
    val fullName: String?,
    val email: String?,
    val role: String,
    val department: String? = null
)




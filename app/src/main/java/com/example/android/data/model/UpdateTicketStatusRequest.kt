package com.example.android.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTicketStatusRequest(
    val status: String
)

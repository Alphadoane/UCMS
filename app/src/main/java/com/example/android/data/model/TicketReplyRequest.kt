package com.example.android.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TicketReplyRequest(
    val message: String
)

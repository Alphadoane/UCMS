package com.example.android.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TicketDetailsResponse(
    val ticket: Ticket,
    val messages: List<TicketMessage>
)

@Serializable
data class Ticket(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val status: String,
    val priority: String,
    val created_at: String
)

@Serializable
data class TicketMessage(
    val id: Int,
    val sender_name: String,
    val is_admin: Boolean = false,
    val message: String,
    val created_at: String
)

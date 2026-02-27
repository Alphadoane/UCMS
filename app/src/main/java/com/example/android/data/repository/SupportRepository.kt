package com.example.android.data.repository

import android.content.Context
import com.example.android.data.network.ApiService
import com.example.android.data.network.NetworkModule
import com.example.android.data.model.User
import com.example.android.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Support Repository
 * Handles Tickets and Users.
 */
class SupportRepository(context: Context? = null) {

    private val api: ApiService by lazy {
        NetworkModule.apiInstance ?: throw IllegalStateException("API not initialized. Call StudentRepository.init(context)")
    }

    suspend fun searchUser(query: String): User? {
        // Implement via getUsers() list filtering if needed
        return try {
             val users = api.getUsers().body() ?: emptyList()
             val found = users.find { it.email == query || it.name.contains(query, ignoreCase = true) }
             if (found != null) {
                 User(
                     id = found.id.toString(),
                     email = found.email,
                     firstName = found.name.split(" ").firstOrNull() ?: "",
                     lastName = found.name.split(" ").drop(1).joinToString(" "),
                     role = UserRole.STUDENT,
                     regNumber = null,
                     course = null,
                     department = null
                 )
             } else null
        } catch (e: Exception) { null }
    }

    suspend fun getSupportTickets(filter: String?): List<Map<String, Any>> {
         return try {
            api.getSupportTickets().body()?.items?.map {
                mapOf(
                    "id" to it.id.toString(),
                    "title" to it.title,
                    "description" to it.description,
                    "status" to it.status,
                    "priority" to it.priority,
                    "created_at" to it.created_at
                )
            } ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }
    
    suspend fun createSupportTicket(title: String, description: String, category: String, priority: String): Result<Unit> {
         return try {
            api.createSupportTicket(ApiService.CreateTicketRequest(title, description, category, priority))
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
    
    suspend fun getTicketDetails(id: String): Result<Pair<com.example.android.data.model.Ticket, List<com.example.android.data.model.TicketMessage>>> {
        return try {
            val response = api.getTicketDetails(id)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.success(Pair(body.ticket, body.messages))
            } else {
                Result.failure(Exception("Failed to load ticket"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun postMessage(ticketId: String, message: String): Result<com.example.android.data.model.TicketMessage> {
        return try {
            val response = api.replyToTicket(ticketId, com.example.android.data.model.TicketReplyRequest(message))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to send message: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTicketStatus(ticketId: String, status: String): Result<com.example.android.data.model.Ticket> {
        return try {
            val response = api.updateTicketStatus(ticketId, com.example.android.data.model.UpdateTicketStatusRequest(status))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update status"))
            }
        } catch (e: Exception) {
             Result.failure(e)
        }
    }
    
    suspend fun submitComplaint(userId: String, title: String, description: String, category: String): Result<Unit> {
        return createSupportTicket(title, description, category, "medium")
    }
    
    fun getComplaints(userId: String): Flow<List<Map<String, Any>>> = flow {
         val tickets = getSupportTickets(null)
         emit(tickets)
    }

    suspend fun getUserProfile(type: String): User? {
        // Mock implementation
        return User(
            id = "admin1", 
            email = "admin@example.com", 
            firstName = "Admin", 
            lastName = "User", 
            role = UserRole.ADMIN
        )
    }

    suspend fun diagnoseUser(userId: String): String {
        return "SQL Backend Active"
    } // Mock for debugging
}

package com.example.android.util

import kotlinx.coroutines.delay

object EmailService {
    suspend fun sendSubmissionConfirmation(email: String, assignmentTitle: String): Boolean {
        // Simulate network delay
        delay(1500)
        // Log "Sending email to $email..."
        return true
    }
}

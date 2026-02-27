package com.example.android.data.ai

class GeminiService {
    suspend fun generateResponse(prompt: String): String {
        // Mock response for now as we don't have an API key configured
        return when {
            prompt.contains("exam", ignoreCase = true) -> "Your next exam is Data Structures on March 15th at 9:00 AM in Hall A."
            prompt.contains("register", ignoreCase = true) -> "Course registration for Semester 2 is now open. Go to Academics -> Course Registration."
            prompt.contains("fees", ignoreCase = true) -> "You have an outstanding balance of KES 5,000. Please clear it before the exams."
            else -> "I'm a student assistant AI. I can help with exams, registration, and general inquiries."
        }
    }
}

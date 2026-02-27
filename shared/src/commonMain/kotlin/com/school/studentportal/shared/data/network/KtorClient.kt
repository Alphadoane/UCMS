package com.school.studentportal.shared.data.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object KtorClient {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    var baseUrl: String = "https://entreatingly-commonable-georgann.ngrok-free.dev/api/" 
    // var baseUrl: String = "http://10.0.2.2:8000/api/"

    val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        
        install(Logging) {
            level = LogLevel.INFO
            logger = Logger.SIMPLE
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 15000
        }

        defaultRequest {
            url(baseUrl)
            // contentType(ContentType.Application.Json) // Removed to avoid interference with Multipart uploads
        }
    }
}

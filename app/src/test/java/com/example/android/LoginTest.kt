package com.example.android

import com.example.android.data.network.ApiService
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.MediaType.Companion.toMediaType
import kotlinx.serialization.json.Json
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import org.junit.Test
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class LoginTest {
    private val baseUrl = "http://10.54.63.91:8000/api/"
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val networkJson = Json { ignoreUnknownKeys = true }

    private val testApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ApiService::class.java)
    
    @Test
    fun testLogin() = runBlocking {
        println("Starting login test...")
        
        try {
            val response = testApi.login(
                com.example.android.data.network.LoginRequest(
                    username = "student1",
                    password = "password123"
                )
            )
            
            println("Response code: ${response.code()}")
            println("Response body: ${response.body()}")
            println("Response error: ${response.errorBody()?.string()}")
            
            if (response.isSuccessful) {
                println("Login successful! Token: ${response.body()?.access}")
            } else {
                println("Login failed with status: ${response.code()}")
            }
        } catch (e: Exception) {
            println("Error during login test: ${e.message}")
            e.printStackTrace()
        }
    }
}

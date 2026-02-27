package com.example.android.data.network

import android.content.Context
import com.example.android.BuildConfig
import com.example.android.data.prefs.UserPrefs
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    var apiInstance: ApiService? = null

    // Ordered base URLs to try on network failure
    private val BASE_URLS: List<String> = listOf(
        "https://entreatingly-commonable-georgann.ngrok-free.dev/api/",
        "http://10.0.2.2:8000/api/",
        "http://10.21.167.10:8000/api/"
    )
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
        explicitNulls = false
        encodeDefaults = true
        coerceInputValues = true
        allowSpecialFloatingPointValues = true
    }
    
    fun createApiService(context: Context): ApiService {
        val userPrefs = UserPrefs(context)
        
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            
            // Get the token from UserPrefs
            val token = runBlocking {
                userPrefs.authToken.first()
            }
            
            // Debug logs only, without sensitive data
            if (BuildConfig.DEBUG) {
                Timber.d("[AuthInterceptor] %s %s", originalRequest.method, originalRequest.url)
            }
            
            // If we have a token, add it to the request
            val requestBuilder = originalRequest.newBuilder()
            if (!token.isNullOrBlank()) {
                // Send real token in header, but never log it
                requestBuilder.header("Authorization", "Bearer $token")
                if (BuildConfig.DEBUG) Timber.d("[AuthInterceptor] Authorization header added")
            } else if (BuildConfig.DEBUG) {
                Timber.d("[AuthInterceptor] No auth token")
            }
            
            // Add common headers
            requestBuilder.header("Content-Type", "application/json")
            requestBuilder.header("Accept", "application/json")
            
            // Proceed with the request
            chain.proceed(requestBuilder.build())
        }
        
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            // Route OkHttp logs to Timber
            Timber.tag("OkHttp").d(message)
        }.apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(TokenAuthenticator(userPrefs, BASE_URLS))
            // Fallback across multiple base URLs on network failures
            .addInterceptor(BaseUrlFallbackInterceptor(BASE_URLS.map { it.toHttpUrl() }))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        
        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URLS.first())
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            
        if (BuildConfig.DEBUG) Timber.i("Retrofit created with base URL: %s (fallbacks: %s)", BASE_URLS.first(), BASE_URLS.drop(1))
        
        val service = retrofit.create(ApiService::class.java)
        apiInstance = service
        return service
    }

    private class BaseUrlFallbackInterceptor(
        private val baseUrls: List<HttpUrl>
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val originalRequest = chain.request()
            var lastException: Exception? = null

            // Determine the initial index by matching the request host to our list, default to 0
            val originalIndex = baseUrls.indexOfFirst {
                it.host == originalRequest.url.host && it.port == originalRequest.url.port && it.scheme == originalRequest.url.scheme
            }.let { if (it >= 0) it else 0 }

            // Try current then the rest cyclically
            for (offset in baseUrls.indices) {
                val idx = (originalIndex + offset) % baseUrls.size
                val base = baseUrls[idx]
                val newUrl = originalRequest.url.newBuilder()
                    .scheme(base.scheme)
                    .host(base.host)
                    .port(base.port)
                    .build()
                val newRequest = originalRequest.newBuilder().url(newUrl).build()
                try {
                    if (BuildConfig.DEBUG && offset > 0) {
                        Timber.w("Retrying with alternate base URL: %s", base)
                    }
                    return chain.proceed(newRequest)
                } catch (e: java.io.IOException) {
                    lastException = e
                    if (BuildConfig.DEBUG) {
                        Timber.w(e, "Network failure with %s, trying next base URL if any", base)
                    }
                    // Continue to next base URL
                }
            }

            // If all attempts failed, throw the last exception
            if (lastException is java.io.IOException) throw lastException
            throw java.io.IOException("All base URLs failed", lastException)
        }
    }

    private class TokenAuthenticator(
        private val userPrefs: UserPrefs,
        private val baseUrls: List<String>
    ) : okhttp3.Authenticator {
        override fun authenticate(route: okhttp3.Route?, response: okhttp3.Response): okhttp3.Request? {
            // Prevent infinite loops: if the failed request already had the new token, give up
            if (responseCount(response) >= 3) {
                return null // Give up after 3 attempts
            }

            // Get refresh token
            val refreshToken = runBlocking { userPrefs.refreshToken.first() }
            if (refreshToken.isNullOrBlank()) {
                return null // No refresh token, can't refresh
            }

            // Try to refresh token
            // We use a raw OkHttp client to avoid circular dependencies and interceptors
            val client = OkHttpClient()
            val refreshUrl = baseUrls.first() + "auth/jwt/refresh" // Use first base URL
            
            // JSON body: {"refresh": "..."}
            val jsonBody = """{"refresh": "$refreshToken"}"""
            val requestBody = okhttp3.RequestBody.create("application/json".toMediaType(), jsonBody)
            
            val refreshRequest = okhttp3.Request.Builder()
                .url(refreshUrl)
                .post(requestBody)
                .build()

            try {
                val refreshResponse = client.newCall(refreshRequest).execute()
                if (refreshResponse.isSuccessful) {
                    val responseBody = refreshResponse.body?.string()
                    if (responseBody != null) {
                        // Parse manually to avoid serialization dependency issues here or use regex
                        // Simple parse for "access" token
                        val accessToken = extractToken(responseBody, "access")
                        // Optional: new refresh token
                        val newRefreshToken = extractToken(responseBody, "refresh")
                        
                        if (accessToken != null) {
                            runBlocking {
                                userPrefs.setAuthToken(accessToken, newRefreshToken ?: refreshToken)
                            }
                            
                            // Retry original request with new token
                            return response.request.newBuilder()
                                .header("Authorization", "Bearer $accessToken")
                                .build()
                        }
                    }
                } else {
                    // Refresh failed (e.g., refresh token expired)
                    // Clear tokens to force logout
                    runBlocking { userPrefs.setAuthToken(null, null) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh token")
            }
            
            return null
        }
        
        private fun extractToken(json: String, key: String): String? {
            // Simple regex to extract string value for key
            val regex = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            return regex.find(json)?.groupValues?.get(1)
        }

        private fun responseCount(response: okhttp3.Response): Int {
            var result = 1
            var prior = response.priorResponse
            while (prior != null) {
                result++
                prior = prior.priorResponse
            }
            return result
        }
    }
}

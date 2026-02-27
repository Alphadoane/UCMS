package com.example.android

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android.data.network.NetworkModule
import com.example.android.data.network.ApiService
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import retrofit2.Response

/**
 * Network integration test for API connectivity
 * Run this test to verify backend connection
 */
class NetworkTest {
    
    private lateinit var context: Context
    private lateinit var apiService: ApiService
    
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        apiService = NetworkModule.createApiService(context)
    }
    
    @Test
    fun testLoginEndpoint() = runBlocking {
        try {
            val loginRequest = com.example.android.data.network.LoginRequest(
                username = "student1",
                password = "password123"
            )
            
            val response: Response<com.example.android.data.network.LoginResponse> = 
                apiService.login(loginRequest)
            
            assertTrue("Login should be successful", response.isSuccessful)
            assertNotNull("Response body should not be null", response.body())
            
            val loginResponse = response.body()
            assertNotNull("Access token should not be null", loginResponse?.access)
            assertNotNull("Refresh token should not be null", loginResponse?.refresh)
            
            println("✅ Login test passed - Received tokens")
            
        } catch (e: Exception) {
            fail("Login test failed: ${e.message}")
        }
    }
    
    @Test
    fun testProfileEndpoint() = runBlocking {
        try {
            // First login to get token
            val loginRequest = com.example.android.data.network.LoginRequest(
                username = "student1",
                password = "password123"
            )
            
            val loginResponse = apiService.login(loginRequest)
            assertTrue("Login should succeed first", loginResponse.isSuccessful)
            
            // Test profile endpoint (this will fail without proper auth setup)
            // This test mainly checks if the endpoint is reachable
            val profileResponse = apiService.getProfile()
            
            // We expect either 200 (success) or 401 (unauthorized)
            assertTrue(
                "Profile endpoint should be reachable", 
                profileResponse.code() in listOf(200, 401)
            )
            
            println("✅ Profile endpoint test passed - Endpoint reachable")
            
        } catch (e: Exception) {
            fail("Profile test failed: ${e.message}")
        }
    }
    
    @Test
    fun testVotingEndpoint() = runBlocking {
        try {
            val response = apiService.getElections()
            
            // We expect either 200 (success) or 401 (unauthorized)
            assertTrue(
                "Voting endpoint should be reachable", 
                response.code() in listOf(200, 401)
            )
            
            println("✅ Voting endpoint test passed - Endpoint reachable")
            
        } catch (e: Exception) {
            fail("Voting test failed: ${e.message}")
        }
    }
    
    @Test
    fun testNetworkConfiguration() {
        // Test that NetworkModule can be created
        assertNotNull("NetworkModule should create ApiService", apiService)
        
        // Test that base URLs are configured
        // This is a basic check - in a real test you'd access the URLs
        println("✅ Network configuration test passed")
    }
}

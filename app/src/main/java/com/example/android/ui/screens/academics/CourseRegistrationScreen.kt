package com.example.android.ui.screens.academics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android.data.repository.AcademicsRepository
import com.example.android.ui.components.*

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CourseRegistrationScreen(
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { AcademicsRepository(context) }
    var userId by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
         val me = repository.getUserProfile("me")
         userId = me?.id
    }
    // State
    val availableCourses = remember { mutableStateListOf<Map<String, Any>>() }
    val cart = remember { mutableStateListOf<Map<String, Any>>() }
    var registeredCourses by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    
    val loading = remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Constants
    val MAX_CREDITS = 24
    val MIN_CREDITS = 15

    LaunchedEffect(userId) {
        val currentUserId = userId
        if (currentUserId != null) {
            try {
                // Fetch already registered courses
                registeredCourses = repository.getCourseRegistration(currentUserId)
                
                // Mock Available Courses (in a real app, fetch from specialized endpoint)
                availableCourses.addAll(listOf(
                    mapOf("id" to "1", "course_code" to "CS101", "course_name" to "Intro to Java", "credits" to 4, "prerequisite" to ""),
                    mapOf("id" to "2", "course_code" to "CS102", "course_name" to "Advanced Java", "credits" to 4, "prerequisite" to "CS101"),
                    mapOf("id" to "3", "course_code" to "MATH201", "course_name" to "Calculus II", "credits" to 3, "prerequisite" to "MATH101"),
                    mapOf("id" to "4", "course_code" to "ENG101", "course_name" to "Communication Skills", "credits" to 2, "prerequisite" to ""),
                    mapOf("id" to "5", "course_code" to "PHY101", "course_name" to "Descriptive Physics", "credits" to 3, "prerequisite" to ""),
                    mapOf("id" to "6", "course_code" to "CS301", "course_name" to "Data Structures", "credits" to 4, "prerequisite" to "CS102")
                ))
            } catch (e: Exception) {
                // Handle error
            } finally {
                loading.value = false
            }
        }
    }
    
    val currentCredits = (registeredCourses.sumOf { (it["credits"] as? Number)?.toInt() ?: 0 }) + 
                         (cart.sumOf { (it["credits"] as? Number)?.toInt() ?: 0 })

    PortalScaffold(title = "Course Registration") {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Credit Counter
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (currentCredits > MAX_CREDITS) Color(0xFFECEFF1) else Color(0xFFE3F2FD))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Credits", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "$currentCredits / $MAX_CREDITS", 
                                style = MaterialTheme.typography.titleLarge, 
                                fontWeight = FontWeight.Bold,
                                color = if (currentCredits > MAX_CREDITS) Color(0xFFF57C00) else Color(0xFF1D3762)
                            )
                        }
                        if (currentCredits < MIN_CREDITS) {
                            Text("Below Minimum ($MIN_CREDITS)", color = Color(0xFFE65100), style = MaterialTheme.typography.labelSmall)
                        } else if (currentCredits > MAX_CREDITS) {
                             Text("Overload!", color = Color(0xFFF57C00), style = MaterialTheme.typography.labelSmall)
                        } else {
                             Text("Good Limit", color = Color(0xFF2E7D32), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                if (loading.value) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // CART SECTION
                        if (cart.isNotEmpty()) {
                            stickyHeader {
                                Surface(color = MaterialTheme.colorScheme.background) {
                                    Text(
                                        "Selected Units (Draft)", 
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D3762)
                                    )
                                }
                            }
                            items(cart) { course ->
                                CartItem(course = course, onRemove = { cart.remove(course) })
                            }
                            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
                        }

                        // AVAILABLE SECTION
                        stickyHeader {
                            Surface(color = MaterialTheme.colorScheme.background) {
                                Text(
                                    "Available for Enrollment", 
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                            }
                        }
                        
                        items(availableCourses) { course ->
                            val isRegistered = registeredCourses.any { it["course_code"] == course["course_code"] }
                            val inCart = cart.any { it["course_code"] == course["course_code"] }
                            
                            CourseItem(
                                course = course,
                                isRegistered = isRegistered,
                                inCart = inCart,
                                onAdd = {
                                    // Mock Prerequisite Check
                                    val prereq = course["prerequisite"] as String
                                    if (prereq.isNotEmpty()) {
                                        // Randomly fail prerequisite for demo if course code ends with '2' or '1' logic could be added
                                        // keeping it simple: allow all for now or show warning
                                    }
                                    
                                    if (currentCredits + ((course["credits"] as Int)) > MAX_CREDITS) {
                                        scope.launch { snackbarHostState.showSnackbar("Credit limit exceeded!") }
                                    } else {
                                        cart.add(course)
                                    }
                                }
                            )
                        }
                    }
                }
                
                // Action Bar
                AnimatedVisibility(visible = cart.isNotEmpty()) {
                    Surface(
                        shadowElevation = 16.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${cart.size} units selected")
                            Button(
                                onClick = {
                                    scope.launch {
                                        loading.value = true
                                        delay(1500) // Simulate network
                                        registeredCourses = registeredCourses + cart
                                        cart.clear()
                                        loading.value = false
                                        snackbarHostState.showSnackbar("Registration Successful!")
                                    }
                                },
                                enabled = !loading.value,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3762))
                            ) {
                                Text("Confirm Registration")
                            }
                        }
                    }
                }
            }
            
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
fun CourseItem(course: Map<String, Any>, isRegistered: Boolean, inCart: Boolean, onAdd: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${course["course_code"]} - ${course["course_name"]}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${course["credits"]} Credits ${if(course["prerequisite"] != "") "• Pre: ${course["prerequisite"]}" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            when {
                isRegistered -> {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                         Text(" Enrolled", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelSmall)
                     }
                }
                inCart -> {
                    Text("In Cart", color = Color(0xFF1D3762), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                else -> {
                    IconButton(onClick = onAdd, modifier = Modifier.background(Color(0xFFE3F2FD), androidx.compose.foundation.shape.CircleShape).size(32.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF1D3762), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CartItem(course: Map<String, Any>, onRemove: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${course["course_code"]}", fontWeight = FontWeight.Bold)
                Text("${course["credits"]} Credits", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Gray)
            }
        }
    }
}



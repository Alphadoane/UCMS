package com.school.studentportal.shared.ui.screens.student.academics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.data.repository.AcademicsRepository
import com.school.studentportal.shared.data.model.AcademicCourse
import kotlinx.coroutines.launch

@Composable
fun CourseRegistrationScreen(
    repository: AcademicsRepository,
    onBack: () -> Unit = {}
) {
    // State
    val availableCourses by repository.availableCourses.collectAsState()
    val cart = remember { mutableStateListOf<AcademicCourse>() }
    
    var loading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val MAX_CREDITS = 24
    val currentCredits = (availableCourses.filter { it.is_enrolled }.sumOf { it.credits }) + (cart.sumOf { it.credits })

    LaunchedEffect(Unit) {
        repository.refreshAvailableCourses()
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (cart.isNotEmpty()) {
                    item { Text("Selected Units (Draft)", fontWeight = FontWeight.Bold, color = Color(0xFF1D3762)) }
                    items(cart) { course ->
                        CartItem(course = course, onRemove = { cart.remove(course) })
                    }
                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
                }

                item { Text("Available for Enrollment", fontWeight = FontWeight.Bold, color = Color.Gray) }
                
                items(availableCourses.filter { !it.is_enrolled }) { course ->
                    val inCart = cart.any { it.id == course.id }
                    
                    CourseItem(
                        course = course,
                        isRegistered = false,
                        inCart = inCart,
                        onAdd = {
                            if (currentCredits + course.credits > MAX_CREDITS) {
                                scope.launch { snackbarHostState.showSnackbar("Credit limit exceeded!") }
                            } else {
                                cart.add(course)
                            }
                        }
                    )
                }

                if (availableCourses.any { it.is_enrolled }) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { Text("Already Enrolled", fontWeight = FontWeight.Bold, color = Color.Gray) }
                    items(availableCourses.filter { it.is_enrolled }) { course ->
                        CourseItem(
                            course = course,
                            isRegistered = true,
                            inCart = false,
                            onAdd = {}
                        )
                    }
                }
            }
            
            AnimatedVisibility(visible = cart.isNotEmpty()) {
                Surface(shadowElevation = 16.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${cart.size} units selected")
                        Button(
                            onClick = {
                                scope.launch {
                                    loading = true
                                    val result = repository.registerCourses(cart.map { it.id })
                                    if (result.isSuccess) {
                                        snackbarHostState.showSnackbar("Registration Successful!")
                                        cart.clear()
                                        repository.refreshAvailableCourses()
                                    } else {
                                        snackbarHostState.showSnackbar("Error: ${result.exceptionOrNull()?.message}")
                                    }
                                    loading = false
                                }
                            },
                            enabled = !loading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3762))
                        ) {
                            if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            else Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseItem(course: AcademicCourse, isRegistered: Boolean, inCart: Boolean, onAdd: () -> Unit) {
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
                Text("${course.code} - ${course.title}", fontWeight = FontWeight.Bold)
                Text("${course.credits} Credits", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            when {
                isRegistered -> Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50))
                inCart -> Text("In Cart", color = Color(0xFF1D3762), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                else -> IconButton(onClick = onAdd, modifier = Modifier.background(Color(0xFFE3F2FD), CircleShape).size(32.dp)) {
                    Icon(Icons.Default.Add, null, tint = Color(0xFF1D3762), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun CartItem(course: AcademicCourse, onRemove: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ShoppingCart, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${course.code}", fontWeight = FontWeight.Bold)
                Text("${course.credits} Credits", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onRemove) { Icon(Icons.Default.Close, null, tint = Color.Gray) }
        }
    }
}

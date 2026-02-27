package com.example.android.ui.screens.academics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grade
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.android.data.repository.AcademicsRepository
import com.example.android.ui.components.*

@Composable
fun ExamResultScreen() {
    val context = LocalContext.current
    val repository = remember { AcademicsRepository(context) }
    var userId by remember { mutableStateOf<String?>(null) }
    val items = remember { mutableStateListOf<Map<String, Any>>() }
    val error = remember { mutableStateOf<String?>(null) }
    val loading = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
         try {
             val me = repository.getUserProfile("me")
             userId = me?.id
         } catch(e: Exception) {}
    }

    LaunchedEffect(userId) {
        val currentUserId = userId
        if (currentUserId != null) {
            try {
                val data = repository.getExamResult(currentUserId)
                items.clear()
                items.addAll(data)
            } catch (e: Exception) {
                error.value = e.message
            } finally {
                loading.value = false
            }
        } else {
            error.value = "User not logged in"
            loading.value = false
        }
    }

    PortalScaffold(title = "Exam Results") {
        when {
            loading.value -> LoadingView()
            error.value != null -> ErrorView(message = error.value!!)
            else -> {
                AnimatedList(items = items) { item ->
                    InfoCard(
                        title = "${item["course_code"]} - ${item["exam_type"]}",
                        subtitle = "Score: ${item["marks"]} / ${item["max_marks"]}\nGrade: ${item["grade"]}",
                        status = item["grade"] as? String,
                        icon = Icons.Default.Grade
                    )
                }
            }
        }
    }
}

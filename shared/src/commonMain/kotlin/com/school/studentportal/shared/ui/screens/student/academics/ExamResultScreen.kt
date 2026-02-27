package com.school.studentportal.shared.ui.screens.student.academics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import com.school.studentportal.shared.ui.components.AppScaffold
import com.school.studentportal.shared.ui.components.InfoCard
import com.school.studentportal.shared.ui.components.AnimatedList

import com.school.studentportal.shared.data.repository.AcademicsRepository

@Composable
fun ExamResultScreen(
    repository: AcademicsRepository,
    onBack: () -> Unit = {}
) {
    val examResults by repository.examResults.collectAsState()

    LaunchedEffect(Unit) {
        repository.refreshExamResults()
    }

    AnimatedList(items = examResults) { item ->
            InfoCard(
                title = "${item.course_code} - ${item.course_title}",
                subtitle = "Score: ${item.score}\nGrade: ${item.grade}\nSemester: ${item.semester}",
                status = item.grade,
                icon = Icons.Default.Grade
            )
    }
}

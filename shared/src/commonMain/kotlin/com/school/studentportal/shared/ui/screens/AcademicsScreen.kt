package com.school.studentportal.shared.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.ui.Routes
import com.school.studentportal.shared.ui.components.AppScaffold

@Composable
fun AcademicsScreen(onNavigate: (String) -> Unit) {
    val items = listOf(
        "Course Registration" to Routes.ACADEMICS_COURSE_REG,
        "Course Work" to Routes.ACADEMICS_COURSE_WORK,
        "Exam Card" to Routes.ACADEMICS_EXAM_CARD,
        "Exam Audit" to Routes.ACADEMICS_EXAM_AUDIT,
        "Exam Result" to Routes.ACADEMICS_EXAM_RESULT,
        "Result Slip" to Routes.ACADEMICS_RESULT_SLIP,
        "Academic Leave" to Routes.ACADEMICS_ACADEMIC_LEAVE,
        "Clearance" to Routes.ACADEMICS_CLEARANCE,
        "Insights" to Routes.ACADEMICS_INSIGHTS
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { (label, route) ->
            NavigationDrawerItem(
                label = { Text(label) },
                selected = false,
                onClick = { onNavigate(route) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}

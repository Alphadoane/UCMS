package com.example.android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.android.ui.components.PortalScaffold
import com.example.android.ui.components.InfoCard
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun AcademicsScreen(onNavigate: (String) -> Unit) {
    val items = listOf(
        "Course Registration" to "academics_course_registration",
        "Course Work" to "academics_course_work",
        "Exam Card" to "academics_exam_card",
        "Exam Audit" to "academics_exam_audit",
        "Result Slip" to "academics_result_slip"
    )
    
    PortalScaffold(
        title = "Academics",
        navigationIcon = {
            IconButton(onClick = { onNavigate("home") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp) // Fixed argument name
        ) {
            items.forEach { (label, route) ->
                InfoCard(
                    title = label,
                    onClick = { onNavigate(route) }
                )
            }
        }
    }
}

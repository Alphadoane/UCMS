package com.school.studentportal.shared.ui.screens.staff

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffStudentLookupScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Lookup") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<") 
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                "Student Lookup Feature Coming Soon",
                modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
            )
        }
    }
}

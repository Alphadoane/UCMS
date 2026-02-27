package com.school.studentportal.shared.ui.screens.student.finance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.ui.Routes
import com.school.studentportal.shared.ui.components.AppScaffold

@Composable
fun FinanceScreen(onNavigate: (String) -> Unit) {
    val items = listOf(
        "Fee Payment" to Routes.FINANCE_FEE_PAYMENT,
        "View Balance" to Routes.FINANCE_VIEW_BALANCE,
        "Receipts" to Routes.FINANCE_RECEIPTS
    )
    
    AppScaffold(title = "Finance", showTopBar = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
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
}

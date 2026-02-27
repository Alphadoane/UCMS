package com.school.studentportal.shared.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.studentportal.shared.data.repository.AnalyticsRepository
import kotlinx.coroutines.launch
import com.school.studentportal.shared.ui.components.DashboardCard
import com.school.studentportal.shared.ui.components.InfoRow
import com.school.studentportal.shared.ui.components.LoadingView
import com.school.studentportal.shared.ui.components.ErrorView
import com.school.studentportal.shared.ui.components.StatSmallCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAnalyticsScreen(
    repository: AnalyticsRepository,
    onBack: () -> Unit
) {
    val analytics by repository.studentAnalytics.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        repository.refreshStudentAnalytics().onSuccess {
            isLoading = false
        }.onFailure {
            error = it.message ?: "Failed to load performance data"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance & Financial Insights") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            LoadingView()
        } else if (error != null) {
            ErrorView(error!!) {
                isLoading = true
                error = null
                scope.launch {
                    repository.refreshStudentAnalytics().onSuccess { isLoading = false }.onFailure { error = it.message }
                }
            }
        } else {
            analytics?.let { data ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Academic Performance Portfolio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    
                    DashboardCard(title = "GPA Trends", icon = Icons.Default.TrendingUp, color = Color(0xFF3F51B5)) {
                        Row(modifier = Modifier.height(150.dp).fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.Bottom) {
                            data.gpaTrend.forEach { point ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .fillMaxHeight((point.gpa / 4.0).toFloat())
                                        .background(Color(0xFF3F51B5), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    )
                                    Text(point.semester, style = MaterialTheme.typography.labelSmall)
                                    Text("${point.gpa}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatSmallCard("Attendance", "${(data.attendanceRate * 100).toInt()}%", Icons.Default.CalendarToday, Color(0xFF009688), Modifier.weight(1f))
                        StatSmallCard("Completion", "${(data.assignmentCompletion * 100).toInt()}%", Icons.Default.Task, Color(0xFFFF9800), Modifier.weight(1f))
                    }

                    Text("Financial Performance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                    DashboardCard(title = "Payment Consistency", icon = Icons.Default.Shield, color = Color(0xFF673AB7)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            CircularProgressIndicator(
                                progress = data.financialStatus.paymentConsistencyScore / 100f,
                                modifier = Modifier.size(100.dp),
                                strokeWidth = 8.dp,
                                color = Color(0xFF673AB7)
                            )
                            Text("${data.financialStatus.paymentConsistencyScore}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        }
                        Text("Financial compliance score based on timely fee settlement.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }

                    DashboardCard(title = "Summary", icon = Icons.Default.Info, color = Color.Gray) {
                        InfoRow("Total Billed", "KSh ${data.financialStatus.totalBilled}")
                        InfoRow("Total Paid", "KSh ${data.financialStatus.totalPaid}")
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        InfoRow("Current Balance", "KSh ${data.financialStatus.balance}")
                    }
                }
            }
        }
    }
}

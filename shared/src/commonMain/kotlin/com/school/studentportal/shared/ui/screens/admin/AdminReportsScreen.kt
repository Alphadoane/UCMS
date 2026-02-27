package com.school.studentportal.shared.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.school.studentportal.shared.data.repository.AnalyticsRepository
import kotlinx.coroutines.launch
import com.school.studentportal.shared.ui.components.DashboardCard
import com.school.studentportal.shared.ui.components.InfoRow
import com.school.studentportal.shared.ui.components.LoadingView
import com.school.studentportal.shared.ui.components.ErrorView
import com.school.studentportal.shared.ui.components.StatSmallCard

@OptIn(ExperimentalExtraMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    repository: AnalyticsRepository,
    onBack: () -> Unit
) {
    val report by repository.adminReport.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Track which sections have been "generated" (requested by user)
    var generatedSections by remember { mutableStateOf(setOf<String>()) }
    var loadingSections by remember { mutableStateOf(setOf<String>()) }

    fun generateSection(section: String) {
        loadingSections = loadingSections + section
        scope.launch {
            repository.refreshAdminReports().onSuccess {
                generatedSections = generatedSections + section
                loadingSections = loadingSections - section
                snackbarHostState.showSnackbar("$section report generated successfully")
            }.onFailure {
                loadingSections = loadingSections - section
                snackbarHostState.showSnackbar("Failed to generate $section report: ${it.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Intelligence & Reports") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Export PDF */ }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF")
                    }
                    if (generatedSections.isNotEmpty()) {
                        TextButton(onClick = { 
                            generatedSections = emptySet()
                        }) {
                            Text("Reset")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "On-Demand Report Generation", 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Select a module to analyze current system data. Reports are not live until explicitly generated.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            // Section 1: Global Overview
            ReportSectionCard(
                title = "Participation & Engagement",
                icon = Icons.Default.Groups,
                color = Color(0xFF2196F3),
                isGenerated = "Overview" in generatedSections,
                isLoading = "Overview" in loadingSections,
                onGenerate = { generateSection("Overview") }
            ) {
                report?.let { data ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatSmallCard("Turnout", "${data.votingTurnout}%", Icons.Default.HowToVote, Color(0xFF4CAF50), Modifier.weight(1f))
                        StatSmallCard("Engagement", "${data.activeStudentsWeekly}", Icons.Default.Groups, Color(0xFF2196F3), Modifier.weight(1f))
                    }
                }
            }

            // Section 2: Financial Health
            ReportSectionCard(
                title = "Financial Performance",
                icon = Icons.Default.AccountBalanceWallet,
                color = Color(0xFFE91E63),
                isGenerated = "Finance" in generatedSections,
                isLoading = "Finance" in loadingSections,
                onGenerate = { generateSection("Finance") }
            ) {
                report?.let { data ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoRow("Total Collected Revenue", "KSh ${data.totalRevenue}")
                        val progress = (data.collectionRate / 100.0).toFloat().coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = progress, 
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), 
                            color = Color(0xFFE91E63),
                            trackColor = Color(0xFFE91E63).copy(alpha = 0.1f)
                        )
                        Text("${data.collectionRate}% of projected fee collection achieved", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Section 3: UCMS Analytics
            ReportSectionCard(
                title = "Support & Resolution (UCMS)",
                icon = Icons.Default.ConfirmationNumber,
                color = Color(0xFF673AB7),
                isGenerated = "UCMS" in generatedSections,
                isLoading = "UCMS" in loadingSections,
                onGenerate = { generateSection("UCMS") }
            ) {
                report?.let { data ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoRow("Total Complaints", "${data.totalComplaints}")
                        InfoRow("Resolution Rate", "${(data.resolvedComplaints.toDouble() / data.totalComplaints * 100).toInt()}%")
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF673AB7).copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Average response time: ${data.averageResponseTimeHours} hours", 
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp),
                                color = Color(0xFF673AB7)
                            )
                        }
                    }
                }
            }

            // Section 4: Module Engagement
            ReportSectionCard(
                title = "Module Usage Breakdown",
                icon = Icons.Default.BarChart,
                color = Color(0xFF009688),
                isGenerated = "Engagement" in generatedSections,
                isLoading = "Engagement" in loadingSections,
                onGenerate = { generateSection("Engagement") }
            ) {
                report?.let { data ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        data.moduleEngagement.forEach { (module, count) ->
                            ModuleEngagementRow(module, count)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ReportSectionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    isGenerated: Boolean,
    isLoading: Boolean,
    onGenerate: () -> Unit,
    content: @Composable () -> Unit
) {
    DashboardCard(title = title, icon = icon, color = color) {
        if (!isGenerated) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp), color = color)
                    Spacer(Modifier.height(12.dp))
                    Text("Fetching data...", style = MaterialTheme.typography.labelMedium)
                } else {
                    Icon(
                        Icons.Default.QueryStats, 
                        contentDescription = null, 
                        modifier = Modifier.size(48.dp),
                        tint = color.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No data generated for this module yet.", 
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onGenerate,
                        colors = ButtonDefaults.buttonColors(containerColor = color),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Generate Report")
                    }
                }
            }
        } else {
            content()
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = onGenerate,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Refresh", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

private annotation class ExperimentalExtraMaterial3Api

@Composable
fun ModuleEngagementRow(name: String, count: Int) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Text("$count interactions", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

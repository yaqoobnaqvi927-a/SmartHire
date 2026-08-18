package com.cs22.example.smarthire.ui.recruiter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.ui.theme.*
import com.cs22.example.smarthire.viewmodel.RecruiterUiState
import com.cs22.example.smarthire.viewmodel.RecruiterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardScreen(viewModel: RecruiterViewModel, navController: NavHostController) {
    val statsState by viewModel.statsState.collectAsState()
    var selectedPeriod by remember { mutableStateOf("30D") }
    val periods = listOf("7D", "30D", "90D", "All Time")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold, color = StOnSurface) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = StOnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StBackground)
            )
        },
        containerColor = StBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(periods.size) { index ->
                        val period = periods[index]
                        val isSelected = period == selectedPeriod
                        Surface(
                            onClick = { selectedPeriod = period },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) StMatchBadgeBg else StSurface,
                            border = BorderStroke(1.dp, if (isSelected) StPrimary else StOutlineVariant)
                        ) {
                            Text(
                                text = period,
                                color = if (isSelected) StPrimary else StOnSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item {
                val stats = (statsState as? RecruiterUiState.Success)?.data
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        MetricCard(Modifier.weight(1f), Icons.Default.Visibility, stats?.total_jobs?.toString() ?: "1,245", "Total Views", "↑12%")
                        MetricCard(Modifier.weight(1f), Icons.Default.People, stats?.total_applications?.toString() ?: "148", "Applications", "↑8%")
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        MetricCard(Modifier.weight(1f), Icons.Default.CheckCircle, "45%", "Interview Rate", "↑5%")
                        MetricCard(Modifier.weight(1f), Icons.Default.Work, "12%", "Offer Rate", "↓2%")
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = StSurface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Top Performing Jobs", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StOnSurface)
                        Spacer(Modifier.height(16.dp))
                        JobPerformanceBar("Senior Android Dev", 0.8f, "124 apps")
                        JobPerformanceBar("Backend Engineer", 0.6f, "98 apps")
                        JobPerformanceBar("UI/UX Designer", 0.4f, "56 apps")
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = StSurface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Conversion Funnel", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StOnSurface)
                        Spacer(Modifier.height(24.dp))
                        
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height
                            val bars = listOf(1f, 0.7f, 0.4f, 0.2f)
                            val barWidth = width / (bars.size * 2)
                            val spacing = barWidth
                            
                            bars.forEachIndexed { index, fill ->
                                val startX = index * (barWidth + spacing) + spacing / 2
                                drawRoundRect(
                                    color = StPrimary,
                                    topLeft = Offset(startX, height - (height * fill)),
                                    size = Size(barWidth, height * fill),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = StSurface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = StPrimary)
                            Spacer(Modifier.width(8.dp))
                            Text("AI Insights", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StOnSurface)
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("• 'Senior Android Dev' role is performing 40% above average.", fontSize = 14.sp, color = StOnSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("• Adding salary details increases applications by 25%.", fontSize = 14.sp, color = StOnSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("• Time-to-hire has decreased by 3 days this month.", fontSize = 14.sp, color = StOnSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, trend: String) {
    val isPositive = trend.startsWith("↑")
    Card(
        modifier = modifier, 
        shape = RoundedCornerShape(18.dp), 
        colors = CardDefaults.cardColors(StSurface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Box(Modifier.background(StMatchBadgeBg, CircleShape).padding(8.dp)) {
                    Icon(icon, null, tint = StPrimary, modifier = Modifier.size(20.dp))
                }
                Surface(
                    color = if (isPositive) StSuccess.copy(alpha = 0.1f) else StError.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        trend, 
                        color = if (isPositive) StSuccess else StError, 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
            Text(label, fontSize = 12.sp, color = StOnSurfaceVariant)
        }
    }
}

@Composable
fun JobPerformanceBar(title: String, progress: Float, trailingText: String) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, fontSize = 14.sp, color = StOnSurface, fontWeight = FontWeight.Medium)
            Text(trailingText, fontSize = 14.sp, color = StTextSecondary)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = StPrimary,
            trackColor = StSurfaceContainer
        )
    }
}

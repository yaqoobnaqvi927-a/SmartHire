package com.cs22.example.smarthire.ui.recruiter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.viewmodel.RecruiterUiState
import com.cs22.example.smarthire.viewmodel.RecruiterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardScreen(viewModel: RecruiterViewModel, navController: NavHostController) {
    val statsState by viewModel.statsState.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF0F131D)).padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF1D2433).copy(alpha=0.8f))) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color(0xFFE1E2E4))
                }
                Spacer(Modifier.width(16.dp))
                Text("Analytics Hub", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE1E2E4))
            }
        },
        containerColor = Color(0xFF0F131D)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 50.dp, y = (-50).dp).size(300.dp).background(Color(0xFF3B82F6).copy(alpha = 0.1f), CircleShape).blur(120.dp))

            LazyColumn(
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Text("Hiring Pipeline Overview", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                item {
                    val stats = (statsState as? RecruiterUiState.Success)?.data
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        MetricCard(Modifier.weight(1f), Icons.Default.Visibility, stats?.total_jobs?.toString() ?: "12", "Jobs")
                        MetricCard(Modifier.weight(1f), Icons.Default.People, stats?.total_applications?.toString() ?: "148", "Applicants")
                        MetricCard(Modifier.weight(1f), Icons.Default.CheckCircle, stats?.screened_candidates?.toString() ?: "45", "Screened")
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B28)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            Text("Profile Views Over Time", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFE1E2E4))
                            Spacer(Modifier.height(24.dp))
                            
                            // Mock Line Chart using Canvas
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height
                                val points = listOf(0.8f, 0.6f, 0.9f, 0.4f, 0.7f, 0.5f, 0.2f)
                                val stepX = width / (points.size - 1)
                                
                                for (i in 0 until points.size - 1) {
                                    val startX = i * stepX
                                    val startY = points[i] * height
                                    val endX = (i + 1) * stepX
                                    val endY = points[i + 1] * height
                                    
                                    drawLine(
                                        brush = Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))),
                                        start = Offset(startX, startY),
                                        end = Offset(endX, endY),
                                        strokeWidth = 4.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                    drawCircle(
                                        color = Color(0xFF3B82F6),
                                        radius = 6.dp.toPx(),
                                        center = Offset(startX, startY)
                                    )
                                }
                                drawCircle(
                                    color = Color(0xFF3B82F6),
                                    radius = 6.dp.toPx(),
                                    center = Offset(width, points.last() * height)
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B28)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            Text("Conversion Funnel", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFE1E2E4))
                            Spacer(Modifier.height(24.dp))
                            
                            // Mock Bar Chart
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height
                                val bars = listOf(1f, 0.6f, 0.3f, 0.1f)
                                val barWidth = width / (bars.size * 2)
                                val spacing = barWidth
                                
                                bars.forEachIndexed { index, fill ->
                                    val startX = index * (barWidth + spacing) + spacing / 2
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF3B82F6))),
                                        topLeft = Offset(startX, height - (height * fill)),
                                        size = Size(barWidth, height * fill),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Card(modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(Color(0xFF161B28)), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(label, fontSize = 12.sp, color = Color(0xFFC2C6D6))
        }
    }
}

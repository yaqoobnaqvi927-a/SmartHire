package com.cs22.example.smarthire.ui.seeker

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.viewmodel.SeekerUiState
import com.cs22.example.smarthire.viewmodel.SeekerViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillGapReportScreen(viewModel: SeekerViewModel, navController: NavHostController, applicationId: String) {
    val skillGapState by viewModel.skillGapState.collectAsState()
    
    LaunchedEffect(applicationId) {
        viewModel.fetchSkillGap(applicationId.toIntOrNull() ?: 0)
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF0F131D)).padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF1D2433).copy(alpha=0.8f))) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color(0xFFE1E2E4))
                }
                Text("AI Match Report", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE1E2E4))
                IconButton(onClick = { }, modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF1D2433).copy(alpha=0.8f))) {
                    Icon(Icons.Default.Share, null, tint = Color(0xFFE1E2E4))
                }
            }
        },
        containerColor = Color(0xFF0F131D)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(modifier = Modifier.offset(x = 100.dp, y = (-50).dp).size(300.dp).background(Color(0xFF10B981).copy(alpha = 0.1f), CircleShape).blur(120.dp))
            Box(modifier = Modifier.align(Alignment.BottomStart).offset(x = (-100).dp, y = 100.dp).size(300.dp).background(Color(0xFF8B5CF6).copy(alpha = 0.1f), CircleShape).blur(120.dp))

            when (val s = skillGapState) {
                is SeekerUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Color(0xFF3B82F6)) }
                is SeekerUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(s.message, color = Color.Red) }
                is SeekerUiState.Success -> {
                    val score = (s.data["match_percentage"] as? Number)?.toFloat() ?: 78f
                    val matched = s.data["matched_skills"] as? List<String> ?: listOf("Python", "Django", "SQL")
                    val missing = s.data["missing_skills"] as? List<String> ?: listOf("Docker", "AWS", "Kubernetes")
                    val rec = s.data["recommendation"] as? String ?: "Consider learning Docker and AWS to improve your score for similar roles."

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item { Spacer(Modifier.height(32.dp)) }
                        item {
                            Box(Modifier.size(160.dp), Alignment.Center) {
                                Canvas(Modifier.fillMaxSize()) {
                                    drawCircle(Color(0xFF1D2433), style = Stroke(12.dp.toPx()))
                                    drawArc(
                                        brush = Brush.sweepGradient(listOf(Color(0xFF3B82F6), Color(0xFF10B981))),
                                        startAngle = -90f, sweepAngle = (score / 100f) * 360f, useCenter = false, style = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${score.toInt()}%", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    Text("Overall Match", fontSize = 14.sp, color = Color(0xFFC2C6D6))
                                }
                            }
                            Spacer(Modifier.height(48.dp))
                        }

                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(Modifier.weight(1f)) {
                                    Text("Required Skills", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFE1E2E4))
                                    Spacer(Modifier.height(16.dp))
                                    matched.forEach { skill ->
                                        Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(skill, color = Color(0xFFC2C6D6), fontSize = 14.sp)
                                        }
                                    }
                                    missing.forEach { skill ->
                                        Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Cancel, null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(skill, color = Color(0xFFC2C6D6), fontSize = 14.sp)
                                        }
                                    }
                                }
                                Column(Modifier.weight(1f)) {
                                    Text("Your Skills", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFE1E2E4))
                                    Spacer(Modifier.height(16.dp))
                                    matched.forEach { skill ->
                                        Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(skill, color = Color(0xFFC2C6D6), fontSize = 14.sp)
                                        }
                                    }
                                    missing.forEach { _ ->
                                        Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.RemoveCircleOutline, null, tint = Color(0xFF1D2433), modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Missing", color = Color(0xFFC2C6D6).copy(alpha=0.5f), fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(48.dp))
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF8B5CF6).copy(alpha=0.1f)),
                                border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha=0.3f))
                            ) {
                                Column(Modifier.padding(24.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(24.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text("Gemini's Advice", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF8B5CF6))
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Text(rec, color = Color(0xFFE1E2E4), fontSize = 15.sp, lineHeight = 24.sp)
                                }
                            }
                            Spacer(Modifier.height(48.dp))
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

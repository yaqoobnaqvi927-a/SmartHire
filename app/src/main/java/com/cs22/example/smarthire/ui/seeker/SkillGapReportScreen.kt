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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.ui.theme.*
import com.cs22.example.smarthire.viewmodel.SeekerUiState
import com.cs22.example.smarthire.viewmodel.SeekerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SkillGapReportScreen(viewModel: SeekerViewModel, navController: NavHostController, applicationId: String) {
    val skillGapState by viewModel.skillGapState.collectAsState()
    
    LaunchedEffect(applicationId) {
        viewModel.fetchSkillGap(applicationId.toIntOrNull() ?: 0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CV Analysis", color = StPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = StOnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StBackground)
            )
        },
        containerColor = StBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = skillGapState) {
                is SeekerUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = StPrimary) }
                is SeekerUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(s.message, color = StError) }
                is SeekerUiState.Success -> {
                    val score = (s.data["match_percentage"] as? Number)?.toFloat() ?: 78f
                    val matched = s.data["matched_skills"] as? List<String> ?: listOf("Python", "Django")
                    val missing = s.data["missing_skills"] as? List<String> ?: listOf("Docker", "AWS")
                    val rec = s.data["recommendation"] as? String ?: "Consider learning Docker."

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item { Spacer(Modifier.height(16.dp)) }
                        
                        // OVERALL MATCH CARD
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(StSurface),
                                border = BorderStroke(1.dp, StOutlineVariant),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(Modifier.size(160.dp), Alignment.Center) {
                                        Canvas(Modifier.fillMaxSize()) {
                                            drawCircle(StOutlineVariant, style = Stroke(12.dp.toPx()))
                                            drawArc(
                                                color = StPrimary,
                                                startAngle = -90f, sweepAngle = (score / 100f) * 360f, useCenter = false, style = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("${score.toInt()}%", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = StOnSurface)
                                        }
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    Text("Overall Match Score", fontSize = 16.sp, color = StTextSecondary, fontWeight = FontWeight.Medium)
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                        }

                        // SKILL ANALYSIS SECTION
                        item {
                            Column(Modifier.fillMaxWidth()) {
                                Text("Skill Analysis", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = StOnSurface)
                                Spacer(Modifier.height(16.dp))
                                
                                if (matched.isNotEmpty()) {
                                    Text("Matched Skills", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = StSuccess)
                                    Spacer(Modifier.height(8.dp))
                                    matched.forEach { skill ->
                                        Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, null, tint = StSuccess, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(skill, color = StOnSurface, fontSize = 14.sp)
                                        }
                                    }
                                    Spacer(Modifier.height(16.dp))
                                }
                                
                                if (missing.isNotEmpty()) {
                                    Text("Missing Skills", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = StError)
                                    Spacer(Modifier.height(8.dp))
                                    missing.forEach { skill ->
                                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.ErrorOutline, null, tint = StError, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(skill, color = StOnSurface, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                            Surface(color = StSurface, shape = RoundedCornerShape(25.dp), border = BorderStroke(1.dp, StOutlineVariant)) {
                                                Text("Learn", color = StTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                        }

                        // AI RECOMMENDATIONS
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(StMatchBadgeBg),
                                border = BorderStroke(1.dp, StOutlineVariant)
                            ) {
                                Column(Modifier.padding(24.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Lightbulb, null, tint = StPrimary, modifier = Modifier.size(24.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text("AI Recommendations", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StPrimary)
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Text(rec, color = StOnSurface, fontSize = 15.sp, lineHeight = 24.sp)
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

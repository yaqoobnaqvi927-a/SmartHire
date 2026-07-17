package com.cs22.example.smarthire.ui.seeker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.cs22.example.smarthire.model.DjangoJob
import com.cs22.example.smarthire.viewmodel.SeekerUiState
import com.cs22.example.smarthire.viewmodel.SeekerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(viewModel: SeekerViewModel, navController: NavHostController, jobId: String) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val jobsState by viewModel.jobsState.collectAsState()
    val matchScoreState by viewModel.matchScoreState.collectAsState()
    
    // Find the job from the current state
    val job = (jobsState as? SeekerUiState.Success)?.data?.find { it.id == jobId }
    
    LaunchedEffect(jobId) {
        viewModel.fetchMatchScore(jobId.toIntOrNull() ?: 0)
    }

    if (job == null) {
        Box(Modifier.fillMaxSize().background(PremiumBg), Alignment.Center) {
            CircularProgressIndicator(color = PremiumPrimary)
        }
        return
    }

    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize().background(PremiumBg)) {
        // Hero Background Image (Parallax Placeholder)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Brush.verticalGradient(listOf(Color(0xFF1E293B), PremiumBg)))
        ) {
            Box(modifier = Modifier.offset(x = (-50).dp, y = (-50).dp).size(300.dp).background(PremiumPrimary.copy(alpha = 0.2f), CircleShape).blur(120.dp))
        }

        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            // Spacer to push content down for parallax effect
            item { Spacer(modifier = Modifier.height(150.dp)) }
            
            // Job Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).offset(y = (-30).dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(PremiumSurfaceContainer.copy(alpha = 0.8f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Box(Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(PremiumSurface), Alignment.Center) {
                                Icon(Icons.Default.BusinessCenter, null, tint = PremiumTextMuted, modifier = Modifier.size(32.dp))
                            }
                            
                            val score = ((matchScoreState as? SeekerUiState.Success)?.data?.get("match_percentage") as? Number)?.toInt() ?: job.match_percentage.toInt()
                            if (score > 0) {
                                Box(Modifier.size(56.dp), Alignment.Center) {
                                    Canvas(Modifier.fillMaxSize()) {
                                        drawCircle(PremiumSurface, style = Stroke(4.dp.toPx()))
                                        drawArc(
                                            brush = Brush.sweepGradient(listOf(PremiumPrimary, PremiumSecondary)), 
                                            startAngle = -90f, sweepAngle = (score / 100f) * 360f, useCenter = false, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                    Text("$score%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PremiumText)
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        Text(job.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PremiumText)
                        Text(job.company, fontSize = 16.sp, color = PremiumTextMuted, modifier = Modifier.padding(top = 4.dp))
                        
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(color = PremiumSurface, shape = RoundedCornerShape(8.dp)) {
                                Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, null, tint = PremiumPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(job.location.ifEmpty { "Remote" }, fontSize = 12.sp, color = PremiumText)
                                }
                            }
                            Surface(color = PremiumSurface, shape = RoundedCornerShape(8.dp)) {
                                Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Work, null, tint = PremiumPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(job.job_type.uppercase(), fontSize = 12.sp, color = PremiumText)
                                }
                            }
                        }
                    }
                }
            }
            
            // Tab Navigation
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val tabs = listOf("AI Insights", "Details", "Company")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) PremiumPrimary.copy(alpha=0.15f) else Color.Transparent)
                                .clickable { selectedTab = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(title, color = if (isSelected) PremiumPrimary else PremiumTextMuted, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp)
                        }
                    }
                }
            }
            
            // Tab Content
            item {
                Box(Modifier.padding(horizontal = 24.dp)) {
                    when (selectedTab) {
                        0 -> AIInsightsTab(job, matchScoreState)
                        1 -> JobDetailsTab(job)
                        2 -> CompanyTab(job)
                    }
                }
            }
            
            item { Spacer(Modifier.height(120.dp)) }
        }

        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(48.dp).clip(CircleShape).background(PremiumSurfaceContainer.copy(alpha=0.8f))) {
                Icon(Icons.Default.ArrowBack, null, tint = PremiumText)
            }
            IconButton(onClick = { }, modifier = Modifier.size(48.dp).clip(CircleShape).background(PremiumSurfaceContainer.copy(alpha=0.8f))) {
                Icon(Icons.Default.BookmarkBorder, null, tint = PremiumText)
            }
        }

        // Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, PremiumBg, PremiumBg)))
                .padding(24.dp)
        ) {
            var applying by remember { mutableStateOf(false) }
            Button(
                onClick = { 
                    applying = true
                    viewModel.applyForJob(jobId) { appId -> 
                        applying = false
                        navController.navigate("chat/$appId") 
                    } 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumPrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                if (applying) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.AutoAwesome, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Apply with AI Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AIInsightsTab(job: DjangoJob, matchScoreState: SeekerUiState<Map<String, Any>>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Skill Gap Analysis from Match Score
        if (matchScoreState is SeekerUiState.Success) {
            val rep = matchScoreState.data
            val matched = rep["matched_skills"] as? List<*> ?: job.skillsList
            val missing = rep["missing_skills"] as? List<*> ?: emptyList<Any>()
            val rec = rep["recommendation"] as? String ?: ""
            
            if (matched.isNotEmpty()) {
                Text("Matched Skills", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PremiumText)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    matched.forEach { sk -> 
                        Surface(color = Color(0xFF10B981).copy(alpha=0.1f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha=0.2f))) { 
                            Text(text = sk.toString(), color = Color(0xFF10B981), fontSize = 13.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontWeight = FontWeight.Medium) 
                        } 
                    }
                }
            }
            
            if (missing.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Missing Skills", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PremiumText)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    missing.forEach { sk -> 
                        Surface(color = Color(0xFFEF4444).copy(alpha=0.1f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha=0.2f))) { 
                            Text(text = sk.toString(), color = Color(0xFFEF4444), fontSize = 13.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontWeight = FontWeight.Medium) 
                        } 
                    }
                }
            }

            if (rec.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(PremiumSurfaceContainer)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, null, tint = PremiumSecondary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("AI Recommendation", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PremiumSecondary)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(rec, fontSize = 14.sp, color = PremiumText, lineHeight = 20.sp)
                    }
                }
            }
        } else {
            // Fallback to just showing the job skills
            Text("Required Skills", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PremiumText)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                job.skillsList.forEach { skill -> 
                    Surface(color = PremiumSurfaceContainer, shape = RoundedCornerShape(8.dp)) { 
                        Text(skill, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = PremiumTextMuted) 
                    } 
                }
            }
        }
    }
}

@Composable
fun JobDetailsTab(job: DjangoJob) {
    Column {
        Text("About the Role", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PremiumText)
        Spacer(Modifier.height(12.dp))
        Text(job.description, fontSize = 14.sp, color = PremiumTextMuted, lineHeight = 22.sp)
    }
}

@Composable
fun CompanyTab(job: DjangoJob) {
    Column {
        Text("About ${job.company}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PremiumText)
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(PremiumSurfaceContainer)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Business, null, tint = PremiumTextMuted, modifier = Modifier.size(48.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(job.company, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PremiumText)
                    Text("Software & Technology", fontSize = 14.sp, color = PremiumTextMuted)
                }
            }
        }
    }
}

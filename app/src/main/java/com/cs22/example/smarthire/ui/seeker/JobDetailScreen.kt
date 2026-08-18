package com.cs22.example.smarthire.ui.seeker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.model.DjangoJob
import com.cs22.example.smarthire.ui.theme.*
import com.cs22.example.smarthire.viewmodel.SeekerUiState
import com.cs22.example.smarthire.viewmodel.SeekerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JobDetailScreen(viewModel: SeekerViewModel, navController: NavHostController, jobId: String) {
    val jobsState by viewModel.jobsState.collectAsState()
    val matchScoreState by viewModel.matchScoreState.collectAsState()
    
    val job = (jobsState as? SeekerUiState.Success)?.data?.find { it.id == jobId }
    
    LaunchedEffect(jobId) {
        viewModel.fetchMatchScore(jobId.toIntOrNull() ?: 0)
    }

    if (job == null) {
        Box(Modifier.fillMaxSize().background(StBackground), Alignment.Center) {
            CircularProgressIndicator(color = StPrimary)
        }
        return
    }

    val context = LocalContext.current
    var isBookmarked by remember { mutableStateOf(false) }
    var applyError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Details", color = StPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = StOnSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        val shareText = "Check out this job: ${job.title} at ${job.company}!\nLocation: ${job.location}\nSalary: ${job.salary_range}"
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Job"))
                    }) {
                        Icon(Icons.Default.Share, null, tint = StOnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StBackground)
            )
        },
        bottomBar = {
            Surface(
                color = StSurface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    applyError?.let { err ->
                        Text(
                            text = err,
                            color = StError,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = { isBookmarked = !isBookmarked },
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isBookmarked) StPrimary else StOutlineVariant),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, 
                                null, 
                                tint = if (isBookmarked) StPrimary else StOnSurface
                            )
                        }
                        var applying by remember { mutableStateOf(false) }
                        Button(
                            onClick = { 
                                applying = true
                                applyError = null
                                viewModel.applyForJob(jobId) { appId -> 
                                    applying = false
                                    if (appId != null) {
                                        navController.navigate("chat/$appId")
                                    } else {
                                        applyError = "Application submitted or already applied!"
                                    }
                                } 
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StPrimary)
                        ) {
                            if (applying) CircularProgressIndicator(color = StSurface, modifier = Modifier.size(24.dp))
                            else Text("Apply Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        },
        containerColor = StBackground
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            item { Spacer(Modifier.height(16.dp)) }
            
            // JOB HEADER CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(StSurface),
                    border = BorderStroke(1.dp, StOutlineVariant),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(StSurfaceContainer), Alignment.Center) {
                                Icon(Icons.Default.Business, null, tint = StTextSecondary, modifier = Modifier.size(32.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(job.title ?: "", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
                                Text(job.company ?: "", fontSize = 16.sp, color = StTextSecondary)
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(color = StSurface, shape = CircleShape, border = BorderStroke(1.dp, StOutlineVariant)) {
                                Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, null, tint = StTextSecondary, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(job.location?.ifEmpty { "Remote" } ?: "Remote", fontSize = 12.sp, color = StOnSurfaceVariant)
                                }
                            }
                            Surface(color = StSurface, shape = CircleShape, border = BorderStroke(1.dp, StOutlineVariant)) {
                                Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Work, null, tint = StTextSecondary, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(job.job_type?.uppercase() ?: "FULL-TIME", fontSize = 12.sp, color = StOnSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
            
            item { Spacer(Modifier.height(24.dp)) }
            
            // MATCH BANNER
            item {
                val score = ((matchScoreState as? SeekerUiState.Success)?.data?.get("match_percentage") as? Number)?.toInt() ?: job.match_percentage.toInt()
                if (score > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(StSurface),
                        border = BorderStroke(1.dp, StOutlineVariant),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                            Box(Modifier.width(4.dp).fillMaxHeight().background(StPrimary))
                            Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(56.dp), Alignment.Center) {
                                    Canvas(Modifier.fillMaxSize()) {
                                        drawCircle(StOutlineVariant, style = Stroke(4.dp.toPx()))
                                        drawArc(
                                            color = StPrimary,
                                            startAngle = -90f, sweepAngle = (score / 100f) * 360f, useCenter = false, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                    Text("$score%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StPrimary)
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(if (score >= 80) "Strong Match" else "Good Match", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = StOnSurface)
                                    Text("Based on your profile skills.", fontSize = 14.sp, color = StTextSecondary)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            // ABOUT SECTION
            item {
                Text("About the Job", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StOnSurface)
                Spacer(Modifier.height(12.dp))
                Text(job.description ?: "", fontSize = 14.sp, color = StOnSurfaceVariant, lineHeight = 22.sp)
                Spacer(Modifier.height(24.dp))
            }
            
            // RESPONSIBILITIES
            item {
                Text("Responsibilities", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StOnSurface)
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Design and build applications", "Collaborate with cross-functional teams", "Fix bugs and improve performance").forEach { req ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.CheckCircle, null, tint = StPrimary, modifier = Modifier.size(20.dp).padding(top = 2.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(req, fontSize = 14.sp, color = StOnSurfaceVariant, lineHeight = 20.sp)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
            
            // REQUIRED SKILLS
            item {
                Text("Required Skills", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StOnSurface)
                Spacer(Modifier.height(12.dp))
                
                val rep = (matchScoreState as? SeekerUiState.Success)?.data
                val matched = rep?.get("matched_skills") as? List<*> ?: job.skillsList ?: emptyList<Any>()
                val missing = rep?.get("missing_skills") as? List<*> ?: emptyList<Any>()
                
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    matched.forEach { sk ->
                        Surface(color = StMatchBadgeBg, shape = CircleShape) {
                            Text(sk.toString(), color = StPrimary, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontWeight = FontWeight.Medium)
                        }
                    }
                    missing.forEach { sk ->
                        Surface(color = StSurfaceContainer, shape = CircleShape) {
                            Text(sk.toString(), color = StOnSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

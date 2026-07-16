package com.cs22.example.smarthire.ui.seeker

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.model.*
import com.cs22.example.smarthire.ui.components.AnimatedBottomBar
import com.cs22.example.smarthire.ui.components.BottomNavigationItem
import com.cs22.example.smarthire.ui.theme.*
import com.cs22.example.smarthire.viewmodel.SeekerUiState
import com.cs22.example.smarthire.viewmodel.SeekerViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeekerDashboard(viewModel: SeekerViewModel, navController: NavHostController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val bottomNavItems = listOf(
        BottomNavigationItem(icon = Icons.Default.Home, label = "Home"),
        BottomNavigationItem(icon = Icons.Default.Work, label = "Jobs"),
        BottomNavigationItem(icon = Icons.Default.FileUpload, label = "Upload CV"),
        BottomNavigationItem(icon = Icons.Default.ChatBubble, label = "Applied"),
        BottomNavigationItem(icon = Icons.Default.Person, label = "Profile")
    )
    
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.uploadCv(it, "resume.pdf")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Box(Modifier.fillMaxWidth(), Alignment.Center) { Text("SmartHire", fontWeight = FontWeight.Bold, color = SmartHirePrimary, fontSize = 22.sp) } },
                navigationIcon = { IconButton(onClick = {}) { Icon(Icons.Default.Menu, "Menu", tint = SmartHireOnSurfaceVariant) } },
                actions = { IconButton(onClick = { navController.navigate("notifications") }) { Icon(Icons.Default.Notifications, "Notifications", tint = SmartHireOnSurfaceVariant) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SmartHireBackground.copy(alpha = 0.9f)),
                modifier = Modifier.shadow(1.dp)
            )
        },
        bottomBar = { 
            AnimatedBottomBar(
                items = bottomNavItems, 
                selectedTab = selectedTab, 
                onTabSelected = { 
                    if (it == 2) {
                        launcher.launch("application/pdf")
                    } else {
                        selectedTab = it 
                    }
                }, 
                activeColor = SmartHirePrimary, 
                backgroundColor = SmartHireBackground
            ) 
        },
        containerColor = SmartHireBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> HomeTab(viewModel, navController)
                1 -> JobsTab(viewModel, navController)
                3 -> AppliedTab(viewModel, navController)
                4 -> ProfileTab(viewModel, navController)
            }
        }
    }
}

// ══════════════════ HOME TAB ══════════════════
@Composable
fun HomeTab(viewModel: SeekerViewModel, navController: NavHostController) {
    val topMatchState by viewModel.topMatchState.collectAsState()
    val statsState by viewModel.statsState.collectAsState()
    val interviewsState by viewModel.interviewsState.collectAsState()
    val recommendedJobsState by viewModel.recommendedJobsState.collectAsState()
    
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Hero Match
        item {
            when (val s = topMatchState) {
                is SeekerUiState.Success -> HeroMatchCard(s.data)
                is SeekerUiState.Loading -> Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) { CircularProgressIndicator(color = SmartHirePrimary) }
                else -> Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(SmartHireSurfaceContainer)) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Upload your CV to get AI-matched jobs!", color = SmartHireOnSurfaceVariant)
                    }
                }
            }
        }
        
        // AI Recommended Jobs
        item {
            Text("AI Recommended Jobs", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
        }
        item {
            when (val s = recommendedJobsState) {
                is SeekerUiState.Success -> {
                    if (s.data.isEmpty()) {
                        Text("No job recommendations yet. Upload your CV to receive tailored matches.", color = SmartHireOnSurfaceVariant, fontSize = 14.sp)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(s.data) { job ->
                                RecommendedJobCard(job)
                            }
                        }
                    }
                }
                is SeekerUiState.Loading -> Box(Modifier.fillMaxWidth().height(120.dp), Alignment.Center) { CircularProgressIndicator(color = SmartHirePrimary) }
                else -> Text("Upload your CV to unlock AI job matches", color = SmartHireOnSurfaceVariant, fontSize = 14.sp)
            }
        }

        // Stats
        item {
            Text("Quick Stats", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
        }
        item {
            val stats = (statsState as? SeekerUiState.Success)?.data
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Modifier.weight(1f), Icons.Default.Send, stats?.apps_sent?.toString() ?: "0", "Apps Sent")
                StatCard(Modifier.weight(1f), Icons.Default.Visibility, stats?.profile_views?.toString() ?: "0", "Profile Views")
            }
        }
        item {
            val stats = (statsState as? SeekerUiState.Success)?.data
            ProfileStrengthCard(stats?.profile_completeness ?: 0)
        }
        // Interviews
        item { Text("Upcoming Interviews", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface) }
        item {
            when (val s = interviewsState) {
                is SeekerUiState.Success -> {
                    if (s.data.isEmpty()) Text("No upcoming interviews", color = SmartHireOnSurfaceVariant, fontSize = 14.sp)
                    else LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(s.data) { iv -> 
                            InterviewCard(
                                title = iv.job_title ?: "Interview", 
                                company = iv.company ?: "", 
                                time = iv.scheduled_at,
                                onCancel = { iv.id.toIntOrNull()?.let { viewModel.cancelInterview(it) } },
                                onComplete = { iv.id.toIntOrNull()?.let { viewModel.completeInterview(it) } }
                            ) 
                        }
                    }
                }
                else -> Text("No upcoming interviews", color = SmartHireOnSurfaceVariant, fontSize = 14.sp)
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
fun RecommendedJobCard(job: DjangoJob) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(150.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(SmartHireSurface),
        border = BorderStroke(1.dp, SmartHireOutline)
    ) {
        Column(Modifier.padding(16.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        job.title, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 14.sp, 
                        color = SmartHireOnSurface, 
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(color = SmartHirePrimary.copy(0.1f), shape = RoundedCornerShape(6.dp)) {
                        Text(
                            "${job.match_percentage.toInt()}% Match", 
                            color = SmartHirePrimary, 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold, 
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(job.company, fontSize = 12.sp, color = SmartHireOnSurfaceVariant, maxLines = 1)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                job.skillsList.take(2).forEach { skill -> SkillBadge(skill) }
            }
        }
    }
}

@Composable fun HeroMatchCard(job: DjangoJob) {
    val matchPct = (job.match_percentage.toFloat() / 100f).coerceIn(0f, 1f)
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(SmartHireSurface), border = BorderStroke(1.dp, SmartHireOutline), elevation = CardDefaults.cardElevation(4.dp)) {
        Box {
            Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(SmartHirePrimary.copy(0.05f), Color.Transparent))))
            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(80.dp), Alignment.Center) {
                        Canvas(Modifier.fillMaxSize()) {
                            drawCircle(Color(0xFFF1F5F9), style = Stroke(6.dp.toPx()))
                            drawArc(color = SmartHirePrimary, startAngle = -90f, sweepAngle = matchPct * 360f, useCenter = false, style = Stroke(6.dp.toPx(), cap = StrokeCap.Round))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${job.match_percentage.toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SmartHirePrimary)
                            Text("MATCH", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SmartHirePrimary)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Surface(color = SmartHirePrimary.copy(0.1f), shape = CircleShape, border = BorderStroke(1.dp, SmartHirePrimary.copy(0.2f))) {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, null, Modifier.size(12.dp), SmartHirePrimary)
                                Spacer(Modifier.width(4.dp)); Text("Match of the Day", fontSize = 10.sp, color = SmartHirePrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(job.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
                        Text("${job.company} • ${job.location.ifEmpty { job.job_type }}", fontSize = 12.sp, color = SmartHireOnSurfaceVariant)
                    }
                }
                if (job.skillsList.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        job.skillsList.take(3).forEach { skill -> SkillBadge(skill) }
                        if (job.salary_range.isNotEmpty()) SkillBadge(job.salary_range)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = {}, Modifier.width(140.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(SmartHirePrimary)) { Text("Review Match", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            }
        }
    }
}

@Composable fun SkillBadge(label: String) { Surface(color = SmartHireSurfaceContainer, shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, SmartHireOutline)) { Text(label, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = SmartHireOnSurface) } }
@Composable fun StatCard(modifier: Modifier, icon: ImageVector, value: String, label: String) { Card(modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(SmartHireSurface), border = BorderStroke(1.dp, SmartHireOutline)) { Column(Modifier.padding(16.dp)) { Icon(icon, null, tint = SmartHirePrimary, modifier = Modifier.size(24.dp)); Spacer(Modifier.height(8.dp)); Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface); Text(label, fontSize = 12.sp, color = SmartHireOnSurfaceVariant) } } }
@Composable fun ProfileStrengthCard(pct: Int) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(SmartHireSurface), border = BorderStroke(1.dp, SmartHireOutline)) { Column(Modifier.padding(16.dp)) { Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("Profile Strength", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SmartHirePrimary); Text("$pct%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SmartHirePrimary) }; Spacer(Modifier.height(8.dp)); LinearProgressIndicator(progress = { pct / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = SmartHirePrimary, trackColor = SmartHireSurfaceContainer) } } }

@Composable fun InterviewCard(title: String, company: String, time: String, onCancel: () -> Unit, onComplete: () -> Unit) { 
    Card(Modifier.width(260.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(SmartHireSurface), border = BorderStroke(1.dp, SmartHireOutline)) { 
        Column(Modifier.padding(16.dp)) { 
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SmartHireOnSurface); 
            Text(company, fontSize = 12.sp, color = SmartHireOnSurfaceVariant); 
            Spacer(Modifier.height(8.dp)); 
            Surface(color = SmartHireSurfaceContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) { 
                Text(text = time.take(16), modifier = Modifier.padding(8.dp), textAlign = TextAlign.Center, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SmartHirePrimary) 
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCancel, Modifier.weight(1f)) {
                    Text("Cancel", color = Color.Red, fontSize = 12.sp)
                }
                Button(onClick = onComplete, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(SmartHirePrimary), shape = RoundedCornerShape(8.dp)) {
                    Text("Done", fontSize = 12.sp)
                }
            }
        } 
    } 
}

// ══════════════════ JOBS TAB ══════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobsTab(viewModel: SeekerViewModel, navController: NavHostController) {
    val jobsState by viewModel.jobsState.collectAsState()
    var searchSkills by remember { mutableStateOf("") }
    val coverLetterState by viewModel.coverLetterState.collectAsState()

    if (coverLetterState is SeekerUiState.Success) {
        AlertDialog(onDismissRequest = { viewModel.resetCoverLetterState() }, title = { Text("AI Cover Letter") },
            text = { Column(Modifier.verticalScroll(rememberScrollState())) { Text((coverLetterState as SeekerUiState.Success<String>).data) } },
            confirmButton = { TextButton(onClick = { viewModel.resetCoverLetterState() }) { Text("Close", color = SmartHirePrimary) } })
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Search Jobs", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = searchSkills, onValueChange = { searchSkills = it }, placeholder = { Text("Skills (e.g. python, react)") },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = SmartHireOutline, focusedBorderColor = SmartHirePrimary, unfocusedContainerColor = SmartHireSurfaceContainer, focusedContainerColor = Color.White))
        Spacer(Modifier.height(8.dp))
        Button(onClick = { viewModel.searchJobs(searchSkills, "", "", "") }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(SmartHirePrimary)) { Text("Search") }
        Spacer(Modifier.height(16.dp))

        when (val s = jobsState) {
            is SeekerUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = SmartHirePrimary) }
            is SeekerUiState.Error -> Text(s.message, color = Color.Red)
            is SeekerUiState.Success -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(s.data) { idx, job ->
                    var expanded by remember { mutableStateOf(false) }
                    Card(Modifier.fillMaxWidth().animateContentSize().clickable { expanded = !expanded }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(SmartHireSurface), border = BorderStroke(1.dp, SmartHireOutline)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text(job.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface, modifier = Modifier.weight(1f))
                                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, "Expand", tint = SmartHirePrimary)
                            }
                            Text("${job.company} • ${job.job_type.uppercase()}", color = SmartHirePrimary, fontSize = 13.sp)
                            if (job.match_percentage > 0) Text("AI Match: ${job.match_percentage.toInt()}%", color = SmartHireSuccess, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                            Spacer(Modifier.height(8.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                job.skillsList.take(5).forEach { skill -> Surface(color = SmartHirePrimary.copy(0.1f), shape = RoundedCornerShape(6.dp), modifier = Modifier.padding(bottom = 4.dp)) { Text(text = skill, color = SmartHirePrimary, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) } }
                            }
                            AnimatedVisibility(expanded) {
                                Column(Modifier.padding(top = 12.dp)) {
                                    if (job.description.isNotEmpty()) Text(job.description.take(200), color = SmartHireOnSurfaceVariant, fontSize = 13.sp)
                                    Spacer(Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        var applying by remember { mutableStateOf(false) }
                                        Button(onClick = { applying = true; job.id?.let { viewModel.applyForJob(it) { appId -> applying = false; navController.navigate("chat/$appId") } } }, Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(SmartHirePrimary)) {
                                            if (applying) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White) else Text("Apply & Chat", fontSize = 12.sp)
                                        }
                                        OutlinedButton(onClick = { job.id?.let { viewModel.generateCoverLetter(it) } }, Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = SmartHirePrimary)) { Text("AI Cover Letter", fontSize = 11.sp) }
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
            else -> {}
        }
    }
}

// ══════════════════ APPLIED TAB ══════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppliedTab(viewModel: SeekerViewModel, navController: NavHostController) {
    val state by viewModel.applicationsState.collectAsState()
    val skillGapState by viewModel.skillGapState.collectAsState()
    
    LaunchedEffect(Unit) { viewModel.getApplications() }
    
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Your Applications", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
        Spacer(Modifier.height(16.dp))
        when (val s = state) {
            is SeekerUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = SmartHirePrimary) }
            is SeekerUiState.Error -> Text(s.message, color = Color.Red)
            is SeekerUiState.Success -> {
                if (s.data.isEmpty()) Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No applications yet. Start searching!", color = SmartHireOnSurfaceVariant) }
                else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(s.data) { app ->
                        var expanded by remember { mutableStateOf(false) }
                        
                        Card(
                            Modifier.fillMaxWidth().animateContentSize().clickable { 
                                expanded = !expanded 
                                if (expanded) {
                                    app.id.toIntOrNull()?.let { viewModel.fetchSkillGap(it) }
                                }
                            }, 
                            shape = RoundedCornerShape(16.dp), 
                            colors = CardDefaults.cardColors(SmartHireSurface), 
                            border = BorderStroke(1.dp, SmartHireOutline)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                    Text(app.job_details?.title ?: "Job #${app.job}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SmartHireOnSurface, modifier = Modifier.weight(1f))
                                    val statusColor = when (app.effectiveStatus) { "rejected" -> Color(0xFFEF4444); "hired" -> SmartHireSuccess; "interview" -> SmartHirePrimary; else -> SmartHireOnSurfaceVariant }
                                    Surface(color = statusColor.copy(0.15f), shape = RoundedCornerShape(8.dp)) { Text(text = app.effectiveStatus.uppercase(), color = statusColor, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                                }
                                Text(app.job_details?.company ?: "", color = SmartHireOnSurfaceVariant, fontSize = 13.sp)
                                if (app.effectiveMatchScore > 0) {
                                    Text(text = "AI Match Score: ${app.effectiveMatchScore.toInt()}%", color = SmartHirePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                                }
                                
                                AnimatedVisibility(expanded) {
                                    Column(Modifier.padding(top = 16.dp)) {
                                        HorizontalDivider(color = SmartHireOutline, thickness = 1.dp)
                                        Spacer(Modifier.height(12.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AutoAwesome, "AI", tint = SmartHirePrimary, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("AI Skill Gap Analysis & Advice", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SmartHirePrimary)
                                        }
                                        Spacer(Modifier.height(10.dp))
                                        
                                        when (val g = skillGapState) {
                                            is SeekerUiState.Loading -> Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) { CircularProgressIndicator(color = SmartHirePrimary) }
                                            is SeekerUiState.Success -> {
                                                val rep = g.data
                                                val matched = rep["matched_skills"] as? List<*> ?: emptyList<Any>()
                                                val missing = rep["missing_skills"] as? List<*> ?: emptyList<Any>()
                                                val rec = rep["recommendation"] as? String ?: ""
                                                
                                                if (matched.isNotEmpty()) {
                                                    Text("Matched Skills", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SmartHireOnSurface)
                                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                                                        matched.forEach { sk -> 
                                                            Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(6.dp), modifier = Modifier.padding(bottom = 4.dp)) { 
                                                                Text(text = sk.toString(), color = Color(0xFF15803D), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Medium) 
                                                            } 
                                                        }
                                                    }
                                                    Spacer(Modifier.height(8.dp))
                                                }
                                                
                                                if (missing.isNotEmpty()) {
                                                    Text("Missing Skills", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SmartHireOnSurface)
                                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                                                        missing.forEach { sk -> 
                                                            Surface(color = Color(0xFFFEE2E2), shape = RoundedCornerShape(6.dp), modifier = Modifier.padding(bottom = 4.dp)) { 
                                                                Text(text = sk.toString(), color = Color(0xFFB91C1C), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Medium) 
                                                            } 
                                                        }
                                                    }
                                                    Spacer(Modifier.height(8.dp))
                                                }
                                                
                                                if (rec.isNotEmpty()) {
                                                    Surface(color = SmartHireSurfaceContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                                                        Text(rec, fontSize = 12.sp, color = SmartHireOnSurface, modifier = Modifier.padding(10.dp), lineHeight = 16.sp)
                                                    }
                                                    Spacer(Modifier.height(12.dp))
                                                }
                                            }
                                            else -> {
                                                // Fallback list from app object
                                                if (app.skill_gap_analysis.isNotEmpty()) {
                                                    Text("Missing Skills", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SmartHireOnSurface)
                                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                                                        app.skill_gap_analysis.forEach { sk ->
                                                            Surface(color = Color(0xFFFEE2E2), shape = RoundedCornerShape(6.dp), modifier = Modifier.padding(bottom = 4.dp)) {
                                                                Text(text = sk, color = Color(0xFFB91C1C), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                            }
                                                        }
                                                    }
                                                    Spacer(Modifier.height(12.dp))
                                                }
                                            }
                                        }
                                        
                                        OutlinedButton(onClick = { navController.navigate("chat/${app.id}") }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = SmartHirePrimary)) { 
                                            Icon(Icons.Default.ChatBubble, null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Open Chat") 
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
            else -> {}
        }
    }
}

// ══════════════════ PROFILE TAB ══════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileTab(viewModel: SeekerViewModel, navController: NavHostController) {
    val profileState by viewModel.profileState.collectAsState()
    val cvState by viewModel.cvSyncState.collectAsState()
    
    LaunchedEffect(Unit) { viewModel.getProfile() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Your Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
        Spacer(Modifier.height(16.dp))
        if (cvState is SeekerUiState.Loading) { 
            Text("Uploading and parsing CV with AI...", color = SmartHirePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(Modifier.fillMaxWidth(), color = SmartHirePrimary) 
        } else if (cvState is SeekerUiState.Success) {
            Text("CV uploaded and parsed successfully!", color = SmartHireSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(16.dp))
        when (val s = profileState) {
            is SeekerUiState.Success -> {
                val p = s.data.profile
                if (p != null) {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(SmartHireSurface), border = BorderStroke(1.dp, SmartHireOutline)) {
                        Column(Modifier.padding(20.dp)) {
                            Text(s.data.full_name ?: s.data.username ?: "User", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
                            if (!p.bio.isNullOrEmpty()) Text(p.bio, color = SmartHireOnSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                            Spacer(Modifier.height(12.dp))
                            Row { Text("Degree: ", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SmartHireOnSurface); Text(p.degree_extracted ?: p.degree ?: "N/A", fontSize = 13.sp, color = SmartHireOnSurfaceVariant) }
                            Row { Text("Experience: ", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SmartHireOnSurface); Text("${p.total_experience} years", fontSize = 13.sp, color = SmartHireOnSurfaceVariant) }
                            Row { Text("Location: ", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SmartHireOnSurface); Text(p.location ?: "N/A", fontSize = 13.sp, color = SmartHireOnSurfaceVariant) }
                            if (!p.extracted_skills_json.isNullOrEmpty()) {
                                Spacer(Modifier.height(12.dp)); Text("Skills", fontWeight = FontWeight.Bold, color = SmartHirePrimary, fontSize = 12.sp)
                                Spacer(Modifier.height(4.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    p.extracted_skills_json.forEach { sk -> Surface(color = SmartHirePrimary.copy(0.1f), shape = RoundedCornerShape(6.dp), modifier = Modifier.padding(bottom = 4.dp)) { Text(text = sk, color = SmartHirePrimary, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) } }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            ProfileStrengthCard(p.profile_completeness)
                        }
                    }
                }
            }
            is SeekerUiState.Loading -> Box(Modifier.fillMaxWidth().height(100.dp), Alignment.Center) { CircularProgressIndicator(color = SmartHirePrimary) }
            else -> {}
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = { viewModel.logout { navController.navigate("auth") { popUpTo(0) { inclusive = true } } } }, Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(Color(0xFFEF4444)), shape = RoundedCornerShape(16.dp)) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.White); Spacer(Modifier.width(8.dp)); Text(text = "Log Out", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(100.dp))
    }
}

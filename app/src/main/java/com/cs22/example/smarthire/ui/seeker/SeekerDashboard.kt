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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
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
import com.cs22.example.smarthire.viewmodel.SeekerUiState
import com.cs22.example.smarthire.viewmodel.SeekerViewModel

// --- Colors based on HTML Palette ---
val PremiumBg = Color(0xFF0F131D)
val PremiumSurface = Color(0xFF161B28)
val PremiumSurfaceContainer = Color(0xFF1D2433)
val PremiumPrimary = Color(0xFF3B82F6)
val PremiumSecondary = Color(0xFF8B5CF6)
val PremiumText = Color(0xFFE1E2E4)
val PremiumTextMuted = Color(0xFFC2C6D6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeekerDashboard(viewModel: SeekerViewModel, navController: NavHostController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val bottomNavItems = listOf(
        BottomNavigationItem(icon = Icons.Default.Home, label = "Home"),
        BottomNavigationItem(icon = Icons.Default.Search, label = "Jobs"),
        BottomNavigationItem(icon = Icons.Default.CloudUpload, label = "Upload CV"),
        BottomNavigationItem(icon = Icons.Default.FactCheck, label = "Applied"),
        BottomNavigationItem(icon = Icons.Default.Person, label = "Profile")
    )
    
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadCv(it, ctx) }
    }

    Box(modifier = Modifier.fillMaxSize().background(PremiumBg)) {
        // Atmospheric effects
        Box(modifier = Modifier.offset(x = (-100).dp, y = (-100).dp).size(400.dp).background(PremiumPrimary.copy(alpha = 0.05f), CircleShape).blur(120.dp))
        Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 100.dp, y = 100.dp).size(400.dp).background(PremiumSecondary.copy(alpha = 0.05f), CircleShape).blur(120.dp))

        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(PremiumSurfaceContainer).border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = PremiumTextMuted)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Hello,", fontSize = 14.sp, color = PremiumTextMuted)
                            Text("Seeker", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PremiumText)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(onClick = { navController.navigate("notifications") }, modifier = Modifier.size(48.dp).clip(CircleShape).background(PremiumSurfaceContainer).border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)) {
                            Icon(Icons.Default.Notifications, null, tint = PremiumText)
                        }
                        IconButton(onClick = { navController.navigate("settings") }, modifier = Modifier.size(48.dp).clip(CircleShape).background(PremiumSurfaceContainer).border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)) {
                            Icon(Icons.Default.Settings, null, tint = PremiumText)
                        }
                    }
                }
            },
            bottomBar = { 
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp, start = 24.dp, end = 24.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)).background(PremiumSurfaceContainer.copy(alpha = 0.8f)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp)).padding(4.dp)) {
                        AnimatedBottomBar(
                            items = bottomNavItems, 
                            selectedTab = selectedTab, 
                            onTabSelected = { 
                                if (it == 2) launcher.launch("application/pdf")
                                else selectedTab = it 
                            }, 
                            activeColor = PremiumPrimary, 
                            backgroundColor = Color.Transparent
                        )
                    }
                }
            },
            containerColor = Color.Transparent
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
}

// ══════════════════ HOME TAB ══════════════════
@Composable
fun HomeTab(viewModel: SeekerViewModel, navController: NavHostController) {
    val topMatchState by viewModel.topMatchState.collectAsState()
    val statsState by viewModel.statsState.collectAsState()
    val recommendedJobsState by viewModel.recommendedJobsState.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), 
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // AI Match of the Day
        item {
            when (val s = topMatchState) {
                is SeekerUiState.Success -> HeroMatchCard(s.data, navController)
                is SeekerUiState.Loading -> Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) { CircularProgressIndicator(color = PremiumPrimary) }
                else -> {
                    // Empty state
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(PremiumSurface)) {
                        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(PremiumSurface, PremiumSurfaceContainer)))) {
                            Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AutoAwesome, null, tint = PremiumPrimary, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(16.dp))
                                Text("Unlock AI Matchmaking", color = PremiumText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("Upload your CV to let Gemini AI find your perfect job matches.", color = PremiumTextMuted, textAlign = TextAlign.Center, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        // Quick Stats
        item {
            Text("Quick Stats", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PremiumText)
        }
        item {
            val stats = (statsState as? SeekerUiState.Success)?.data
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(Modifier.weight(1f), Icons.Default.Visibility, stats?.profile_views?.toString() ?: "0", "Profile Views")
                StatCard(Modifier.weight(1f), Icons.Default.Send, stats?.apps_sent?.toString() ?: "0", "Apps Sent")
                StatCard(Modifier.weight(1f), Icons.Default.Event, "0", "Interviews")
            }
        }

        // Recommended Jobs
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Recommended for You", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PremiumText)
                Text("See All", fontSize = 14.sp, color = PremiumPrimary)
            }
        }
        item {
            when (val s = recommendedJobsState) {
                is SeekerUiState.Success -> {
                    if (s.data.isEmpty()) {
                        Text("No job recommendations yet. Upload your CV to receive tailored matches.", color = PremiumTextMuted, fontSize = 14.sp)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(s.data) { job -> RecommendedJobCard(job, navController) }
                        }
                    }
                }
                is SeekerUiState.Loading -> Box(Modifier.fillMaxWidth().height(120.dp), Alignment.Center) { CircularProgressIndicator(color = PremiumPrimary) }
                else -> Text("Upload your CV to unlock AI job matches", color = PremiumTextMuted, fontSize = 14.sp)
            }
        }
        item { Spacer(Modifier.height(100.dp)) } // Bottom nav padding
    }
}

@Composable
fun HeroMatchCard(job: DjangoJob, navController: NavHostController) {
    val matchPct = (job.match_percentage.toFloat() / 100f).coerceIn(0f, 1f)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF1D1B4B)) // Deep indigo
            .border(1.dp, PremiumSecondary.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
    ) {
        // Gradient glow behind card
        Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(PremiumSecondary.copy(alpha = 0.3f), Color.Transparent))))

        Column(Modifier.padding(24.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = PremiumSecondary.copy(0.2f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, PremiumSecondary.copy(0.5f))) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, Modifier.size(16.dp), PremiumSecondary)
                        Spacer(Modifier.width(6.dp))
                        Text("AI Match of the Day", fontSize = 12.sp, color = PremiumSecondary, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = {}) { Icon(Icons.Default.BookmarkBorder, null, tint = PremiumText) }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Match Progress Circle
                Box(Modifier.size(80.dp), Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(PremiumSurfaceContainer, style = Stroke(6.dp.toPx()))
                        drawArc(
                            brush = Brush.sweepGradient(listOf(PremiumPrimary, PremiumSecondary, PremiumPrimary)), 
                            startAngle = -90f, sweepAngle = matchPct * 360f, useCenter = false, style = Stroke(8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${job.match_percentage.toInt()}%", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
                Spacer(Modifier.width(20.dp))
                Column(Modifier.weight(1f)) {
                    Text(job.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(4.dp))
                    Text(job.company, fontSize = 16.sp, color = PremiumTextMuted)
                    Spacer(Modifier.height(8.dp))
                    Text(job.location.ifEmpty { job.job_type.uppercase() }, fontSize = 14.sp, color = PremiumTextMuted, fontWeight = FontWeight.Medium)
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = { navController.navigate("job_detail/${job.id}") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumPrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text("Review Match Details", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, null)
            }
        }
    }
}

@Composable
fun RecommendedJobCard(job: DjangoJob, navController: NavHostController) {
    Card(
        modifier = Modifier.width(280.dp).clickable { navController.navigate("job_detail/${job.id}") },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(PremiumSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(Modifier.padding(20.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(PremiumSurfaceContainer), Alignment.Center) {
                        Icon(Icons.Default.Business, null, tint = PremiumTextMuted) // Company Logo
                    }
                    Surface(color = PremiumPrimary.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text("${job.match_percentage.toInt()}% Match", color = PremiumPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(job.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PremiumText, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(job.company, fontSize = 14.sp, color = PremiumTextMuted, maxLines = 1)
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                job.skillsList.take(2).forEach { skill -> SkillBadge(skill) }
                if(job.skillsList.size > 2) {
                    SkillBadge("+${job.skillsList.size - 2}")
                }
            }
        }
    }
}

@Composable fun SkillBadge(label: String) { 
    Surface(color = PremiumSurfaceContainer, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))) { 
        Text(label, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = PremiumTextMuted) 
    } 
}

@Composable fun StatCard(modifier: Modifier, icon: ImageVector, value: String, label: String) { 
    Card(modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(PremiumSurface), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))) { 
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) { 
            Icon(icon, null, tint = PremiumPrimary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(12.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PremiumText)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 12.sp, color = PremiumTextMuted) 
        } 
    } 
}

// ══════════════════ JOBS TAB ══════════════════
// Re-using the Jobs tab logic, applying dark theme colors
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobsTab(viewModel: SeekerViewModel, navController: NavHostController) {
    val jobsState by viewModel.jobsState.collectAsState()
    var searchSkills by remember { mutableStateOf("") }
    val coverLetterState by viewModel.coverLetterState.collectAsState()

    if (coverLetterState is SeekerUiState.Success) {
        AlertDialog(
            onDismissRequest = { viewModel.resetCoverLetterState() }, 
            title = { Text("AI Cover Letter", color = PremiumText) },
            text = { Column(Modifier.verticalScroll(rememberScrollState())) { Text((coverLetterState as SeekerUiState.Success<String>).data, color = PremiumTextMuted) } },
            confirmButton = { TextButton(onClick = { viewModel.resetCoverLetterState() }) { Text("Close", color = PremiumPrimary) } },
            containerColor = PremiumSurface
        )
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 16.dp)) {
        Text("Search Jobs", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PremiumText)
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = searchSkills, onValueChange = { searchSkills = it }, 
            placeholder = { Text("Skills (e.g. python, react)", color = PremiumTextMuted.copy(alpha=0.5f)) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent, focusedBorderColor = PremiumPrimary, 
                unfocusedContainerColor = PremiumSurface, focusedContainerColor = PremiumSurface,
                unfocusedTextColor = PremiumText, focusedTextColor = PremiumText
            ),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = PremiumTextMuted) }
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { viewModel.searchJobs(searchSkills, "", "", "") }, 
            Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp), 
            colors = ButtonDefaults.buttonColors(PremiumPrimary)
        ) { Text("Search", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        
        Spacer(Modifier.height(24.dp))

        when (val s = jobsState) {
            is SeekerUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = PremiumPrimary) }
            is SeekerUiState.Error -> Text(s.message, color = Color.Red)
            is SeekerUiState.Success -> LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                itemsIndexed(s.data) { idx, job ->
                    var expanded by remember { mutableStateOf(false) }
                    Card(
                        Modifier.fillMaxWidth().animateContentSize().clickable { 
                            expanded = !expanded 
                            if (expanded) job.id?.toIntOrNull()?.let { viewModel.fetchMatchScore(it) }
                        }, 
                        shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(PremiumSurface), border = BorderStroke(1.dp, Color.White.copy(alpha=0.05f))
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text(job.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PremiumText, modifier = Modifier.weight(1f))
                                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, "Expand", tint = PremiumTextMuted)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("${job.company} • ${job.job_type.uppercase()}", color = PremiumPrimary, fontSize = 14.sp)
                            
                            Spacer(Modifier.height(12.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                job.skillsList.take(5).forEach { skill -> SkillBadge(skill) }
                            }
                            
                            AnimatedVisibility(expanded) {
                                Column(Modifier.padding(top = 16.dp)) {
                                    val matchScoreState by viewModel.matchScoreState.collectAsState()
                                    if (matchScoreState is SeekerUiState.Success) {
                                        val matchMap = (matchScoreState as SeekerUiState.Success<Map<String, Any>>).data
                                        val score = (matchMap["match_percentage"] as? Number)?.toInt() ?: 0
                                        if (score > 0) {
                                            Surface(color = Color(0xFF10B981).copy(0.1f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.AutoAwesome, "AI", tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                                    Spacer(Modifier.width(12.dp))
                                                    Text("Live AI Match Preview: $score%", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                }
                                            }
                                        }
                                    } else if (matchScoreState is SeekerUiState.Loading) {
                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), color = PremiumPrimary)
                                    }
                                    
                                    if (job.description.isNotEmpty()) Text(job.description.take(200) + "...", color = PremiumTextMuted, fontSize = 14.sp, lineHeight = 20.sp)
                                    Spacer(Modifier.height(20.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        var applying by remember { mutableStateOf(false) }
                                        Button(
                                            onClick = { applying = true; job.id?.let { viewModel.applyForJob(it) { appId -> applying = false; navController.navigate("chat/$appId") } } }, 
                                            Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(PremiumPrimary)
                                        ) {
                                            if (applying) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White) else Text("Apply & Chat", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = { job.id?.let { navController.navigate("job_detail/$it") } }, 
                                            Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = PremiumText), border = BorderStroke(1.dp, Color.White.copy(alpha=0.1f))
                                        ) { Text("View Details", fontSize = 14.sp) }
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
            else -> {}
        }
    }
}

// ══════════════════ APPLIED TAB ══════════════════
@Composable
fun AppliedTab(viewModel: SeekerViewModel, navController: NavHostController) {
    AppliedTrackingScreen(viewModel, navController)
}

// ══════════════════ PROFILE TAB ══════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileTab(viewModel: SeekerViewModel, navController: NavHostController) {
    val profileState by viewModel.profileState.collectAsState()
    LaunchedEffect(Unit) { viewModel.getProfile() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(top = 16.dp)) {
        Text("Your Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PremiumText)
        Spacer(Modifier.height(24.dp))
        
        when (val s = profileState) {
            is SeekerUiState.Success -> {
                val p = s.data.profile
                if (p != null) {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(PremiumSurface), border = BorderStroke(1.dp, Color.White.copy(alpha=0.05f))) {
                        Column(Modifier.padding(24.dp)) {
                            Box(Modifier.size(80.dp).clip(CircleShape).background(PremiumSurfaceContainer), Alignment.Center) { Icon(Icons.Default.Person, null, tint = PremiumTextMuted, modifier = Modifier.size(40.dp)) }
                            Spacer(Modifier.height(16.dp))
                            Text(s.data.full_name ?: s.data.username ?: "User", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PremiumText)
                            if (!p.bio.isNullOrEmpty()) Text(p.bio, color = PremiumTextMuted, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                            Spacer(Modifier.height(24.dp))
                            
                            // Info Grid
                            Row(Modifier.fillMaxWidth()) {
                                Column(Modifier.weight(1f)) {
                                    Text("Experience", color = PremiumTextMuted, fontSize = 12.sp)
                                    Text("${p.total_experience} years", color = PremiumText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                }
                                Column(Modifier.weight(1f)) {
                                    Text("Location", color = PremiumTextMuted, fontSize = 12.sp)
                                    Text(p.location ?: "N/A", color = PremiumText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            
                            if (!p.extracted_skills_json.isNullOrEmpty()) {
                                Spacer(Modifier.height(24.dp))
                                Text("Skills", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PremiumText)
                                Spacer(Modifier.height(12.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    p.extracted_skills_json.forEach { skill -> SkillBadge(skill) }
                                }
                            }
                        }
                    }
                }
            }
            is SeekerUiState.Loading -> Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) { CircularProgressIndicator(color = PremiumPrimary) }
            else -> {}
        }
        
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { viewModel.logout { navController.navigate("auth") { popUpTo(0) { inclusive = true } } } }, 
            modifier = Modifier.fillMaxWidth().height(56.dp), 
            colors = ButtonDefaults.buttonColors(PremiumSurface), 
            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color(0xFFEF4444))
            Spacer(Modifier.width(12.dp))
            Text(text = "Log Out", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(100.dp))
    }
}

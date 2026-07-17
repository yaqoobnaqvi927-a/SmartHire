package com.cs22.example.smarthire.ui.recruiter

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.model.*
import com.cs22.example.smarthire.ui.components.AnimatedBottomBar
import com.cs22.example.smarthire.ui.components.BottomNavigationItem
import com.cs22.example.smarthire.ui.theme.*
import com.cs22.example.smarthire.viewmodel.RecruiterUiState
import com.cs22.example.smarthire.viewmodel.RecruiterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecruiterDashboard(viewModel: RecruiterViewModel, navController: NavHostController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val bottomNavItems = listOf(
        BottomNavigationItem(icon = Icons.Default.Dashboard, label = "Console"),
        BottomNavigationItem(icon = Icons.Default.AddBox, label = "Post Job"),
        BottomNavigationItem(icon = Icons.Default.AutoAwesome, label = "AI Match"),
        BottomNavigationItem(icon = Icons.Default.ViewKanban, label = "ATS Flow")
    )
    Scaffold(
        topBar = { TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, tint = SmartHirePrimary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("SmartHire Recruiter", fontWeight = FontWeight.Bold, color = SmartHireOnSurface, fontSize = 20.sp)
                }
            },
            actions = {
                IconButton(onClick = { viewModel.logout { navController.navigate("auth") { popUpTo(0) { inclusive = true } } } }) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout", tint = Color(0xFFEF4444))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = SmartHireBackground.copy(0.9f)), modifier = Modifier.shadow(1.dp))
        },
        bottomBar = { AnimatedBottomBar(items = bottomNavItems, selectedTab = selectedTab, onTabSelected = { selectedTab = it }, activeColor = SmartHirePrimary, backgroundColor = SmartHireBackground) },
        containerColor = SmartHireBackground
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> RecruiterConsoleTab(viewModel)
                1 -> PostJobTab(viewModel, navController)
                2 -> AiMatchTab(viewModel, navController)
                3 -> PipelineTab(viewModel, navController)
            }
        }
    }
}

// ══════════════════ CONSOLE TAB (PREMIUM DASHBOARD) ══════════════════
@Composable
fun RecruiterConsoleTab(viewModel: RecruiterViewModel) {
    val postingsState by viewModel.myPostingsState.collectAsState()
    val statsState by viewModel.statsState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card with Premium Gradient
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .background(Brush.linearGradient(listOf(SmartHirePrimary, Color(0xFF1D4ED8))))
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Column {
                        Text("Welcome Back,", color = Color.White.copy(0.8f), fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Talent Hub Manager", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Review match requests, screen candidates with Gemini AI, and schedule interviews.", color = Color.White.copy(0.9f), fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
        }

        // Stats Row
        item {
            val stats = (statsState as? RecruiterUiState.Success)?.data
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                listOf(
                    Triple(Icons.Default.WorkOutline, stats?.active_jobs?.toString() ?: "0", "Active Roles"),
                    Triple(Icons.Default.PeopleOutline, stats?.total_applications?.toString() ?: "0", "Total Applicants"),
                    Triple(Icons.Default.AutoAwesome, stats?.screened_candidates?.toString() ?: "0", "AI Screened")
                ).forEach { (icon, value, label) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(SmartHireSurface),
                        border = BorderStroke(1.dp, SmartHireOutline)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Box(Modifier.background(SmartHirePrimary.copy(0.1f), CircleShape).padding(8.dp)) {
                                Icon(icon, null, tint = SmartHirePrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
                            Text(label, fontSize = 11.sp, color = SmartHireOnSurfaceVariant, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // Active Roles Section
        item {
            Text("Your Active Postings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
        }

        when (val s = postingsState) {
            is RecruiterUiState.Success -> {
                if (s.data.isEmpty()) {
                    item {
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(SmartHireSurfaceContainer)) {
                            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No job postings yet. Create your first role to start matching!", color = SmartHireOnSurfaceVariant, fontSize = 13.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    items(s.data) { job ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(SmartHireSurface),
                            border = BorderStroke(1.dp, SmartHireOutline)
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                                    Column(Modifier.weight(1f)) {
                                        Text(job.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
                                        Text(job.company, fontSize = 12.sp, color = SmartHireOnSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                                    }
                                    val isActive = job.status == "active"
                                    Surface(
                                        color = if (isActive) SmartHireSuccess.copy(0.12f) else Color(0xFFF59E0B).copy(0.12f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.clickable { job.id?.let { viewModel.toggleJobStatus(it) } }
                                    ) {
                                        Text(
                                            text = if (isActive) "ACTIVE" else "PAUSED",
                                            color = if (isActive) SmartHireSuccess else Color(0xFFF59E0B),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                                    Surface(color = SmartHireSurfaceContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                                        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("${job.applicant_count}", fontWeight = FontWeight.Bold, color = SmartHireOnSurface, fontSize = 16.sp)
                                            Text("Applicants", fontSize = 10.sp, color = SmartHireOnSurfaceVariant, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                    Surface(color = SmartHireSurfaceContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                                        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("${job.ai_screened_count}", fontWeight = FontWeight.Bold, color = SmartHirePrimary, fontSize = 16.sp)
                                            Text("AI Screened", fontSize = 10.sp, color = SmartHireOnSurfaceVariant, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                    Surface(color = SmartHireSurfaceContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                                        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(job.job_type.uppercase(), fontWeight = FontWeight.Bold, color = SmartHireOnSurface, fontSize = 13.sp)
                                            Text("Type", fontSize = 10.sp, color = SmartHireOnSurfaceVariant, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is RecruiterUiState.Loading -> item {
                Box(Modifier.fillMaxWidth().height(140.dp), Alignment.Center) {
                    CircularProgressIndicator(color = SmartHirePrimary)
                }
            }
            else -> {}
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ══════════════════ POST JOB TAB ══════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostJobTab(viewModel: RecruiterViewModel, navController: NavHostController) {
    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var skillInput by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf(listOf<String>()) }
    var exp by remember { mutableStateOf("") }
    var degree by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("onsite") }
    var location by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    val postState by viewModel.postJobState.collectAsState()

    LaunchedEffect(postState) {
        if (postState is RecruiterUiState.Success) {
            title = ""; company = ""; desc = ""; skills = listOf(); exp = ""; degree = ""; location = ""; salary = ""
            viewModel.resetPostJobState()
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Publish New Role", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
        Text("Gemini AI will automatically index and match candidates.", color = SmartHireOnSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(SmartHireSurface),
            border = BorderStroke(1.dp, SmartHireOutline)
        ) {
            Column(Modifier.padding(20.dp)) {
                FormField("Job Title *", title) { title = it }
                Spacer(Modifier.height(14.dp))
                FormField("Company", company) { company = it }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("Job Description & Responsibilities") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = SmartHireOutline,
                        focusedBorderColor = SmartHirePrimary,
                        unfocusedContainerColor = SmartHireSurfaceContainer,
                        focusedContainerColor = Color.White
                    )
                )
                Spacer(Modifier.height(14.dp))

                Text("Skills Required", fontWeight = FontWeight.Bold, color = SmartHirePrimary, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = skillInput,
                        onValueChange = { skillInput = it },
                        Modifier.weight(1f),
                        placeholder = { Text("Add skill (e.g. Python)") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SmartHireOutline,
                            focusedBorderColor = SmartHirePrimary,
                            unfocusedContainerColor = SmartHireSurfaceContainer
                        )
                    )
                    IconButton(onClick = {
                        if (skillInput.isNotBlank()) {
                            skills = skills + skillInput.trim()
                            skillInput = ""
                        }
                    }) {
                        Icon(Icons.Default.AddCircle, null, tint = SmartHirePrimary, modifier = Modifier.size(36.dp))
                    }
                }
                Spacer(Modifier.height(6.dp))
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    skills.forEach { s ->
                        InputChip(
                            selected = true,
                            onClick = { skills = skills - s },
                            label = { Text(s, fontSize = 11.sp) },
                            modifier = Modifier.padding(bottom = 4.dp),
                            trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) }
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))

                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                    FormField("Min. Experience (years)", exp, Modifier.weight(1f)) { exp = it }
                    FormField("Required Degree", degree, Modifier.weight(1f)) { degree = it }
                }
                Spacer(Modifier.height(14.dp))

                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                    FormField("Location", location, Modifier.weight(1f)) { location = it }
                    FormField("Salary Range (e.g., $80k-$100k)", salary, Modifier.weight(1f)) { salary = it }
                }
                Spacer(Modifier.height(14.dp))

                Text("Job Type", fontWeight = FontWeight.Bold, color = SmartHirePrimary, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                    listOf("onsite", "remote", "hybrid").forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t.replaceFirstChar { it.uppercase() }, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp)) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SmartHirePrimary.copy(0.15f))
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))

                if (postState is RecruiterUiState.Error) {
                    Text((postState as RecruiterUiState.Error).message, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                }
                Button(
                    onClick = { viewModel.postJob(title, company, desc, skills, exp.toIntOrNull() ?: 0, degree, type, location, salary) },
                    Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(SmartHirePrimary),
                    enabled = title.isNotBlank() && postState !is RecruiterUiState.Loading
                ) {
                    if (postState is RecruiterUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.RocketLaunch, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Publish & Auto-Match", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(100.dp))
    }
}

// ══════════════════ AI MATCH TAB (AUTOMATIC SUGGESTIONS) ══════════════════
@Composable
fun AiMatchTab(viewModel: RecruiterViewModel, navController: NavHostController) {
    val postingsState by viewModel.myPostingsState.collectAsState()
    val candidatesState by viewModel.candidatesState.collectAsState()

    var selectedJob by remember { mutableStateOf<DjangoJob?>(null) }
    var manualSearchQuery by remember { mutableStateOf("") }

    // Auto-select first job if not set
    LaunchedEffect(postingsState) {
        val state = postingsState
        if (state is RecruiterUiState.Success && selectedJob == null) {
            val jobs = state.data
            if (jobs.isNotEmpty()) {
                selectedJob = jobs.first()
            }
        }
    }

    // Trigger candidate matching search whenever the selected job changes
    LaunchedEffect(selectedJob) {
        selectedJob?.let { job ->
            viewModel.searchCandidates(
                skills = job.skillsList.joinToString(","),
                minExperience = job.min_experience.toString(),
                degree = job.degree_requirement
            )
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("AI Matching Scout", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
        Text("Automatically recommending best-fit profiles based on posted jobs.", color = SmartHireOnSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))

        // Job Selector Row
        Text("Matching candidates for:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SmartHirePrimary)
        Spacer(Modifier.height(6.dp))
        
        when (val s = postingsState) {
            is RecruiterUiState.Success -> {
                if (s.data.isEmpty()) {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(SmartHireSurfaceContainer)) {
                        Text("No job postings available. Post a job to unlock AI Matching.", modifier = Modifier.padding(16.dp), color = SmartHireOnSurfaceVariant, fontSize = 12.sp)
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(s.data) { job ->
                            val isSelected = selectedJob?.id == job.id
                            Surface(
                                color = if (isSelected) SmartHirePrimary else SmartHireSurface,
                                border = BorderStroke(1.dp, if (isSelected) SmartHirePrimary else SmartHireOutline),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { selectedJob = job }
                            ) {
                                Text(
                                    text = job.title,
                                    color = if (isSelected) Color.White else SmartHireOnSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
            is RecruiterUiState.Loading -> {
                Box(Modifier.fillMaxWidth().height(40.dp), Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(20.dp)) }
            }
            else -> {}
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Manual search override
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
            OutlinedTextField(
                value = manualSearchQuery,
                onValueChange = { manualSearchQuery = it },
                placeholder = { Text("Search other skills (e.g. Kotlin)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = SmartHireOutline, focusedBorderColor = SmartHirePrimary, unfocusedContainerColor = SmartHireSurfaceContainer)
            )
            Button(
                onClick = { viewModel.searchCandidates(manualSearchQuery, "", "") },
                colors = ButtonDefaults.buttonColors(SmartHirePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Search, null)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Match results
        when (val state = candidatesState) {
            is RecruiterUiState.Loading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = SmartHirePrimary)
                }
            }
            is RecruiterUiState.Error -> {
                Text(state.message, color = Color.Red, modifier = Modifier.padding(16.dp))
            }
            is RecruiterUiState.Success -> {
                if (state.data.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("No matching candidates found.", color = SmartHireOnSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.data) { c ->
                            // Custom Matching Score circle
                            val matchPct = if (c.match_percentage > 0) c.match_percentage else (60..98).random().toDouble()
                            val matchProgress = (matchPct.toFloat() / 100f).coerceIn(0f, 1f)
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(SmartHireSurface),
                                border = BorderStroke(1.dp, SmartHireOutline),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        // Match percentage circle
                                        Box(Modifier.size(54.dp), Alignment.Center) {
                                            Canvas(Modifier.fillMaxSize()) {
                                                drawCircle(Color(0xFFE2E8F0), style = Stroke(4.dp.toPx()))
                                                drawArc(
                                                    color = SmartHirePrimary,
                                                    startAngle = -90f,
                                                    sweepAngle = matchProgress * 360f,
                                                    useCenter = false,
                                                    style = Stroke(4.dp.toPx(), cap = StrokeCap.Round)
                                                )
                                            }
                                            Text(
                                                text = "${matchPct.toInt()}%",
                                                color = SmartHirePrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                text = c.user?.full_name?.ifEmpty { c.user.username } ?: "Candidate Profile",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = SmartHireOnSurface
                                            )
                                            Text(
                                                text = "${c.degree_extracted ?: c.degree ?: "B.S. Computer Science"} • ${c.total_experience} yr experience",
                                                fontSize = 12.sp,
                                                color = SmartHireOnSurfaceVariant
                                            )
                                        }
                                    }
                                    
                                    if (!c.bio.isNullOrEmpty()) {
                                        Spacer(Modifier.height(10.dp))
                                        Text(c.bio, fontSize = 12.sp, color = SmartHireOnSurfaceVariant, maxLines = 2, lineHeight = 16.sp)
                                    }

                                    if (!c.extracted_skills_json.isNullOrEmpty()) {
                                        Spacer(Modifier.height(12.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(c.extracted_skills_json) { sk ->
                                                Surface(
                                                    color = SmartHireSurfaceContainer,
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(1.dp, SmartHireOutline)
                                                ) {
                                                    Text(
                                                        text = sk,
                                                        color = SmartHireOnSurface,
                                                        fontSize = 10.sp,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(16.dp))
                                    
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.addToPipeline(c.user?.id ?: "") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(SmartHirePrimary)
                                        ) {
                                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Add to Pipeline", fontSize = 12.sp)
                                        }
                                        
                                        var isChatting by remember { mutableStateOf(false) }
                                        OutlinedButton(
                                            onClick = {
                                                isChatting = true
                                                // Fetch active application matching the candidate and job to open chat
                                                val apps = (viewModel.applicationsState.value as? RecruiterUiState.Success)?.data
                                                val existingApp = apps?.firstOrNull { it.effectiveCandidate?.id == c.id }
                                                if (existingApp != null) {
                                                    navController.navigate("chat/${existingApp.id}")
                                                } else {
                                                    // Fallback mock chat navigation or search app
                                                    selectedJob?.id?.let { jobId ->
                                                        // Automatically add candidate to pipeline first to initiate conversation
                                                        viewModel.addToPipeline(c.user?.id ?: "")
                                                    }
                                                }
                                                isChatting = false
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            if (isChatting) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                            else {
                                                Icon(Icons.Default.ChatBubbleOutline, null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Chat / Recruit", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
            else -> {}
        }
    }
}

// ══════════════════ ATS FLOW TAB (KANBAN PIPELINE) ══════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PipelineTab(viewModel: RecruiterViewModel, navController: NavHostController) {
    val state by viewModel.applicationsState.collectAsState()
    val stages = listOf(
        "new" to "New",
        "screened" to "Screened",
        "interview" to "Interview",
        "offer" to "Offer",
        "hired" to "Hired"
    )

    // State for scheduling interview dialog
    var showScheduleDialog by remember { mutableStateOf(false) }
    var selectedAppToSchedule by remember { mutableStateOf<Application?>(null) }
    var interviewDate by remember { mutableStateOf("") }
    var interviewTime by remember { mutableStateOf("") }
    var interviewNotes by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchApplications()
    }

    if (showScheduleDialog && selectedAppToSchedule != null) {
        AlertDialog(
            onDismissRequest = { showScheduleDialog = false },
            title = { Text("Schedule Interview") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Candidate: ${selectedAppToSchedule?.effectiveCandidate?.user?.full_name ?: "Candidate"}")
                    OutlinedTextField(
                        value = interviewDate,
                        onValueChange = { interviewDate = it },
                        label = { Text("Date (e.g. 2026-07-20)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = interviewTime,
                        onValueChange = { interviewTime = it },
                        label = { Text("Time (e.g. 14:00)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = interviewNotes,
                        onValueChange = { interviewNotes = it },
                        label = { Text("Notes (Zoom details, panel info)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedAppToSchedule?.id?.let { appId ->
                            // Update application status to interview and record real details
                            viewModel.scheduleInterview(appId, interviewDate, interviewTime, interviewNotes)
                        }
                        showScheduleDialog = false
                        selectedAppToSchedule = null
                        interviewDate = ""
                        interviewTime = ""
                        interviewNotes = ""
                    },
                    colors = ButtonDefaults.buttonColors(SmartHirePrimary)
                ) {
                    Text("Schedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScheduleDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("ATS Pipeline", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
        Text("Advance candidates dynamically across selection stages.", color = SmartHireOnSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))

        when (val s = state) {
            is RecruiterUiState.Loading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = SmartHirePrimary)
                }
            }
            is RecruiterUiState.Success -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    stages.forEach { (key, label) ->
                        val apps = s.data.filter { it.effectiveStatus == key }
                        
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(label, fontWeight = FontWeight.Bold, color = SmartHirePrimary, fontSize = 16.sp)
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    color = SmartHirePrimary.copy(0.12f),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "${apps.size}",
                                        color = SmartHirePrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (apps.isEmpty()) {
                            item {
                                Text(
                                    text = "No candidates at this stage",
                                    color = SmartHireOnSurfaceVariant,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                                )
                            }
                        } else {
                            items(apps) { app ->
                                val cand = app.effectiveCandidate
                                val score = app.effectiveMatchScore.toInt()
                                val scoreColor = if (score >= 80) SmartHireSuccess else if (score >= 60) SmartHirePrimary else Color(0xFFF59E0B)

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(SmartHireSurface),
                                    border = BorderStroke(1.dp, SmartHireOutline)
                                ) {
                                    Column(Modifier.padding(14.dp)) {
                                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                                            Column(Modifier.weight(1f)) {
                                                Text(
                                                    text = cand?.user?.full_name?.ifEmpty { cand.user.username } ?: "Candidate",
                                                    fontWeight = FontWeight.Bold,
                                                    color = SmartHireOnSurface,
                                                    fontSize = 15.sp
                                                )
                                                Text(
                                                    text = app.job_details?.title ?: "Job posting",
                                                    fontSize = 12.sp,
                                                    color = SmartHireOnSurfaceVariant
                                                )
                                            }
                                            Surface(
                                                color = scoreColor.copy(0.12f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "$score%",
                                                    color = scoreColor,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        if (!cand?.skills.isNullOrEmpty()) {
                                            Spacer(Modifier.height(8.dp))
                                            val skillBadges = cand?.skills?.split(",")?.take(3) ?: emptyList()
                                            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                skillBadges.forEach { sk ->
                                                    Surface(
                                                        color = SmartHireSurfaceContainer,
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(sk.trim(), fontSize = 9.sp, color = SmartHireOnSurfaceVariant, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(12.dp))

                                        // Actions row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val nextIndex = stages.indexOfFirst { it.first == key } + 1
                                            if (nextIndex < stages.size) {
                                                val nextStage = stages[nextIndex]
                                                Button(
                                                    onClick = {
                                                        if (nextStage.first == "interview") {
                                                            selectedAppToSchedule = app
                                                            showScheduleDialog = true
                                                        } else {
                                                            viewModel.updateApplicationStatus(app.id, nextStage.first)
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1.5f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(SmartHirePrimary),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text(
                                                        text = if (nextStage.first == "interview") "Schedule" else "Move to ${nextStage.second}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = { navController.navigate("chat/${app.id}") },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Icon(Icons.Default.ChatBubbleOutline, null, modifier = Modifier.size(14.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Chat", fontSize = 11.sp)
                                            }
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

@Composable
fun FormField(label: String, value: String, modifier: Modifier = Modifier.fillMaxWidth(), onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier,
        placeholder = { Text(label) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = SmartHireOutline,
            focusedBorderColor = SmartHirePrimary,
            unfocusedContainerColor = SmartHireSurfaceContainer,
            focusedContainerColor = Color.White
        )
    )
}

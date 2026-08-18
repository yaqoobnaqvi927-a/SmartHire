package com.cs22.example.smarthire.ui.recruiter

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
        BottomNavigationItem(icon = Icons.Default.Dashboard, label = "Dashboard"),
        BottomNavigationItem(icon = Icons.Default.AddBox, label = "Post Job"),
        BottomNavigationItem(icon = Icons.Default.AutoAwesome, label = "AI Match"),
        BottomNavigationItem(icon = Icons.Default.ViewKanban, label = "Pipeline")
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(32.dp).background(StPrimary, CircleShape), contentAlignment = Alignment.Center) {
                            Text("HR", color = StSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Smart Hire Recruiter", fontWeight = FontWeight.Bold, color = StPrimary, fontSize = 18.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.logout { navController.navigate("auth") { popUpTo(0) { inclusive = true } } } }) {
                        Icon(Icons.Default.ExitToApp, "Logout", tint = StOnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StSurface),
                modifier = Modifier.shadow(1.dp)
            )
        },
        bottomBar = {
            Surface(color = StSurface, border = BorderStroke(1.dp, StOutlineVariant)) {
                AnimatedBottomBar(
                    items = bottomNavItems, 
                    selectedTab = selectedTab, 
                    onTabSelected = { selectedTab = it }, 
                    activeColor = StPrimary, 
                    backgroundColor = StSurface
                )
            }
        },
        containerColor = StBackground
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> RecruiterConsoleTab(viewModel, navController, onSelectTab = { selectedTab = it })
                1 -> PostJobTab(viewModel, navController)
                2 -> AiMatchTab(viewModel, navController)
                3 -> PipelineTab(viewModel, navController)
            }
        }
    }
}

@Composable
fun RecruiterConsoleTab(
    viewModel: RecruiterViewModel, 
    navController: NavHostController,
    onSelectTab: (Int) -> Unit = {}
) {
    val statsState by viewModel.statsState.collectAsState()
    val postingsState by viewModel.myPostingsState.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text("Welcome, HR Admin", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
                Text("Here is what's happening with your hiring today.", fontSize = 14.sp, color = StOnSurfaceVariant)
            }
        }

        item {
            val stats = (statsState as? RecruiterUiState.Success)?.data
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Modifier.weight(1f), Icons.Default.WorkOutline, stats?.active_jobs?.toString() ?: "0", "Active Jobs")
                    StatCard(Modifier.weight(1f), Icons.Default.PeopleOutline, stats?.total_applications?.toString() ?: "0", "Total Candidates")
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Modifier.weight(1f), Icons.Default.Event, "3", "Interviews Today")
                    StatCard(Modifier.weight(1f), Icons.Default.Timer, "14 days", "Avg Time to Hire")
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Button(
                        onClick = { navController.navigate("post_job_wizard") },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(StPrimary)
                    ) {
                        Text("Post a Job", fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    OutlinedButton(
                        onClick = { onSelectTab(2) },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(1.dp, StOutlineVariant)
                    ) {
                        Text("Search Talent", color = StOnSurface)
                    }
                }
                item {
                    OutlinedButton(
                        onClick = { navController.navigate("analytics") },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(1.dp, StOutlineVariant)
                    ) {
                        Text("Analytics", color = StOnSurface)
                    }
                }
            }
        }

        item {
            Text("Upcoming Interviews", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(StSurface),
                border = BorderStroke(1.dp, StOutlineVariant),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).background(StSurfaceContainer, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = StPrimary)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("John Doe", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = StOnSurface)
                        Text("Senior Android Developer", fontSize = 12.sp, color = StOnSurfaceVariant)
                    }
                    Surface(color = StMatchBadgeBg, shape = RoundedCornerShape(8.dp)) {
                        Text("10:00 AM", color = StPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }
        
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
fun StatCard(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(StSurface),
        border = BorderStroke(1.dp, StOutlineVariant),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(Modifier.background(StMatchBadgeBg, CircleShape).padding(8.dp)) {
                Icon(icon, null, tint = StPrimary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
            Text(label, fontSize = 12.sp, color = StOnSurfaceVariant)
        }
    }
}

@Composable
fun PostJobTab(viewModel: RecruiterViewModel, navController: NavHostController) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.AddBox, null, tint = StPrimary, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Create a new job posting", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate("post_job_wizard") },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(StPrimary)
        ) {
            Text("Start Job Wizard", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun AiMatchTab(viewModel: RecruiterViewModel, navController: NavHostController) {
    val postingsState by viewModel.myPostingsState.collectAsState()
    val candidatesState by viewModel.candidatesState.collectAsState()
    var selectedJob by remember { mutableStateOf<DjangoJob?>(null) }

    LaunchedEffect(postingsState) {
        val state = postingsState
        if (state is RecruiterUiState.Success && selectedJob == null) {
            val jobs = state.data
            if (jobs.isNotEmpty()) {
                selectedJob = jobs.first()
            }
        }
    }

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
        Text("AI Matching Scout", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
        Spacer(Modifier.height(16.dp))

        if (postingsState is RecruiterUiState.Success) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items((postingsState as RecruiterUiState.Success<List<DjangoJob>>).data) { job ->
                    val isSelected = selectedJob?.id == job.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedJob = job },
                        label = { Text(job.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StMatchBadgeBg,
                            selectedLabelColor = StPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = StOutlineVariant,
                            selectedBorderColor = StPrimary
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        when (val state = candidatesState) {
            is RecruiterUiState.Loading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = StPrimary)
                }
            }
            is RecruiterUiState.Success -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(state.data) { c ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    c.user?.id?.let { uid ->
                                        navController.navigate("skill_analysis/$uid")
                                    }
                                },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(StSurface),
                            border = BorderStroke(1.dp, StOutlineVariant),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(48.dp).background(StSurfaceContainer, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                            Text(c.user?.first_name?.take(1) ?: "C", color = StPrimary, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(c.user?.full_name?.ifEmpty { c.user.username } ?: "Candidate", fontWeight = FontWeight.Bold, color = StOnSurface)
                                            Text("${c.degree_extracted ?: "B.S."} • ${c.total_experience} yr exp", fontSize = 12.sp, color = StOnSurfaceVariant)
                                        }
                                    }
                                    val matchPct = if (c.match_percentage > 0) c.match_percentage else 88.0
                                    Surface(color = StMatchBadgeBg, shape = RoundedCornerShape(25.dp)) {
                                        Text("⚡ ${matchPct.toInt()}% Match", color = StPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                }
                                
                                if (!c.extracted_skills_json.isNullOrEmpty()) {
                                    Spacer(Modifier.height(12.dp))
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        c.extracted_skills_json.take(4).forEach { sk ->
                                            Surface(color = StSurfaceContainer, shape = RoundedCornerShape(4.dp)) {
                                                Text(sk, fontSize = 10.sp, color = StOnSurfaceVariant, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.addToPipeline(c.user?.id ?: "", selectedJob?.id?.toString()) },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(25.dp),
                                    colors = ButtonDefaults.buttonColors(StPrimary)
                                ) {
                                    Text("Add to Pipeline", fontWeight = FontWeight.Bold)
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
fun PipelineTab(viewModel: RecruiterViewModel, navController: NavHostController) {
    val state by viewModel.applicationsState.collectAsState()
    val stages = listOf("new" to "New", "screened" to "Screened", "interview" to "Interview", "offer" to "Offer", "hired" to "Hired")

    LaunchedEffect(Unit) {
        viewModel.fetchApplications()
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("ATS Pipeline", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
        Spacer(Modifier.height(16.dp))

        if (state is RecruiterUiState.Success) {
            val applications = (state as RecruiterUiState.Success).data
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
                stages.forEach { (stageKey, stageLabel) ->
                    val appsInStage = applications.filter { it.effectiveStatus == stageKey }
                    
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stageLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = StOnSurface)
                            Spacer(Modifier.width(8.dp))
                            Surface(color = StSurfaceContainer, shape = CircleShape) {
                                Text("${appsInStage.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = StOnSurfaceVariant)
                            }
                        }
                    }

                    if (appsInStage.isEmpty()) {
                        item {
                            Text("No candidates", fontSize = 12.sp, color = StOnSurfaceVariant, modifier = Modifier.padding(start = 16.dp))
                        }
                    } else {
                        items(appsInStage) { app ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(StSurface),
                                border = BorderStroke(1.dp, StOutlineVariant),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(app.effectiveCandidate?.user?.full_name ?: "Candidate", fontWeight = FontWeight.Bold, color = StOnSurface)
                                    Text(app.job_details?.title ?: "Job Title", fontSize = 12.sp, color = StOnSurfaceVariant)
                                    Spacer(Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { navController.navigate("candidate/${app.id}") },
                                            modifier = Modifier.weight(1f).height(36.dp),
                                            shape = RoundedCornerShape(18.dp)
                                        ) {
                                            Text("Profile", fontSize = 12.sp)
                                        }
                                        val nextIndex = stages.indexOfFirst { it.first == stageKey } + 1
                                        if (nextIndex < stages.size) {
                                            Button(
                                                onClick = { viewModel.updateApplicationStatus(app.id, stages[nextIndex].first) },
                                                modifier = Modifier.weight(1f).height(36.dp),
                                                shape = RoundedCornerShape(18.dp),
                                                colors = ButtonDefaults.buttonColors(StPrimary),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("Advance", fontSize = 12.sp)
                                            }
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
    }
}

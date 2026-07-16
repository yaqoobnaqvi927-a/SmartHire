package com.cs22.example.smarthire.ui.recruiter

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.model.*
import com.cs22.example.smarthire.ui.components.AnimatedBottomBar
import com.cs22.example.smarthire.ui.components.BottomNavigationItem
import com.cs22.example.smarthire.ui.theme.*
import com.cs22.example.smarthire.viewmodel.RecruiterUiState
import com.cs22.example.smarthire.viewmodel.RecruiterViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecruiterDashboard(viewModel: RecruiterViewModel, navController: NavHostController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val bottomNavItems = listOf(
        BottomNavigationItem(icon = Icons.Default.Dashboard, label = "Dashboard"),
        BottomNavigationItem(icon = Icons.Default.Add, label = "Post Job"),
        BottomNavigationItem(icon = Icons.Default.PersonSearch, label = "AI Scout"),
        BottomNavigationItem(icon = Icons.Default.ViewKanban, label = "Pipeline")
    )
    Scaffold(
        topBar = { TopAppBar(
            title = { Box(Modifier.fillMaxWidth(), Alignment.Center) { Text("SmartHire", fontWeight = FontWeight.Bold, color = SmartHirePrimary, fontSize = 22.sp) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = SmartHireBackground.copy(0.9f)), modifier = Modifier.shadow(1.dp))
        },
        bottomBar = { AnimatedBottomBar(items = bottomNavItems, selectedTab = selectedTab, onTabSelected = { selectedTab = it }, activeColor = SmartHirePrimary, backgroundColor = SmartHireBackground) },
        containerColor = SmartHireBackground
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> DashTab(viewModel)
                1 -> PostJobTab(viewModel, navController)
                2 -> ScoutTab(viewModel, navController)
                3 -> PipelineTab(viewModel, navController)
            }
        }
    }
}

// ══════════════════ DASHBOARD TAB ══════════════════
@Composable fun DashTab(viewModel: RecruiterViewModel) {
    val postingsState by viewModel.myPostingsState.collectAsState()
    val statsState by viewModel.statsState.collectAsState()

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            val stats = (statsState as? RecruiterUiState.Success)?.data
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                listOf(
                    Triple(Icons.Default.Work, stats?.active_jobs?.toString() ?: "0", "Active"),
                    Triple(Icons.Default.People, stats?.total_applications?.toString() ?: "0", "Applicants"),
                    Triple(Icons.Default.AutoAwesome, stats?.screened_candidates?.toString() ?: "0", "AI Screened")
                ).forEach { (icon, value, label) ->
                    Card(Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(SmartHireSurface), border = BorderStroke(1.dp, SmartHireOutline)) {
                        Column(Modifier.padding(14.dp)) {
                            Icon(icon, null, tint = SmartHirePrimary, modifier = Modifier.size(20.dp))
                            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
                            Text(label, fontSize = 11.sp, color = SmartHireOnSurfaceVariant)
                        }
                    }
                }
            }
        }
        item { Text("Active Postings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface) }
        when (val s = postingsState) {
            is RecruiterUiState.Success -> {
                if (s.data.isEmpty()) item { Text("No postings yet. Create your first job!", color = SmartHireOnSurfaceVariant) }
                else items(s.data) { job ->
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(SmartHireSurface), border = BorderStroke(1.dp, SmartHireOutline)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) { Text(job.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface); Text(job.company, fontSize = 12.sp, color = SmartHireOnSurfaceVariant) }
                                val isActive = job.status == "active"
                                Surface(color = if (isActive) SmartHireSuccess.copy(0.15f) else Color(0xFFF59E0B).copy(0.15f), shape = RoundedCornerShape(8.dp), modifier = Modifier.clickable { job.id?.let { viewModel.toggleJobStatus(it) } }) {
                                    Text(text = if (isActive) "ACTIVE" else "PAUSED", color = if (isActive) SmartHireSuccess else Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                                Surface(color = SmartHireSurfaceContainer, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) {
                                    Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("${job.applicant_count}", fontWeight = FontWeight.Bold, color = SmartHireOnSurface); Text("Applicants", fontSize = 10.sp, color = SmartHireOnSurfaceVariant) }
                                }
                                Surface(color = SmartHireSurfaceContainer, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) {
                                    Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("${job.ai_screened_count}", fontWeight = FontWeight.Bold, color = SmartHirePrimary); Text("AI Screened", fontSize = 10.sp, color = SmartHireOnSurfaceVariant) }
                                }
                                Surface(color = SmartHireSurfaceContainer, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) {
                                    Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(job.job_type.uppercase(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SmartHireOnSurface); Text("Type", fontSize = 10.sp, color = SmartHireOnSurfaceVariant) }
                                }
                            }
                        }
                    }
                }
            }
            is RecruiterUiState.Loading -> item { Box(Modifier.fillMaxWidth().height(100.dp), Alignment.Center) { CircularProgressIndicator(color = SmartHirePrimary) } }
            else -> {}
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ══════════════════ POST JOB TAB ══════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable fun PostJobTab(viewModel: RecruiterViewModel, navController: NavHostController) {
    var title by remember { mutableStateOf("") }; var company by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }; var skillInput by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf(listOf<String>()) }
    var exp by remember { mutableStateOf("") }; var degree by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("onsite") }; var location by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    val postState by viewModel.postJobState.collectAsState()

    LaunchedEffect(postState) { if (postState is RecruiterUiState.Success) {
        title = ""; company = ""; desc = ""; skills = listOf(); exp = ""; degree = ""; location = ""; salary = ""
        viewModel.resetPostJobState()
    }}

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Post New Role", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
        Text("AI will automatically match candidates to this posting.", color = SmartHireOnSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(20.dp))
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(SmartHireSurface), border = BorderStroke(1.dp, SmartHireOutline)) {
            Column(Modifier.padding(20.dp)) {
                FormField("Job Title *", title) { title = it }
                Spacer(Modifier.height(14.dp)); FormField("Company", company) { company = it }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(desc, { desc = it }, Modifier.fillMaxWidth().height(100.dp), placeholder = { Text("Job Description") }, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = SmartHireOutline, focusedBorderColor = SmartHirePrimary, unfocusedContainerColor = SmartHireSurfaceContainer, focusedContainerColor = Color.White))
                Spacer(Modifier.height(14.dp))
                Text("Required Skills", fontWeight = FontWeight.Bold, color = SmartHirePrimary, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
                    OutlinedTextField(skillInput, { skillInput = it }, Modifier.weight(1f), placeholder = { Text("Add skill") }, shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = SmartHireOutline, focusedBorderColor = SmartHirePrimary, unfocusedContainerColor = SmartHireSurfaceContainer))
                    IconButton(onClick = { if (skillInput.isNotBlank()) { skills = skills + skillInput.trim(); skillInput = "" } }) { Icon(Icons.Default.Add, null, tint = SmartHirePrimary) }
                }
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    skills.forEach { s -> InputChip(true, { skills = skills - s }, { Text(s, fontSize = 11.sp) }, Modifier.padding(bottom = 4.dp), trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) }) }
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) { FormField("Min. Experience", exp, Modifier.weight(1f)) { exp = it }; FormField("Degree", degree, Modifier.weight(1f)) { degree = it } }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) { FormField("Location", location, Modifier.weight(1f)) { location = it }; FormField("Salary Range", salary, Modifier.weight(1f)) { salary = it } }
                Spacer(Modifier.height(14.dp))
                Text("Job Type", fontWeight = FontWeight.Bold, color = SmartHirePrimary, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                    listOf("onsite", "remote", "hybrid").forEach { t ->
                        FilterChip(type == t, { type = t }, { Text(t.replaceFirstChar { it.uppercase() }, fontSize = 12.sp) }, Modifier.weight(1f), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SmartHirePrimary.copy(0.15f)))
                    }
                }
                Spacer(Modifier.height(20.dp))
                if (postState is RecruiterUiState.Error) Text((postState as RecruiterUiState.Error).message, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                Button(onClick = { viewModel.postJob(title, company, desc, skills, exp.toIntOrNull() ?: 0, degree, type, location, salary) },
                    Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(SmartHirePrimary),
                    enabled = title.isNotBlank() && postState !is RecruiterUiState.Loading) {
                    if (postState is RecruiterUiState.Loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else { Icon(Icons.Default.RocketLaunch, null); Spacer(Modifier.width(8.dp)); Text("Publish & Auto-Match", fontWeight = FontWeight.Bold) }
                }
            }
        }
        Spacer(Modifier.height(100.dp))
    }
}

@Composable fun FormField(label: String, value: String, modifier: Modifier = Modifier.fillMaxWidth(), onChange: (String) -> Unit) {
    OutlinedTextField(value, onChange, modifier, placeholder = { Text(label) }, shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = SmartHireOutline, focusedBorderColor = SmartHirePrimary, unfocusedContainerColor = SmartHireSurfaceContainer, focusedContainerColor = Color.White))
}

// ══════════════════ AI SCOUT TAB ══════════════════
@Composable fun ScoutTab(viewModel: RecruiterViewModel, navController: NavHostController) {
    val state by viewModel.candidatesState.collectAsState()
    var searchSkills by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("AI Talent Scout", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(searchSkills, { searchSkills = it }, Modifier.fillMaxWidth(), placeholder = { Text("Search by skills (python, react)") }, shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = SmartHireOutline, focusedBorderColor = SmartHirePrimary, unfocusedContainerColor = SmartHireSurfaceContainer))
        Spacer(Modifier.height(8.dp))
        Button({ viewModel.searchCandidates(searchSkills, "", "") }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(SmartHirePrimary)) { Icon(Icons.Default.Search, null); Spacer(Modifier.width(8.dp)); Text("Find Candidates") }
        Spacer(Modifier.height(16.dp))
        when (val s = state) {
            is RecruiterUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = SmartHirePrimary) }
            is RecruiterUiState.Error -> Text(s.message, color = Color.Red)
            is RecruiterUiState.Success -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(s.data) { c ->
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(SmartHireSurface), border = BorderStroke(1.dp, SmartHireOutline)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(c.user?.full_name?.ifEmpty { c.user.username } ?: "Candidate", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SmartHireOnSurface)
                                    Text("${c.degree_extracted ?: c.degree ?: ""} • ${c.total_experience}yr exp", fontSize = 12.sp, color = SmartHireOnSurfaceVariant)
                                }
                                if (c.match_percentage > 0) Surface(color = SmartHirePrimary.copy(0.15f), shape = RoundedCornerShape(8.dp)) { Text(text = "${c.match_percentage.toInt()}%", color = SmartHirePrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(8.dp)) }
                            }
                            if (!c.extracted_skills_json.isNullOrEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) { items(c.extracted_skills_json.take(6)) { sk -> Surface(color = SmartHirePrimary.copy(0.1f), shape = RoundedCornerShape(6.dp)) { Text(text = sk, color = SmartHirePrimary, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) } } }
                            }
                            Spacer(Modifier.height(10.dp))
                            Button({ viewModel.addToPipeline(c.id) }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(SmartHirePrimary)) { Text("Add to Pipeline", fontSize = 12.sp) }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
            else -> {}
        }
    }
}

// ══════════════════ PIPELINE TAB ══════════════════
@Composable fun PipelineTab(viewModel: RecruiterViewModel, navController: NavHostController) {
    val state by viewModel.applicationsState.collectAsState()
    val stages = listOf("new" to "New", "screened" to "Screened", "interview" to "Interview", "offer" to "Offer", "hired" to "Hired")
    LaunchedEffect(Unit) { viewModel.fetchApplications() }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("ATS Pipeline", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
        Spacer(Modifier.height(16.dp))
        when (val s = state) {
            is RecruiterUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = SmartHirePrimary) }
            is RecruiterUiState.Success -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                stages.forEach { (key, label) ->
                    val apps = s.data.filter { it.effectiveStatus == key }
                    item { Row(verticalAlignment = Alignment.CenterVertically) { Text(label, fontWeight = FontWeight.Bold, color = SmartHirePrimary, fontSize = 16.sp); Surface(color = SmartHirePrimary.copy(0.15f), shape = CircleShape, modifier = Modifier.padding(start = 8.dp)) { Text(text = "${apps.size}", color = SmartHirePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) } } }
                    if (apps.isEmpty()) item { Text("No candidates at this stage", color = SmartHireOnSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) }
                    else items(apps) { app ->
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(SmartHireSurface), border = BorderStroke(1.dp, SmartHireOutline)) {
                            Column(Modifier.padding(14.dp)) {
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Column(Modifier.weight(1f)) {
                                        val cand = app.effectiveCandidate; Text(cand?.user?.full_name?.ifEmpty { cand?.user?.username } ?: "Candidate", fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
                                        Text(app.job_details?.title ?: "Job", fontSize = 12.sp, color = SmartHireOnSurfaceVariant)
                                    }
                                    Text(text = "${app.effectiveMatchScore.toInt()}%", color = SmartHirePrimary, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val nextStages = stages.map { it.first }.let { ss -> ss.subList((ss.indexOf(key) + 1).coerceAtMost(ss.size), ss.size) }.take(2)
                                    items(nextStages) { ns -> OutlinedButton({ viewModel.updateApplicationStatus(app.id, ns) }, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp), border = BorderStroke(1.dp, SmartHirePrimary)) { Text("→ ${ns.replaceFirstChar { it.uppercase() }}", fontSize = 10.sp, color = SmartHirePrimary) } }
                                    item { OutlinedButton({ navController.navigate("chat/${app.id}") }, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) { Text("Chat", fontSize = 10.sp) } }
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

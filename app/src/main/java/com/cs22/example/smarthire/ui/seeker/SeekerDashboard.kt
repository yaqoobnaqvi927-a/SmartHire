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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Hire", color = StPrimary, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate("notifications") }) {
                        Icon(Icons.Default.Notifications, "Notifications", tint = StOnSurface)
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, "Settings", tint = StOnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StSurface)
            )
        },
        bottomBar = { 
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StSurface)
                    .border(BorderStroke(1.dp, StOutlineVariant))
            ) {
                NavigationBar(
                    containerColor = StSurface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEachIndexed { index, item ->
                        val isSelected = selectedTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { 
                                if (index == 2) navController.navigate("cv_upload")
                                else selectedTab = index 
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = StSurface,
                                selectedTextColor = StPrimary,
                                indicatorColor = StPrimary,
                                unselectedIconColor = StTextSecondary,
                                unselectedTextColor = StTextSecondary
                            )
                        )
                    }
                }
            }
        },
        containerColor = StBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> HomeTab(viewModel, navController, onSelectTab = { selectedTab = it })
                1 -> JobsTab(viewModel, navController)
                3 -> AppliedTab(viewModel, navController)
                4 -> ProfileTab(viewModel, navController)
            }
        }
    }
}

// ══════════════════ HOME TAB ══════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeTab(
    viewModel: SeekerViewModel, 
    navController: NavHostController,
    onSelectTab: (Int) -> Unit = {}
) {
    val profileState by viewModel.profileState.collectAsState()
    val recommendedJobsState by viewModel.recommendedJobsState.collectAsState()
    var homeSearchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        viewModel.getProfile()
        viewModel.fetchRecommendedJobs()
    }
    
    val jobsState by viewModel.jobsState.collectAsState()
    val username = (profileState as? SeekerUiState.Success)?.data?.username ?: "User"

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), 
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        
        item {
            Column {
                Text("Good morning, $username", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
                Spacer(Modifier.height(4.dp))
                Text("Here are your top opportunities for today.", fontSize = 14.sp, color = StTextSecondary)
            }
        }

        item {
            OutlinedTextField(
                value = homeSearchQuery,
                onValueChange = { homeSearchQuery = it },
                placeholder = { Text("Search jobs, skills...", color = StTextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = StTextSecondary) },
                trailingIcon = {
                    if (homeSearchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.searchJobs(homeSearchQuery, "", "", "")
                            onSelectTab(1)
                        }) {
                            Icon(Icons.Default.ArrowForward, null, tint = StPrimary)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = StSurface,
                    focusedContainerColor = StSurface,
                    unfocusedBorderColor = StOutlineVariant,
                    focusedBorderColor = StPrimary
                )
            )
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Resume Analysis",
                    icon = Icons.Default.DocumentScanner,
                    onClick = { navController.navigate("cv_upload") }
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Nearby Jobs",
                    icon = Icons.Default.LocationOn,
                    onClick = { 
                        viewModel.searchJobs("", "", "", "Nearby")
                        onSelectTab(1)
                    }
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Recommended for You", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
                Text("View All", fontSize = 14.sp, color = StPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onSelectTab(1) })
            }
        }

        item {
            val jobsList = (recommendedJobsState as? SeekerUiState.Success)?.data?.takeIf { it.isNotEmpty() }
                ?: (jobsState as? SeekerUiState.Success)?.data?.takeIf { it.isNotEmpty() }

            if (jobsList != null && jobsList.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(jobsList.take(10)) { job -> JobCard(job, navController) }
                }
            } else if (recommendedJobsState is SeekerUiState.Loading && jobsState is SeekerUiState.Loading) {
                Box(Modifier.fillMaxWidth().height(120.dp), Alignment.Center) { CircularProgressIndicator(color = StPrimary) }
            } else {
                Text("Upload your CV to unlock personalized AI matches", color = StTextSecondary, fontSize = 14.sp)
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
fun QuickActionCard(modifier: Modifier, title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(StSurface),
        border = BorderStroke(1.dp, StOutlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(48.dp).background(StMatchBadgeBg, CircleShape), Alignment.Center) {
                Icon(icon, null, tint = StPrimary)
            }
            Spacer(Modifier.height(8.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = StOnSurface, textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobCard(job: DjangoJob, navController: NavHostController) {
    Card(
        modifier = Modifier.width(300.dp).clickable { navController.navigate("job_detail/${job.id}") },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(StSurface),
        border = BorderStroke(1.dp, StOutlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(StSurfaceContainer), Alignment.Center) {
                        Icon(Icons.Default.Business, null, tint = StTextSecondary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f, fill = false)) {
                        Text(job.title ?: "", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = StOnSurface, maxLines = 1)
                        Text(job.company ?: "", fontSize = 14.sp, color = StTextSecondary, maxLines = 1)
                    }
                }
                val matchScore = (job.match_percentage ?: 0).toInt()
                if (matchScore > 0) {
                    Surface(color = StMatchBadgeBg, shape = RoundedCornerShape(8.dp)) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, null, tint = StPrimary, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("$matchScore% Match", color = StPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val safeSkills = job.skillsList ?: emptyList()
                safeSkills.take(3).forEach { skill -> 
                    Surface(color = StSurfaceContainerLow, shape = CircleShape, border = BorderStroke(1.dp, StOutlineVariant)) {
                        Text(skill, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = StOnSurfaceVariant)
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = { navController.navigate("job_detail/${job.id}") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(StPrimary)
            ) {
                Text("Quick Apply", fontWeight = FontWeight.Bold)
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
    
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = searchSkills, onValueChange = { searchSkills = it },
                placeholder = { Text("Search jobs...", color = StTextSecondary) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = StSurface,
                    focusedContainerColor = StSurface,
                    unfocusedBorderColor = StOutlineVariant,
                    focusedBorderColor = StPrimary
                ),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = StTextSecondary) }
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { viewModel.searchJobs(searchSkills, "", "", "") }, modifier = Modifier.size(56.dp).background(StSurface, RoundedCornerShape(12.dp)).border(1.dp, StOutlineVariant, RoundedCornerShape(12.dp))) {
                Icon(Icons.Default.FilterList, "Filter", tint = StOnSurface)
            }
        }
        
        var selectedFilterIndex by remember { mutableIntStateOf(0) }
        
        Spacer(Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val chips = listOf("All", "Nearby", "Remote", "Full-time")
            itemsIndexed(chips) { index, chip ->
                val isSelected = selectedFilterIndex == index
                Surface(
                    color = if (isSelected) StMatchBadgeBg else StSurface,
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, if (isSelected) StPrimary else StOutlineVariant),
                    modifier = Modifier.clickable {
                        selectedFilterIndex = index
                        val loc = if (chip == "Nearby") "Nearby" else ""
                        viewModel.searchJobs(skills = searchSkills, minExperience = "", location = loc, degree = "")
                    }
                ) {
                    Text(
                        chip, 
                        color = if (isSelected) StPrimary else StOnSurfaceVariant, 
                        fontSize = 14.sp, 
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), 
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        when (val s = jobsState) {
            is SeekerUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = StPrimary) }
            is SeekerUiState.Error -> Column(Modifier.fillMaxSize().padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Could not load jobs: ${s.message}", color = StError, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.observeJobs() }, colors = ButtonDefaults.buttonColors(containerColor = StPrimary)) {
                    Text("Retry")
                }
            }
            is SeekerUiState.Success -> {
                if (s.data.isEmpty()) {
                    Column(Modifier.fillMaxSize().padding(top = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No jobs found matching your criteria", color = StTextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = { 
                            searchSkills = ""
                            selectedFilterIndex = 0
                            viewModel.observeJobs() 
                        }) {
                            Text("Show All Jobs")
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(s.data) { job ->
                            JobCard(job = job, navController = navController)
                        }
                        item { Spacer(Modifier.height(100.dp)) }
                    }
                }
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
    LaunchedEffect(Unit) { 
        if (profileState !is SeekerUiState.Success) {
            viewModel.getProfile() 
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
        Spacer(Modifier.height(24.dp))
        
        when (val s = profileState) {
            is SeekerUiState.Success -> {
                val p = s.data.profile
                if (p != null) {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(StSurface), border = BorderStroke(1.dp, StOutlineVariant), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(Modifier.padding(24.dp)) {
                            val photoUrl = s.data.photo_url ?: p.photo_url
                            if (!photoUrl.isNullOrEmpty()) {
                                coil.compose.AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.size(80.dp).clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Box(Modifier.size(80.dp).clip(CircleShape).background(StPrimary.copy(alpha = 0.12f)), Alignment.Center) { 
                                    val initial = (s.data.full_name?.takeIf { it.isNotBlank() } ?: s.data.username ?: "U").take(1).uppercase()
                                    Text(initial, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = StPrimary)
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(s.data.full_name?.takeIf { it.isNotBlank() } ?: s.data.username ?: "User", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
                            if (!p.bio.isNullOrEmpty()) Text(p.bio, color = StTextSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                            Spacer(Modifier.height(24.dp))
                            
                            Row(Modifier.fillMaxWidth()) {
                                Column(Modifier.weight(1f)) {
                                    Text("Experience", color = StTextSecondary, fontSize = 12.sp)
                                    Text("${p.total_experience} years", color = StOnSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                }
                                Column(Modifier.weight(1f)) {
                                    Text("Location", color = StTextSecondary, fontSize = 12.sp)
                                    Text(p.location ?: "N/A", color = StOnSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            
                            if (!p.extracted_skills_json.isNullOrEmpty()) {
                                Spacer(Modifier.height(24.dp))
                                Text("Skills", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = StOnSurface)
                                Spacer(Modifier.height(12.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    p.extracted_skills_json.forEach { skill -> 
                                        Surface(color = StSurfaceContainerLow, shape = CircleShape, border = BorderStroke(1.dp, StOutlineVariant)) {
                                            Text(skill, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = StOnSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is SeekerUiState.Loading -> Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) { CircularProgressIndicator(color = StPrimary) }
            else -> {}
        }
        
        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("settings") },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(StSurface),
            border = BorderStroke(1.dp, StOutlineVariant),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(StMatchBadgeBg), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Settings, null, tint = StPrimary)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Account & Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = StOnSurface)
                        Text("Preferences, Security & Notifications", fontSize = 12.sp, color = StTextSecondary)
                    }
                }
                Icon(Icons.Default.ChevronRight, null, tint = StTextSecondary)
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.logout { navController.navigate("auth") { popUpTo(0) { inclusive = true } } } }, 
            modifier = Modifier.fillMaxWidth().height(56.dp), 
            colors = ButtonDefaults.buttonColors(StSurface), 
            border = BorderStroke(1.dp, StError),
            shape = RoundedCornerShape(25.dp)
        ) {
            Icon(Icons.Default.ExitToApp, null, tint = StError)
            Spacer(Modifier.width(12.dp))
            Text(text = "Log Out", color = StError, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(100.dp))
    }
}

package com.cs22.example.smarthire.ui.seeker

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.model.Application
import com.cs22.example.smarthire.ui.theme.*
import com.cs22.example.smarthire.viewmodel.SeekerUiState
import com.cs22.example.smarthire.viewmodel.SeekerViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppliedTrackingScreen(viewModel: SeekerViewModel, navController: NavHostController) {
    val state by viewModel.applicationsState.collectAsState()
    LaunchedEffect(Unit) { viewModel.getApplications() }
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "In Review", "Interview", "Offer")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Applications", color = StPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StBackground)
            )
        },
        containerColor = StBackground
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Tab row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = StBackground,
                contentColor = StPrimary,
                edgePadding = 16.dp,
                divider = { HorizontalDivider(color = StOutlineVariant) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium) },
                        selectedContentColor = StPrimary,
                        unselectedContentColor = StTextSecondary
                    )
                }
            }
            
            if (state is SeekerUiState.Loading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = StPrimary) }
            } else {
                val applications = (state as? SeekerUiState.Success)?.data ?: emptyList()
                val filtered = when (selectedTab) {
                    0 -> applications
                    1 -> applications.filter { it.effectiveStatus.lowercase() in listOf("screening", "reviewed", "applied") }
                    2 -> applications.filter { it.effectiveStatus.lowercase() in listOf("interview", "shortlisted") }
                    3 -> applications.filter { it.effectiveStatus.lowercase() in listOf("hired", "accepted", "offer") }
                    else -> applications
                }
                
                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FolderOpen, null, modifier = Modifier.size(64.dp), tint = StOutlineVariant)
                            Spacer(Modifier.height(16.dp))
                            Text("No applications found", color = StTextSecondary, fontSize = 16.sp)
                        }
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(filtered, key = { it.id }) { app ->
                            ApplicationCard(app, navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationCard(app: Application, navController: NavHostController) {
    val status = app.effectiveStatus.lowercase()
    val isInterview = status in listOf("interview", "shortlisted")
    val isOffer = status in listOf("hired", "accepted", "offer")
    val isRejected = status == "rejected"
    
    val badgeColor = when {
        isOffer -> StSuccess
        isInterview -> StPrimary
        isRejected -> StError
        else -> Color(0xFFF5A623) // Warning amber for review
    }
    
    val badgeText = when {
        isOffer -> "Offer Received"
        isInterview -> "Interview Scheduled"
        isRejected -> "Rejected"
        else -> "Under Review"
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { navController.navigate("chat/${app.id}") },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(StSurface),
        border = BorderStroke(1.dp, StOutlineVariant),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(StSurfaceContainer), Alignment.Center) {
                        Icon(Icons.Default.Business, null, tint = StTextSecondary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(app.job_details?.title ?: "Job #${app.job}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = StOnSurface)
                        Text(app.job_details?.company ?: "Company", fontSize = 14.sp, color = StTextSecondary)
                    }
                }
                Surface(color = badgeColor.copy(alpha = 0.1f), shape = RoundedCornerShape(25.dp)) {
                    Text(badgeText, color = badgeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
            
            Spacer(Modifier.height(12.dp))
            Text("Applied ${app.applied_at ?: "recently"}", color = StTextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigate("skill_gap/${app.id}") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, StOutlineVariant)
                ) {
                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp), tint = StPrimary)
                    Spacer(Modifier.width(6.dp))
                    Text("AI Report", fontSize = 12.sp, color = StOnSurface)
                }

                if (isInterview) {
                    Button(
                        onClick = { navController.navigate("chat/${app.id}") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(StPrimary)
                    ) {
                        Text("View Details", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else if (isOffer) {
                    Button(
                        onClick = { navController.navigate("chat/${app.id}") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(StSuccess)
                    ) {
                        Text("Review Offer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = { navController.navigate("chat/${app.id}") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(StPrimary)
                    ) {
                        Text("Messages", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

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
import com.cs22.example.smarthire.viewmodel.SeekerUiState
import com.cs22.example.smarthire.viewmodel.SeekerViewModel

enum class KanbanColumnType(val title: String, val color: Color) {
    APPLIED("Applied", Color(0xFF3B82F6)),
    SCREENING("Screening", Color(0xFFF59E0B)),
    INTERVIEW("Interview", Color(0xFF8B5CF6)),
    OFFER("Offer", Color(0xFF10B981)),
    REJECTED("Rejected", Color(0xFFEF4444))
}

fun getKanbanColumnForStatus(status: String): KanbanColumnType {
    return when (status.lowercase()) {
        "applied", "pending", "new" -> KanbanColumnType.APPLIED
        "reviewed", "screening" -> KanbanColumnType.SCREENING
        "interview", "shortlisted" -> KanbanColumnType.INTERVIEW
        "hired", "accepted", "offer" -> KanbanColumnType.OFFER
        "rejected" -> KanbanColumnType.REJECTED
        else -> KanbanColumnType.APPLIED
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppliedTrackingScreen(viewModel: SeekerViewModel, navController: NavHostController) {
    val state by viewModel.applicationsState.collectAsState()
    LaunchedEffect(Unit) { viewModel.getApplications() }

    if (state is SeekerUiState.Loading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { 
            CircularProgressIndicator(color = PremiumPrimary) 
        }
        return
    }

    val applications = if (state is SeekerUiState.Success) {
        (state as SeekerUiState.Success).data
    } else emptyList()

    val columnsMap = KanbanColumnType.values().associateWith { col ->
        applications.filter { app -> getKanbanColumnForStatus(app.effectiveStatus) == col }
    }

    Column(Modifier.fillMaxSize().padding(top = 16.dp)) {
        Column(Modifier.padding(horizontal = 24.dp)) {
            Text("Kanban Board", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PremiumText)
            Text(
                "Track your active applications and interview stages.", 
                fontSize = 14.sp, 
                color = PremiumTextMuted, 
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(KanbanColumnType.values()) { column ->
                KanbanColumnView(
                    column = column,
                    applications = columnsMap[column] ?: emptyList(),
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KanbanColumnView(column: KanbanColumnType, applications: List<Application>, navController: NavHostController, viewModel: SeekerViewModel) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(PremiumSurface)
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(column.color)
            )
            Spacer(Modifier.width(8.dp))
            Text(column.title, color = PremiumText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            
            // Pulsing badge
            val infiniteTransition = rememberInfiniteTransition()
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "Badge Alpha Animation"
            )
            
            Surface(
                color = column.color.copy(alpha = 0.15f * alpha),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, column.color.copy(alpha = 0.5f * alpha))
            ) {
                Text(
                    text = applications.size.toString(),
                    color = column.color.copy(alpha = alpha.coerceAtLeast(0.7f)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        
        if (applications.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No applications here", color = PremiumTextMuted, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(applications, key = { it.id }) { app ->
                    KanbanCard(
                        app = app,
                        columnColor = column.color,
                        modifier = Modifier.animateItem(),
                        navController = navController,
                        onWithdraw = { viewModel.withdrawApplication(app.id.toString()) }
                    )
                }
            }
        }
    }
}

@Composable
fun KanbanCard(app: Application, columnColor: Color, modifier: Modifier = Modifier, navController: NavHostController, onWithdraw: () -> Unit) {
    val companyName = app.job_details?.company ?: "Company"
    val jobTitle = app.job_details?.title ?: "Job #${app.job}"
    val initial = companyName.firstOrNull()?.uppercaseChar()?.toString() ?: "C"
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { navController.navigate("chat/${app.id}") },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(PremiumSurfaceContainer),
        border = BorderStroke(1.dp, Color.White.copy(alpha=0.05f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(columnColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initial, color = columnColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(jobTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PremiumText, maxLines = 1)
                    Spacer(Modifier.height(4.dp))
                    Text(companyName, color = PremiumTextMuted, fontSize = 12.sp, maxLines = 1)
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(app.applied_at ?: "Just now", color = PremiumTextMuted, fontSize = 10.sp)
                
                app.effectiveMatchScore?.let { score ->
                    val scoreColor = when {
                        score >= 80 -> Color(0xFF10B981)
                        score >= 60 -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }
                    Surface(
                        color = scoreColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "$score% Match",
                            color = scoreColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onWithdraw, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Withdraw Application", tint = PremiumTextMuted)
                }
            }
        }
    }
}

@Composable
fun ApplicationPipelineCard(app: Application, navController: NavHostController) {
    val status = app.effectiveStatus.lowercase()
    val statusLevel = when (status) {
        "applied", "pending" -> 1
        "reviewed" -> 2
        "interview", "shortlisted" -> 3
        "hired", "accepted" -> 4
        "rejected" -> -1
        else -> 1
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(28.dp), 
        colors = CardDefaults.cardColors(PremiumSurface), 
        border = BorderStroke(1.dp, Color.White.copy(alpha=0.05f))
    ) {
        Column(Modifier.padding(24.dp)) {
            // Header
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(PremiumSurfaceContainer), Alignment.Center) {
                        Icon(Icons.Default.Business, null, tint = PremiumTextMuted)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(app.job_details?.title ?: "Job #${app.job}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PremiumText)
                        Spacer(Modifier.height(4.dp))
                        Text(app.job_details?.company ?: "Company Name", color = PremiumTextMuted, fontSize = 14.sp)
                    }
                }
                
                val statusColor = when (statusLevel) { -1 -> Color(0xFFEF4444); 4 -> Color(0xFF10B981); else -> PremiumPrimary }
                Surface(color = statusColor.copy(0.15f), shape = RoundedCornerShape(8.dp)) { 
                    Text(text = app.effectiveStatus.uppercase(), color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) 
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Visual Pipeline
            if (statusLevel != -1) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    PipelineStep(label = "Applied", isCompleted = statusLevel >= 1, isActive = statusLevel == 1)
                    PipelineDivider(isCompleted = statusLevel >= 2, isActive = statusLevel == 1, modifier = Modifier.weight(1f))
                    PipelineStep(label = "Reviewed", isCompleted = statusLevel >= 2, isActive = statusLevel == 2)
                    PipelineDivider(isCompleted = statusLevel >= 3, isActive = statusLevel == 2, modifier = Modifier.weight(1f))
                    PipelineStep(label = "Interview", isCompleted = statusLevel >= 3, isActive = statusLevel == 3)
                    PipelineDivider(isCompleted = statusLevel >= 4, isActive = statusLevel == 3, modifier = Modifier.weight(1f))
                    PipelineStep(label = "Offer", isCompleted = statusLevel >= 4, isActive = statusLevel == 4)
                }
            } else {
                Text("Application Rejected", color = Color(0xFFEF4444), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (statusLevel >= 3) {
                    Button(
                        onClick = { /* Join call logic */ },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(PremiumPrimary)
                    ) {
                        Icon(Icons.Default.Videocam, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Join Call", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = { navController.navigate("chat/${app.id}") },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PremiumText),
                        border = BorderStroke(1.dp, Color.White.copy(alpha=0.1f))
                    ) {
                        Icon(Icons.Default.ChatBubble, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Open Chat", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PipelineStep(label: String, isCompleted: Boolean, isActive: Boolean) {
    val color = if (isCompleted) PremiumPrimary else if (isActive) PremiumPrimary else PremiumSurfaceContainer
    val iconColor = if (isCompleted) Color.White else PremiumTextMuted
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
        Box(
            modifier = Modifier.size(24.dp).clip(CircleShape).background(color).border(2.dp, if (isActive && !isCompleted) PremiumPrimary.copy(alpha=0.5f) else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(Icons.Default.Check, null, tint = iconColor, modifier = Modifier.size(14.dp))
            } else {
                Box(Modifier.size(8.dp).clip(CircleShape).background(if (isActive) PremiumPrimary else PremiumTextMuted.copy(alpha=0.5f)))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 10.sp, color = if (isCompleted || isActive) PremiumText else PremiumTextMuted, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun PipelineDivider(isCompleted: Boolean, isActive: Boolean, modifier: Modifier) {
    val color = if (isCompleted) PremiumPrimary else PremiumSurfaceContainer
    Box(modifier = modifier.height(2.dp).padding(horizontal = 4.dp).background(color, RoundedCornerShape(1.dp)).offset(y = (-10).dp))
}

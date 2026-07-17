package com.cs22.example.smarthire.ui.seeker

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun AppliedTrackingScreen(viewModel: SeekerViewModel, navController: NavHostController) {
    val state by viewModel.applicationsState.collectAsState()
    LaunchedEffect(Unit) { viewModel.getApplications() }
    
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 16.dp)) {
        Text("Application Status", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PremiumText)
        Text("Track your active applications and interview stages.", fontSize = 14.sp, color = PremiumTextMuted, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
        
        when (val s = state) {
            is SeekerUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = PremiumPrimary) }
            is SeekerUiState.Error -> Text(s.message, color = Color.Red)
            is SeekerUiState.Success -> {
                if (s.data.isEmpty()) Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No applications yet. Start searching!", color = PremiumTextMuted) }
                else LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    items(s.data) { app ->
                        ApplicationPipelineCard(app, navController)
                    }
                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
            else -> {}
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

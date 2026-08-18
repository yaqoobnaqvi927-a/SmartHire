package com.cs22.example.smarthire.ui.recruiter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cs22.example.smarthire.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateSkillAnalysisScreen(
    navController: NavController, 
    candidateId: String?,
    viewModel: com.cs22.example.smarthire.viewmodel.RecruiterViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skill Analysis", color = StOnSurface, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = StOnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StBackground)
            )
        },
        containerColor = StBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Match score circle
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                CircularProgressIndicator(
                    progress = { 0.85f },
                    modifier = Modifier.fillMaxSize(),
                    color = StPrimary,
                    strokeWidth = 10.dp,
                    trackColor = StSurfaceContainer
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("85%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = StPrimary)
                    Text("Strong Fit", fontSize = 12.sp, color = StTextSecondary)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Skills Breakdown Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StSurface),
                border = BorderStroke(1.dp, StOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SkillRow("Kotlin", StSuccess, Icons.Default.CheckCircle, "Expert match")
                    SkillRow("Jetpack Compose", StSuccess, Icons.Default.CheckCircle, "Expert match")
                    SkillRow("CI/CD", StWarning, Icons.Default.Warning, "2 years but 3 required")
                    SkillRow("AWS", StError, Icons.Default.Cancel, "Missing skill")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // AI Recommendation
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F5FF)),
                border = BorderStroke(1.dp, StOutlineVariant)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Lightbulb, contentDescription = "AI", tint = StPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("AI Recommendation", color = StPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Strong candidate. While lacking AWS experience, their deep knowledge in Kotlin and Compose makes them an excellent fit for the frontend role.", color = StOnSurface, fontSize = 14.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = { 
                        candidateId?.let { id ->
                            viewModel.updateApplicationStatus(id, "rejected")
                        }
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StError),
                    border = BorderStroke(1.dp, StError)
                ) {
                    Text("Reject", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { 
                        navController.navigate("recruiter_interviews")
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StPrimary)
                ) {
                    Text("Schedule", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SkillRow(name: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(name, fontWeight = FontWeight.SemiBold, color = StOnSurface, fontSize = 16.sp)
            Text(desc, color = StTextSecondary, fontSize = 12.sp)
        }
    }
}

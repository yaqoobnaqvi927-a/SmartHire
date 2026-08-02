package com.cs22.example.smarthire.ui.recruiter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cs22.example.smarthire.ui.theme.PremiumBg
import com.cs22.example.smarthire.ui.theme.PremiumPrimary
import com.cs22.example.smarthire.ui.theme.PremiumSurface
import com.cs22.example.smarthire.ui.theme.SmartHireOnSurface
import com.cs22.example.smarthire.ui.theme.SmartHireOnSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateSkillAnalysisScreen(navController: NavController, candidateId: String?) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skill Analysis", color = SmartHireOnSurface, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PremiumPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PremiumBg
                )
            )
        },
        containerColor = PremiumBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Detailed AI Analysis for Candidate: $candidateId",
                color = SmartHireOnSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = PremiumSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Strengths", color = PremiumPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Strong proficiency in modern frameworks.\n• Excellent problem-solving skills.", color = SmartHireOnSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Areas for Improvement", color = PremiumPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Needs more experience with cloud deployment.\n• Could improve algorithmic optimization.", color = SmartHireOnSurfaceVariant)
                }
            }
        }
    }
}

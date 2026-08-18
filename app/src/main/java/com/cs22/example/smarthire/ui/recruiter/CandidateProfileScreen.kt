package com.cs22.example.smarthire.ui.recruiter

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cs22.example.smarthire.ui.theme.*
import com.cs22.example.smarthire.viewmodel.RecruiterUiState
import com.cs22.example.smarthire.viewmodel.RecruiterViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CandidateProfileScreen(
    navController: NavController,
    applicationId: String,
    viewModel: RecruiterViewModel
) {
    val applicationsState by viewModel.applicationsState.collectAsState()
    val applications = (applicationsState as? RecruiterUiState.Success)?.data ?: emptyList()
    val application = applications.find { it.id == applicationId }
    val candidate = application?.effectiveCandidate

    if (candidate == null) {
        Box(modifier = Modifier.fillMaxSize().background(StBackground), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = StPrimary)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Candidate Profile", color = StOnSurface, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = StOnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StBackground)
            )
        },
        containerColor = StBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StSurface),
                border = BorderStroke(1.dp, StOutlineVariant),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Surface(
                            shape = RoundedCornerShape(25.dp),
                            color = StMatchBadgeBg
                        ) {
                            Text(
                                "⚡ ${candidate.match_percentage}% Match", 
                                color = StPrimary, 
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    val initials = "${candidate.user?.first_name?.firstOrNull() ?: ""}${candidate.user?.last_name?.firstOrNull() ?: ""}".uppercase()
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(StMatchBadgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = initials, color = StPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "${candidate.user?.first_name ?: ""} ${candidate.user?.last_name ?: ""}".trim(),
                        color = StOnSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = candidate.bio ?: "No bio provided", color = StOnSurfaceVariant, fontSize = 14.sp, textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoChip(Icons.Default.LocationOn, candidate.location ?: "N/A")
                        InfoChip(Icons.Default.Star, "${candidate.experience_years ?: 0} yrs exp")
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { navController.navigate("chat/${applicationId}") },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(25.dp),
                            border = BorderStroke(1.dp, StPrimary)
                        ) {
                            Text("Message", color = StPrimary, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { navController.navigate("recruiter_interviews") },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StPrimary)
                        ) {
                            Text("Schedule Interview", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Skills Card
            if (!candidate.extracted_skills_json.isNullOrEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = StSurface),
                    border = BorderStroke(1.dp, StOutlineVariant)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Top Skills", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = StOnSurface)
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            candidate.extracted_skills_json.forEach { skill ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = StMatchBadgeBg
                                ) {
                                    Text(text = skill, color = StPrimary, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Experience Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StSurface),
                border = BorderStroke(1.dp, StOutlineVariant)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Experience & Education", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = StOnSurface)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.Top) {
                        Box(Modifier.background(StMatchBadgeBg, CircleShape).padding(8.dp)) {
                            Icon(Icons.Default.School, null, tint = StPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(candidate.degree ?: "Degree", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StOnSurface)
                            Text(candidate.university ?: "University", fontSize = 14.sp, color = StOnSurfaceVariant)
                        }
                    }
                }
            }

            // AI Analysis Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StSurface),
                border = BorderStroke(1.dp, StOutlineVariant)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = StPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Gemini Analysis", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = StOnSurface)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Highly recommended candidate. Shows strong alignment with required skills and experience level. Technical background matches the role profile.", fontSize = 14.sp, color = StOnSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Strengths", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StOnSurface)
                    Text("• Matching degree requirement\n• Core skills present", fontSize = 14.sp, color = StOnSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = StSurfaceContainer
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = StOnSurfaceVariant, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, color = StOnSurfaceVariant, fontSize = 12.sp)
        }
    }
}

package com.cs22.example.smarthire.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cs22.example.smarthire.model.InterviewResponse
import com.cs22.example.smarthire.viewmodel.RecruiterUiState
import com.cs22.example.smarthire.viewmodel.RecruiterViewModel
import com.cs22.example.smarthire.viewmodel.SeekerUiState
import com.cs22.example.smarthire.viewmodel.SeekerViewModel

private val PremiumBg = Color(0xFF0F131D)
private val PremiumSurface = Color(0xFF161B28)
private val PremiumSurfaceContainer = Color(0xFF1D2433)
private val PremiumPrimary = Color(0xFF3B82F6)
private val PremiumSecondary = Color(0xFF8B5CF6)
private val PremiumText = Color(0xFFE1E2E4)
private val PremiumTextMuted = Color(0xFFC2C6D6)

@Composable
fun SeekerInterviewScreen(navController: NavController, viewModel: SeekerViewModel) {
    val interviewsState by viewModel.interviewsState.collectAsState()
    val interviews = (interviewsState as? SeekerUiState.Success)?.data ?: emptyList()
    LaunchedEffect(Unit) { viewModel.observeInterviews() }
    InterviewScheduleScreen(navController = navController, interviews = interviews, onDelete = { viewModel.deleteInterview(it) })
}

@Composable
fun RecruiterInterviewScreen(navController: NavController, viewModel: RecruiterViewModel) {
    val applicationsState by viewModel.applicationsState.collectAsState()
    val applications = (applicationsState as? RecruiterUiState.Success)?.data ?: emptyList()
    // Derive interviews from applications in interview/shortlisted status
    val interviews = applications
        .filter { it.effectiveStatus.lowercase() in listOf("interview", "shortlisted", "scheduled") }
        .map { app ->
            InterviewResponse(
                id = app.id,
                application = app.id,
                scheduled_at = app.applied_at ?: "",
                status = "scheduled",
                job_title = app.job_details?.title,
                company = app.job_details?.company,
                candidate_name = app.effectiveCandidate?.user?.full_name
            )
        }
    InterviewScheduleScreen(navController = navController, interviews = interviews, onDelete = { viewModel.deleteInterview(it) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewScheduleScreen(
    navController: NavController,
    interviews: List<InterviewResponse>,
    onDelete: (String) -> Unit
) {
    val upcoming = interviews.filter { it.status == "scheduled" }
    val past = interviews.filter { it.status != "scheduled" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interviews", color = PremiumText) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumBg)
            )
        },
        containerColor = PremiumBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Upcoming", color = PremiumText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (upcoming.isEmpty()) {
                    Text("No upcoming interviews.", color = PremiumTextMuted, fontStyle = FontStyle.Italic)
                }
            }

            items(upcoming) { interview ->
                InterviewCard(navController = navController, interview = interview, isUpcoming = true, onDelete = { interview.id?.let { onDelete(it) } })
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Past", color = PremiumText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (past.isEmpty()) {
                    Text("No past interviews.", color = PremiumTextMuted, fontStyle = FontStyle.Italic)
                }
            }

            items(past) { interview ->
                InterviewCard(navController = navController, interview = interview, isUpcoming = false, onDelete = { interview.id?.let { onDelete(it) } })
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun InterviewCard(navController: NavController, interview: InterviewResponse, isUpcoming: Boolean, onDelete: () -> Unit = {}) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val modifier = if (isUpcoming) {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PremiumSurface)
            .border(1.5.dp, PremiumPrimary.copy(alpha = pulseAlpha), RoundedCornerShape(12.dp))
            .padding(16.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PremiumSurface.copy(alpha = 0.6f))
            .padding(16.dp)
            .alpha(0.7f)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = interview.company ?: "Company",
                    color = PremiumText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = interview.job_title ?: "Role",
                    color = PremiumTextMuted,
                    fontSize = 14.sp
                )
            }
            if (!isUpcoming) {
                Icon(
                    imageVector = Icons.Default.CheckCircle, 
                    contentDescription = "Completed", 
                    tint = Color(0xFF10B981), 
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(PremiumSurfaceContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Scheduled", color = PremiumPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = PremiumTextMuted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = interview.scheduled_at?.take(16)?.replace("T", " ") ?: "Time TBD",
                    color = PremiumTextMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PremiumSurfaceContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("${interview.duration_minutes ?: 30} min", color = PremiumTextMuted, fontSize = 10.sp)
                }
            }

            if (isUpcoming) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel Interview", tint = Color(0xFFEF4444))
                    }
                    Button(
                        onClick = { navController.navigate("video_call") },
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Join Call", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Join Call", fontSize = 12.sp)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Candidate: ${interview.candidate_name ?: "Unknown"}",
            color = PremiumTextMuted,
            fontSize = 12.sp
        )
    }
}

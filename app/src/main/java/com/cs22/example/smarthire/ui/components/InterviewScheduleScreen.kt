package com.cs22.example.smarthire.ui.components

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cs22.example.smarthire.model.InterviewResponse
import com.cs22.example.smarthire.ui.theme.*
import com.cs22.example.smarthire.viewmodel.RecruiterUiState
import com.cs22.example.smarthire.viewmodel.RecruiterViewModel
import com.cs22.example.smarthire.viewmodel.SeekerUiState
import com.cs22.example.smarthire.viewmodel.SeekerViewModel

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
    var selectedDay by remember { mutableStateOf("Mon") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interview Schedule", color = StOnSurface, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = StOnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StBackground)
            )
        },
        containerColor = StBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                        val isActive = day == selectedDay
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedDay = day }
                        ) {
                            Text(day, fontSize = 12.sp, color = if (isActive) StPrimary else StTextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) StPrimary else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("12", color = if (isActive) Color.White else StOnSurface, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            
            item {
                Text("Available Slots", color = StOnSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SlotCard(time = "09:00 AM", duration = "30 min", isSelected = true)
                    SlotCard(time = "10:30 AM", duration = "45 min", isSelected = false)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SlotCard(time = "02:00 PM", duration = "30 min", isSelected = false)
                    SlotCard(time = "04:00 PM", duration = "60 min", isSelected = false)
                }
            }
            
            item {
                Text("Booked Interviews", color = StOnSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            items(upcoming) { interview ->
                InterviewTimelineCard(navController = navController, interview = interview)
            }
            
            item {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StPrimary)
                ) {
                    Text("Schedule New Interview", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SlotCard(time: String, duration: String, isSelected: Boolean) {
    Card(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) StMatchBadgeBg else StSurface),
        border = BorderStroke(1.dp, if (isSelected) StPrimary else StOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(time, color = if (isSelected) StPrimary else StOnSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(duration, color = if (isSelected) StPrimary.copy(alpha = 0.8f) else StTextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun InterviewTimelineCard(navController: NavController, interview: InterviewResponse) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
            Text("10:00", fontWeight = FontWeight.Bold, color = StOnSurface, fontSize = 14.sp)
            Text("AM", color = StTextSecondary, fontSize = 12.sp)
            Box(modifier = Modifier.width(2.dp).height(80.dp).background(StOutlineVariant).padding(vertical = 8.dp))
        }
        
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = StSurface),
            border = BorderStroke(1.dp, StOutlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(StMatchBadgeBg), contentAlignment = Alignment.Center) {
                        Text(interview.candidate_name?.firstOrNull()?.toString() ?: "C", color = StPrimary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(interview.candidate_name ?: "Candidate Name", fontWeight = FontWeight.Bold, color = StOnSurface, fontSize = 16.sp)
                        Text(interview.job_title ?: "Role", color = StTextSecondary, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { navController.navigate("video_call") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, StPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StPrimary)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Join Video Call")
                }
            }
        }
    }
}

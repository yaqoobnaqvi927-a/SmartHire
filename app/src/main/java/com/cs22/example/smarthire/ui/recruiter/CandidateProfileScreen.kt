package com.cs22.example.smarthire.ui.recruiter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cs22.example.smarthire.viewmodel.RecruiterUiState
import com.cs22.example.smarthire.viewmodel.RecruiterViewModel
import kotlinx.coroutines.delay

private val PremiumBg = Color(0xFF0F131D)
private val PremiumSurface = Color(0xFF161B28)
private val PremiumSurfaceContainer = Color(0xFF1D2433)
private val PremiumPrimary = Color(0xFF3B82F6)
private val PremiumSecondary = Color(0xFF8B5CF6)
private val PremiumText = Color(0xFFE1E2E4)
private val PremiumTextMuted = Color(0xFFC2C6D6)

@OptIn(ExperimentalMaterial3Api::class)
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
        Box(modifier = Modifier.fillMaxSize().background(PremiumBg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PremiumPrimary)
        }
        return
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Candidate Profile", color = PremiumText) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PremiumText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PremiumBg
                )
            )
        },
        containerColor = PremiumBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Hero Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(PremiumPrimary, PremiumSecondary)
                        )
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                val firstName = candidate.user?.first_name ?: ""
                val lastName = candidate.user?.last_name ?: ""
                val initials = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}"
                
                Box(
                    modifier = Modifier
                        .offset(y = 40.dp)
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(PremiumSurfaceContainer)
                        .border(4.dp, PremiumBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials.uppercase(),
                        color = PremiumText,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(50.dp))

            // Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${candidate.user?.first_name ?: ""} ${candidate.user?.last_name ?: ""}".trim(),
                    color = PremiumText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${candidate.degree ?: "Degree"} at ${candidate.university ?: "University"}",
                    color = PremiumTextMuted,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { navController.navigate("chat/${applicationId}") },
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumPrimary)
                    ) {
                        Text("💬 Chat")
                    }
                    Button(
                        onClick = { navController.navigate("recruiter_interviews") },
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumSurfaceContainer)
                    ) {
                        Text("📅 Interviews", color = PremiumText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Match Score & Bio
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Match Score
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = PremiumSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("AI Match", color = PremiumTextMuted, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val targetScore = candidate.match_percentage.toFloat() / 100f
                        var animationPlayed by remember { mutableStateOf(false) }
                        val currentScore by animateFloatAsState(
                            targetValue = if (animationPlayed) targetScore else 0f,
                            animationSpec = tween(1500, easing = FastOutSlowInEasing)
                        )

                        LaunchedEffect(Unit) {
                            animationPlayed = true
                        }

                        Box(
                            modifier = Modifier.size(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(80.dp)) {
                                drawArc(
                                    color = PremiumSurfaceContainer,
                                    startAngle = 140f,
                                    sweepAngle = 260f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    brush = Brush.linearGradient(listOf(PremiumPrimary, PremiumSecondary)),
                                    startAngle = 140f,
                                    sweepAngle = 260f * currentScore,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Text(
                                text = "${(currentScore * 100).toInt()}%",
                                color = PremiumText,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Quick Info
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = PremiumSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InfoRow(icon = Icons.Default.LocationOn, text = candidate.location ?: "N/A")
                        InfoRow(icon = Icons.Default.Star, text = "${candidate.experience_years ?: 0} yrs exp")
                        InfoRow(icon = Icons.Default.Email, text = candidate.user?.email ?: "N/A")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bio
            if (!candidate.bio.isNullOrBlank()) {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text("Bio", color = PremiumText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = candidate.bio ?: "",
                        color = PremiumTextMuted,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Skills
            if (!candidate.extracted_skills_json.isNullOrEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text("Skills", color = PremiumText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        candidate.extracted_skills_json.forEachIndexed { index, skill ->
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(index * 100L)
                                visible = true
                            }
                            
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn() + scaleIn()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(PremiumSurfaceContainer)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(text = skill, color = PremiumText, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = PremiumPrimary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = PremiumTextMuted, fontSize = 12.sp, maxLines = 1)
    }
}

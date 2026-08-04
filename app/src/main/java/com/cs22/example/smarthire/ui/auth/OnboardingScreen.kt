package com.cs22.example.smarthire.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

data class OnboardingPageData(
    val stepText: String,
    val progress: Float,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val secondaryIcon: ImageVector? = null,
    val badgeLabel: String? = null
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(navController: NavHostController) {
    var currentPage by remember { mutableIntStateOf(0) }

    val pages = listOf(
        OnboardingPageData(
            stepText = "1/3",
            progress = 0.33f,
            title = "AI-Powered\nCV Banking",
            description = "Automated, offline CV text and skill extraction using spaCy NLP algorithms.",
            icon = Icons.Default.Description,
            secondaryIcon = Icons.Default.AutoAwesome
        ),
        OnboardingPageData(
            stepText = "2/3",
            progress = 0.66f,
            title = "Smart Job Matching\n85% Accuracy",
            description = "Cosine similarity algorithm calculating real-time candidate match scores.",
            icon = Icons.Default.Settings,
            secondaryIcon = Icons.Default.Analytics,
            badgeLabel = "85%"
        ),
        OnboardingPageData(
            stepText = "3/3",
            progress = 1.0f,
            title = "Video Interviews &\nProgress Tracking",
            description = "In-app video interviews and real-time application pipeline tracking.",
            icon = Icons.Default.Videocam,
            secondaryIcon = Icons.Default.Work
        )
    )

    val currentData = pages[currentPage]

    fun completeOnboarding() {
        navController.navigate("role_selection") {
            popUpTo("onboarding") { inclusive = true }
        }
    }

    val primaryBlue = Color(0xFF3B82F6)
    val lightBg = Color(0xFFF3F6FC)
    val cardBg = Color.White
    val textDark = Color(0xFF1E293B)
    val textMuted = Color(0xFF64748B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Title
            Spacer(modifier = Modifier.height(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SmartHire",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textDark
                )
                Text(
                    text = "Powered For Quest Nawabshah",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = textMuted
                )
            }

            // Main Card Container (matching screenshot layout)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 24.dp)
                    .shadow(16.dp, shape = RoundedCornerShape(32.dp), spotColor = Color.Black.copy(0.08f)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } + fadeIn() with
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            slideInHorizontally { width -> -width } + fadeIn() with
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
                    val page = pages[pageIndex]
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        // Top Illustration Graphic
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .background(primaryBlue.copy(alpha = 0.08f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .background(primaryBlue.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = page.icon,
                                    contentDescription = null,
                                    tint = primaryBlue,
                                    modifier = Modifier.size(56.dp)
                                )

                                page.badgeLabel?.let { badge ->
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(48.dp)
                                            .background(primaryBlue, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = badge,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Title & Description
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = page.title,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark,
                                textAlign = TextAlign.Center,
                                lineHeight = 32.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = page.description,
                                fontSize = 14.sp,
                                color = textMuted,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }

                        // Circular Progress Indicator Badge (1/3, 2/3, 3/3)
                        Box(
                            modifier = Modifier.size(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = primaryBlue.copy(alpha = 0.15f),
                                    style = Stroke(width = 4.dp.toPx())
                                )
                                drawArc(
                                    color = primaryBlue,
                                    startAngle = -90f,
                                    sweepAngle = 360f * page.progress,
                                    useCenter = false,
                                    style = Stroke(width = 4.dp.toPx())
                                )
                            }
                            Text(
                                text = page.stepText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Row (Next / Skip / Get Started)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentPage < pages.size - 1) {
                        Text(
                            text = "Skip",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = primaryBlue,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { completeOnboarding() }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    Button(
                        onClick = {
                            if (currentPage < pages.size - 1) {
                                currentPage++
                            } else {
                                completeOnboarding()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                    ) {
                        Text(
                            text = if (currentPage == pages.size - 1) "Get Started" else "Next",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (currentPage < pages.size - 1) {
                        Text(
                            text = "Skip",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = primaryBlue,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { completeOnboarding() }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

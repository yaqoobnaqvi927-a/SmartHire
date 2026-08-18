package com.cs22.example.smarthire.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector
)

val StBgOnboarding = Color(0xFFF3F7FF)
val StPrimaryOnboarding = Color(0xFF0057C0)
val StSurfaceOnboarding = Color(0xFFFFFFFF)
val StSurfaceContainerOnboarding = Color(0xFFECEDF7)
val StTextSecondaryOnboarding = Color(0xFF68738A)
val StOnSurfaceOnboarding = Color(0xFF191B22)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(navController: NavHostController) {
    var currentPage by remember { mutableIntStateOf(0) }

    val pages = listOf(
        OnboardingPageData(
            title = "Your Next Opportunity Starts Here",
            description = "Discover thousands of jobs perfectly matched to your skills and experience.",
            icon = Icons.Default.Work
        ),
        OnboardingPageData(
            title = "Smart Matching, Better Opportunities",
            description = "Our AI intelligently analyzes your profile to find your best cultural and skill fit.",
            icon = Icons.Default.AutoAwesome
        ),
        OnboardingPageData(
            title = "Verified. Professional. Hired.",
            description = "Join a verified professional network and fast-track your career progression.",
            icon = Icons.Default.CheckCircle
        )
    )

    fun completeOnboarding() {
        navController.navigate("role_selection") {
            popUpTo("onboarding") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StBgOnboarding)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Illustrations & Content
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
                modifier = Modifier.weight(1f),
                label = "onboarding_animation"
            ) { pageIndex ->
                val page = pages[pageIndex]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(320.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(StSurfaceOnboarding),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = null,
                            tint = StPrimaryOnboarding,
                            modifier = Modifier.size(120.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Text(
                        text = page.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = StOnSurfaceOnboarding,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = page.description,
                        fontSize = 14.sp,
                        color = StTextSecondaryOnboarding,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Bottom Navigation
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator
                Row(
                    modifier = Modifier.padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pages.indices.forEach { index ->
                        val isSelected = currentPage == index
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (isSelected) 24.dp else 8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) StPrimaryOnboarding else StSurfaceContainerOnboarding)
                        )
                    }
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
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StPrimaryOnboarding)
                ) {
                    Text(
                        text = if (currentPage == pages.size - 1) "Get Started" else "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (currentPage < pages.size - 1) {
                    Text(
                        text = "Skip",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = StTextSecondaryOnboarding,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { completeOnboarding() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(36.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

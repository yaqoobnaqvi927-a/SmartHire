package com.cs22.example.smarthire.ui.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

val PremiumBg = Color(0xFF0F131D)
val PremiumPrimary = Color(0xFF3B82F6)
val PremiumSecondary = Color(0xFF8B5CF6)
val PremiumText = Color(0xFFE1E2E4)
val PremiumTextMuted = Color(0xFFC2C6D6)

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val gradientColors: List<Color>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Default.AutoAwesome,
            title = "AI-Powered Matching",
            subtitle = "SmartHire analyzes your skills and matches you with the perfect opportunities automatically.",
            gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF60A5FA)) // Blue
        ),
        OnboardingPage(
            icon = Icons.Default.Description,
            title = "Instant Cover Letters",
            subtitle = "Generate personalized, professional cover letters for any job in seconds with AI.",
            gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA)) // Purple
        ),
        OnboardingPage(
            icon = Icons.Default.Videocam,
            title = "Built-In Video Interviews",
            subtitle = "Schedule and join interviews directly in the app—no third-party tools needed.",
            gradientColors = listOf(Color(0xFF10B981), Color(0xFF34D399)) // Green
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumBg)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { pageIndex ->
            OnboardingSlide(page = pages[pageIndex])
        }

        // Indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            pages.forEachIndexed { index, _ ->
                val width by animateDpAsState(
                    targetValue = if (pagerState.currentPage == index) 24.dp else 8.dp,
                    animationSpec = tween(300),
                    label = "indicatorWidth"
                )
                val color by animateColorAsState(
                    targetValue = if (pagerState.currentPage == index) PremiumPrimary else PremiumTextMuted.copy(alpha = 0.3f),
                    animationSpec = tween(300),
                    label = "indicatorColor"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        // Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.lastIndex) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        navController.navigate("role_selection") {
                            popUpTo(0)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(listOf(PremiumPrimary, PremiumSecondary))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.lastIndex) "Get Started" else "Next",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    navController.navigate("role_selection") {
                        popUpTo(0)
                    }
                }
            ) {
                Text(
                    text = "Skip",
                    color = PremiumTextMuted,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun OnboardingSlide(page: OnboardingPage) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(scale)
                .background(
                    brush = Brush.linearGradient(page.gradientColors),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.subtitle,
            fontSize = 16.sp,
            color = PremiumTextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

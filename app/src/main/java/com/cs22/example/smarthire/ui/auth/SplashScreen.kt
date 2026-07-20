package com.cs22.example.smarthire.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

import androidx.navigation.NavHostController
import com.cs22.example.smarthire.viewmodel.AuthViewModel

@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
    navController: NavHostController
) {
    // Pulse animation for text glow
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glowAlpha"
    )

    // Moving line animation
    val lineOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "lineOffset"
    )

    val authState by viewModel.uiState.collectAsState()

    LaunchedEffect(authState.isLoggedIn, authState.userRole) {
        delay(2500) // Show splash for 2.5 seconds
        if (authState.isLoggedIn && authState.userRole != null) {
            val role = authState.userRole
            val setupComplete = authState.setupComplete
            if (role == "recruiter") {
                if (setupComplete) navController.navigate("recruiter_flow") { popUpTo("splash") { inclusive = true } }
                else navController.navigate("recruiter_setup") { popUpTo("splash") { inclusive = true } }
            } else {
                if (setupComplete) navController.navigate("job_seeker_flow") { popUpTo("splash") { inclusive = true } }
                else navController.navigate("seeker_setup") { popUpTo("splash") { inclusive = true } }
            }
        } else if (!authState.isLoading) {
            navController.navigate("role_selection") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19)), // Background level 0
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                // Logo Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // App Name with fake text shadow/glow via drawWithContent
                Text(
                    text = "SmartHire",
                    color = Color(0xFFADC6FF), // Primary color
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.drawWithContent {
                        drawContent() // Native text drawing (we can add complex shadow logic later if needed)
                    }
                )
            }

            // Animated Loading Line
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.DarkGray.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .offset(x = (lineOffset * 100).dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF3B82F6), Color.Transparent)
                            )
                        )
                )
            }
        }
    }
}

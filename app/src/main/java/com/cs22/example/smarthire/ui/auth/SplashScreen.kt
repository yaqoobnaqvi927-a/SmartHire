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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
    navController: NavHostController
) {
    val authState by viewModel.uiState.collectAsState()
    val infiniteTransition = rememberInfiniteTransition()

    // Pulse Animation for text
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Logo Entrance Animation
    var logoVisible by remember { mutableStateOf(false) }
    val logoScale by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.5f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing)
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0f,
        animationSpec = tween(1000)
    )

    // Progress Bar Animation
    var progressFill by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progressFill,
        animationSpec = tween(2000, easing = LinearEasing)
    )

    LaunchedEffect(Unit) {
        logoVisible = true
        progressFill = 1f
    }

    LaunchedEffect(authState.isLoggedIn, authState.userRole) {
        delay(2500) // Premium 2.5 second delay
        
        if (authState.isLoggedIn && authState.userRole != null) {
            val role = authState.userRole
            val setupComplete = authState.setupComplete
            
            if (role == "recruiter") {
                if (setupComplete) navController.navigate("recruiter_flow") { popUpTo("splash") { inclusive = true } }
                else navController.navigate("profile_setup") { popUpTo("splash") { inclusive = true } }
            } else {
                if (setupComplete) navController.navigate("job_seeker_flow") { popUpTo("splash") { inclusive = true } }
                else navController.navigate("profile_setup") { popUpTo("splash") { inclusive = true } }
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
            .background(Color(0xFF0B0F19)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .scale(logoScale)
                    .padding(bottom = 48.dp)
            ) {
                // Gradient Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Work, contentDescription = "Logo", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Pulsing Text
                Text(
                    text = "SmartHire",
                    color = Color(0xFFADC6FF).copy(alpha = logoAlpha),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.scale(pulseScale)
                )
            }

            // Animated Progress Track
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .background(Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))))
                )
            }
        }
    }
}

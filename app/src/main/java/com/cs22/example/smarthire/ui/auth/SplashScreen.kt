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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.ui.theme.*
import com.cs22.example.smarthire.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
    navController: NavHostController
) {
    val authState by viewModel.uiState.collectAsState()
    
    // Progress Bar Animation
    var progressFill by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progressFill,
        animationSpec = tween(2500, easing = LinearEasing),
        label = "ProgressAnimation"
    )

    LaunchedEffect(Unit) {
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
            navController.navigate("onboarding") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x1A000000))
                    .clip(RoundedCornerShape(16.dp))
                    .background(StSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Work,
                    contentDescription = "Logo",
                    tint = StPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Smart Hire",
                color = StPrimary,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Connect. Match. Get Hired.",
                color = StTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(64.dp))

            // Loading Bar
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(StSurfaceContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .background(StPrimary)
                )
            }
        }
    }
}

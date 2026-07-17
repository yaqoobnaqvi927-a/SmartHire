package com.cs22.example.smarthire.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Domain
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs22.example.smarthire.ui.theme.*

@Composable
fun RoleSelectionScreen(
    onJobSeekerSelected: () -> Unit,
    onRecruiterSelected: () -> Unit
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111415)),
        contentAlignment = Alignment.TopCenter
    ) {
        // Background Glow
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(400.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.1f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "How do you want to use ",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE1E2E4),
                textAlign = TextAlign.Center
            )
            Text(
                text = "SmartHire?",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFADC6FF),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Select your primary goal to tailor your experience.",
                fontSize = 18.sp,
                color = Color(0xFFC2C6D6),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                RoleSelectionCard(
                    title = "Job Seeker",
                    subtitle = "I'm looking for a job",
                    icon = Icons.Default.Work,
                    isSelected = selectedRole == "seeker",
                    onClick = { selectedRole = "seeker" }
                )
                
                RoleSelectionCard(
                    title = "Recruiter",
                    subtitle = "I'm looking to hire",
                    icon = Icons.Default.Domain,
                    isSelected = selectedRole == "recruiter",
                    onClick = { selectedRole = "recruiter" }
                )
            }
        }

        // Bottom Action Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xFF111415).copy(alpha = 0.8f))
                .padding(24.dp)
        ) {
            Button(
                onClick = { 
                    if (selectedRole == "seeker") onJobSeekerSelected()
                    else if (selectedRole == "recruiter") onRecruiterSelected()
                },
                enabled = selectedRole != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color(0xFF323537).copy(alpha = 0.5f),
                    disabledContentColor = Color(0xFFC2C6D6)
                ),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = if (selectedRole != null) {
                                Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6)))
                            } else {
                                Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedRole != null) Color.White else Color(0xFFC2C6D6)
                    )
                }
            }
        }
    }
}

@Composable
fun RoleSelectionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(targetValue = if (isSelected) 1.02f else 1f, label = "scale")
    val borderColor = if (isSelected) Color(0xFF3B82F6) else Color.White.copy(alpha = 0.1f)
    val bgColor = if (isSelected) Color(0xFF111827).copy(alpha = 0.8f) else Color(0xFF111827).copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(28.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(28.dp))
            .clickable(onClick = onClick)
            .padding(32.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color(0xFF4D8EFF).copy(alpha = 0.2f) 
                        else Color(0xFF1D2022)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFFADC6FF),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE1E2E4)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontSize = 16.sp,
                color = Color(0xFFC2C6D6)
            )
        }

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = Color(0xFFADC6FF)
            )
        }
    }
}

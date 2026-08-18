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
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val StBgRole = Color(0xFFF3F7FF)
val StSurfaceRole = Color(0xFFFFFFFF)
val StSurfaceContainerLowRole = Color(0xFFF2F3FD)
val StSurfaceContainerRole = Color(0xFFECEDF7)
val StPrimaryRole = Color(0xFF0057C0)
val StOnSurfaceRole = Color(0xFF191B22)
val StTextSecondaryRole = Color(0xFF68738A)
val StOutlineVariantRole = Color(0xFFDCE5F3)

@Composable
fun RoleSelectionScreen(
    onJobSeekerSelected: () -> Unit,
    onRecruiterSelected: () -> Unit
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StBgRole)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "How will you use Smart Hire?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = StOnSurfaceRole,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Select your primary goal to tailor your experience.",
                fontSize = 16.sp,
                color = StTextSecondaryRole,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp, bottom = 48.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                RoleSelectionCard(
                    title = "Job Seeker",
                    subtitle = "I'm looking for a job or new opportunities",
                    icon = Icons.Default.PersonSearch,
                    isSelected = selectedRole == "seeker",
                    onClick = { selectedRole = "seeker" }
                )
                
                RoleSelectionCard(
                    title = "Recruiter",
                    subtitle = "I'm looking to hire great talent",
                    icon = Icons.Default.BusinessCenter,
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
                .background(StBgRole)
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
                    containerColor = StPrimaryRole,
                    disabledContainerColor = StOutlineVariantRole,
                    disabledContentColor = StTextSecondaryRole
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
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
    val borderColor = if (isSelected) StPrimaryRole else StOutlineVariantRole
    val bgColor = if (isSelected) StSurfaceContainerLowRole else StSurfaceRole

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(StSurfaceContainerRole),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = StPrimaryRole,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = StOnSurfaceRole
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = StTextSecondaryRole
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
                tint = StPrimaryRole,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

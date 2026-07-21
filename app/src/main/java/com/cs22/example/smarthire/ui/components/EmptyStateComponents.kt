package com.cs22.example.smarthire.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

private val PremiumPrimary = Color(0xFF3B82F6)
private val PremiumSecondary = Color(0xFF8B5CF6)
private val PremiumText = Color(0xFFE1E2E4)
private val PremiumTextMuted = Color(0xFFC2C6D6)

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "iconPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(PremiumPrimary, PremiumSecondary)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            color = PremiumText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = subtitle,
            color = PremiumTextMuted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(listOf(PremiumPrimary, PremiumSecondary))
                        )
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = actionLabel, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun NoJobsEmptyState(onAction: () -> Unit) {
    EmptyStateView(
        icon = Icons.Default.SearchOff,
        title = "No Jobs Found",
        subtitle = "Try adjusting your search filters",
        actionLabel = "Browse All Jobs",
        onAction = onAction
    )
}

@Composable
fun NoApplicationsEmptyState(onAction: () -> Unit) {
    EmptyStateView(
        icon = Icons.Default.Inbox,
        title = "No Applications Yet",
        subtitle = "Start applying to your dream jobs!",
        actionLabel = "Find Jobs",
        onAction = onAction
    )
}

@Composable
fun NoNotificationsEmptyState() {
    EmptyStateView(
        icon = Icons.Default.NotificationsNone,
        title = "All Caught Up!",
        subtitle = "No new notifications right now"
    )
}

@Composable
fun NoInterviewsEmptyState() {
    EmptyStateView(
        icon = Icons.Default.EventBusy,
        title = "No Interviews Scheduled",
        subtitle = "Your upcoming interviews will appear here"
    )
}

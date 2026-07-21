package com.cs22.example.smarthire.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val PremiumBg = Color(0xFF0F131D)
private val PremiumSurface = Color(0xFF161B28)
private val PremiumSurfaceContainer = Color(0xFF1D2433)
private val ShimmerColorShaded = Color(0x80252D40)
private val ShimmerColorLight = Color(0x403A4660)

@Composable
fun ShimmerBrush(showShimmer: Boolean = true): Brush {
    return if (showShimmer) {
        val transition = rememberInfiniteTransition(label = "shimmerTransition")
        val translateAnim = transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerAnimation"
        )
        Brush.linearGradient(
            colors = listOf(ShimmerColorShaded, ShimmerColorLight, ShimmerColorShaded),
            start = Offset.Zero,
            end = Offset(x = translateAnim.value, y = translateAnim.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}

@Composable
fun ShimmerJobCard() {
    val shimmerBrush = ShimmerBrush()
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(160.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(shimmerBrush)
                    )
                }
            }
        }
    }
}

@Composable
fun ShimmerCandidateCard() {
    val shimmerBrush = ShimmerBrush()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(shimmerBrush)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
            }
        }
    }
}

@Composable
fun ShimmerDashboardHeader() {
    val shimmerBrush = ShimmerBrush()
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(shimmerBrush)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush)
        )
    }
}

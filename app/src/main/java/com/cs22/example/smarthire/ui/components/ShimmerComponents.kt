package com.cs22.example.smarthire.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cs22.example.smarthire.ui.theme.*

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
            colors = listOf(StSurfaceContainer, StSurfaceContainerLow, StSurfaceContainer),
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
fun ShimmerEffect(modifier: Modifier) {
    Box(modifier = modifier.background(ShimmerBrush()))
}

@Composable
fun JobCardSkeleton() {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = StSurface,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                ShimmerEffect(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerEffect(Modifier.fillMaxWidth(0.7f).height(16.dp).clip(RoundedCornerShape(4.dp)))
                    Spacer(modifier = Modifier.height(8.dp))
                    ShimmerEffect(Modifier.fillMaxWidth(0.4f).height(14.dp).clip(RoundedCornerShape(4.dp)))
                }
                ShimmerEffect(Modifier.width(60.dp).height(24.dp).clip(RoundedCornerShape(12.dp)))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShimmerEffect(Modifier.width(80.dp).height(24.dp).clip(RoundedCornerShape(12.dp)))
                ShimmerEffect(Modifier.width(100.dp).height(24.dp).clip(RoundedCornerShape(12.dp)))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                ShimmerEffect(Modifier.width(80.dp).height(16.dp).clip(RoundedCornerShape(4.dp)))
                ShimmerEffect(Modifier.width(120.dp).height(40.dp).clip(RoundedCornerShape(20.dp)))
            }
        }
    }
}

@Composable
fun StatCardSkeleton() {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = StSurface,
        modifier = Modifier.size(160.dp).padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            ShimmerEffect(Modifier.size(40.dp).clip(CircleShape))
            Spacer(modifier = Modifier.height(12.dp))
            ShimmerEffect(Modifier.width(60.dp).height(28.dp).clip(RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerEffect(Modifier.width(100.dp).height(14.dp).clip(RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
fun NotificationItemSkeleton() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShimmerEffect(Modifier.size(48.dp).clip(CircleShape))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            ShimmerEffect(Modifier.fillMaxWidth(0.8f).height(16.dp).clip(RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerEffect(Modifier.fillMaxWidth(0.5f).height(12.dp).clip(RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
fun ProfileSkeleton() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShimmerEffect(Modifier.size(100.dp).clip(CircleShape))
        Spacer(modifier = Modifier.height(16.dp))
        ShimmerEffect(Modifier.width(150.dp).height(24.dp).clip(RoundedCornerShape(8.dp)))
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerEffect(Modifier.width(200.dp).height(16.dp).clip(RoundedCornerShape(4.dp)))
    }
}

package com.cs22.example.smarthire.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs22.example.smarthire.ui.theme.*

data class BottomNavigationItem(
    val icon: ImageVector,
    val label: String,
    val badge: Int = 0
)

@Composable
fun AnimatedBottomBar(
    items: List<BottomNavigationItem>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    activeColor: Color = StPrimary,
    backgroundColor: Color = Color.White
) {
    Column {
        HorizontalDivider(color = StOutlineVariant, thickness = 1.dp)
        Surface(
            color = backgroundColor,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(80.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    BottomBarItem(
                        item = item,
                        isSelected = selectedTab == index,
                        activeColor = activeColor,
                        onClick = { onTabSelected(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomBarItem(
    item: BottomNavigationItem,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(targetValue = if (isSelected) activeColor else StTextSecondary, label = "color")
    val animatedScale by animateFloatAsState(targetValue = if (isSelected) 1.0f else 0.95f, label = "scale")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .scale(animatedScale)
                    .background(
                        color = if (isSelected) StMatchBadgeBg else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = animatedColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            if (item.badge > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(StError),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (item.badge > 9) "9+" else item.badge.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = item.label,
            color = animatedColor,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

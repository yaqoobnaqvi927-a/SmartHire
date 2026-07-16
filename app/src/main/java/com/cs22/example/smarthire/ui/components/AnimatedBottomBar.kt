package com.cs22.example.smarthire.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BottomNavigationItem(
    val icon: ImageVector,
    val label: String
)

@Composable
fun AnimatedBottomBar(
    items: List<BottomNavigationItem>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    activeColor: Color,
    backgroundColor: Color = Color.White
) {
    Surface(
        color = backgroundColor.copy(alpha = 0.9f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
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

@Composable
fun BottomBarItem(
    item: BottomNavigationItem,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(targetValue = if (isSelected) activeColor else Color.Gray, label = "color")
    val animatedScale by animateFloatAsState(targetValue = if (isSelected) 1.15f else 1.0f, label = "scale")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = animatedColor,
            modifier = Modifier.scale(animatedScale)
        )
        AnimatedVisibility(visible = isSelected) {
            Text(
                text = item.label,
                color = animatedColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

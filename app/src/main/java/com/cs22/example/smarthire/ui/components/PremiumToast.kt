package com.cs22.example.smarthire.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val PremiumSurface = Color(0xFF161B28)
private val PremiumText = Color(0xFFE1E2E4)

enum class ToastType {
    SUCCESS, WARNING, ERROR, INFO
}

data class PremiumToastData(
    val message: String,
    val type: ToastType
)

class ToastState(initialToast: PremiumToastData? = null) {
    var toastData by mutableStateOf(initialToast)
        private set

    fun show(message: String, type: ToastType) {
        toastData = PremiumToastData(message, type)
    }

    fun dismiss() {
        toastData = null
    }
}

@Composable
fun rememberToastState(): ToastState {
    return remember { ToastState() }
}

@Composable
fun PremiumToast(data: PremiumToastData?, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = data != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            if (data != null) {
                LaunchedEffect(data) {
                    delay(3000)
                    onDismiss()
                }

                val (borderColor, icon) = when (data.type) {
                    ToastType.SUCCESS -> Color(0xFF10B981) to Icons.Default.CheckCircle
                    ToastType.ERROR -> Color(0xFFEF4444) to Icons.Default.Error
                    ToastType.WARNING -> Color(0xFFF59E0B) to Icons.Default.Warning
                    ToastType.INFO -> Color(0xFF3B82F6) to Icons.Default.Info
                }

                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PremiumSurface),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(4.dp)
                            .background(borderColor)
                    )
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = borderColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = data.message,
                            color = PremiumText,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

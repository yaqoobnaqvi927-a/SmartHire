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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs22.example.smarthire.ui.theme.*
import kotlinx.coroutines.delay

enum class ToastType {
    SUCCESS, ERROR, INFO, WARNING
}

data class ToastMessage(
    val message: String,
    val type: ToastType = ToastType.INFO
)

@Composable
fun PremiumToast(message: ToastMessage?, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = message != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            if (message != null) {
                LaunchedEffect(message) {
                    delay(3000)
                    onDismiss()
                }

                val (borderColor, icon) = when (message.type) {
                    ToastType.SUCCESS -> StSuccess to Icons.Default.CheckCircle
                    ToastType.ERROR -> StError to Icons.Default.Error
                    ToastType.INFO -> StPrimary to Icons.Default.Info
                    ToastType.WARNING -> StWarning to Icons.Default.Warning
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StSurface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(4.dp)
                                .background(borderColor)
                        )
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
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
                                text = message.message,
                                color = StOnSurface,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.cs22.example.smarthire.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlin.math.roundToInt

@Composable
fun VideoCallScreen(navController: NavHostController) {
    var isMuted by remember { mutableStateOf(false) }
    var isVideoOff by remember { mutableStateOf(false) }
    
    // PIP Drag State
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F131D))) {
        // Mock Remote Video Feed (Blurred Background)
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1D2433))) {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF3B82F6).copy(alpha = 0.2f)).blur(100.dp))
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White.copy(alpha=0.1f), modifier = Modifier.size(200.dp).align(Alignment.Center))
        }

        // Top Overlay Details
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(horizontal = 24.dp, vertical = 48.dp), // Safe area padding
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Technical Interview", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("SmartHire via Zoom", color = Color(0xFFC2C6D6), fontSize = 14.sp)
            }
            Surface(color = Color(0xFFEF4444).copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                    Spacer(Modifier.width(8.dp))
                    Text("12:45", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Draggable PIP (Local Video)
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .padding(top = 120.dp, end = 24.dp)
                .align(Alignment.TopEnd)
                .size(width = 120.dp, height = 160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
                .border(2.dp, Color.White.copy(alpha=0.2f), RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
        ) {
            if (!isVideoOff) {
                // Mock local camera view
                Icon(Icons.Default.Face, null, tint = Color.White.copy(alpha=0.5f), modifier = Modifier.align(Alignment.Center).size(64.dp))
            } else {
                Icon(Icons.Default.VideocamOff, null, tint = Color.White, modifier = Modifier.align(Alignment.Center))
            }
        }

        // Glassmorphic Control Dock
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Mute
                IconButton(
                    onClick = { isMuted = !isMuted },
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(if (isMuted) Color.White else Color(0xFF1D2433))
                ) {
                    Icon(if (isMuted) Icons.Default.MicOff else Icons.Default.Mic, null, tint = if (isMuted) Color.Black else Color.White)
                }

                // End Call
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.CallEnd, null, tint = Color.White)
                }

                // Video
                IconButton(
                    onClick = { isVideoOff = !isVideoOff },
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(if (isVideoOff) Color.White else Color(0xFF1D2433))
                ) {
                    Icon(if (isVideoOff) Icons.Default.VideocamOff else Icons.Default.Videocam, null, tint = if (isVideoOff) Color.Black else Color.White)
                }
            }
        }
    }
}

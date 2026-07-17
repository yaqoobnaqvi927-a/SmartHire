package com.cs22.example.smarthire.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF0F131D)).padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF1D2433).copy(alpha=0.8f))) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color(0xFFE1E2E4))
                }
                Spacer(Modifier.width(16.dp))
                Text("Notifications", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE1E2E4))
            }
        },
        containerColor = Color(0xFF0F131D)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Today", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2C6D6))
            }
            item {
                NotificationCard(
                    title = "Interview Scheduled",
                    description = "TechCorp has scheduled a video interview for the Senior Python Developer role.",
                    time = "2m ago",
                    icon = Icons.Default.Event,
                    iconTint = Color(0xFF3B82F6),
                    iconBg = Color(0xFF3B82F6).copy(alpha = 0.1f),
                    hasActions = true
                )
            }
            item {
                NotificationCard(
                    title = "Application Update",
                    description = "Your application for Backend Engineer at InnovateTech is now under review.",
                    time = "1h ago",
                    icon = Icons.Default.CheckCircle,
                    iconTint = Color(0xFF10B981),
                    iconBg = Color(0xFF10B981).copy(alpha = 0.1f),
                    hasActions = false
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
            item {
                Text("Yesterday", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2C6D6))
            }
            item {
                NotificationCard(
                    title = "New AI Match",
                    description = "A new job matching 88% of your skills was just posted.",
                    time = "1d ago",
                    icon = Icons.Default.AutoAwesome,
                    iconTint = Color(0xFF8B5CF6),
                    iconBg = Color(0xFF8B5CF6).copy(alpha = 0.1f),
                    hasActions = false
                )
            }
        }
    }
}

@Composable
fun NotificationCard(
    title: String,
    description: String,
    time: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    hasActions: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B28)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconTint)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFE1E2E4))
                        Text(time, fontSize = 12.sp, color = Color(0xFFC2C6D6))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(description, fontSize = 14.sp, color = Color(0xFFC2C6D6), lineHeight = 20.sp)
                }
            }

            if (hasActions) {
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth().padding(start = 64.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text("Accept", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE1E2E4)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text("Reschedule", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

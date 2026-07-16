package com.cs22.example.smarthire.ui.seeker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cs22.example.smarthire.model.Notification
import com.cs22.example.smarthire.network.RetrofitClient
import kotlinx.coroutines.delay

@Composable
fun NotificationsScreen(modifier: Modifier = Modifier) {
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while(true) {
            try {
                notifications = RetrofitClient.api.getNotifications()
                isLoading = false
            } catch (e: Exception) {
                // Silently drop
            }
            delay(5000L) // Refresh every 5 secs
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .padding(16.dp)
    ) {
        Text("Notifications", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFF1D4ED8), modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (notifications.isEmpty()) {
            Text("No new notifications.", color = Color(0xFF64748B), modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(notifications) { notif ->
                    NotificationCard(notif)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notif: Notification) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = if (notif.is_read) Color(0xFF64748B) else Color(0xFF10B981))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(notif.title, color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(notif.message, color = Color(0xFF64748B), style = MaterialTheme.typography.bodyMedium)
                if (notif.action_url.contains("/interviews/")) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(
                            onClick = { /* Call confirm API */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) { Text("Confirm") }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { /* Call decline API */ }) { Text("Decline", color = Color(0xFFEF4444)) }
                    }
                }
            }
        }
    }
}

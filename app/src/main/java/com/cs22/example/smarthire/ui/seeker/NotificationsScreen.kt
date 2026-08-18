package com.cs22.example.smarthire.ui.seeker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs22.example.smarthire.model.Notification
import com.cs22.example.smarthire.network.RetrofitClient
import com.cs22.example.smarthire.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    navController: androidx.navigation.NavController? = null
) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = StOnSurface, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = StOnSurface)
                    }
                },
                actions = {
                    TextButton(onClick = { 
                        notifications = notifications.map { it.copy(is_read = true) }
                    }) {
                        Text("Mark all read", color = StPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StBackground)
            )
        },
        containerColor = StBackground
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StPrimary)
                }
            } else if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = StTextSecondary, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("No notifications yet", color = StTextSecondary, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Text("Today", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StTextSecondary, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(notifications) { notif ->
                        NotificationItem(notif)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notif: Notification) {
    val isMatch = notif.title.contains("Match", ignoreCase = true)
    val isEvent = notif.title.contains("Interview", ignoreCase = true) || notif.action_url?.contains("/interviews/") == true
    
    val icon = when {
        isMatch -> Icons.Default.Bolt
        isEvent -> Icons.Default.Event
        else -> Icons.Default.Work
    }
    
    val iconBgColor = when {
        isMatch -> StMatchBadgeBg
        isEvent -> StWarning.copy(alpha = 0.1f)
        else -> StPrimary.copy(alpha = 0.1f)
    }
    
    val iconTint = when {
        isMatch -> StSuccess
        isEvent -> StWarning
        else -> StPrimary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = StSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, StOutlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
        ) {
            if (!notif.is_read) {
                Box(modifier = Modifier.fillMaxHeight().width(4.dp).background(StPrimary))
            }
            
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(iconBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconTint)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Text(notif.title, color = StOnSurface, fontWeight = if (!notif.is_read) FontWeight.Bold else FontWeight.Medium, fontSize = 16.sp)
                            Text("2h ago", color = StTextSecondary, fontSize = 12.sp) // Mocked time
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(notif.message, color = StTextSecondary, fontSize = 14.sp)
                        
                        if (isMatch) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = StMatchBadgeBg,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = StPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("92% Match", color = StPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        if (notif.action_url?.contains("/interviews/") == true) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row {
                                Button(
                                    onClick = { /* Call confirm API */ },
                                    colors = ButtonDefaults.buttonColors(containerColor = StPrimary),
                                    shape = RoundedCornerShape(25.dp),
                                    modifier = Modifier.height(36.dp)
                                ) { Text("Confirm", fontSize = 14.sp) }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { /* Call decline API */ },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StTextSecondary),
                                    border = BorderStroke(1.dp, StOutlineVariant),
                                    shape = RoundedCornerShape(25.dp),
                                    modifier = Modifier.height(36.dp)
                                ) { Text("Decline", fontSize = 14.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
}

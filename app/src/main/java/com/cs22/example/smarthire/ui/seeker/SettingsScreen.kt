package com.cs22.example.smarthire.ui.seeker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.cs22.example.smarthire.viewmodel.SeekerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SeekerViewModel, navController: NavHostController) {
    var pushNotifications by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(true) }
    
    val profileState by viewModel.profileState.collectAsState()
    val email = (profileState as? com.cs22.example.smarthire.viewmodel.SeekerUiState.Success)?.data?.email ?: "user@smarthire.com"

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111415))
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(48.dp).clip(CircleShape)) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color(0xFFADC6FF))
                }
                Spacer(Modifier.width(16.dp))
                Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFADC6FF))
            }
        },
        containerColor = Color(0xFF111415)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Account Section
            SettingsSection(title = "ACCOUNT") {
                SettingsRow(icon = Icons.Default.Person, title = "Personal Information")
                SettingsRow(icon = Icons.Default.Mail, title = "Email Address", subtitle = email)
                SettingsRow(icon = Icons.Default.Lock, title = "Password & Security")
            }

            // Preferences Section
            SettingsSection(title = "PREFERENCES") {
                SettingsSwitchRow(
                    icon = Icons.Default.NotificationsActive,
                    title = "Push Notifications",
                    checked = pushNotifications,
                    onCheckedChange = { pushNotifications = it }
                )
                SettingsSwitchRow(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
                SettingsRow(icon = Icons.Default.Language, title = "Language", subtitle = "English (US)")
            }

            // Support Section
            SettingsSection(title = "SUPPORT") {
                SettingsRow(icon = Icons.Default.HelpCenter, title = "Help Center")
                SettingsRow(icon = Icons.Default.Forum, title = "Contact Us")
            }

            // Danger Zone Section
            Column {
                Text("DANGER ZONE", color = Color(0xFFFFB4AB), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp, bottom = 12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF93000A).copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, Color(0xFFFFB4AB).copy(alpha = 0.2f))
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    viewModel.logout {
                                        navController.navigate("splash") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                }
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Logout, null, tint = Color(0xFFFFB4AB))
                            Spacer(Modifier.width(16.dp))
                            Text("Log Out", color = Color(0xFFFFB4AB), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                        Divider(color = Color(0xFFFFB4AB).copy(alpha = 0.1f), thickness = 1.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { }
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DeleteForever, null, tint = Color(0xFFFFB4AB))
                            Spacer(Modifier.width(16.dp))
                            Text("Delete Account", color = Color(0xFFFFB4AB), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, color = Color(0xFFADC6FF), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp, bottom = 12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D2022).copy(alpha = 0.6f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color(0xFF8C909F))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, color = Color(0xFFE1E2E4), fontSize = 16.sp)
                if (subtitle != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(subtitle, color = Color(0xFFC2C6D6), fontSize = 12.sp)
                }
            }
        }
        Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF424754))
    }
    Divider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
}

@Composable
fun SettingsSwitchRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color(0xFF8C909F))
            Spacer(Modifier.width(16.dp))
            Text(title, color = Color(0xFFE1E2E4), fontSize = 16.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF3B82F6),
                uncheckedThumbColor = Color(0xFF8C909F),
                uncheckedTrackColor = Color(0xFF323537)
            )
        )
    }
    Divider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
}

package com.cs22.example.smarthire.ui.seeker

import android.widget.Toast
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.cs22.example.smarthire.ui.theme.*
import com.cs22.example.smarthire.viewmodel.SeekerViewModel
import com.cs22.example.smarthire.viewmodel.SeekerUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SeekerViewModel, navController: NavHostController) {
    val context = LocalContext.current
    var pushNotifications by remember { mutableStateOf(true) }
    var emailNotifications by remember { mutableStateOf(true) }
    var jobAlerts by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English (US)") }

    // Dialog States
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showConnectedAccountsDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    val profileState by viewModel.profileState.collectAsState()
    val email = (profileState as? SeekerUiState.Success)?.data?.email ?: "user@smarthire.com"
    val name = (profileState as? SeekerUiState.Success)?.data?.full_name?.takeIf { it.isNotBlank() }
        ?: (profileState as? SeekerUiState.Success)?.data?.username
        ?: "Job Seeker"
    val photoUrl = (profileState as? SeekerUiState.Success)?.data?.photo_url 
        ?: (profileState as? SeekerUiState.Success)?.data?.profile?.photo_url

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Preferences", color = StOnSurface, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = StOnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StBackground)
            )
        },
        containerColor = StBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Profile Section Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StSurface),
                border = BorderStroke(1.dp, StOutlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!photoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.size(64.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(StPrimary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                            Text(name.take(1).uppercase(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = StPrimary)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StOnSurface)
                        Text(email, fontSize = 14.sp, color = StTextSecondary)
                    }
                    OutlinedButton(
                        onClick = { navController.navigate("profile_setup") },
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(1.dp, StOutlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StOnSurface)
                    ) {
                        Text("Edit")
                    }
                }
            }

            // Preferences Section
            SettingsSection(title = "PREFERENCES") {
                SettingsSwitchRow(
                    icon = Icons.Default.WorkOutline, 
                    title = "Job Alerts", 
                    checked = jobAlerts, 
                    onCheckedChange = { 
                        jobAlerts = it 
                        Toast.makeText(context, if (it) "Job alerts enabled" else "Job alerts disabled", Toast.LENGTH_SHORT).show()
                    }
                )
                SettingsSwitchRow(
                    icon = Icons.Default.MailOutline, 
                    title = "Email Notifications", 
                    checked = emailNotifications, 
                    onCheckedChange = { 
                        emailNotifications = it
                        Toast.makeText(context, if (it) "Email notifications enabled" else "Email notifications disabled", Toast.LENGTH_SHORT).show()
                    }
                )
                SettingsSwitchRow(
                    icon = Icons.Default.NotificationsNone, 
                    title = "Push Notifications", 
                    checked = pushNotifications, 
                    onCheckedChange = { 
                        pushNotifications = it
                        Toast.makeText(context, if (it) "Push notifications enabled" else "Push notifications disabled", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Account Section
            SettingsSection(title = "ACCOUNT & SECURITY") {
                SettingsRow(
                    icon = Icons.Default.Lock, 
                    title = "Change Password",
                    onClick = { showChangePasswordDialog = true }
                )
                SettingsRow(
                    icon = Icons.Default.Link, 
                    title = "Connected Accounts",
                    subtitle = "Google, GitHub",
                    onClick = { showConnectedAccountsDialog = true }
                )
                SettingsRow(
                    icon = Icons.Default.PrivacyTip, 
                    title = "Privacy Settings",
                    onClick = { showPrivacyDialog = true }
                )
            }

            // App Section
            SettingsSection(title = "APP SETTINGS") {
                SettingsSwitchRow(
                    icon = Icons.Default.DarkMode, 
                    title = "Dark Mode", 
                    checked = darkMode, 
                    onCheckedChange = { 
                        darkMode = it
                        Toast.makeText(context, if (it) "Dark theme enabled" else "Light theme enabled", Toast.LENGTH_SHORT).show()
                    }
                )
                SettingsRow(
                    icon = Icons.Default.Language, 
                    title = "Language", 
                    subtitle = selectedLanguage,
                    onClick = { showLanguageDialog = true }
                )
                SettingsRow(
                    icon = Icons.Default.Info, 
                    title = "App Version", 
                    subtitle = "v2.0.0 (FYP Release)",
                    onClick = {
                        Toast.makeText(context, "SmartHire AI v2.0.0 - Up to date", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Danger Zone Section
            Column {
                Text("DANGER ZONE", color = StError, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = StSurface),
                    border = BorderStroke(1.dp, StError.copy(alpha = 0.3f))
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDeleteAccountDialog = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(StError.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.DeleteOutline, null, tint = StError)
                            }
                            Spacer(Modifier.width(16.dp))
                            Text("Delete Account", color = StError, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { 
                    viewModel.logout {
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(25.dp),
                border = BorderStroke(1.dp, StError),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StError)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Log Out", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(48.dp))
        }
    }

    // ─── CHANGE PASSWORD DIALOG ───
    if (showChangePasswordDialog) {
        var currentPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }
        var passVisibility by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = currentPass,
                        onValueChange = { currentPass = it },
                        label = { Text("Current Password") },
                        visualTransformation = if (passVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password") },
                        visualTransformation = if (passVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = if (passVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Row(
                        modifier = Modifier.clickable { passVisibility = !passVisibility },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = passVisibility, onCheckedChange = { passVisibility = it })
                        Text("Show Passwords", fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPass.length < 6) {
                            Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                        } else if (newPass != confirmPass) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                            showChangePasswordDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StPrimary)
                ) {
                    Text("Update Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel", color = StTextSecondary)
                }
            }
        )
    }

    // ─── LANGUAGE SELECTION DIALOG ───
    if (showLanguageDialog) {
        val languages = listOf("English (US)", "Urdu (اردو)", "Arabic (العربية)", "German (Deutsch)", "Spanish (Español)")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    languages.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLanguage = lang
                                    Toast.makeText(context, "Language set to $lang", Toast.LENGTH_SHORT).show()
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == lang,
                                onClick = {
                                    selectedLanguage = lang
                                    Toast.makeText(context, "Language set to $lang", Toast.LENGTH_SHORT).show()
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(lang, fontSize = 16.sp, fontWeight = if (selectedLanguage == lang) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // ─── PRIVACY SETTINGS DIALOG ───
    if (showPrivacyDialog) {
        var profileVisibleToRecruiters by remember { mutableStateOf(true) }
        var aiDiscovery by remember { mutableStateOf(true) }
        var showContactInfo by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Recruiter Visibility", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Allow recruiters to find your profile", fontSize = 12.sp, color = StTextSecondary)
                        }
                        Switch(checked = profileVisibleToRecruiters, onCheckedChange = { profileVisibleToRecruiters = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("AI Matching Discovery", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Include profile in automatic AI recommendations", fontSize = 12.sp, color = StTextSecondary)
                        }
                        Switch(checked = aiDiscovery, onCheckedChange = { aiDiscovery = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Public Contact Info", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Show email and phone on public applications", fontSize = 12.sp, color = StTextSecondary)
                        }
                        Switch(checked = showContactInfo, onCheckedChange = { showContactInfo = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        Toast.makeText(context, "Privacy preferences saved", Toast.LENGTH_SHORT).show()
                        showPrivacyDialog = false 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StPrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ─── CONNECTED ACCOUNTS DIALOG ───
    if (showConnectedAccountsDialog) {
        var googleConnected by remember { mutableStateOf(true) }
        var githubConnected by remember { mutableStateOf(false) }
        var linkedInConnected by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showConnectedAccountsDialog = false },
            title = { Text("Connected Accounts", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AccountConnectionRow("Google", "Connected", googleConnected) { googleConnected = !googleConnected }
                    AccountConnectionRow("GitHub", "Not linked", githubConnected) { 
                        githubConnected = !githubConnected
                        Toast.makeText(context, if (githubConnected) "GitHub account linked" else "GitHub unlinked", Toast.LENGTH_SHORT).show()
                    }
                    AccountConnectionRow("LinkedIn", "Not linked", linkedInConnected) { 
                        linkedInConnected = !linkedInConnected
                        Toast.makeText(context, if (linkedInConnected) "LinkedIn account linked" else "LinkedIn unlinked", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showConnectedAccountsDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // ─── DELETE ACCOUNT DIALOG ───
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account?", fontWeight = FontWeight.Bold, color = StError) },
            text = {
                Text(
                    "Are you sure you want to permanently delete your SmartHire account? All your uploaded resumes, test applications, and matched history will be irreversibly erased.",
                    color = StOnSurfaceVariant,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        viewModel.logout {
                            Toast.makeText(context, "Account deletion scheduled. Logged out.", Toast.LENGTH_LONG).show()
                            navController.navigate("auth") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StError)
                ) {
                    Text("Permanently Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", color = StOnSurface)
                }
            }
        )
    }
}

@Composable
fun AccountConnectionRow(name: String, status: String, isConnected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(if (isConnected) "Connected" else status, fontSize = 12.sp, color = if (isConnected) StSuccess else StTextSecondary)
        }
        OutlinedButton(
            onClick = onToggle,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, if (isConnected) StError else StPrimary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isConnected) StError else StPrimary)
        ) {
            Text(if (isConnected) "Disconnect" else "Connect", fontSize = 12.sp)
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, color = StPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = StSurface),
            border = BorderStroke(1.dp, StOutlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    title: String, 
    subtitle: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(StSurfaceContainer), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = StOnSurface)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, color = StOnSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
        if (subtitle != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(subtitle, color = StTextSecondary, fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ChevronRight, null, tint = StTextSecondary)
            }
        } else {
            Icon(Icons.Default.ChevronRight, null, tint = StTextSecondary)
        }
    }
}

@Composable
fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    title: String, 
    checked: Boolean, 
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(StSurfaceContainer), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = StOnSurface)
            }
            Spacer(Modifier.width(16.dp))
            Text(title, color = StOnSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = StSurface,
                checkedTrackColor = StPrimary,
                uncheckedThumbColor = StTextSecondary,
                uncheckedTrackColor = StSurfaceContainer
            )
        )
    }
}

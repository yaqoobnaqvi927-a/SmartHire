package com.cs22.example.smarthire.ui.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecruiterSetupScreen(navController: NavController) {
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var companyName by remember { mutableStateOf("") }
    var companyWebsite by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf("") }
    var companySize by remember { mutableStateOf("") }
    var companyBio by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        logoUri = uri
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111415))
    ) {
        // Atmospheric effects
        Box(modifier = Modifier.offset(x = 200.dp, y = (-100).dp).size(300.dp).background(Color(0xFFADC6FF).copy(alpha = 0.1f), CircleShape).blur(100.dp).align(Alignment.TopEnd))
        Box(modifier = Modifier.offset(x = (-100).dp, y = 100.dp).size(300.dp).background(Color(0xFF571BC1).copy(alpha = 0.1f), CircleShape).blur(100.dp).align(Alignment.BottomStart))

        Column(modifier = Modifier.fillMaxSize()) {
            // TopAppBar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(Color(0xFF111415).copy(alpha = 0.8f))
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFFC2C6D6))
                }
                Text("SmartHire", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFADC6FF))
                IconButton(onClick = { }) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = Color(0xFFC2C6D6))
                }
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 32.dp, bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Company Profile",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE1E2E4)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.BusinessCenter, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(32.dp))
                }
                
                Text(
                    text = "Build your employer brand to attract top AI-matched talent.",
                    fontSize = 16.sp,
                    color = Color(0xFFC2C6D6),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                // Logo Upload
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1F2937))
                        .border(2.dp, Color(0xFF3B82F6).copy(alpha = 0.5f), CircleShape)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (logoUri != null) {
                        // In a real app, use Coil AsyncImage. Here we just show checkmark.
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
                    } else {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color(0xFFC2C6D6), modifier = Modifier.size(48.dp))
                    }
                    
                    // Edit badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-8).dp, y = (-8).dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6))
                            .border(2.dp, Color(0xFF111415), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Form Fields
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFF111827).copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                        .padding(24.dp)
                ) {
                    Text("Company Name", fontSize = 14.sp, color = Color(0xFFC2C6D6))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = companyName, onValueChange = { companyName = it },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFF1F2937), focusedContainerColor = Color(0xFF1F2937), unfocusedBorderColor = Color(0xFF424754), focusedBorderColor = Color(0xFF3B82F6), unfocusedTextColor = Color(0xFFE1E2E4))
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Company Website", fontSize = 14.sp, color = Color(0xFFC2C6D6))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = companyWebsite, onValueChange = { companyWebsite = it },
                        placeholder = { Text("https://", color = Color(0xFFC2C6D6).copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFF1F2937), focusedContainerColor = Color(0xFF1F2937), unfocusedBorderColor = Color(0xFF424754), focusedBorderColor = Color(0xFF3B82F6), unfocusedTextColor = Color(0xFFE1E2E4))
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Industry", fontSize = 14.sp, color = Color(0xFFC2C6D6))
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = industry, onValueChange = { industry = it },
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFF1F2937), focusedContainerColor = Color(0xFF1F2937), unfocusedBorderColor = Color(0xFF424754), focusedBorderColor = Color(0xFF3B82F6), unfocusedTextColor = Color(0xFFE1E2E4))
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Company Size", fontSize = 14.sp, color = Color(0xFFC2C6D6))
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = companySize, onValueChange = { companySize = it },
                                placeholder = { Text("e.g. 1-50", color = Color(0xFFC2C6D6).copy(alpha = 0.5f)) },
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFF1F2937), focusedContainerColor = Color(0xFF1F2937), unfocusedBorderColor = Color(0xFF424754), focusedBorderColor = Color(0xFF3B82F6), unfocusedTextColor = Color(0xFFE1E2E4))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Company Bio", fontSize = 14.sp, color = Color(0xFFC2C6D6))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = companyBio, onValueChange = { companyBio = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFF1F2937), focusedContainerColor = Color(0xFF1F2937), unfocusedBorderColor = Color(0xFF424754), focusedBorderColor = Color(0xFF3B82F6), unfocusedTextColor = Color(0xFFE1E2E4))
                    )
                }
            }
        }

        // Action Footer
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF111415), Color(0xFF111415))))
                .padding(24.dp)
        ) {
            val isValid = companyName.isNotBlank() && industry.isNotBlank()
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val uid = com.cs22.example.smarthire.firebase.FirebaseClient.uid ?: return@launch
                            val db = com.cs22.example.smarthire.firebase.FirebaseClient.db
                            db.collection("users").document(uid).update("setup_complete", true).await()
                            
                            val profile = mapOf(
                                "company_name" to companyName,
                                "website" to companyWebsite,
                                "industry" to industry,
                                "company_size" to companySize,
                                "bio" to companyBio
                            )
                            db.collection("users").document(uid).collection("recruiter_profile").document("profile")
                                .set(profile, com.google.firebase.firestore.SetOptions.merge()).await()

                            navController.navigate("recruiter_flow") { popUpTo("recruiter_setup") { inclusive = true } }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = isValid && !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color(0xFF323537).copy(alpha = 0.5f)),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(if (isValid) Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Launch Command Center", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = if (isValid) Color.White else Color(0xFFC2C6D6))
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = if (isValid) Color.White else Color(0xFFC2C6D6))
                        }
                    }
                }
            }
        }
    }
}

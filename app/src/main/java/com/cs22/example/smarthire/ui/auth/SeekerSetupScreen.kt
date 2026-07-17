package com.cs22.example.smarthire.ui.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
fun SeekerSetupScreen(navController: NavController) {
    var cvUri by remember { mutableStateOf<Uri?>(null) }
    var isManualExpanded by remember { mutableStateOf(false) }
    
    var jobTitle by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        cvUri = uri
    }

    // Shimmer Animation for the Dropzone
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "shimmerTranslate"
    )

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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFFC2C6D6))
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
                Text(
                    text = "Let's build your AI Profile",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE1E2E4),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "Upload your CV and let Gemini do the heavy lifting.",
                    fontSize = 18.sp,
                    color = Color(0xFFC2C6D6),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                // Dropzone
                val dropzoneBorderColor = if (cvUri != null) Color(0xFF8B5CF6) else Color(0xFF424754)
                val dropzoneBgColor = if (cvUri != null) Color(0xFF8B5CF6).copy(alpha = 0.15f) else Color(0xFF8B5CF6).copy(alpha = 0.05f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(dropzoneBgColor)
                        .border(2.dp, dropzoneBorderColor, RoundedCornerShape(28.dp))
                        .clickable { launcher.launch("application/pdf") },
                    contentAlignment = Alignment.Center
                ) {
                    // Shimmer overlay
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(200.dp)
                            .offset(x = shimmerTranslate.dp)
                            .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.1f), Color.Transparent)))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1D2022))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (cvUri != null) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = if (cvUri != null) Color(0xFF10B981) else Color(0xFFADC6FF),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (cvUri != null) "Resume Uploaded Successfully!" else "Tap to upload Resume/CV (PDF)",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE1E2E4)
                        )
                        Text(
                            text = if (cvUri != null) "Ready for AI Analysis" else "Max file size: 10MB",
                            fontSize = 14.sp,
                            color = Color(0xFFC2C6D6),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Manual Details Expandable
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFF111827).copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isManualExpanded = !isManualExpanded }
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Or enter manually", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE1E2E4))
                            Icon(
                                imageVector = if (isManualExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Color(0xFFC2C6D6)
                            )
                        }
                        
                        AnimatedVisibility(visible = isManualExpanded) {
                            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 16.dp))
                                
                                Text("Current Job Title", fontSize = 14.sp, color = Color(0xFFC2C6D6))
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = jobTitle,
                                    onValueChange = { jobTitle = it },
                                    placeholder = { Text("e.g. Senior Software Engineer", color = Color(0xFFC2C6D6).copy(alpha = 0.5f)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = Color(0xFF1F2937),
                                        focusedContainerColor = Color(0xFF1F2937),
                                        unfocusedBorderColor = Color(0xFF424754),
                                        focusedBorderColor = Color(0xFF3B82F6),
                                        unfocusedTextColor = Color(0xFFE1E2E4),
                                        focusedTextColor = Color(0xFFE1E2E4)
                                    )
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Years Exp", fontSize = 14.sp, color = Color(0xFFC2C6D6))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = experience,
                                            onValueChange = { experience = it },
                                            placeholder = { Text("e.g. 3", color = Color(0xFFC2C6D6).copy(alpha = 0.5f)) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedContainerColor = Color(0xFF1F2937),
                                                focusedContainerColor = Color(0xFF1F2937),
                                                unfocusedBorderColor = Color(0xFF424754),
                                                focusedBorderColor = Color(0xFF3B82F6),
                                                unfocusedTextColor = Color(0xFFE1E2E4)
                                            )
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Location", fontSize = 14.sp, color = Color(0xFFC2C6D6))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = location,
                                            onValueChange = { location = it },
                                            placeholder = { Text("City, Country", color = Color(0xFFC2C6D6).copy(alpha = 0.5f)) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedContainerColor = Color(0xFF1F2937),
                                                focusedContainerColor = Color(0xFF1F2937),
                                                unfocusedBorderColor = Color(0xFF424754),
                                                focusedBorderColor = Color(0xFF3B82F6),
                                                unfocusedTextColor = Color(0xFFE1E2E4)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
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
            val isValid = cvUri != null || (jobTitle.isNotBlank() && experience.isNotBlank())
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val uid = com.cs22.example.smarthire.firebase.FirebaseClient.uid ?: return@launch
                            val db = com.cs22.example.smarthire.firebase.FirebaseClient.db
                            db.collection("users").document(uid).update("setup_complete", true).await()
                            
                            val profile = mapOf(
                                "jobTitle" to jobTitle,
                                "experience" to experience,
                                "location" to location
                            )
                            db.collection("users").document(uid).collection("candidate_profile").document("profile")
                                .set(profile, com.google.firebase.firestore.SetOptions.merge()).await()

                            navController.navigate("job_seeker_flow") { popUpTo("seeker_setup") { inclusive = true } }
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
                            Text("Analyze My CV & Continue", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = if (isValid) Color.White else Color(0xFFC2C6D6))
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = if (isValid) Color.White else Color(0xFFC2C6D6))
                        }
                    }
                }
            }
        }
    }
}

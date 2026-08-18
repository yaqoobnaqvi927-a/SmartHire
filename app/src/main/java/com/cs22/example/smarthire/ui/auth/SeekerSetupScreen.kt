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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

val StBgSeeker = Color(0xFFF3F7FF)
val StSurfaceSeeker = Color(0xFFFFFFFF)
val StPrimarySeeker = Color(0xFF0057C0)
val StOnSurfaceSeeker = Color(0xFF191B22)
val StTextSecondarySeeker = Color(0xFF68738A)
val StOutlineVariantSeeker = Color(0xFFDCE5F3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeekerSetupScreen(navController: NavController) {
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var fullName by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("Entry Level") }
    var location by remember { mutableStateOf("") }
    
    var expExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        photoUri = uri
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StBgSeeker)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Progress
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StSurfaceSeeker)
                    .padding(vertical = 16.dp, horizontal = 24.dp)
            ) {
                Text(
                    text = "Step 1 of 4 • Profile Basics",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StPrimarySeeker
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { 0.25f },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = StPrimarySeeker,
                    trackColor = StOutlineVariantSeeker
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
                    .padding(bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Let's Build Your Profile",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = StOnSurfaceSeeker,
                    modifier = Modifier.padding(bottom = 32.dp).align(Alignment.Start)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(StSurfaceSeeker)
                        .border(1.dp, StOutlineVariantSeeker, RoundedCornerShape(18.dp))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        
                        // Photo Uploader
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF8FAFC))
                                .clickable { launcher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            // Using a simple dashed border approximation with a normal border
                            Box(modifier = Modifier.fillMaxSize().border(2.dp, StOutlineVariantSeeker, CircleShape))
                            if (photoUri != null) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StPrimarySeeker, modifier = Modifier.size(40.dp))
                            } else {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = StTextSecondarySeeker, modifier = Modifier.size(32.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Full Name
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Full Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StOnSurfaceSeeker)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = fullName, onValueChange = { fullName = it },
                                placeholder = { Text("Jane Doe", color = StTextSecondarySeeker) },
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = StSurfaceSeeker, focusedContainerColor = StSurfaceSeeker,
                                    unfocusedBorderColor = StOutlineVariantSeeker, focusedBorderColor = StPrimarySeeker,
                                    unfocusedTextColor = StOnSurfaceSeeker, focusedTextColor = StOnSurfaceSeeker
                                ), singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Job Title
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Professional Title", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StOnSurfaceSeeker)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = jobTitle, onValueChange = { jobTitle = it },
                                placeholder = { Text("e.g. Product Designer", color = StTextSecondarySeeker) },
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = StSurfaceSeeker, focusedContainerColor = StSurfaceSeeker,
                                    unfocusedBorderColor = StOutlineVariantSeeker, focusedBorderColor = StPrimarySeeker,
                                    unfocusedTextColor = StOnSurfaceSeeker, focusedTextColor = StOnSurfaceSeeker
                                ), singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Location
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Location", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StOnSurfaceSeeker)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = location, onValueChange = { location = it },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = StTextSecondarySeeker) },
                                placeholder = { Text("City, State", color = StTextSecondarySeeker) },
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = StSurfaceSeeker, focusedContainerColor = StSurfaceSeeker,
                                    unfocusedBorderColor = StOutlineVariantSeeker, focusedBorderColor = StPrimarySeeker,
                                    unfocusedTextColor = StOnSurfaceSeeker, focusedTextColor = StOnSurfaceSeeker
                                ), singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Experience Level Dropdown
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Experience Level", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StOnSurfaceSeeker)
                            Spacer(modifier = Modifier.height(6.dp))
                            ExposedDropdownMenuBox(
                                expanded = expExpanded,
                                onExpandedChange = { expExpanded = !expExpanded }
                            ) {
                                OutlinedTextField(
                                    value = experience, onValueChange = {}, readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = StSurfaceSeeker, focusedContainerColor = StSurfaceSeeker,
                                        unfocusedBorderColor = StOutlineVariantSeeker, focusedBorderColor = StPrimarySeeker,
                                        unfocusedTextColor = StOnSurfaceSeeker, focusedTextColor = StOnSurfaceSeeker
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expExpanded,
                                    onDismissRequest = { expExpanded = false }
                                ) {
                                    listOf("Entry Level", "Mid Level", "Senior Level", "Lead / Manager").forEach { selection ->
                                        DropdownMenuItem(
                                            text = { Text(selection) },
                                            onClick = {
                                                experience = selection
                                                expExpanded = false
                                            }
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
                .background(StBgSeeker)
                .padding(24.dp)
        ) {
            val isValid = fullName.isNotBlank() && jobTitle.isNotBlank()
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val uid = com.cs22.example.smarthire.firebase.FirebaseClient.uid ?: return@launch
                            val db = com.cs22.example.smarthire.firebase.FirebaseClient.db
                            db.collection("users").document(uid).update("setup_complete", true).await()
                            
                            val profile = mapOf(
                                "full_name" to fullName,
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
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StPrimarySeeker, disabledContainerColor = StOutlineVariantSeeker, disabledContentColor = StTextSecondarySeeker)
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

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
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

val StSurfaceContainerLowRec = Color(0xFFF2F3FD)
val StSurfaceRec = Color(0xFFFFFFFF)
val StPrimaryRec = Color(0xFF0057C0)
val StOnSurfaceRec = Color(0xFF191B22)
val StTextSecondaryRec = Color(0xFF68738A)
val StOutlineVariantRec = Color(0xFFDCE5F3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecruiterSetupScreen(navController: NavController) {
    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }
    var companyLogoUri by remember { mutableStateOf<Uri?>(null) }
    var fullName by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf("Technology") }
    var companySize by remember { mutableStateOf("1-50") }
    
    var indExpanded by remember { mutableStateOf(false) }
    var sizeExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    val profileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> profilePhotoUri = uri }
    val logoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> companyLogoUri = uri }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StSurfaceContainerLowRec),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(StSurfaceRec)
                    .border(1.dp, StOutlineVariantRec, RoundedCornerShape(18.dp))
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Recruiter Profile",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = StOnSurfaceRec
                    )
                    Text(
                        text = "Tell us about yourself and your company.",
                        fontSize = 14.sp,
                        color = StTextSecondaryRec,
                        modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                    )

                    // Photo Uploaders Side-by-Side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Profile Photo
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, StOutlineVariantRec, CircleShape)
                                    .clickable { profileLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (profilePhotoUri != null) Icon(Icons.Default.CheckCircle, null, tint = StPrimaryRec, modifier = Modifier.size(32.dp))
                                else Icon(Icons.Default.Person, null, tint = StTextSecondaryRec, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Your Photo", fontSize = 12.sp, color = StTextSecondaryRec)
                        }

                        // Company Logo
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, StOutlineVariantRec, RoundedCornerShape(12.dp))
                                    .clickable { logoLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (companyLogoUri != null) Icon(Icons.Default.CheckCircle, null, tint = StPrimaryRec, modifier = Modifier.size(32.dp))
                                else Icon(Icons.Default.Business, null, tint = StTextSecondaryRec, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Company Logo", fontSize = 12.sp, color = StTextSecondaryRec)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = StOutlineVariantRec)
                    Spacer(modifier = Modifier.height(24.dp))

                    // Full Name
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Full Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StOnSurfaceRec)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = fullName, onValueChange = { fullName = it },
                            placeholder = { Text("John Doe", color = StTextSecondaryRec) },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = StSurfaceRec, focusedContainerColor = StSurfaceRec,
                                unfocusedBorderColor = StOutlineVariantRec, focusedBorderColor = StPrimaryRec,
                                unfocusedTextColor = StOnSurfaceRec, focusedTextColor = StOnSurfaceRec
                            ), singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Job Title
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Job Title / Role", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StOnSurfaceRec)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = jobTitle, onValueChange = { jobTitle = it },
                            placeholder = { Text("e.g. Technical Recruiter", color = StTextSecondaryRec) },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = StSurfaceRec, focusedContainerColor = StSurfaceRec,
                                unfocusedBorderColor = StOutlineVariantRec, focusedBorderColor = StPrimaryRec,
                                unfocusedTextColor = StOnSurfaceRec, focusedTextColor = StOnSurfaceRec
                            ), singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Company Name
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Company Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StOnSurfaceRec)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = companyName, onValueChange = { companyName = it },
                            placeholder = { Text("e.g. TechCorp", color = StTextSecondaryRec) },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = StSurfaceRec, focusedContainerColor = StSurfaceRec,
                                unfocusedBorderColor = StOutlineVariantRec, focusedBorderColor = StPrimaryRec,
                                unfocusedTextColor = StOnSurfaceRec, focusedTextColor = StOnSurfaceRec
                            ), singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2-Column Dropdowns
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Industry", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StOnSurfaceRec)
                            Spacer(modifier = Modifier.height(6.dp))
                            ExposedDropdownMenuBox(expanded = indExpanded, onExpandedChange = { indExpanded = !indExpanded }) {
                                OutlinedTextField(
                                    value = industry, onValueChange = {}, readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = indExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = StSurfaceRec, focusedContainerColor = StSurfaceRec,
                                        unfocusedBorderColor = StOutlineVariantRec, focusedBorderColor = StPrimaryRec,
                                        unfocusedTextColor = StOnSurfaceRec, focusedTextColor = StOnSurfaceRec
                                    )
                                )
                                ExposedDropdownMenu(expanded = indExpanded, onDismissRequest = { indExpanded = false }) {
                                    listOf("Technology", "Finance", "Healthcare", "Education").forEach { sel ->
                                        DropdownMenuItem(text = { Text(sel) }, onClick = { industry = sel; indExpanded = false })
                                    }
                                }
                            }
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Company Size", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StOnSurfaceRec)
                            Spacer(modifier = Modifier.height(6.dp))
                            ExposedDropdownMenuBox(expanded = sizeExpanded, onExpandedChange = { sizeExpanded = !sizeExpanded }) {
                                OutlinedTextField(
                                    value = companySize, onValueChange = {}, readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sizeExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = StSurfaceRec, focusedContainerColor = StSurfaceRec,
                                        unfocusedBorderColor = StOutlineVariantRec, focusedBorderColor = StPrimaryRec,
                                        unfocusedTextColor = StOnSurfaceRec, focusedTextColor = StOnSurfaceRec
                                    )
                                )
                                ExposedDropdownMenu(expanded = sizeExpanded, onDismissRequest = { sizeExpanded = false }) {
                                    listOf("1-50", "51-200", "201-500", "500+").forEach { sel ->
                                        DropdownMenuItem(text = { Text(sel) }, onClick = { companySize = sel; sizeExpanded = false })
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
                .background(StSurfaceContainerLowRec)
                .padding(24.dp)
        ) {
            val isValid = fullName.isNotBlank() && companyName.isNotBlank()
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
                                "company_name" to companyName,
                                "industry" to industry,
                                "company_size" to companySize
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
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .height(56.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StPrimaryRec, disabledContainerColor = StOutlineVariantRec, disabledContentColor = StTextSecondaryRec)
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

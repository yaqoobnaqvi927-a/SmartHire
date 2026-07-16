package com.cs22.example.smarthire.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var university by remember { mutableStateOf("") }
    var degree by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val surfaceColor = Color.White
    val accentColor = Color(0xFF10B981) // Emerald for Seekers

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to SmartHire",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Let's establish your Candidate baseline.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = university,
                    onValueChange = { university = it },
                    label = { Text("University / Institution", color = Color(0xFF64748B)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = degree,
                    onValueChange = { degree = it },
                    label = { Text("Degree Name (Optional)", color = Color(0xFF64748B)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = skills,
                    onValueChange = { skills = it },
                    label = { Text("Core Skills (Comma separated)", color = Color(0xFF64748B)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))

                val isValid = university.isNotBlank() && skills.isNotBlank()

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val uid = com.cs22.example.smarthire.firebase.FirebaseClient.uid ?: return@launch
                                val db = com.cs22.example.smarthire.firebase.FirebaseClient.db
                                
                                // Update main user doc
                                db.collection("users").document(uid).update("setup_complete", true).await()
                                
                                // Update candidate profile
                                val profile = mapOf(
                                    "university" to university,
                                    "degree" to degree,
                                    "skills" to skills.split(",").map { it.trim() }
                                )
                                db.collection("users").document(uid)
                                    .collection("candidate_profile").document("profile")
                                    .set(profile, com.google.firebase.firestore.SetOptions.merge()).await()

                                navController.navigate("job_seeker_flow") {
                                    popUpTo("seeker_setup") { inclusive = true }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("SetupError", "Firestore Setup Failed: ${e.message}", e)
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    enabled = isValid && !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Boot Dashboard", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

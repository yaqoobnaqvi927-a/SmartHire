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
fun RecruiterSetupScreen(navController: NavController) {
    var companyName by remember { mutableStateOf("") }
    var companySize by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val surfaceColor = Color.White
    val accentColor = Color(0xFF0EA5E9)

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
                    text = "Welcome to SmartHire Enterprise",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Let's configure your Company Workspace.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company Name", color = Color(0xFF64748B)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = industry,
                    onValueChange = { industry = it },
                    label = { Text("Primary Industry / Hiring Need", color = Color(0xFF64748B)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = companySize,
                    onValueChange = { companySize = it },
                    label = { Text("Company Size (e.g. 1-50)", color = Color(0xFF64748B)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("Your Designation (Optional)", color = Color(0xFF64748B)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))

                val isValid = companyName.isNotBlank() && industry.isNotBlank() && companySize.isNotBlank()

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val uid = com.cs22.example.smarthire.firebase.FirebaseClient.uid ?: return@launch
                                val db = com.cs22.example.smarthire.firebase.FirebaseClient.db
                                
                                // Update main user doc
                                db.collection("users").document(uid).update("setup_complete", true).await()
                                
                                // Update recruiter profile
                                val profile = mapOf(
                                    "company_name" to companyName,
                                    "industry" to industry,
                                    "company_size" to companySize,
                                    "designation" to designation
                                )
                                db.collection("users").document(uid)
                                    .collection("recruiter_profile").document("profile")
                                    .set(profile, com.google.firebase.firestore.SetOptions.merge()).await()

                                navController.navigate("recruiter_flow") {
                                    popUpTo("recruiter_setup") { inclusive = true }
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

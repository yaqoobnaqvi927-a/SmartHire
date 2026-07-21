package com.cs22.example.smarthire.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cs22.example.smarthire.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicProfileSetupScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val authState by viewModel.uiState.collectAsState()
    var isRecruiter by remember { mutableStateOf(authState.userRole == "recruiter") }

    // Student Fields
    var degree by remember { mutableStateOf("") }
    var university by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var certifications by remember { mutableStateOf("") }

    // Recruiter Fields
    var companyName by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf("") }
    var companySize by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Header
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFADC6FF), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SMARTHIRE SETUP", color = Color(0xFFADC6FF), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Text("Complete Your Profile", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            
            Spacer(modifier = Modifier.height(32.dp))

            // Premium Toggle Switch
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1F2937))
                    .clickable { isRecruiter = !isRecruiter },
                contentAlignment = Alignment.CenterStart
            ) {
                // Animated Pill
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .padding(4.dp)
                        .offset(x = if (isRecruiter) 116.dp else 0.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF3B82F6))
                        .animateContentSize(tween(300))
                )
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Text("Student", color = if (!isRecruiter) Color.White else Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Text("Recruiter", color = if (isRecruiter) Color.White else Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Glassmorphic Card Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF111827).copy(alpha = 0.6f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                    .padding(24.dp)
            ) {
                AnimatedVisibility(
                    visible = !isRecruiter,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 50 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -50 })
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PremiumTextField("Degree", Icons.Default.School, "e.g. B.S. Computer Science", degree) { degree = it }
                        PremiumTextField("University", Icons.Default.AccountBalance, "e.g. Stanford University", university) { university = it }
                        PremiumTextField("Skills (Comma-separated)", Icons.Default.Code, "Python, React, Machine Learning...", skills) { skills = it }
                        PremiumTextField("Certifications", Icons.Default.WorkspacePremium, "AWS Certified Developer", certifications) { certifications = it }
                    }
                }

                AnimatedVisibility(
                    visible = isRecruiter,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 50 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -50 })
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PremiumTextField("Company Name", Icons.Default.Domain, "e.g. Acme Corp", companyName) { companyName = it }
                        PremiumTextField("Industry", Icons.Default.Category, "Technology, Finance...", industry) { industry = it }
                        PremiumTextField("Company Size", Icons.Default.Groups, "1-50, 50-200...", companySize) { companySize = it }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Submit Button
            val isValid = if (!isRecruiter) degree.isNotBlank() && skills.isNotBlank() else companyName.isNotBlank()
            
            Button(
                onClick = {
                    val payload = if (!isRecruiter) {
                        mapOf("degree" to degree, "university" to university, "skills" to skills, "certifications" to certifications)
                    } else {
                        mapOf("company_name" to companyName, "industry" to industry, "company_size" to companySize)
                    }
                    
                    viewModel.setupProfile(if (isRecruiter) "recruiter" else "student", payload) {
                        if (isRecruiter) navController.navigate("recruiter_flow") { popUpTo("profile_setup") { inclusive = true } }
                        else navController.navigate("job_seeker_flow") { popUpTo("profile_setup") { inclusive = true } }
                    }
                },
                enabled = isValid && !authState.isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color(0xFF323537).copy(alpha = 0.5f)),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isValid) Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))),
                    contentAlignment = Alignment.Center
                ) {
                    if (authState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Complete Setup", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = if (isValid) Color.White else Color(0xFFC2C6D6))
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = if (isValid) Color.White else Color(0xFFC2C6D6))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumTextField(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, placeholder: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, fontSize = 14.sp, color = Color(0xFFC2C6D6), modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = { Icon(icon, contentDescription = null, tint = Color.Gray) },
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFF1F2937),
                focusedContainerColor = Color(0xFF1F2937),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color(0xFF3B82F6),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White
            )
        )
    }
}

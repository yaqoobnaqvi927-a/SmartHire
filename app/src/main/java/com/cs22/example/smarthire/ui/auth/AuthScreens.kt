package com.cs22.example.smarthire.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: AuthViewModel, navController: NavHostController, preSelectedRole: String = "seeker") {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val authState by viewModel.uiState.collectAsState()
    val isLoggingIn = authState.isLoading

    val googleSignInClient = remember {
        var webClientId = ""
        try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) webClientId = context.getString(resId)
        } catch (e: Exception) { e.printStackTrace() }
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).apply {
            if (webClientId.isNotEmpty()) requestIdToken(webClientId)
        }.requestEmail().build()
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account?.idToken != null) {
                    val backendRole = if (preSelectedRole == "recruiter") "recruiter" else "student"
                    viewModel.loginWithGoogle(account.idToken!!, backendRole)
                }
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(authState.isLoggedIn, authState.userRole) {
        if (authState.isLoggedIn && authState.userRole != null) {
            val role = authState.userRole
            val setupComplete = authState.setupComplete
            
            if (role == "recruiter") {
                if (setupComplete) navController.navigate("recruiter_flow") { popUpTo("auth") { inclusive = true } }
                else navController.navigate("recruiter_setup") { popUpTo("auth") { inclusive = true } }
            } else {
                if (setupComplete) navController.navigate("job_seeker_flow") { popUpTo("auth") { inclusive = true } }
                else navController.navigate("seeker_setup") { popUpTo("auth") { inclusive = true } }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        // Decorative glowing background elements
        Box(
            modifier = Modifier
                .offset(x = (-60).dp, y = (-60).dp)
                .size(300.dp)
                .background(Color(0xFF3B82F6).copy(alpha = 0.2f), CircleShape)
                .blur(120.dp)
                .align(Alignment.TopStart)
        )
        Box(
            modifier = Modifier
                .offset(x = 60.dp, y = 60.dp)
                .size(300.dp)
                .background(Color(0xFF8B5CF6).copy(alpha = 0.2f), CircleShape)
                .blur(120.dp)
                .align(Alignment.BottomEnd)
        )

        // Glass Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF111827).copy(alpha = 0.7f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                .padding(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF4D8EFF).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFADC6FF).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color(0xFFADC6FF), modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Welcome back to SmartHire",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE1E2E4),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "Enter your details to access your AI profile.",
                    fontSize = 14.sp,
                    color = Color(0xFFC2C6D6),
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                // Error Message
                if (authState.error != null) {
                    Text(
                        text = authState.error ?: "",
                        color = Color(0xFFFFB4AB),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Email Input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Email", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFFE1E2E4))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("name@company.com", color = Color(0xFFC2C6D6).copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = Color(0xFFC2C6D6)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF1F2937),
                            focusedContainerColor = Color(0xFF1F2937),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedTextColor = Color(0xFFE1E2E4),
                            focusedTextColor = Color(0xFFE1E2E4)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Password Input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Password", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFFE1E2E4))
                        Text("Forgot password?", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFADC6FF), modifier = Modifier.clickable { })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("••••••••", color = Color(0xFFC2C6D6).copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFC2C6D6)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color(0xFFC2C6D6)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF1F2937),
                            focusedContainerColor = Color(0xFF1F2937),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedTextColor = Color(0xFFE1E2E4),
                            focusedTextColor = Color(0xFFE1E2E4)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Login Button
                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6)))),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoggingIn) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Log In", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Divider
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF323537))
                    Text("Or continue with", fontSize = 12.sp, color = Color(0xFFC2C6D6), modifier = Modifier.padding(horizontal = 16.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF323537))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Google Button
                OutlinedButton(
                    onClick = { launcher.launch(googleSignInClient.signInIntent) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Normally you'd use a Google Icon painter here, using generic account for now
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Continue with Google", color = Color(0xFFE1E2E4), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Don't have an account? ", fontSize = 14.sp, color = Color(0xFFC2C6D6))
                    Text(
                        "Sign Up",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFADC6FF),
                        modifier = Modifier.clickable { navController.navigate("register") }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(viewModel: AuthViewModel, navController: NavHostController) {
    // Reusing the same stunning design for Registration
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("student") }

    val authState by viewModel.uiState.collectAsState()
    val isRegistering = authState.isLoading

    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) navController.popBackStack()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0B0F19)).verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.offset(x = (-60).dp, y = (-60).dp).size(300.dp).background(Color(0xFF3B82F6).copy(alpha = 0.2f), CircleShape).blur(120.dp).align(Alignment.TopStart))
        Box(modifier = Modifier.offset(x = 60.dp, y = 60.dp).size(300.dp).background(Color(0xFF8B5CF6).copy(alpha = 0.2f), CircleShape).blur(120.dp).align(Alignment.BottomEnd))

        Box(
            modifier = Modifier.fillMaxWidth().padding(24.dp).clip(RoundedCornerShape(28.dp)).background(Color(0xFF111827).copy(alpha = 0.7f)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp)).padding(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Create Account", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE1E2E4))
                Text("Join SmartHire today.", fontSize = 14.sp, color = Color(0xFFC2C6D6), modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))

                if (authState.error != null) {
                    Text(authState.error ?: "", color = Color(0xFFFFB4AB), fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
                }

                // Inputs
                OutlinedTextField(
                    value = username, onValueChange = { username = it }, placeholder = { Text("Username", color = Color(0xFFC2C6D6).copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFFC2C6D6)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFF1F2937), focusedContainerColor = Color(0xFF1F2937), unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color(0xFF3B82F6), unfocusedTextColor = Color(0xFFE1E2E4), focusedTextColor = Color(0xFFE1E2E4))
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = email, onValueChange = { email = it }, placeholder = { Text("Email", color = Color(0xFFC2C6D6).copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Default.Mail, null, tint = Color(0xFFC2C6D6)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFF1F2937), focusedContainerColor = Color(0xFF1F2937), unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color(0xFF3B82F6), unfocusedTextColor = Color(0xFFE1E2E4), focusedTextColor = Color(0xFFE1E2E4))
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it }, placeholder = { Text("Password", color = Color(0xFFC2C6D6).copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFFC2C6D6)) },
                    trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = Color(0xFFC2C6D6)) } },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFF1F2937), focusedContainerColor = Color(0xFF1F2937), unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color(0xFF3B82F6), unfocusedTextColor = Color(0xFFE1E2E4), focusedTextColor = Color(0xFFE1E2E4))
                )
                
                Spacer(Modifier.height(24.dp))
                Text("I am a:", color = Color(0xFFE1E2E4), fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(12.dp)).background(if (selectedRole == "student") Color(0xFF3B82F6).copy(alpha = 0.2f) else Color(0xFF1F2937)).border(1.dp, if (selectedRole == "student") Color(0xFF3B82F6) else Color.Transparent, RoundedCornerShape(12.dp)).clickable { selectedRole = "student" }, contentAlignment = Alignment.Center) {
                        Text("Job Seeker", color = if (selectedRole == "student") Color(0xFFADC6FF) else Color(0xFFC2C6D6))
                    }
                    Box(modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(12.dp)).background(if (selectedRole == "recruiter") Color(0xFF3B82F6).copy(alpha = 0.2f) else Color(0xFF1F2937)).border(1.dp, if (selectedRole == "recruiter") Color(0xFF3B82F6) else Color.Transparent, RoundedCornerShape(12.dp)).clickable { selectedRole = "recruiter" }, contentAlignment = Alignment.Center) {
                        Text("Recruiter", color = if (selectedRole == "recruiter") Color(0xFFADC6FF) else Color(0xFFC2C6D6))
                    }
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.register(email, password, username, selectedRole) },
                    modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues()
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6)))), contentAlignment = Alignment.Center) {
                        if (isRegistering) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Sign Up", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Already have an account? ", fontSize = 14.sp, color = Color(0xFFC2C6D6))
                    Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFADC6FF), modifier = Modifier.clickable { navController.popBackStack() })
                }
            }
        }
    }
}

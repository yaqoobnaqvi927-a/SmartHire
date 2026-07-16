package com.cs22.example.smarthire.ui.auth

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.ui.theme.*
import com.cs22.example.smarthire.ui.components.RoleOptionCard
import com.cs22.example.smarthire.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0.5f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SmartHireBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "SmartHire",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = SmartHirePrimary,
                modifier = Modifier.scale(scale.value)
            )
            Text(
                "AI Command Center",
                fontSize = 14.sp,
                color = SmartHireOnSurfaceVariant,
                letterSpacing = 2.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: AuthViewModel, navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("seeker") } // "seeker" or "recruiter"
    
    val context = LocalContext.current
    val authState by viewModel.uiState.collectAsState()
    val isLoggingIn = authState.isLoading

    // Google Sign-In Setup
    val googleSignInClient = remember {
        var webClientId = ""
        try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                webClientId = context.getString(resId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).apply {
            if (webClientId.isNotEmpty()) {
                requestIdToken(webClientId)
            }
        }.requestEmail().build()
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account?.idToken != null) {
                    val role = if (selectedRole == "recruiter") "recruiter" else "student"
                    viewModel.loginWithGoogle(account.idToken!!, role)
                } else {
                    android.widget.Toast.makeText(context, "Google Sign-In failed: No ID Token", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                e.printStackTrace()
                android.widget.Toast.makeText(context, "Google Sign-In error (Code ${e.statusCode})", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
             android.widget.Toast.makeText(context, "Google Sign-In cancelled", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val cvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { 
            viewModel.setCvUri(it) 
            android.widget.Toast.makeText(context, "CV selected successfully", android.widget.Toast.LENGTH_SHORT).show()
        } ?: run {
            android.widget.Toast.makeText(context, "No CV selected", android.widget.Toast.LENGTH_SHORT).show()
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

    Box(modifier = Modifier.fillMaxSize().background(SmartHireBackground)) {
        // Decorative Background Orbs & Grid (Simplified for Compose)
        Box(modifier = Modifier.fillMaxSize()) {
            // Top orb
            Box(modifier = Modifier.offset((-100).dp, (-100).dp).size(400.dp).background(SmartHireSurfaceContainer.copy(alpha = 0.5f), CircleShape))
            // Bottom orb
            Box(modifier = Modifier.align(Alignment.BottomEnd).offset(100.dp, 100.dp).size(300.dp).background(SmartHireOutline.copy(alpha = 0.3f), CircleShape))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Branding
            Text("SmartHire", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = SmartHirePrimary)
            Text(
                "The AI-driven command center for talent.",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = SmartHireOnSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                "Discover exceptional opportunities or talent using our advanced algorithmic matching.",
                fontSize = 14.sp,
                color = SmartHireOnSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // Security Badge
            Surface(
                color = SmartHireSurfaceContainer,
                shape = CircleShape,
                border = BorderStroke(1.dp, SmartHireOutline)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = SmartHirePrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enterprise-grade security", fontSize = 12.sp, color = SmartHireOnSurface)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Auth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Select Your Path", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
                    Text("How do you want to use SmartHire?", fontSize = 12.sp, color = SmartHireOnSurfaceVariant)
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Role Selector
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RoleOptionCard(
                            modifier = Modifier.weight(1f),
                            label = "Finding Work",
                            icon = Icons.Default.Work,
                            isSelected = selectedRole == "seeker",
                            onClick = { selectedRole = "seeker" }
                        )
                        RoleOptionCard(
                            modifier = Modifier.weight(1f),
                            label = "Hiring Talents",
                            icon = Icons.Default.Groups,
                            isSelected = selectedRole == "recruiter",
                            onClick = { selectedRole = "recruiter" }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Email Input
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = SmartHireOnSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SmartHireOutline,
                            focusedBorderColor = SmartHirePrimary,
                            unfocusedContainerColor = SmartHireSurfaceContainer,
                            focusedContainerColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Password (Simplified for the new UI)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SmartHireOnSurfaceVariant) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SmartHireOutline,
                            focusedBorderColor = SmartHirePrimary,
                            unfocusedContainerColor = SmartHireSurfaceContainer,
                            focusedContainerColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.login(email, password) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SmartHirePrimary),
                        enabled = !isLoggingIn
                    ) {
                        if (isLoggingIn) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else {
                            Text("Boot to Dashboard", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = SmartHireOutline)
                        Text("OR", modifier = Modifier.padding(horizontal = 16.dp), fontSize = 12.sp, color = SmartHireOnSurfaceVariant)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = SmartHireOutline)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = { launcher.launch(googleSignInClient.signInIntent) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SmartHireOutline)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = SmartHirePrimary)
                            Spacer(Modifier.width(12.dp))
                            Text("Continue with Google", color = SmartHireOnSurface, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // CV/JD Dropzone (Visual representation)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(SmartHireSurfaceContainer, RoundedCornerShape(16.dp))
                            .border(2.dp, SmartHireOutline, RoundedCornerShape(16.dp))
                            .clickable { cvLauncher.launch("application/pdf") },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, tint = if (authState.cvUri != null) SmartHireSuccess else SmartHirePrimary)
                            Text(if (authState.cvUri != null) "CV Selected ✓" else "Upload CV / Job Description", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
                            Text("Auto-fill profile with AI", fontSize = 10.sp, color = SmartHireOnSurfaceVariant)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(onClick = { navController.navigate("register") }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Don't have an account? Sign up", color = SmartHirePrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Register Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(viewModel: AuthViewModel, navController: NavHostController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("student") }

    val authState by viewModel.uiState.collectAsState()
    val isRegistering = authState.isLoading

    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            navController.popBackStack()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(SmartHireBackground), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.fillMaxWidth().padding(24.dp), shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(8.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SmartHireOnSurface)
                Text("Join SmartHire today.", fontSize = 14.sp, color = SmartHireOnSurfaceVariant)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(value = username, onValueChange = { username = it }, placeholder = { Text("Username") },
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = SmartHireOnSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = SmartHireOutline, focusedBorderColor = SmartHirePrimary, unfocusedContainerColor = SmartHireSurfaceContainer))
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, placeholder = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Mail, null, tint = SmartHireOnSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = SmartHireOutline, focusedBorderColor = SmartHirePrimary, unfocusedContainerColor = SmartHireSurfaceContainer))
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, placeholder = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = SmartHireOnSurfaceVariant) },
                    visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = SmartHireOutline, focusedBorderColor = SmartHirePrimary, unfocusedContainerColor = SmartHireSurfaceContainer))
                Spacer(Modifier.height(16.dp))

                Text("Select Role", fontWeight = FontWeight.SemiBold, color = SmartHireOnSurfaceVariant, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RoleOptionCard(Modifier.weight(1f), "Job Seeker", Icons.Default.Work, role == "student") { role = "student" }
                    RoleOptionCard(Modifier.weight(1f), "Recruiter", Icons.Default.Groups, role == "recruiter") { role = "recruiter" }
                }
                Spacer(Modifier.height(20.dp))

                if (authState.error != null) {
                    Surface(color = Color(0xFFFEF2F2), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFFF87171)), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Text(authState.error ?: "Error", color = Color(0xFF991B1B), fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                    }
                }

                Button(onClick = { viewModel.register(email, password, username, role) }, modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = SmartHirePrimary), enabled = !isRegistering) {
                    if (isRegistering) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    else Text("Sign Up", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = { navController.popBackStack() }) { Text("Already have an account? Log in", color = SmartHirePrimary) }
            }
        }
    }
}

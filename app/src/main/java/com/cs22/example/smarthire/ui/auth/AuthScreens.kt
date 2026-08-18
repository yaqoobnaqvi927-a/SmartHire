package com.cs22.example.smarthire.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

val StBgAuth = Color(0xFFF3F7FF)
val StSurfaceAuth = Color(0xFFFFFFFF)
val StPrimaryAuth = Color(0xFF0057C0)
val StPrimaryContainerAuth = Color(0xFF2870E3)
val StOnSurfaceAuth = Color(0xFF191B22)
val StTextSecondaryAuth = Color(0xFF68738A)
val StOutlineVariantAuth = Color(0xFFDCE5F3)
val StErrorAuth = Color(0xFFBA1A1A)

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
                    viewModel.loginWithGoogle(account.idToken ?: "", backendRole)
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
                else navController.navigate("profile_setup") { popUpTo("auth") { inclusive = true } }
            } else {
                if (setupComplete) navController.navigate("job_seeker_flow") { popUpTo("auth") { inclusive = true } }
                else navController.navigate("profile_setup") { popUpTo("auth") { inclusive = true } }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StBgAuth)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 448.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(StSurfaceAuth)
                    .border(1.dp, StOutlineVariantAuth, RoundedCornerShape(18.dp))
                    .padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to Smart Hire",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = StOnSurfaceAuth,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Sign in to continue.",
                        fontSize = 14.sp,
                        color = StTextSecondaryAuth,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    if (authState.error != null) {
                        Text(
                            text = authState.error ?: "",
                            color = StErrorAuth,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Email
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Email", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StOnSurfaceAuth)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("name@email.com", color = StTextSecondaryAuth) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = StSurfaceAuth,
                                focusedContainerColor = StSurfaceAuth,
                                unfocusedBorderColor = StOutlineVariantAuth,
                                focusedBorderColor = StPrimaryAuth,
                                unfocusedTextColor = StOnSurfaceAuth,
                                focusedTextColor = StOnSurfaceAuth
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Password", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StOnSurfaceAuth)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("••••••••", color = StTextSecondaryAuth) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = StTextSecondaryAuth
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = StSurfaceAuth,
                                focusedContainerColor = StSurfaceAuth,
                                unfocusedBorderColor = StOutlineVariantAuth,
                                focusedBorderColor = StPrimaryAuth,
                                unfocusedTextColor = StOnSurfaceAuth,
                                focusedTextColor = StOnSurfaceAuth
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { viewModel.login(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StPrimaryAuth),
                        enabled = !isLoggingIn
                    ) {
                        if (isLoggingIn) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = StOutlineVariantAuth)
                        Text("Or", fontSize = 12.sp, color = StTextSecondaryAuth, modifier = Modifier.padding(horizontal = 16.dp))
                        HorizontalDivider(modifier = Modifier.weight(1f), color = StOutlineVariantAuth)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = { launcher.launch(googleSignInClient.signInIntent) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = StSurfaceAuth),
                        border = BorderStroke(1.dp, StOutlineVariantAuth)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = StOnSurfaceAuth)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Continue with Google", color = StOnSurfaceAuth, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account? ", fontSize = 14.sp, color = StTextSecondaryAuth)
                Text(
                    "Sign Up",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = StPrimaryAuth,
                    modifier = Modifier.clickable { navController.navigate("register/$preSelectedRole") }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(viewModel: AuthViewModel, navController: NavHostController, preSelectedRole: String = "student") {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(preSelectedRole) }

    val authState by viewModel.uiState.collectAsState()
    val isRegistering = authState.isLoading

    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) navController.popBackStack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StBgAuth)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 448.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(StSurfaceAuth)
                    .border(1.dp, StOutlineVariantAuth, RoundedCornerShape(18.dp))
                    .padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Create Account", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = StOnSurfaceAuth)
                    Text("Join Smart Hire today.", fontSize = 14.sp, color = StTextSecondaryAuth, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))

                    if (authState.error != null) {
                        Text(authState.error ?: "", color = StErrorAuth, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Username", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StOnSurfaceAuth)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = username, onValueChange = { username = it },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = StSurfaceAuth, focusedContainerColor = StSurfaceAuth, unfocusedBorderColor = StOutlineVariantAuth, focusedBorderColor = StPrimaryAuth, unfocusedTextColor = StOnSurfaceAuth, focusedTextColor = StOnSurfaceAuth),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Email", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StOnSurfaceAuth)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email, onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = StSurfaceAuth, focusedContainerColor = StSurfaceAuth, unfocusedBorderColor = StOutlineVariantAuth, focusedBorderColor = StPrimaryAuth, unfocusedTextColor = StOnSurfaceAuth, focusedTextColor = StOnSurfaceAuth),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Password", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StOnSurfaceAuth)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password, onValueChange = { password = it },
                            trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = StTextSecondaryAuth) } },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = StSurfaceAuth, focusedContainerColor = StSurfaceAuth, unfocusedBorderColor = StOutlineVariantAuth, focusedBorderColor = StPrimaryAuth, unfocusedTextColor = StOnSurfaceAuth, focusedTextColor = StOnSurfaceAuth),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text("I am a:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StOnSurfaceAuth, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedRole == "student") Color(0xFFEAF2FF) else StSurfaceAuth)
                                .border(1.dp, if (selectedRole == "student") StPrimaryAuth else StOutlineVariantAuth, RoundedCornerShape(12.dp))
                                .clickable { selectedRole = "student" },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Job Seeker", color = if (selectedRole == "student") StPrimaryAuth else StTextSecondaryAuth, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedRole == "recruiter") Color(0xFFEAF2FF) else StSurfaceAuth)
                                .border(1.dp, if (selectedRole == "recruiter") StPrimaryAuth else StOutlineVariantAuth, RoundedCornerShape(12.dp))
                                .clickable { selectedRole = "recruiter" },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Recruiter", color = if (selectedRole == "recruiter") StPrimaryAuth else StTextSecondaryAuth, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { viewModel.register(email, password, username, selectedRole) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StPrimaryAuth),
                        enabled = !isRegistering
                    ) {
                        if (isRegistering) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", fontSize = 14.sp, color = StTextSecondaryAuth)
                Text(
                    "Log In",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = StPrimaryAuth,
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
            }
        }
    }
}

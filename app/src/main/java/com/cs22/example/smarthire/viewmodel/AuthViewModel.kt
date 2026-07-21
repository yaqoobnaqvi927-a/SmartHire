package com.cs22.example.smarthire.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs22.example.smarthire.firebase.AuthRepository
import com.cs22.example.smarthire.firebase.FirebaseClient
import com.cs22.example.smarthire.model.*
import com.cs22.example.smarthire.network.RetrofitClient
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: FirebaseUser? = null,
    val userRole: String? = null,       // "student" or "recruiter"
    val setupComplete: Boolean = false,
    val cvUri: android.net.Uri? = null,
    val error: String? = null
)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Observe Firebase auth state in real-time
        viewModelScope.launch {
            FirebaseClient.authStateFlow().collect { user ->
                if (user != null) {
                    val data = AuthRepository.getUserData(user.uid)
                    val role = data?.get("role") as? String ?: "student"
                    _uiState.update {
                        it.copy(
                            isLoggedIn = true,
                            currentUser = user,
                            userRole = RetrofitClient.tokenManager.getRole() ?: role,
                            setupComplete = data?.get("setup_complete") as? Boolean ?: false,
                            isLoading = false
                        )
                    }
                    registerFcmTokenOnBackend()
                } else {
                    RetrofitClient.tokenManager.clear()
                    _uiState.update {
                        it.copy(isLoggedIn = false, currentUser = null, userRole = null, setupComplete = false)
                    }
                }
            }
        }
    }

    private fun registerFcmTokenOnBackend() {
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    viewModelScope.launch {
                        try {
                            RetrofitClient.api.registerFcmToken(mapOf("fcm_token" to token, "device_type" to "android"))
                        } catch (e: Exception) {
                            // non-critical failure
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Firebase messaging might not be initialized
        }
    }

    // ── Email Registration ─────────────────────────────────────────────────
    fun register(email: String, password: String, username: String, role: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            AuthRepository.registerWithEmail(email, password, username, role)
                .onSuccess { uid ->
                    try {
                        val regRequest = RegisterRequest(
                            username = username,
                            email = email,
                            password = password,
                            role_type = role
                        )
                        RetrofitClient.api.register(regRequest)
                        
                        // Login to Django to get token
                        val authResponse = RetrofitClient.api.login(LoginRequest(username, password))
                        RetrofitClient.tokenManager.saveTokens(authResponse.access, authResponse.refresh)
                        RetrofitClient.tokenManager.saveRole(authResponse.role ?: role)
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                isLoggedIn = true, 
                                userRole = authResponse.role ?: role,
                                setupComplete = authResponse.setup_complete
                            ) 
                        }
                        registerFcmTokenOnBackend()
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isLoading = false, error = "Firebase registration succeeded, but Django backend sync failed: ${e.message}") }
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    // ── Email Login ────────────────────────────────────────────────────────
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            AuthRepository.loginWithEmail(email, password)
                .onSuccess { uid ->
                    try {
                        val data = AuthRepository.getUserData(uid)
                        val username = data?.get("username") as? String ?: email.substringBefore("@")
                        val role = data?.get("role") as? String ?: "student"
                        
                        val authResponse = RetrofitClient.api.login(LoginRequest(username, password))
                        RetrofitClient.tokenManager.saveTokens(authResponse.access, authResponse.refresh)
                        RetrofitClient.tokenManager.saveRole(authResponse.role ?: role)
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                isLoggedIn = true, 
                                userRole = authResponse.role ?: role,
                                setupComplete = authResponse.setup_complete
                            ) 
                        }
                        registerFcmTokenOnBackend()
                    } catch (e: Exception) {
                        // Attempt fallback registration if Django user doesn't exist yet
                        try {
                            val data = AuthRepository.getUserData(uid)
                            val username = data?.get("username") as? String ?: email.substringBefore("@")
                            val role = data?.get("role") as? String ?: "student"
                            
                            val regRequest = RegisterRequest(
                                username = username,
                                email = email,
                                password = password,
                                role_type = role
                            )
                            RetrofitClient.api.register(regRequest)
                            
                            val authResponse = RetrofitClient.api.login(LoginRequest(username, password))
                            RetrofitClient.tokenManager.saveTokens(authResponse.access, authResponse.refresh)
                            RetrofitClient.tokenManager.saveRole(authResponse.role ?: role)
                            
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    isLoggedIn = true, 
                                    userRole = authResponse.role ?: role,
                                    setupComplete = authResponse.setup_complete
                                ) 
                            }
                            registerFcmTokenOnBackend()
                        } catch (fallbackEx: Exception) {
                            _uiState.update { it.copy(isLoading = false, error = "Firebase login succeeded, but Django backend sync failed: ${fallbackEx.message ?: e.message}") }
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    // ── Google Sign-In ─────────────────────────────────────────────────────
    fun loginWithGoogle(idToken: String, selectedRole: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val dbRole = if (selectedRole == "recruiter") "recruiter" else "student"
            AuthRepository.loginWithGoogle(idToken, dbRole)
                .onSuccess { (uid, isNew) ->
                    try {
                        val role = dbRole // Force the selected role for the demo presentation
                        
                        val googleReq = GoogleLoginRequest(id_token = idToken, role_type = role)
                        val authResponse = RetrofitClient.api.googleLogin(googleReq)
                        RetrofitClient.tokenManager.saveTokens(authResponse.access, authResponse.refresh)
                        RetrofitClient.tokenManager.saveRole(authResponse.role ?: role)
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                isLoggedIn = true, 
                                userRole = authResponse.role ?: role,
                                setupComplete = authResponse.setup_complete
                            ) 
                        }
                        registerFcmTokenOnBackend()
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isLoading = false, error = "Firebase login succeeded, but Django backend Google sync failed: ${e.message}") }
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    // ── Profile Setup ──────────────────────────────────────────────────────
    fun setupProfile(role: String, payload: Map<String, String>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Call the Django setup endpoint
                RetrofitClient.api.setupProfile(payload)
                
                // Update local state to prevent login loop
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        setupComplete = true
                    ) 
                }
                
                // Also update Firestore if you still want Firebase sync
                val uid = com.cs22.example.smarthire.firebase.FirebaseClient.uid
                if (uid != null) {
                    val db = com.cs22.example.smarthire.firebase.FirebaseClient.db
                    db.collection("users").document(uid).update("setup_complete", true)
                }
                
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to complete setup") }
            }
        }
    }

    // ── Logout ─────────────────────────────────────────────────────────────
    fun logout() {
        AuthRepository.logout()
        RetrofitClient.tokenManager.clear()
        _uiState.update { AuthUiState() }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    
    fun setCvUri(uri: android.net.Uri) {
        _uiState.update { it.copy(cvUri = uri) }
    }
}

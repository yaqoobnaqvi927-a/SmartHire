package com.cs22.example.smarthire.firebase

import com.cs22.example.smarthire.firebase.FirebaseClient.auth
import com.cs22.example.smarthire.firebase.FirebaseClient.db
import com.cs22.example.smarthire.firebase.Collections
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Handles all Firebase Authentication operations:
 * - Email/Password login & registration
 * - Google Sign-In
 * - Logout
 * - Profile creation in Firestore after registration
 */
object AuthRepository {

    // ── Email / Password Registration ──────────────────────────────────────
    suspend fun registerWithEmail(
        email: String,
        password: String,
        username: String,
        role: String  // "student" or "recruiter"
    ): Result<String> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user!!.uid

        // Create user profile document in Firestore
        val userDoc = mapOf(
            "uid"        to uid,
            "email"      to email,
            "username"   to username,
            "role"       to role,
            "created_at" to com.google.firebase.Timestamp.now(),
            "setup_complete" to false
        )
        db.collection(Collections.USERS).document(uid).set(userDoc).await()

        // Create role-specific profile sub-document
        val profileCollection = if (role == "recruiter") "recruiter_profile" else "candidate_profile"
        db.collection(Collections.USERS).document(uid)
            .collection(profileCollection).document("profile")
            .set(mapOf("uid" to uid, "role" to role)).await()

        uid
    }

    // ── Email / Password Login ─────────────────────────────────────────────
    suspend fun loginWithEmail(email: String, password: String): Result<String> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user!!.uid
    }

    // ── Google Sign-In ─────────────────────────────────────────────────────
    suspend fun loginWithGoogle(idToken: String, selectedRole: String): Result<Pair<String, Boolean>> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val uid = result.user!!.uid
        val isNewUser = result.additionalUserInfo?.isNewUser == true

        if (isNewUser) {
            val dbRole = if (selectedRole == "recruiter") "recruiter" else "student"
            // Auto-create Firestore profile for new Google users
            val userDoc = mapOf(
                "uid"            to uid,
                "email"          to result.user!!.email,
                "username"       to (result.user!!.displayName ?: "User"),
                "role"           to dbRole,
                "created_at"     to com.google.firebase.Timestamp.now(),
                "setup_complete" to false,
                "photo_url"      to result.user!!.photoUrl?.toString()
            )
            db.collection(Collections.USERS).document(uid).set(userDoc).await()

            // Create role-specific profile sub-document
            val profileCollection = if (dbRole == "recruiter") "recruiter_profile" else "candidate_profile"
            db.collection(Collections.USERS).document(uid)
                .collection(profileCollection).document("profile")
                .set(mapOf("uid" to uid, "role" to dbRole)).await()
        }
        Pair(uid, isNewUser)
    }

    // ── Fetch User Role from Firestore ─────────────────────────────────────
    suspend fun getUserRole(uid: String): String? {
        return try {
            val doc = db.collection(Collections.USERS).document(uid).get().await()
            doc.getString("role")
        } catch (e: Exception) { null }
    }

    suspend fun getUserData(uid: String): Map<String, Any>? {
        return try {
            val doc = db.collection(Collections.USERS).document(uid).get().await()
            doc.data
        } catch (e: Exception) { null }
    }

    // ── Logout ─────────────────────────────────────────────────────────────
    fun logout() = auth.signOut()
}

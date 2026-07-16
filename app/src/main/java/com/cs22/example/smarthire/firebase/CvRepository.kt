package com.cs22.example.smarthire.firebase

import android.net.Uri
import com.cs22.example.smarthire.firebase.FirebaseClient.db
import com.cs22.example.smarthire.firebase.FirebaseClient.storage
import com.cs22.example.smarthire.firebase.FirebaseClient.functions
import com.cs22.example.smarthire.firebase.FirebaseClient.uid
import com.cs22.example.smarthire.firebase.Collections
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Handles CV uploads to Firebase Storage and triggers the
 * Gemini AI parsing via Firebase Cloud Functions.
 */
object CvRepository {

    // ── Upload CV PDF to Firebase Storage ─────────────────────────────────
    suspend fun uploadCv(fileUri: Uri, fileName: String): Result<String> = runCatching {
        val currentUid = uid ?: throw Exception("User not logged in")

        // Upload to Storage: cvs/{uid}/{fileName}
        val storageRef = storage.reference
            .child("cvs/$currentUid/$fileName")

        storageRef.putFile(fileUri).await()
        val downloadUrl = storageRef.downloadUrl.await().toString()

        // Save CV record to Firestore
        val docRef = db.collection(Collections.CVS).document()
        db.collection(Collections.CVS).document(docRef.id).set(
            mapOf(
                "id"           to docRef.id,
                "user_uid"     to currentUid,
                "file_name"    to fileName,
                "download_url" to downloadUrl,
                "uploaded_at"  to Timestamp.now(),
                "is_primary"   to false,
                "parsed"       to false
            )
        ).await()

        // Trigger Gemini AI parsing via Cloud Function
        triggerCvParsing(currentUid, downloadUrl, docRef.id)

        downloadUrl
    }

    // ── Trigger Cloud Function to parse CV with Gemini ────────────────────
    private suspend fun triggerCvParsing(
        userUid: String,
        downloadUrl: String,
        cvId: String
    ) {
        try {
            functions
                .getHttpsCallable("parseCvWithGemini")
                .call(mapOf(
                    "userUid"     to userUid,
                    "downloadUrl" to downloadUrl,
                    "cvId"        to cvId
                )).await()
        } catch (e: Exception) {
            // Parsing failed — CV is still saved, profile update will retry
            e.printStackTrace()
        }
    }

    // ── Get all CVs for current user (real-time) ──────────────────────────
    fun getMyCvsFlow() = callbackFlow<List<Map<String, Any>>> {
        val listener = db.collection(Collections.CVS)
            .whereEqualTo("user_uid", uid)
            .orderBy("uploaded_at", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val cvs = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                trySend(cvs)
            }
        awaitClose { listener.remove() }
    }

    // ── Set a CV as primary ───────────────────────────────────────────────
    suspend fun setPrimary(cvId: String): Result<Unit> = runCatching {
        val currentUid = uid ?: return@runCatching
        // Unset all first
        val all = db.collection(Collections.CVS)
            .whereEqualTo("user_uid", currentUid).get().await()
        val batch = db.batch()
        all.documents.forEach { doc ->
            batch.update(doc.reference, "is_primary", doc.id == cvId)
        }
        batch.commit().await()
    }
}

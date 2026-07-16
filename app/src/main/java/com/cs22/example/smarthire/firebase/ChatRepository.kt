package com.cs22.example.smarthire.firebase

import com.cs22.example.smarthire.firebase.FirebaseClient.db
import com.cs22.example.smarthire.firebase.FirebaseClient.uid
import com.cs22.example.smarthire.firebase.Collections
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Handles real-time chat messages and interview scheduling in Firestore.
 */
object ChatRepository {

    // ── Real-time messages between two users ──────────────────────────────
    fun getMessagesFlow(conversationId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val listener = db.collection(Collections.MESSAGES)
            .document(conversationId)
            .collection("thread")
            .orderBy("sent_at", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val messages = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    // ── Send a message ────────────────────────────────────────────────────
    suspend fun sendMessage(
        conversationId: String,
        text: String,
        receiverUid: String
    ): Result<Unit> = runCatching {
        val senderUid = uid ?: throw Exception("Not logged in")
        val msgRef = db.collection(Collections.MESSAGES)
            .document(conversationId)
            .collection("thread")
            .document()

        msgRef.set(mapOf(
            "id"          to msgRef.id,
            "text"        to text,
            "sender_uid"  to senderUid,
            "receiver_uid" to receiverUid,
            "sent_at"     to Timestamp.now(),
            "read"        to false
        )).await()

        // Update conversation metadata
        db.collection(Collections.MESSAGES).document(conversationId).set(mapOf(
            "last_message"   to text,
            "last_sender"    to senderUid,
            "updated_at"     to Timestamp.now(),
            "participants"   to listOf(senderUid, receiverUid)
        ), com.google.firebase.firestore.SetOptions.merge()).await()
    }

    // ── Get all conversations for current user (real-time) ────────────────
    fun getConversationsFlow(): Flow<List<Map<String, Any>>> = callbackFlow {
        val listener = db.collection(Collections.MESSAGES)
            .whereArrayContains("participants", uid ?: "")
            .orderBy("updated_at", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val conversations = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                trySend(conversations)
            }
        awaitClose { listener.remove() }
    }
}

/**
 * Handles interview scheduling in Firestore.
 */
object InterviewRepository {

    // ── Real-time upcoming interviews for current user ────────────────────
    fun getUpcomingInterviewsFlow(): Flow<List<Map<String, Any>>> = callbackFlow {
        val now = Timestamp.now()
        val listener = db.collection(Collections.INTERVIEWS)
            .whereEqualTo("participant_uid", uid)
            .whereGreaterThan("scheduled_at", now)
            .orderBy("scheduled_at")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val interviews = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                trySend(interviews)
            }
        awaitClose { listener.remove() }
    }

    // ── Schedule an interview ─────────────────────────────────────────────
    suspend fun scheduleInterview(
        applicationId: String,
        candidateUid: String,
        scheduledAt: Timestamp,
        type: String, // "video", "phone", "onsite"
        meetingLink: String = ""
    ): Result<String> = runCatching {
        val docRef = db.collection(Collections.INTERVIEWS).document()
        docRef.set(mapOf(
            "id"              to docRef.id,
            "application_id"  to applicationId,
            "recruiter_uid"   to uid,
            "participant_uid" to candidateUid,
            "scheduled_at"    to scheduledAt,
            "type"            to type,
            "meeting_link"    to meetingLink,
            "status"          to "scheduled",
            "created_at"      to Timestamp.now()
        )).await()
        docRef.id
    }
}

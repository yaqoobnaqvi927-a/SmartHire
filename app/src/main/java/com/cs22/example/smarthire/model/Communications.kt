package com.cs22.example.smarthire.model

data class UserDetail(
    val id: String = "",
    val username: String,
    val full_name: String? = null
)

data class ChatMessage(
    val id: String = "",
    val thread: String? = null,
    val sender_id: String? = null,
    val sender_name: String? = null,
    val content: String,
    val timestamp: String,
    val sender_details: UserDetail? = null
)

data class ChatThreadResponse(
    val thread_id: String = "",
    val messages: List<ChatMessage>
)

data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val action_url: String? = null,
    val is_read: Boolean = false,
    val timestamp: String = ""
)

data class SendMessageRequest(
    val application_id: String,
    val text: String
)

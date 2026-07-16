package com.cs22.example.smarthire.network

import android.util.Log
import com.cs22.example.smarthire.model.ChatMessage
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*
import org.json.JSONObject

class ChatWebSocketClient(private val tokenManager: TokenManager) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val gson = Gson()
    
    private val _messages = MutableSharedFlow<ChatMessage>(replay = 10)
    val messages: SharedFlow<ChatMessage> = _messages

    fun connect(threadId: String) {
        val wsUrl = "ws://192.168.1.40:8000/ws/chat/$threadId/"
        val request = Request.Builder()
            .url(wsUrl)
            .build()
            
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSockets", "Connected to Chat Thread $threadId")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSockets", "Message Received: $text")
                try {
                    val message = gson.fromJson(text, ChatMessage::class.java)
                    _messages.tryEmit(message)
                } catch (e: Exception) {
                    Log.e("WebSockets", "Failed to parse websocket message", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSockets", "WebSocket Failure", t)
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSockets", "WebSocket Closed: $reason")
            }
        })
    }
    
    fun sendMessage(content: String, senderId: String) {
        val payload = JSONObject().apply {
            put("content", content)
            put("sender_id", senderId)
        }
        webSocket?.send(payload.toString())
    }

    fun disconnect() {
        webSocket?.close(1000, "User disposed Chat Screen")
        webSocket = null
    }
}

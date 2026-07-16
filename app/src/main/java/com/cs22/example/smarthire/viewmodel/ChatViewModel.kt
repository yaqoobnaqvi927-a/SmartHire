package com.cs22.example.smarthire.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs22.example.smarthire.model.ChatMessage
import com.cs22.example.smarthire.network.ChatWebSocketClient
import com.cs22.example.smarthire.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val threadId: String? = null
)

class ChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var wsClient: ChatWebSocketClient? = null

    fun connectToChat(applicationId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Determine user identity
                val profile = RetrofitClient.api.getProfile()
                val currentUserId = profile.id
                
                // Fetch existing messages and retrieve thread_id
                val response = RetrofitClient.api.getMessages(applicationId)
                val threadId = response.thread_id
                
                _uiState.update { 
                    it.copy(
                        messages = response.messages.sortedByDescending { msg -> msg.timestamp }, // reverseLayout needs newest first
                        currentUserId = currentUserId,
                        threadId = threadId,
                        isLoading = false
                    ) 
                }

                // Connect WebSocket
                wsClient = ChatWebSocketClient(RetrofitClient.tokenManager)
                wsClient?.connect(threadId)
                
                // Collect incoming WebSocket streams strictly enforcing UDF
                wsClient?.messages?.collect { newMsg ->
                    _uiState.update { state ->
                        val updatedList = listOf(newMsg) + state.messages // Prepend for reverseLayout
                        state.copy(messages = updatedList.distinctBy { it.id })
                    }
                }
                
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun sendMessage(content: String) {
        val state = _uiState.value
        val threadId = state.threadId
        val senderId = state.currentUserId
        
        if (threadId != null && senderId.isNotEmpty() && content.isNotBlank()) {
            wsClient?.sendMessage(content, senderId)
            // Predictable UI update or rely on ws echo: The server broadcasts it back explicitly,
            // so we don't necessarily need to optimistically insert, though we could.
        }
    }

    override fun onCleared() {
        super.onCleared()
        wsClient?.disconnect()
    }
}

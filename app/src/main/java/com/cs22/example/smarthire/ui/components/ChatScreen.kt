package com.cs22.example.smarthire.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs22.example.smarthire.ui.theme.*
import com.cs22.example.smarthire.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    applicationId: String, 
    modifier: Modifier = Modifier,
    navController: androidx.navigation.NavController? = null,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var textInput by remember { mutableStateOf("") }

    LaunchedEffect(applicationId) {
        viewModel.connectToChat(applicationId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Application Chat", color = StOnSurface, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF20B26B)))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = StOnSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { navController?.navigate("video_call") }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = StPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StSurface,
                    titleContentColor = StOnSurface
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(StSurface)) {
                Divider(color = StOutlineVariant, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = StTextSecondary)
                    }
                    
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        placeholder = { Text("Type a message...", color = StTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = StSurfaceContainer,
                            unfocusedContainerColor = StSurfaceContainer,
                            focusedTextColor = StOnSurface,
                            unfocusedTextColor = StOnSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.EmojiEmotions, contentDescription = "Emoji", tint = StTextSecondary)
                            }
                        }
                    )
                    
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendMessage(textInput.trim())
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(StPrimary, CircleShape)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        },
        containerColor = StBackground
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages) { msg ->
                    val isMe = msg.sender_id == uiState.currentUserId
                    
                    if (msg.sender_id == "system") {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = msg.content,
                                color = StTextSecondary,
                                fontStyle = FontStyle.Italic,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Box(modifier = Modifier.fillMaxWidth(0.75f), contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart) {
                                Surface(
                                    color = if (isMe) StPrimary else StSurface,
                                    shape = RoundedCornerShape(
                                        topStart = 18.dp,
                                        topEnd = 18.dp,
                                        bottomStart = if (isMe) 18.dp else 4.dp,
                                        bottomEnd = if (isMe) 4.dp else 18.dp
                                    ),
                                    shadowElevation = if (isMe) 0.dp else 2.dp,
                                    modifier = if (!isMe) Modifier.border(1.dp, StOutlineVariant, RoundedCornerShape(
                                        topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp
                                    )) else Modifier
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = msg.content,
                                            color = if (isMe) Color.White else StOnSurface,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "10:30 AM", // Replace with real timestamp if available
                                            color = if (isMe) Color.White.copy(alpha = 0.7f) else StTextSecondary,
                                            fontSize = 11.sp,
                                            modifier = Modifier.align(if (isMe) Alignment.Start else Alignment.End)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

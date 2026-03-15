package com.example.lingora_fe.user.chatbot.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.lingora_fe.user.chatbot.domain.model.ChatSender
import com.example.lingora_fe.user.chatbot.domain.model.ConversationCorrection
import com.example.lingora_fe.user.chatbot.domain.model.ConversationMessage
import com.example.lingora_fe.user.chatbot.presentation.components.CorrectionBlock
import com.example.lingora_fe.user.chatbot.presentation.components.PhaseIndicator
import com.example.lingora_fe.user.chatbot.presentation.components.SuggestionChips
import com.example.lingora_fe.core.ui.theme.GradientStart

val mockMessages = listOf(
    ConversationMessage(
        id = "1",
        sessionId = "mock-session-id",
        sender = ChatSender.AI,
        content = "Welcome to the hotel! How can I help you today?",
        corrections = null,
        suggestions = null,
        createdAt = "10:00 AM"
    ),
    ConversationMessage(
        id = "2",
        sessionId = "mock-session-id",
        sender = ChatSender.USER,
        content = "I want to checks in please.",
        corrections = ConversationCorrection(
            hasError = true,
            original = "I want to checks in please.",
            corrected = "I want to check in please.",
            explanation = "Dùng động từ nguyên mẫu 'check' sau 'to'."
        ),
        suggestions = null,
        createdAt = "10:01 AM"
    ),
    ConversationMessage(
        id = "3",
        sessionId = "mock-session-id",
        sender = ChatSender.AI,
        content = "Certainly. May I have your name and reservation number?",
        corrections = null,
        suggestions = listOf("My name is John. Here is my booking.", "I booked under the name Mary."),
        createdAt = "10:01 AM"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationChatScreen(
    contextId: Int,
    onBack: () -> Unit,
    onSessionEnded: (String) -> Unit
) {
    var inputText by remember { mutableStateOf(TextFieldValue("")) }

    // Hardcoded mock state
    val messages = mockMessages
    val currentSuggestions = messages.lastOrNull()?.suggestions ?: emptyList()
    val currentPhase = "developing"

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Luyện hội thoại") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Trở về")
                        }
                    },
                    actions = {
                        TextButton(onClick = { onSessionEnded("mock-session-id") }) {
                            Text("Kết thúc", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                PhaseIndicator(currentPhase = currentPhase)
                HorizontalDivider()
            }
        },
        bottomBar = {
            Column {
                SuggestionChips(
                    suggestions = currentSuggestions,
                    onSuggestionClick = { inputText = TextFieldValue(it) }
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Nhập tin nhắn...") },
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { inputText = TextFieldValue("") },
                        modifier = Modifier
                            .background(GradientStart, shape = RoundedCornerShape(50))
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Gửi", tint = Color.White)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message)
            }
        }
    }
}

@Composable
fun MessageBubble(message: ConversationMessage) {
    val isUser = message.sender == ChatSender.USER
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 16.dp
                    )
                )
                .background(if (isUser) GradientStart else Color(0xFFE0E0E0))
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message.content,
                color = if (isUser) Color.White else Color.Black,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        message.corrections?.let { correction ->
            Spacer(modifier = Modifier.height(4.dp))
            CorrectionBlock(
                correction = correction,
                modifier = Modifier.widthIn(max = 300.dp)
            )
        }
    }
}

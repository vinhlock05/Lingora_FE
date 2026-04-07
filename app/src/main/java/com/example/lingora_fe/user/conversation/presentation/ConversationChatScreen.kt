package com.example.lingora_fe.user.conversation.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.lingora_fe.user.conversation.domain.model.ConversationMessage
import com.example.lingora_fe.user.conversation.presentation.components.CorrectionBlock
import com.example.lingora_fe.user.conversation.presentation.components.ImprovementBlock
import com.example.lingora_fe.user.conversation.presentation.components.PhaseIndicator
import com.example.lingora_fe.user.conversation.presentation.components.SuggestionChips
import com.example.lingora_fe.user.conversation.presentation.components.VocabularyBlock
import com.example.lingora_fe.core.ui.theme.GradientStart

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationChatScreen(
    contextId: Int,
    sessionId: String? = null,
    onBack: () -> Unit,
    onSessionEnded: (String) -> Unit,
    viewModel: ConversationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()

    LaunchedEffect(sessionId) {
        if (sessionId != null) {
            viewModel.startSession(sessionId)
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.chatMessages.size, uiState.isSendingMessage) {
        if (uiState.chatMessages.isNotEmpty()) {
            // Scroll to the last item, or +1 if typing indicator is showing
            val targetIndex = uiState.chatMessages.size - 1 + if (uiState.isSendingMessage) 1 else 0
            listState.animateScrollToItem(targetIndex.coerceAtMost(uiState.chatMessages.size))
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(if (uiState.isReviewMode) "Lịch sử hội thoại" else "Luyện hội thoại") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Trở về")
                        }
                    },
                    actions = {
                        if (!uiState.isReviewMode) {
                            if (uiState.isEndingSession) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp).padding(end = 8.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                TextButton(onClick = { 
                                    viewModel.endSession { sessionId ->
                                        onSessionEnded(sessionId)
                                    }
                                }) {
                                    Text("Kết thúc", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                )
                if (!uiState.isReviewMode) {
                    PhaseIndicator(currentPhase = uiState.currentPhase)
                }
                HorizontalDivider()
            }
        },
        bottomBar = {
            if (!uiState.isReviewMode) {
                Column(modifier = Modifier.imePadding()) {
                    SuggestionChips(
                        suggestions = uiState.currentSuggestions,
                        onSuggestionClick = { inputText = TextFieldValue(it) }
                    )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Nhập tin nhắn...") },
                        shape = RoundedCornerShape(24.dp),
                        enabled = !uiState.isSendingMessage
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { 
                            if (inputText.text.isNotBlank() && !uiState.isSendingMessage) {
                                viewModel.sendMessage(inputText.text)
                                inputText = TextFieldValue("")
                            }
                        },
                        modifier = Modifier
                            .background(if (uiState.isSendingMessage) Color.Gray else GradientStart, shape = RoundedCornerShape(50))
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Gửi", tint = Color.White)
                    }
                }
            }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(uiState.chatMessages) { message ->
                MessageBubble(message)
            }
            // Typing indicator while waiting for AI response
            if (uiState.isSendingMessage) {
                item {
                    TypingIndicator()
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    val dot1Alpha by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
        label = "dot1"
    )
    val dot2Alpha by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 200, easing = LinearEasing), RepeatMode.Reverse),
        label = "dot2"
    )
    val dot3Alpha by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 400, easing = LinearEasing), RepeatMode.Reverse),
        label = "dot3"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp))
                .background(Color(0xFFE0E0E0))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(dot1Alpha, dot2Alpha, dot3Alpha).forEach { alpha ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ConversationMessage) {
    val isUser = message.sender == "USER"
    
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
        
        message.improvement?.let { improvement ->
            Spacer(modifier = Modifier.height(4.dp))
            ImprovementBlock(
                improvement = improvement,
                modifier = Modifier.widthIn(max = 300.dp)
            )
        }
        
        message.vocabulary?.let { vocabulary ->
            Spacer(modifier = Modifier.height(4.dp))
            VocabularyBlock(
                vocabulary = vocabulary,
                modifier = Modifier.widthIn(max = 300.dp)
            )
        }
    }
}

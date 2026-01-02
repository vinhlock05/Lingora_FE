package com.example.lingora_fe.user.chatbot.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.TopBarBorder
import com.example.lingora_fe.user.chatbot.domain.model.ChatMessage
import com.example.lingora_fe.user.chatbot.domain.model.ChatSender
import com.example.lingora_fe.user.chatbot.domain.model.ChatSession
import com.example.lingora_fe.util.DateFormatHelper
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    onBackClick: () -> Unit,
    viewModel: ChatbotViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            // Small delay so new message composes before scrolling
            delay(100)
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.dismissError()
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = Color(0xFFF0F9F4),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Lingora AI", fontWeight = FontWeight.SemiBold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    if (uiState.isAuthenticated) {
                        IconButton(onClick = { viewModel.toggleHistoryDialog(true) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Article,
                                contentDescription = "Lịch sử hội thoại"
                            )
                        }
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            ChatInputBar(
                value = uiState.question,
                onValueChanged = viewModel::onQuestionChange,
                onSend = viewModel::sendMessage,
                enabled = !uiState.isSending
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F9F4))
                .padding(padding)
        ) {
            if (uiState.isLoading && uiState.messages.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                ChatMessages(
                    messages = uiState.messages,
                    modifier = Modifier.fillMaxSize(),
                    listState = listState,
                    isBotTyping = uiState.isBotTyping
                )
            }

            AnimatedVisibility(
                visible = uiState.isSending,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
            ) {
                Text(
                    text = "Đang gửi câu hỏi...",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary)
                )
            }

            if (uiState.messages.isEmpty() && !uiState.isLoading) {
                EmptyChatPlaceholder(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    if (uiState.showHistoryDialog) {
        ChatHistoryDialog(
            sessions = uiState.sessions,
            isLoading = uiState.isHistoryLoading,
            deletingSessionId = uiState.deletingSessionId,
            onDismiss = { viewModel.toggleHistoryDialog(false) },
            onSessionSelected = { sessionId ->
                viewModel.loadSessionMessages(sessionId)
            },
            onNewSession = { viewModel.startNewSession() },
            onDeleteSession = viewModel::deleteSession
        )
    }
}

@Composable
private fun ChatMessages(
    messages: List<ChatMessage>,
    modifier: Modifier = Modifier,
    listState: LazyListState,
    isBotTyping: Boolean
) {
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            ChatBubble(message = message)
        }
        if (isBotTyping) {
            item(key = "typing") {
                TypingIndicatorBubble()
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage
) {
    val isUser = message.sender == ChatSender.USER
    val formattedTime = remember(message.createdAt) { formatTimestamp(message.createdAt) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        val bubbleShape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomEnd = if (isUser) 0.dp else 16.dp,
            bottomStart = if (isUser) 16.dp else 0.dp
        )
        Column(
            modifier = Modifier
                .clip(bubbleShape)
                .background(
                    color = if (isUser) GradientStart else TopBarBorder
                )
                .padding(14.dp)
                .widthIn(max = 320.dp)
        ) {
            Text(
                text = if (isUser) "Bạn" else "Lingora AI",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary
                    else MainText,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            MarkdownText(
                markdown = message.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MainText
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    else MainText.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun TypingIndicatorBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        val bubbleShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 0.dp)
        Row(
            modifier = Modifier
                .clip(bubbleShape)
                .background(TopBarBorder)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = GradientStart
            )
            Text(
                text = "Lingora AI đang trả lời...",
                style = MaterialTheme.typography.bodySmall.copy(color = MainText)
            )
        }
    }
}

@Composable
private fun EmptyChatPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.AddComment,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Chào bạn! Hỏi Lingora AI bất cứ điều gì về ngữ pháp, từ vựng hay cách luyện tập.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChanged: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChanged,
            label = { Text("Nhập câu hỏi...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (enabled && value.isNotBlank()) {
                        onSend()
                    }
                }
            ),
            trailingIcon = {
                IconButton(
                    onClick = onSend,
                    enabled = enabled && value.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Gửi tin nhắn",
                        tint = if (enabled && value.isNotBlank()) {
                            GradientStart
                        } else {
                            TopBarBorder
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun ChatHistoryDialog(
    sessions: List<ChatSession>,
    isLoading: Boolean,
    deletingSessionId: String?,
    onDismiss: () -> Unit,
    onSessionSelected: (String) -> Unit,
    onNewSession: () -> Unit,
    onDeleteSession: (String) -> Unit
) {
    var sessionToConfirm by remember { mutableStateOf<ChatSession?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = GradientStart)
            }
        },
        title = {
            Text(text = "Lịch sử hội thoại", fontWeight = FontWeight.SemiBold, color = Color.Black)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onNewSession) {
                    Text("Bắt đầu cuộc hội thoại mới", color = Color.Gray)
                }
                if (isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (sessions.isEmpty()) {
                    Text(
                        text = "Chưa có cuộc hội thoại nào. Gửi câu hỏi để bắt đầu nhé!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(sessions, key = { it.id }) { session ->
                            SessionRow(
                                session = session,
                                isDeleting = deletingSessionId == session.id,
                                onClick = {
                                    onDismiss()
                                    onSessionSelected(session.id)
                                },
                                onDelete = { sessionToConfirm = session }
                            )
                        }
                    }
                }
            }
        }
    )

    sessionToConfirm?.let { session ->
        val isDeleting = deletingSessionId == session.id
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) {
                    sessionToConfirm = null
                }
            },
            containerColor = Color.White,
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSession(session.id)
                        sessionToConfirm = null
                    },
                    enabled = !isDeleting
                ) {
                    Text("Xoá", color = Color(0xFFDC2626))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { sessionToConfirm = null },
                    enabled = !isDeleting
                ) {
                    Text("Huỷ", color = GradientStart)
                }
            },
            title = { Text("Xác nhận xoá", color = Color.Black) },
            text = {
                Text(
                    text = "Bạn chắc chắn muốn xoá cuộc hội thoại \"${session.title.ifBlank { "Không tiêu đề" }}\"?",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )
    }
}

@Composable
private fun SessionRow(
    session: ChatSession,
    isDeleting: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val formattedTime = remember(session.updatedAt) {
        formatTimestamp(session.updatedAt, includeDate = true)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TopBarBorder)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.title.ifBlank { "Cuộc hội thoại không tiêu đề" },
                style = MaterialTheme.typography.bodyMedium.copy(color = MainText),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Cập nhật: $formattedTime",
                style = MaterialTheme.typography.labelSmall,
                color = MainText.copy(alpha = 0.7f)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Article,
                contentDescription = null,
                tint = GradientStart
            )
            IconButton(
                onClick = onDelete,
                enabled = !isDeleting
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = GradientStart
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Xóa phiên",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(raw: String, includeDate: Boolean = false): String {
    return DateFormatHelper.formatChatTimestamp(raw, includeDate)
}


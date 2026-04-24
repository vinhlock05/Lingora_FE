package com.example.lingora_fe.user.conversation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.user.conversation.domain.model.ConversationContext

// Mocks removed: Using ViewModel state

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextSelectionScreen(
    onBack: () -> Unit,
    onSessionStarted: (String) -> Unit,
    onSessionResumed: (String) -> Unit = onSessionStarted, // Resume an active session
    onSessionViewed: (String) -> Unit = {}, // View a completed session summary
    viewModel: ConversationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show async errors from context/session loading and clear afterward.
    LaunchedEffect(uiState.errorMessage) {
        val error = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(error)
        viewModel.dismissError()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Luyện hội thoại",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MainText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Trở về"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GradientStart.copy(alpha = 0.06f),
                            GradientEnd.copy(alpha = 0.02f)
                        )
                    )
                )
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(
                    text = "Luyện hội thoại",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Lịch sử hội thoại",
                    isSelected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        viewModel.fetchSessions(page = 1) // Refresh when switching to history tab
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            if (selectedTab == 0) {
                Text(
                    text = "Chọn ngữ cảnh luyện tập",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                if (uiState.isContextsLoading && uiState.contexts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GradientStart)
                    }
                } else if (uiState.contexts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Chưa có chủ đề nào", color = MainText)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.contexts) { context ->
                            ContextCard(
                                context = context,
                                onClick = { 
                                    viewModel.createSession(context.id) { sessionId ->
                                        onSessionStarted(sessionId)
                                    }
                                }
                            )
                        }
                        if (uiState.isContextsLoading && uiState.contexts.isNotEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = GradientStart)
                                }
                            }
                        }
                    }
                }
            } else {
                if (uiState.isSessionsLoading && uiState.sessions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GradientStart)
                    }
                } else if (uiState.sessions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Chưa có lịch sử hội thoại", color = MainText)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.sessions) { session ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (session.status == "ACTIVE") {
                                        onSessionResumed(session.id)
                                    } else {
                                        onSessionViewed(session.id)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val sessionTitle = session.title?.takeIf { it.isNotBlank() } ?: session.context?.name ?: "Cuộc hội thoại"
                                    Column {
                                        Text(text = sessionTitle, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = session.createdAt.substringBefore("T"), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (session.overallScore != null) {
                                        Text(
                                            text = "${session.overallScore.toInt()}/100",
                                            color = GradientStart,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Text(
                                            text = session.status,
                                            color = if (session.status == "ACTIVE") Color(0xFFFFA000) else Color.Gray,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                        if (uiState.isSessionsLoading && uiState.sessions.isNotEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = GradientStart)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContextCard(
    context: ConversationContext,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .aspectRatio(0.85f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = context.name.first().toString(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = context.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = context.difficultyLevel,
                style = MaterialTheme.typography.labelSmall,
                color = when (context.difficultyLevel) {
                    "BEGINNER" -> Color(0xFF4CAF50)
                    "INTERMEDIATE" -> Color(0xFFFFA000)
                    else -> Color(0xFFE53935)
                }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GradientStart,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = "Bắt đầu", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) GradientStart else Color.White,
        border = if (isSelected) null else BorderStroke(1.5.dp, GradientStart.copy(alpha = 0.3f)),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else GradientStart
            )
        }
    }
}

package com.example.lingora_fe.user.conversation.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lingora_fe.core.ui.theme.GradientStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSummaryScreen(
    sessionId: String,
    viewModel: ConversationViewModel,
    onRetry: () -> Unit,
    onNewContext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val session = uiState.endedSession

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (session == null) {
                // Loading / fallback
                CircularProgressIndicator(color = GradientStart)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Đang tải kết quả...", color = Color.Gray)
            } else {
                Text(
                    text = "Hoàn thành phiên hội thoại!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = GradientStart
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ĐIỂM SỐ TỔNG QUAN",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        val overallScore = session.overallScore?.toInt() ?: 0
                        Text(
                            text = "$overallScore/100", 
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = GradientStart
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        val grammarScore = session.grammarScore?.toInt() ?: 0
                        val fluencyScore = session.fluencyScore?.toInt() ?: 0
                        ScoreRow(
                            label = "Ngữ pháp",
                            score = "$grammarScore/100",
                            progress = (session.grammarScore ?: 0f) / 100f
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ScoreRow(
                            label = "Độ lưu loát",
                            score = "$fluencyScore/100",
                            progress = (session.fluencyScore ?: 0f) / 100f
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                label = "Lượt chat",
                                value = "${session.totalMessages}"
                            )
                            StatItem(
                                label = "Lỗi sai",
                                value = "${session.errorCount}",
                                isError = session.errorCount > 0
                            )
                            // Calculate duration
                            val duration = calculateDuration(session.createdAt, session.endedAt)
                            StatItem(
                                label = "Thời gian",
                                value = duration
                            )
                        }
                    }
                }

                if (!session.feedback.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "NHẬN XÉT TỪ AI",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = session.feedback,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GradientStart,
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Luyện lại với ngữ cảnh này")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = onNewContext,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GradientStart
                    ),
                    border = BorderStroke(1.dp, GradientStart)
                ) {
                    Text("Chọn ngữ cảnh khác")
                }
            }
        }
    }
}

private fun calculateDuration(startTime: String, endTime: String?): String {
    return try {
        val start = java.time.Instant.parse(startTime)
        val end = if (endTime != null) java.time.Instant.parse(endTime) else java.time.Instant.now()
        val seconds = java.time.Duration.between(start, end).seconds
        when {
            seconds < 60 -> "${seconds}s"
            seconds < 3600 -> "${seconds / 60}p ${seconds % 60}s"
            else -> "${seconds / 3600}h ${(seconds % 3600) / 60}p"
        }
    } catch (e: Exception) {
        "--"
    }
}

@Composable
fun ScoreRow(label: String, score: String, progress: Float) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontWeight = FontWeight.Medium)
            Text(text = score, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = GradientStart,
            trackColor = Color(0xFFE0E0E0),
        )
    }
}

@Composable
fun StatItem(label: String, value: String, isError: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (isError) MaterialTheme.colorScheme.error else GradientStart
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

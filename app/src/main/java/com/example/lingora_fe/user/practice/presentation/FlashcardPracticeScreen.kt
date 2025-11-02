package com.example.lingora_fe.user.practice.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.core.ui.theme.TopBarBorder

// Data class for review word
data class ReviewWord(
    val word: String,
    val pronunciation: String,
    val translation: String,
    val example: String,
    val exampleTranslation: String,
    val level: String = "Trung bình",
    val imageUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardPracticeScreen(
    navController: NavController
) {
    // Sample word list for review
    val words = remember {
        listOf(
            ReviewWord(
                word = "Ambitious",
                pronunciation = "/æm'bɪʃəs/",
                translation = "Tham vọng, đầy tham vọng",
                example = "She is very ambitious and wants to become a CEO.",
                exampleTranslation = "Cô ấy rất tham vọng và muốn trở thành CEO.",
                level = "Trung bình"
            ),
            ReviewWord(
                word = "Beautiful",
                pronunciation = "/'bju:tɪfəl/",
                translation = "Đẹp",
                example = "What a beautiful day!",
                exampleTranslation = "Thật là một ngày đẹp trời!",
                level = "Dễ"
            ),
            ReviewWord(
                word = "Confident",
                pronunciation = "/'kɒnfɪdənt/",
                translation = "Tự tin",
                example = "He feels confident about the exam.",
                exampleTranslation = "Anh ấy cảm thấy tự tin về kỳ thi.",
                level = "Trung bình"
            ),
            ReviewWord(
                word = "Diligent",
                pronunciation = "/'dɪlɪdʒənt/",
                translation = "Siêng năng, cần cù",
                example = "She is a diligent student.",
                exampleTranslation = "Cô ấy là một học sinh siêng năng.",
                level = "Trung bình"
            ),
            ReviewWord(
                word = "Enthusiastic",
                pronunciation = "/ɪn,θju:zi'æstɪk/",
                translation = "Nhiệt tình, hăng hái",
                example = "The team is enthusiastic about the project.",
                exampleTranslation = "Nhóm rất nhiệt tình với dự án.",
                level = "Khó"
            )
        )
    }

    var currentCardIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var showMeaning by remember { mutableStateOf(false) }
    var masteredCount by remember { mutableStateOf(0) }
    var needReviewCount by remember { mutableStateOf(0) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showCompletionDialog by remember { mutableStateOf(false) }

    // Back handler
    BackHandler {
        showExitDialog = true
    }

    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Thoát khỏi ôn tập?") },
            text = { Text("Tiến trình ôn tập của bạn sẽ không được lưu. Bạn có chắc muốn thoát?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Thoát", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Tiếp tục ôn tập")
                }
            }
        )
    }

    // Completion dialog
    if (showCompletionDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { 
                Text(
                    "Hoàn thành! 🎉",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = { 
                Column {
                    Text(
                        "Bạn đã hoàn thành ôn tập flashcard!",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Kết quả:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "✅ Đã thuộc: $masteredCount từ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GradientStart
                    )
                    Text(
                        "🔄 Cần ôn: $needReviewCount từ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFEF4444)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
                ) {
                    Text("Hoàn thành")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Ôn tập Flashcard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MainText
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
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
                .padding(paddingValues)
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val currentWord = words[currentCardIndex]
            val progressPercent = ((currentCardIndex + 1).toFloat() / words.size * 100).toInt()

            // Progress
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Từ ${currentCardIndex + 1}/${words.size}",
                        fontSize = 14.sp,
                        color = NavBarText
                    )
                    Text(
                        text = "$progressPercent%",
                        fontSize = 14.sp,
                        color = NavBarText
                    )
                }

                LinearProgressIndicator(
                    progress = (currentCardIndex + 1).toFloat() / words.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = GradientStart,
                    trackColor = Color(0xFFE5E7EB)
                )
            }

            // Flashcard
            FlipCard(
                word = currentWord,
                isFlipped = isFlipped,
                showMeaning = showMeaning,
                onFlip = { 
                    isFlipped = !isFlipped
                    showMeaning = !showMeaning
                }
            )

            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            needReviewCount++
                            if (currentCardIndex < words.size - 1) {
                                currentCardIndex++
                                isFlipped = false
                                showMeaning = false
                            } else {
                                showCompletionDialog = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color(0xFFEF4444)
                            )
                            Text(
                                text = "Cần ôn thêm",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Button(
                        onClick = {
                            masteredCount++
                            if (currentCardIndex < words.size - 1) {
                                currentCardIndex++
                                isFlipped = false
                                showMeaning = false
                            } else {
                                showCompletionDialog = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GradientStart
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null
                            )
                            Text(
                                text = "Đã thuộc",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            if (currentCardIndex > 0) {
                                currentCardIndex--
                                isFlipped = false
                                showMeaning = false
                            }
                        },
                        enabled = currentCardIndex > 0
                    ) {
                        Text(
                            text = "Từ trước",
                            color = if (currentCardIndex > 0) NavBarText else NavBarText.copy(alpha = 0.5f)
                        )
                    }

                    TextButton(
                        onClick = {
                            if (currentCardIndex < words.size - 1) {
                                currentCardIndex++
                                isFlipped = false
                                showMeaning = false
                            }
                        },
                        enabled = currentCardIndex < words.size - 1
                    ) {
                        Text(
                            text = "Từ tiếp",
                            color = if (currentCardIndex < words.size - 1) NavBarText else NavBarText.copy(alpha = 0.5f)
                        )
                    }
                }

                // Score display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$masteredCount",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = GradientStart
                        )
                        Text(
                            text = "Đã thuộc",
                            fontSize = 12.sp,
                            color = NavBarText
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$needReviewCount",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                        Text(
                            text = "Cần ôn",
                            fontSize = 12.sp,
                            color = NavBarText
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlipCard(
    word: ReviewWord,
    isFlipped: Boolean,
    showMeaning: Boolean,
    onFlip: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400)
    )

    val levelColor = when (word.level) {
        "Dễ" -> Color(0xFFD1FAE5) to Color(0xFF065F46)
        "Khó" -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        else -> Color(0xFFFEF3C7) to Color(0xFF92400E)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(onClick = onFlip),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF0FDFA)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!showMeaning) {
                    // Front side - Word
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = levelColor.first,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = word.level,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = levelColor.second
                            )
                        }

                        Text(
                            text = word.word,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MainText
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Play audio",
                                tint = GradientStart,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = word.pronunciation,
                                fontSize = 16.sp,
                                color = NavBarText
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Nhấn để xem nghĩa",
                            fontSize = 14.sp,
                            color = NavBarText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Back side - Meaning & Example
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.graphicsLayer { rotationY = 180f }
                    ) {
                        Text(
                            text = word.word,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MainText
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Play audio",
                                tint = GradientStart,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = word.pronunciation,
                                fontSize = 16.sp,
                                color = NavBarText
                            )
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = Color(0xFFE5E7EB)
                        )

                        // Translation
                        Text(
                            text = word.translation,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GradientStart,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Example
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFEFF6FF)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = word.example,
                                    fontSize = 13.sp,
                                    color = MainText,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 18.sp
                                )
                                Text(
                                    text = word.exampleTranslation,
                                    fontSize = 12.sp,
                                    color = NavBarText,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Text(
                            text = "Nhấn để xem từ",
                            fontSize = 12.sp,
                            color = NavBarText
                        )
                    }
                }
            }
        }
    }
}



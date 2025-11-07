package com.example.lingora_fe.user.vocabulary.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.TopBarBorder
import com.example.lingora_fe.user.vocabulary.domain.model.Word

@Composable
fun BottomFeedbackCard(
    isCorrect: Boolean,
    correctAnswer: String,
    word: Word,
    currentIndex: Int,
    totalQuestions: Int,
    onNextClick: () -> Unit,
    onPronunciationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) GradientStart.copy(alpha = 0.95f) else MaterialTheme.colorScheme.error.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Status header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                
                Text(
                    text = if (isCorrect) "Chính xác! 🎉" else "Chưa đúng 😔",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (!isCorrect) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Đáp án đúng: $correctAnswer",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.95f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Word details card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Word
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GradientStart
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Pronunciation with speaker
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onPronunciationClick,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Pronounce",
                                tint = GradientEnd,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        word.phonetic?.let { phonetic ->
                            Text(
                                text = phonetic,
                                style = MaterialTheme.typography.bodyMedium,
                                color = GradientEnd
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Meaning
                    word.meaning?.let { meaning ->
                        Text(
                            text = meaning,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Example
                    if (word.example != null || word.exampleTranslation != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TopBarBorder, shape = RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                word.example?.let { example ->
                                    Text(
                                        text = "\"$example\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                word.exampleTranslation?.let { exampleTranslation ->
                                    if (word.example != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                    Text(
                                        text = "\"$exampleTranslation\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Next button
            Button(
                onClick = onNextClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (currentIndex < totalQuestions - 1) "Câu tiếp theo" else "Hoàn thành",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) GradientStart else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}


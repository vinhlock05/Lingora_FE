package com.example.lingora_fe.user.vocabulary.presentation.components

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.TopBarBorder
import com.example.lingora_fe.user.vocabulary.presentation.screen.QuestionType
import com.example.lingora_fe.user.vocabulary.presentation.screen.QuizQuestion

@Composable
fun QuizPhaseContent(
    question: QuizQuestion,
    currentIndex: Int,
    totalQuestions: Int,
    selectedAnswer: String?,
    typedAnswer: String,
    isAnswerChecked: Boolean,
    onAnswerSelected: (String) -> Unit,
    onTypedAnswerChanged: (String) -> Unit,
    onCheckAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onPronunciationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = if (isAnswerChecked) 280.dp else 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Câu ${currentIndex + 1}/$totalQuestions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Question card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TopBarBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = getQuestionTypeLabel(question.type),
                            style = MaterialTheme.typography.labelMedium,
                            color = GradientStart,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Listen button for listening questions
                        if (question.type == QuestionType.LISTEN_FILL) {
                            IconButton(
                                onClick = onPronunciationClick,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(GradientEnd.copy(alpha = 0.2f), shape = RoundedCornerShape(20.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Listen",
                                    tint = GradientEnd,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = question.question,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Answer options based on question type
            if (question.type == QuestionType.FILL_WORD) {
                // Text input for fill-word questions
                OutlinedTextField(
                    value = typedAnswer,
                    onValueChange = onTypedAnswerChanged,
                    enabled = !isAnswerChecked,
                    label = { Text("Nhập câu trả lời") },
                    placeholder = { Text("Nhập từ tiếng Anh...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    trailingIcon = {
                        if (isAnswerChecked) {
                            val isCorrect = typedAnswer.trim().equals(question.correctAnswer, ignoreCase = true)
                            Icon(
                                imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isCorrect) GradientStart else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                )
            } else {
                // Multiple choice options
                question.options.forEach { option ->
                    val isSelected = selectedAnswer == option
                    val isCorrect = option == question.correctAnswer
                    val showResult = isAnswerChecked && (isSelected || isCorrect)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .shadow(2.dp, shape = RoundedCornerShape(12.dp))
                            .clickable(enabled = !isAnswerChecked) {
                                onAnswerSelected(option)
                            }
                            .then(
                                if (showResult) {
                                    Modifier.border(
                                        width = 2.dp,
                                        color = if (isCorrect) GradientStart else MaterialTheme.colorScheme.error,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                } else if (isSelected) {
                                    Modifier.border(
                                        width = 2.dp,
                                        color = GradientEnd,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                } else {
                                    Modifier.border(
                                        width = 1.dp,
                                        color = TopBarBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = TopBarBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )

                            if (isAnswerChecked && (isSelected || isCorrect)) {
                                Icon(
                                    imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (isCorrect) GradientStart else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Check button (only shown when answer not checked yet)
            if (!isAnswerChecked) {
                val enabled = if (question.type == QuestionType.FILL_WORD) {
                    typedAnswer.trim().isNotEmpty()
                } else {
                    selectedAnswer != null
                }
                Button(
                    onClick = onCheckAnswer,
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (enabled) {
                                    listOf(GradientStart, GradientEnd)
                                } else {
                                    listOf(
                                        GradientStart.copy(alpha = 0.3f),
                                        GradientEnd.copy(alpha = 0.3f)
                                    )
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Kiểm tra",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (enabled) Color.White else Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom feedback card (shown when answer is checked)
        if (isAnswerChecked) {
            val userAnswer = if (question.type == QuestionType.FILL_WORD) {
                typedAnswer.trim()
            } else {
                selectedAnswer
            }
            val isCorrect = userAnswer?.equals(question.correctAnswer, ignoreCase = true) == true

            BottomFeedbackCard(
                isCorrect = isCorrect,
                correctAnswer = question.correctAnswer,
                word = question.word,
                currentIndex = currentIndex,
                totalQuestions = totalQuestions,
                onNextClick = onNextQuestion,
                onPronunciationClick = onPronunciationClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

private fun getQuestionTypeLabel(type: QuestionType): String {
    return when (type) {
        QuestionType.CHOOSE_MEANING -> "CHỌN NGHĨA"
        QuestionType.FILL_WORD -> "ĐIỀN TỪ"
        QuestionType.TRUE_FALSE -> "ĐÚNG/SAI"
        QuestionType.LISTEN_FILL -> "NGHE VÀ ĐIỀN"
    }
}


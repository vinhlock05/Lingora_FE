package com.example.lingora_fe.user.exam.presentation.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lingora_fe.user.exam.domain.model.QuestionOptionsParser
import com.example.lingora_fe.user.exam.domain.repository.ExamQuestionLite
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.core.ui.theme.GradientStart

/**
 * Universal question renderer that handles all question types
 * Supports: MULTIPLE_CHOICE, FILL_IN_THE_BLANK, MATCHING, YES_NO_NOT_GIVEN, etc.
 */
@Composable
fun UniversalQuestionRenderer(
    question: ExamQuestionLite,
    answer: Any?,
    onAnswerChange: (Any?) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = GradientStart
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Question prompt
        Text(
            text = question.prompt,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MainText
        )
        
        // Image if available
        val imageUrl = QuestionOptionsParser.getImageUrl(question.metadata)
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Question image",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
        
        // Render based on question type
        when (question.questionType) {
            "MULTIPLE_CHOICE", "MATCHING" -> {
                MultipleChoiceRenderer(question, answer, onAnswerChange, accentColor)
            }
            
            "FILL_IN_THE_BLANK", "NOTE_COMPLETION", "SHORT_ANSWER" -> {
                FillInBlankRenderer(question, answer, onAnswerChange, accentColor)
            }
            
            "YES_NO_NOT_GIVEN", "TRUE_FALSE" -> {
                YesNoNotGivenRenderer(question, answer, onAnswerChange, accentColor)
            }
            
            "ESSAY" -> {
                EssayRenderer(question, answer, onAnswerChange, accentColor)
            }
            
            else -> {
                Text(
                    "Unsupported question type: ${question.questionType}",
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun MultipleChoiceRenderer(
    question: ExamQuestionLite,
    answer: Any?,
    onAnswerChange: (Any?) -> Unit,
    accentColor: Color
) {
    val options = QuestionOptionsParser.parseSimpleOptions(question.options)
    val allowsMultiple = QuestionOptionsParser.allowsMultipleSelection(question.metadata)
    val maxSelection = QuestionOptionsParser.getMaxSelection(question.metadata)
    
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (allowsMultiple) {
            // Multiple selection with checkboxes
            val selected = (answer as? List<*>)?.map { it.toString() } ?: emptyList()
            
            options.forEach { option ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newSelected = if (option in selected) {
                                selected - option
                            } else {
                                if (maxSelection != null && selected.size >= maxSelection) {
                                    return@clickable
                                }
                                selected + option
                            }
                            onAnswerChange(newSelected)
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = if (option in selected) accentColor.copy(alpha = 0.1f) else Color.White,
                    border = BorderStroke(
                        width = 2.dp,
                        color = if (option in selected) accentColor else Color(0xFFE5E7EB)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Checkbox(
                            checked = option in selected,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = accentColor)
                        )
                        Text(
                            text = option,
                            fontSize = 15.sp,
                            color = MainText,
                            fontWeight = if (option in selected) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
            
            maxSelection?.let {
                Text(
                    "Select up to $it options",
                    fontSize = 12.sp,
                    color = NavBarText,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        } else {
            // Single selection with radio buttons
            options.forEach { option ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = answer == option,
                            onClick = { onAnswerChange(option) }
                        ),
                    shape = RoundedCornerShape(10.dp),
                    color = if (answer == option) accentColor.copy(alpha = 0.1f) else Color.White,
                    border = BorderStroke(
                        width = 2.dp,
                        color = if (answer == option) accentColor else Color(0xFFE5E7EB)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = answer == option,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                        )
                        Text(
                            text = option,
                            fontSize = 15.sp,
                            color = MainText,
                            fontWeight = if (answer == option) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FillInBlankRenderer(
    question: ExamQuestionLite,
    answer: Any?,
    onAnswerChange: (Any?) -> Unit,
    accentColor: Color
) {
    val wordLimit = QuestionOptionsParser.getWordLimit(question.metadata)
    val text = answer as? String ?: ""
    val currentWordCount = if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size
    val isError = wordLimit != null && currentWordCount > wordLimit
    
    OutlinedTextField(
        value = text,
        onValueChange = onAnswerChange,
        modifier = Modifier.fillMaxWidth(),
        isError = isError,
        supportingText = {
            wordLimit?.let {
                Text(
                    "$currentWordCount / $it words",
                    color = if (isError) Color.Red else NavBarText
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedBorderColor = Color(0xFFE5E7EB)
        ),
        shape = RoundedCornerShape(10.dp),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                // optional: đóng bàn phím
                defaultKeyboardAction(ImeAction.Done)
            }
        )
    )
}

@Composable
private fun MatchingRenderer(
    question: ExamQuestionLite,
    answer: Any?,
    onAnswerChange: (Any?) -> Unit,
    accentColor: Color
) {
    val options = QuestionOptionsParser.parseMatchingOptions(question.options)
    Log.d("MatchingRenderer", "Options: $options")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { option ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = answer == option.key,
                        onClick = { onAnswerChange(option.key) }
                    ),
                shape = RoundedCornerShape(10.dp),
                color = if (answer == option.key) accentColor.copy(alpha = 0.1f) else Color.White,
                border = BorderStroke(
                    width = 2.dp,
                    color = if (answer == option.key) accentColor else Color(0xFFE5E7EB)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = answer == option.key,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                    )
                    Text(
                        text = "${option.key}. ${option.value}",
                        fontSize = 15.sp,
                        color = MainText,
                        fontWeight = if (answer == option.key) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun YesNoNotGivenRenderer(
    question: ExamQuestionLite,
    answer: Any?,
    onAnswerChange: (Any?) -> Unit,
    accentColor: Color
) {
    val options = when (question.questionType) {
        "YES_NO_NOT_GIVEN" -> listOf("YES", "NO", "NOT GIVEN")
        "TRUE_FALSE" -> listOf("TRUE", "FALSE")
        else -> QuestionOptionsParser.parseSimpleOptions(question.options)
    }
    
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { option ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = answer == option,
                        onClick = { onAnswerChange(option) }
                    ),
                shape = RoundedCornerShape(10.dp),
                color = if (answer == option) accentColor.copy(alpha = 0.1f) else Color.White,
                border = BorderStroke(
                    width = 2.dp,
                    color = if (answer == option) accentColor else Color(0xFFE5E7EB)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = answer == option,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                    )
                    Text(
                        text = option,
                        fontSize = 15.sp,
                        color = MainText,
                        fontWeight = if (answer == option) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun EssayRenderer(
    question: ExamQuestionLite,
    answer: Any?,
    onAnswerChange: (Any?) -> Unit,
    accentColor: Color
) {
    val minWordCount = QuestionOptionsParser.getMinWordCount(question.metadata)
    val text = answer as? String ?: ""
    val currentWordCount = if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size
    val meetsMinimum = minWordCount == null || currentWordCount >= minWordCount
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = onAnswerChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Color(0xFFE5E7EB)
            ),
            shape = RoundedCornerShape(10.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "$currentWordCount words",
                fontSize = 12.sp,
                color = NavBarText
            )
            
            minWordCount?.let {
                Text(
                    "Minimum: $it words",
                    fontSize = 12.sp,
                    color = if (meetsMinimum) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }
        }
    }
}

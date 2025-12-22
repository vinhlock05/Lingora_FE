package com.example.lingora_fe.admin.exam.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.admin.exam.presentation.AdminExamViewModel
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminExamDetailScreen(
    onNavigateBack: () -> Unit,
    examId: Int,
    viewModel: AdminExamViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(examId) {
        viewModel.loadExamDetail(examId)
    }

    if (state.isEditExamDialogVisible) {
        EditExamDialog(
            exam = state.examDetail,
            isUpdating = state.isUpdating,
            onDismiss = viewModel::hideEditExamDialog,
            onSave = { title, code, isPublished ->
                viewModel.updateExamInfo(examId, title, code, isPublished, state.examDetail?.examType ?: "")
            }
        )
    }

    Scaffold {
        val bottomPadding = it.calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)
                .background(Color(0xFFF9FAFB))
        ) {
            if (state.isLoading && state.examDetail == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.examDetail != null) {
                val exam = state.examDetail!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = exam.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Divider(color = Color(0xFFE5E7EB))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                InfoItem("Code", exam.code)
                                InfoItem("Type", exam.examType)
                                InfoItem("Status", if (exam.isPublished) "Published" else "Draft")
                            }
                        }
                    }

                    // Sections
                    Text("Sections", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    exam.sections?.forEach { section ->
                        val icon = when (section.sectionType) {
                            "LISTENING" -> Icons.Default.Headset
                            "READING" -> Icons.Default.MenuBook
                            "WRITING" -> Icons.Default.Edit
                            "SPEAKING" -> Icons.Default.Mic
                            else -> Icons.Default.Description
                        }
                         val iconColor = when (section.sectionType) {
                            "LISTENING" -> Color(0xFF3B82F6)
                            "READING" -> Color(0xFF10B981)
                            "WRITING" -> Color(0xFF9333EA)
                            else -> Color(0xFF6366F1)
                        }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = null, tint = iconColor)
                                }
                                Column {
                                    Text(text = section.sectionType, fontWeight = FontWeight.Bold, color = iconColor)
                                    Text(text = section.title ?: "Untitled Section", style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "${(section.durationSeconds ?: 0) / 60} mins", style = MaterialTheme.typography.bodySmall, color = NavBarText)
                                }
                            }
                        }
                    }
                }
            } else if (state.error != null) {
                Text(text = state.error!!, color = Color.Red, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = NavBarText)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EditExamDialog(
    exam: com.example.lingora_fe.user.exam.data.remote.dto.ExamDto?,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean) -> Unit
) {
    if (exam == null) return
    var title by remember { mutableStateOf(exam.title) }
    var code by remember { mutableStateOf(exam.code) }
    var isPublished by remember { mutableStateOf(exam.isPublished) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Exam Info") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Code") },
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPublished, onCheckedChange = { isPublished = it })
                    Text("Published")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, code, isPublished) },
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                   CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                } else {
                   Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

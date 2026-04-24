package com.example.lingora_fe.user.classroom.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.user.classroom.presentation.components.ClassroomColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuizScreen(
    navController: NavController,
    classroomId: String,
    viewModel: CreateQuizViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            navController.popBackStack()
        }
    }

    val brandFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ClassroomColors.BrandPrimary,
        focusedLabelColor = ClassroomColors.BrandPrimaryStrong,
        cursorColor = ClassroomColors.BrandPrimary,
        disabledBorderColor = ClassroomColors.NeutralBorder,
        disabledLabelColor = ClassroomColors.TextMuted,
        disabledTextColor = MainText
    )

    Scaffold(
        containerColor = ClassroomColors.ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditMode) "Chỉnh sửa bài kiểm tra" else "Thêm bài kiểm tra mới",
                        fontWeight = FontWeight.SemiBold,
                        color = MainText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MainText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ClassroomColors.HeaderSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title field (required)
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.onTitleChange(it) },
                label = { Text("Tiêu đề bài kiểm tra *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.error != null && state.title.isBlank(),
                colors = brandFieldColors
            )

            // Description field
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Mô tả bài kiểm tra") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 3,
                colors = brandFieldColors
            )

            // Time Limit field
            OutlinedTextField(
                value = state.timeLimitSeconds?.toString() ?: "",
                onValueChange = { viewModel.onTimeLimitSecondsChange(it) },
                label = { Text("Giới hạn thời gian (giây)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("Tùy chọn") },
                colors = brandFieldColors
            )

            // Max Attempts field
            OutlinedTextField(
                value = state.maxAttempts.toString(),
                onValueChange = { viewModel.onMaxAttemptsChange(it) },
                label = { Text("Số lần thử tối đa") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = brandFieldColors
            )

            // Passing Score field
            OutlinedTextField(
                value = state.passingScore,
                onValueChange = { viewModel.onPassingScoreChange(it) },
                label = { Text("Điểm đạt (%)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                placeholder = { Text("70") },
                colors = brandFieldColors
            )

            // Published toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Công khai bài kiểm tra",
                    color = MainText,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = state.isPublished,
                    onCheckedChange = { viewModel.onIsPublishedChange(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ClassroomColors.BrandPrimary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = ClassroomColors.NeutralBorder
                    )
                )
            }

            // Error message
            state.error?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = ClassroomColors.Danger,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Create button
            Button(
                onClick = { viewModel.createQuiz() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = state.title.isNotBlank() && !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClassroomColors.BrandPrimary,
                    contentColor = Color.White
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (state.isEditMode) "Cập nhật bài kiểm tra" else "Tạo Bài Kiểm Tra",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

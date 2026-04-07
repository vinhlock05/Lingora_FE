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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.user.classroom.util.ClassroomLessonType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLessonScreen(
    navController: NavController,
    classroomId: String,
    viewModel: CreateLessonViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var expandedLessonType by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo bài học mới") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title field (required)
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.onTitleChange(it) },
                label = { Text("Tiêu đề bài học *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.error != null && state.title.isBlank()
            )

            // Description field
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Mô tả bài học") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 3
            )

            // Lesson Type Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.lessonType.displayName,
                    onValueChange = {},
                    label = { Text("Loại bài học") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .noRippleClickable { expandedLessonType = true },
                    enabled = false,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.rotate(270f)
                        )
                    }
                )

                DropdownMenu(
                    expanded = expandedLessonType,
                    onDismissRequest = { expandedLessonType = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    ClassroomLessonType.entries.forEach { lessonType ->
                        DropdownMenuItem(
                            text = { Text(lessonType.displayName) },
                            onClick = {
                                viewModel.onLessonTypeChange(lessonType)
                                expandedLessonType = false
                            }
                        )
                    }
                }
            }

            // Content field
            OutlinedTextField(
                value = state.content,
                onValueChange = { viewModel.onContentChange(it) },
                label = { Text("Nội dung bài học") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5
            )

            // Sort Order field
            OutlinedTextField(
                value = state.sortOrder.toString(),
                onValueChange = { viewModel.onSortOrderChange(it) },
                label = { Text("Thứ tự sắp xếp") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Published toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Công khai bài học")
                Switch(
                    checked = state.isPublished,
                    onCheckedChange = { viewModel.onIsPublishedChange(it) }
                )
            }

            // Error message
            state.error?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Create button
            Button(
                onClick = { viewModel.createLesson() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = state.title.isNotBlank() && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Tạo Bài Học")
                }
            }
        }
    }
}


private fun Modifier.rotate(degrees: Float): Modifier =
    this.then(Modifier.graphicsLayer(rotationZ = degrees))

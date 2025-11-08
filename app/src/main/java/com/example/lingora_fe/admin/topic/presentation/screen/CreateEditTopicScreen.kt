package com.example.lingora_fe.admin.topic.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.admin.topic.presentation.TopicFormState
import com.example.lingora_fe.admin.topic.presentation.TopicManagementEvent
import com.example.lingora_fe.admin.topic.presentation.TopicManagementViewModel
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.NavBarText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditTopicScreen(
    topicId: Int?,
    categoryId: Int,
    onNavigateBack: () -> Unit,
    viewModel: TopicManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val isEditMode = topicId != null

    // Load topic data for edit mode
    LaunchedEffect(topicId, categoryId) {
        if (topicId != null) {
            viewModel.loadTopicIntoForm(topicId)
        } else {
            viewModel.resetFormState()
            // For standalone topics, categoryId can be 0 (which means null)
            if (categoryId > 0) {
                viewModel.setFormCategoryId(categoryId, isEditMode = false)
            } else {
                viewModel.setFormCategoryId(null, isEditMode = false)
            }
        }
    }

    // Handle success
    LaunchedEffect(state.actionSuccess) {
        state.actionSuccess?.let {
            onNavigateBack()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        val bottomPadding = paddingValues.calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Field
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = { 
                        viewModel.updateFormState(formState.copy(name = it), isEditMode = isEditMode)
                    },
                    label = { Text("Topic Name *") },
                    placeholder = { Text("e.g., Thorough Experience") },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.Topic, 
                            "Topic",
                            tint = GradientStart
                        ) 
                    },
                    isError = formState.nameError != null,
                    supportingText = {
                        formState.nameError?.let { Text(it) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Description Field
                OutlinedTextField(
                    value = formState.description,
                    onValueChange = { 
                        viewModel.updateFormState(formState.copy(description = it), isEditMode = isEditMode)
                    },
                    label = { Text("Description *") },
                    placeholder = { Text("Enter topic description...") },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.Description, 
                            "Description",
                            tint = GradientStart
                        )
                    },
                    isError = formState.descriptionError != null,
                    supportingText = {
                        formState.descriptionError?.let { Text(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    minLines = 5,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, "Cancel", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (isEditMode && topicId != null) {
                                viewModel.onEvent(
                                    TopicManagementEvent.UpdateTopic(
                                        topicId = topicId,
                                        name = formState.name,
                                        description = formState.description,
                                        categoryId = formState.categoryId
                                    )
                                )
                            } else {
                                viewModel.onEvent(
                                    TopicManagementEvent.CreateTopic(
                                        name = formState.name,
                                        description = formState.description,
                                        categoryId = formState.categoryId
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer {
                                alpha = if (formState.isValid && !state.isCreating && !state.isUpdating) 1f else 0.5f
                            }
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(GradientStart, GradientEnd)
                                ),
                                shape = RoundedCornerShape(40.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = Color.White.copy(alpha = 0.7f),
                            contentColor = Color.White
                        ),
                        enabled = formState.isValid && !state.isCreating && !state.isUpdating
                    ) {
                        if (state.isCreating || state.isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                if (isEditMode) Icons.Default.Save else Icons.Default.Add,
                                if (isEditMode) "Save" else "Create",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isEditMode) "Save Changes" else "Create",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            // Loading Overlay
            if (state.isCreating || state.isUpdating) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Optional: add a scrim
                }
            }
        }
    }

    // Snackbar Messages
    LaunchedEffect(state.actionError) {
        state.actionError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
            viewModel.onEvent(TopicManagementEvent.ClearActionMessages)
        }
    }
}


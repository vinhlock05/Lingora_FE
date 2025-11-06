package com.example.lingora_fe.admin.category.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.admin.category.presentation.CategoryFormState
import com.example.lingora_fe.admin.category.presentation.CategoryManagementEvent
import com.example.lingora_fe.admin.category.presentation.CategoryManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditCategoryScreen(
    categoryId: Int?,
    onNavigateBack: () -> Unit,
    viewModel: CategoryManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val isEditMode = categoryId != null

    // Load category data for edit mode
    LaunchedEffect(categoryId) {
        if (categoryId != null) {
            viewModel.loadCategoryIntoForm(categoryId)
        } else {
            viewModel.resetFormState()
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
    ) {
        val bottomPadding = it.calculateBottomPadding()
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
                        viewModel.updateFormState(formState.copy(name = it))
                    },
                    label = { Text("Category Name *") },
                    placeholder = { Text("e.g., Everyday English") },
                    leadingIcon = { Icon(Icons.Default.Category, "Category") },
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
                        viewModel.updateFormState(formState.copy(description = it))
                    },
                    label = { Text("Description *") },
                    placeholder = { Text("Enter category description...") },
                    leadingIcon = { 
                        Icon(Icons.Default.Description, "Description")
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
                            if (isEditMode && categoryId != null) {
                                viewModel.onEvent(
                                    CategoryManagementEvent.UpdateCategory(
                                        categoryId = categoryId,
                                        name = formState.name,
                                        description = formState.description
                                    )
                                )
                            } else {
                                viewModel.onEvent(
                                    CategoryManagementEvent.CreateCategory(
                                        name = formState.name,
                                        description = formState.description
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = formState.isValid && !state.isCreating && !state.isUpdating
                    ) {
                        if (state.isCreating || state.isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                if (isEditMode) Icons.Default.Save else Icons.Default.Add,
                                if (isEditMode) "Save" else "Create",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isEditMode) "Save Changes" else "Create")
                    }
                }

                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Category Information",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Categories help organize learning content. Make sure to provide a clear name and description.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Loading Overlay
            if (state.isCreating || state.isUpdating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
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
            viewModel.onEvent(CategoryManagementEvent.ClearActionMessages)
        }
    }
}


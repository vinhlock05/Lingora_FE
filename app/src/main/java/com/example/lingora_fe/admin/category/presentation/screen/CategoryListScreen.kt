package com.example.lingora_fe.admin.category.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.admin.category.domain.model.Category
import com.example.lingora_fe.admin.category.domain.model.CategorySortOption
import com.example.lingora_fe.admin.category.presentation.CategoryManagementEvent
import com.example.lingora_fe.admin.category.presentation.CategoryManagementViewModel
import com.example.lingora_fe.admin.common.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    onNavigateToCreateCategory: () -> Unit,
    onNavigateToEditCategory: (Int) -> Unit,
    onNavigateToCategoryTopics: (Int) -> Unit,
    viewModel: CategoryManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateCategory,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Create Category")
            }
        }
    ) {
        val bottomPadding = it.calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

            // Search Bar and Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.onEvent(CategoryManagementEvent.SearchCategories(it)) },
                    placeholder = "Search categories...",
                    modifier = Modifier.weight(1f)
                )
                
                // Sort Button
                SortButton(
                    onClick = { showSortMenu = true },
                    hasActiveSort = state.selectedSort != null
                )
            }

            // Active Sort
            if (state.selectedSort != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.selectedSort?.let { sort ->
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.onEvent(CategoryManagementEvent.SortBy(null)) },
                            label = { Text("Sort: ${sort.displayName}") },
                            trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            // Category List
            when {
                state.isLoading && state.categories.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null && state.categories.isEmpty() -> {
                    ErrorContent(
                        message = state.error!!,
                        onRetry = { viewModel.onEvent(CategoryManagementEvent.RefreshCategories) }
                    )
                }
                state.categories.isEmpty() -> {
                    EmptyContent(message = "No categories found", icon = Icons.Default.Folder)
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.categories, key = { it.id }) { category ->
                                CategoryCard(
                                    category = category,
                                    onClick = { onNavigateToCategoryTopics(category.id) },
                                    onEdit = { onNavigateToEditCategory(category.id) },
                                    onDelete = { showDeleteDialog = category.id }
                                )
                            }

                            // Pagination
                            if (state.totalPages > 1) {
                                item {
                                    PaginationControls(
                                        currentPage = state.currentPage,
                                        totalPages = state.totalPages,
                                        onPageChange = { page ->
                                            viewModel.onEvent(CategoryManagementEvent.LoadCategories(page))
                                        }
                                    )
                                }
                            }
                        }

                        // Loading Overlay
                        if (state.isLoading && state.categories.isNotEmpty()) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showSortMenu) {
        SortDialog(
            selectedSort = state.selectedSort,
            onDismiss = { showSortMenu = false },
            onSelectSort = { sort ->
                viewModel.onEvent(CategoryManagementEvent.SortBy(sort))
                showSortMenu = false
            }
        )
    }

    showDeleteDialog?.let { categoryId ->
        DeleteConfirmDialog(
            onConfirm = {
                viewModel.onEvent(CategoryManagementEvent.DeleteCategory(categoryId))
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }

    // Snackbar Messages
    LaunchedEffect(state.actionSuccess, state.actionError) {
        state.actionSuccess?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.onEvent(CategoryManagementEvent.ClearActionMessages)
        }
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
}

@Composable
fun CategoryCard(
    category: Category,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Topics count
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Article,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${category.totalTopics} topics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}


@Composable
fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, "Warning", tint = MaterialTheme.colorScheme.error) },
        title = { Text("Delete Category") },
        text = { Text("Are you sure you want to delete this category? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun SortDialog(
    selectedSort: CategorySortOption?,
    onDismiss: () -> Unit,
    onSelectSort: (CategorySortOption?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort Categories") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategorySortOption.values().forEach { sortOption ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSort(sortOption) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSort == sortOption,
                            onClick = { onSelectSort(sortOption) }
                        )
                        Text(
                            text = sortOption.displayName,
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        dismissButton = {
            if (selectedSort != null) {
                TextButton(onClick = { onSelectSort(null) }) {
                    Text("Clear")
                }
            }
        }
    )
}


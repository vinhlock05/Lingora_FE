package com.example.lingora_fe.admin.word.presentation.screen

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.admin.word.presentation.WordManagementEvent
import com.example.lingora_fe.admin.word.presentation.WordManagementViewModel
import com.example.lingora_fe.admin.word.presentation.screen.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordsInTopicScreen(
    topicId: Int,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    viewModel: WordManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAttach by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(topicId) { viewModel.onEvent(WordManagementEvent.LoadInTopic(topicId)) }

    LaunchedEffect(state.actionSuccess) {
        state.actionSuccess?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(WordManagementEvent.ClearActionMessages)
        }
    }

    LaunchedEffect(state.actionError) {
        state.actionError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            viewModel.onEvent(WordManagementEvent.ClearActionMessages)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallFloatingActionButton(onClick = { showAttach = true }) { Icon(Icons.Default.Link, null) }
                FloatingActionButton(onClick = onNavigateToCreate) { Icon(Icons.Default.Add, null) }
            }
        }
    ) { padding ->
        val bottom = padding.calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottom)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Topic Info Card
                state.currentTopic?.let { topic ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = topic.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = topic.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${state.totalWords ?: state.total} words",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Search Bar and Filter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChange = { viewModel.onEvent(WordManagementEvent.Search(it)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterButton(
                        onClick = { showFilterDialog = true },
                        hasActiveFilters = state.cefrFilter != null || state.typeFilter != null
                    )
                    SortButton(
                        onClick = { showSortDialog = true },
                        currentSort = state.selectedSort
                    )
                }

                // Active Filters
                if (state.cefrFilter != null || state.typeFilter != null || state.selectedSort != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.cefrFilter?.let { cefr ->
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.setCefrFilter(null) },
                                label = { Text(cefr) },
                                trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) }
                            )
                        }
                        state.typeFilter?.let { type ->
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.setTypeFilter(null) },
                                label = { Text(type) },
                                trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) }
                            )
                        }
                        state.selectedSort?.let { sort ->
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.onEvent(WordManagementEvent.SortBy(null)) },
                                label = { Text("Sort: $sort") },
                                trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }

                if (state.isLoading && state.words.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.words, key = { it.id }) { word ->
                            WordCard(
                                word = word,
                                onEdit = { onNavigateToEdit(word.id) },
                                onDelete = { showDeleteDialog = word.id }
                            )
                        }
                    }
                }
            }
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        WordFilterDialog(
            selectedCefr = state.cefrFilter,
            selectedType = state.typeFilter,
            onDismiss = { showFilterDialog = false },
            onApply = { cefr, type ->
                viewModel.setCefrFilter(cefr)
                viewModel.setTypeFilter(type)
                showFilterDialog = false
            },
            onClear = {
                viewModel.setCefrFilter(null)
                viewModel.setTypeFilter(null)
                showFilterDialog = false
            }
        )
    }

    // Sort Dialog
    if (showSortDialog) {
        WordSortDialog(
            selectedSort = state.selectedSort,
            onDismiss = { showSortDialog = false },
            onApply = { sort ->
                viewModel.onEvent(WordManagementEvent.SortBy(sort))
                showSortDialog = false
            },
            onClear = {
                viewModel.onEvent(WordManagementEvent.SortBy(null))
                showSortDialog = false
            }
        )
    }

    // Confirm remove from topic
    showDeleteDialog?.let { id ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Remove word") },
            text = { Text("Remove this word from the topic? The word will remain in the system.") },
            confirmButton = { TextButton(onClick = { viewModel.onEvent(WordManagementEvent.RemoveFromTopic(id)); showDeleteDialog = null }) { Text("Remove") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") } }
        )
    }

    if (showAttach) {
        // Reuse pagination-aware unclassified loader
        LaunchedEffect(Unit) { viewModel.onEvent(WordManagementEvent.LoadUnclassified()) }
        AlertDialog(
            onDismissRequest = { showAttach = false },
            confirmButton = { TextButton(onClick = { showAttach = false }) { Text("Close") } },
            title = { Text("Attach existing word") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.isLoadingUnclassified) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    } else {
                        if (state.unclassifiedWords.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(360.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No unclassified words available",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(Modifier.height(360.dp)) {
                                items(state.unclassifiedWords, key = { it.id }) { w ->
                                    ListItem(
                                        headlineContent = { Text(w.word) },
                                        supportingContent = { Text(w.meaning) },
                                        trailingContent = {
                                            TextButton(onClick = {
                                                viewModel.onEvent(WordManagementEvent.AttachExisting(w.id, topicId))
                                                showAttach = false
                                            }) { Text("Attach") }
                                        }
                                    )
                                }
                                item {
                                    if (state.unclassifiedTotalPages > 1) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                            TextButton(enabled = state.unclassifiedCurrentPage > 1, onClick = {
                                                viewModel.onEvent(WordManagementEvent.LoadUnclassified(state.unclassifiedCurrentPage - 1))
                                            }) { Text("Prev") }
                                            Spacer(Modifier.width(8.dp))
                                            Text("${state.unclassifiedCurrentPage}/${state.unclassifiedTotalPages}")
                                            Spacer(Modifier.width(8.dp))
                                            TextButton(enabled = state.unclassifiedCurrentPage < state.unclassifiedTotalPages, onClick = {
                                                viewModel.onEvent(WordManagementEvent.LoadUnclassified(state.unclassifiedCurrentPage + 1))
                                            }) { Text("Next") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

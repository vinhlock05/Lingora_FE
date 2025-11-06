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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.lingora_fe.admin.word.domain.model.Word
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
        AddExistingWordDialog(
            topicId = topicId,
            unclassifiedWords = state.unclassifiedWords,
            isLoading = state.isLoadingUnclassified,
            currentPage = state.unclassifiedCurrentPage,
            totalPages = state.unclassifiedTotalPages,
            onDismiss = { showAttach = false },
            onAttachWord = { wordId ->
                viewModel.onEvent(WordManagementEvent.AttachExisting(wordId, topicId))
                showAttach = false
            },
            onLoadUnclassified = { page ->
                viewModel.onEvent(WordManagementEvent.LoadUnclassified(page))
            }
        )
    }
}

@Composable
private fun AddExistingWordDialog(
    topicId: Int,
    unclassifiedWords: List<Word>,
    isLoading: Boolean,
    currentPage: Int,
    totalPages: Int,
    onDismiss: () -> Unit,
    onAttachWord: (Int) -> Unit,
    onLoadUnclassified: (Int) -> Unit
) {
    // Load unclassified words when dialog opens
    LaunchedEffect(Unit) {
        onLoadUnclassified(1)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Link, "Attach Existing Word") },
        title = { Text("Attach Existing Word") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Select an unclassified word to add to this topic:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    unclassifiedWords.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.TextFields,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "No unclassified words available",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(unclassifiedWords, key = { it.id }) { word ->
                                    WordCardForDialog(
                                        word = word,
                                        onClick = { onAttachWord(word.id) }
                                    )
                                }
                            }

                            // Pagination
                            if (totalPages > 1) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { onLoadUnclassified(currentPage - 1) },
                                        enabled = currentPage > 1 && !isLoading
                                    ) {
                                        Icon(Icons.Default.ChevronLeft, "Previous")
                                    }
                                    
                                    Text(
                                        text = "Page $currentPage of $totalPages",
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    
                                    IconButton(
                                        onClick = { onLoadUnclassified(currentPage + 1) },
                                        enabled = currentPage < totalPages && !isLoading
                                    ) {
                                        Icon(Icons.Default.ChevronRight, "Next")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun WordCardForDialog(
    word: Word,
    onClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image or placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (word.imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(word.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = word.word,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.TextFields,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Word Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    // Type
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = word.type,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    // CEFR Level
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = word.cefrLevel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = word.meaning,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Add Icon
            Icon(
                Icons.Default.Add,
                contentDescription = "Attach",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

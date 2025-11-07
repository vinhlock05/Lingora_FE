package com.example.lingora_fe.admin.word.presentation.screen

import android.media.MediaPlayer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.lingora_fe.admin.word.domain.model.Word
import com.example.lingora_fe.admin.word.presentation.WordManagementEvent
import com.example.lingora_fe.admin.word.presentation.WordManagementViewModel
import com.example.lingora_fe.admin.common.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    viewModel: WordManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onEvent(WordManagementEvent.LoadAll()) }

    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

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
            FloatingActionButton(onClick = onNavigateToCreate) { Icon(Icons.Default.Add, null) }
        }
    ) { padding ->
        val bottom = padding.calculateBottomPadding()
        Column(modifier = Modifier.fillMaxSize().padding(bottom = bottom)) {
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
                    placeholder = "Search words...",
                    modifier = Modifier.weight(1f)
                )
                FilterButton(
                    onClick = { showFilterDialog = true },
                    hasActiveFilters = state.cefrFilter != null || state.typeFilter != null
                )
                SortButton(
                    onClick = { showSortDialog = true },
                    hasActiveSort = state.selectedSort != null
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

            when {
                state.isLoading && state.words.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
                state.error != null && state.words.isEmpty() -> {
                    ErrorContent(
                        message = state.error!!,
                        onRetry = { viewModel.onEvent(WordManagementEvent.LoadAll()) }
                    )
                }
                state.words.isEmpty() -> {
                    EmptyContent(message = "No words found", icon = Icons.Default.TextFields)
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.words, key = { it.id }) { word ->
                                WordCard(
                                    word = word,
                                    onEdit = { onNavigateToEdit(word.id) },
                                    onDelete = { showDeleteDialog = word.id }
                                )
                            }

                            // Pagination
                            if (state.totalPages > 1) {
                                item {
                                    PaginationControls(
                                        currentPage = state.currentPage,
                                        totalPages = state.totalPages,
                                        onPageChange = { page: Int ->
                                            viewModel.onEvent(WordManagementEvent.LoadAll(page))
                                        }
                                    )
                                }
                            }
                        }

                        // Loading Overlay
                        if (state.isLoading && state.words.isNotEmpty()) {
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

    // Confirm delete
    showDeleteDialog?.let { id ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete word") },
            text = { Text("Are you sure you want to delete this word?") },
            confirmButton = { TextButton(onClick = { viewModel.onEvent(WordManagementEvent.Delete(id)); showDeleteDialog = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun WordCard(word: Word, onEdit: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row with word and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = word.word,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        word.phonetic?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Audio button
                        word.audioUrl?.let { audioUrl ->
                            IconButton(
                                onClick = {
                                    try {
                                        mediaPlayer?.release()
                                        mediaPlayer = MediaPlayer().apply {
                                            setDataSource(audioUrl)
                                            prepare()
                                            start()
                                            setOnCompletionListener { release() }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.VolumeUp,
                                    "Play audio",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = word.cefrLevel,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = word.type,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Image if available
            word.imageUrl?.let { imageUrl ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = word.word,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Meaning
            Text(
                text = word.meaning,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Example
            word.example?.let { example ->
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    Text(
                        text = "Example:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = example,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    word.exampleTranslation?.let { translation ->
                        Text(
                            text = translation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }
}


@Composable
fun WordFilterDialog(
    selectedCefr: String?,
    selectedType: String?,
    onDismiss: () -> Unit,
    onApply: (String?, String?) -> Unit,
    onClear: () -> Unit
) {
    var tempCefr by remember { mutableStateOf(selectedCefr) }
    var tempType by remember { mutableStateOf(selectedType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Words") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // CEFR Level Filter
                Text("CEFR Level", style = MaterialTheme.typography.titleSmall)
                listOf("A1", "A2", "B1", "B2", "C1", "C2").forEach { level ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempCefr = if (tempCefr == level) null else level }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tempCefr == level,
                            onClick = { tempCefr = if (tempCefr == level) null else level }
                        )
                        Text(level, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Divider()

                // Type Filter
                Text("Type", style = MaterialTheme.typography.titleSmall)
                listOf("noun", "verb", "adjective", "adverb", "phrase", "preposition", "conjunction", "interjection", "pronoun", "determiner", "article", "numeral", "unknown").forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempType = if (tempType == type) null else type }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tempType == type,
                            onClick = { tempType = if (tempType == type) null else type }
                        )
                        Text(type, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(tempCefr, tempType) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClear) {
                    Text("Clear All")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun WordSortDialog(
    selectedSort: String?,
    onDismiss: () -> Unit,
    onApply: (String?) -> Unit,
    onClear: () -> Unit
) {
    var tempSort by remember { mutableStateOf(selectedSort) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort Words") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("id", "word", "cefrLevel").forEach { sort ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempSort = if (tempSort == sort) null else sort }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tempSort == sort,
                            onClick = { tempSort = if (tempSort == sort) null else sort }
                        )
                        Text(sort, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(tempSort) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClear) {
                    Text("Clear")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}


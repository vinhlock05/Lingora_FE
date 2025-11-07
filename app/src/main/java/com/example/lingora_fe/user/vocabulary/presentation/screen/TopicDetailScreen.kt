package com.example.lingora_fe.user.vocabulary.presentation.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lingora_fe.admin.common.presentation.components.SearchBar
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.TopBarBorder
import com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType
import com.example.lingora_fe.user.vocabulary.presentation.viewmodel.TopicDetailUiState
import com.example.lingora_fe.user.vocabulary.presentation.viewmodel.TopicDetailViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    topicId: Int,
    onBackClick: () -> Unit,
    onStartLearning: (Int, Int) -> Unit, // topicId, wordCount
    viewModel: TopicDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Learning, 1 = Word List
    val listState = rememberLazyListState()

    // Debug logging
    LaunchedEffect(uiState) {
        android.util.Log.d("TopicDetailScreen", "UI State updated: words=${uiState.words.size}, isLoading=${uiState.isLoading}, error=${uiState.error}")
        android.util.Log.d("TopicDetailScreen", "Stats: totalWordsAll=${uiState.totalWordsAll}, learnedCountAll=${uiState.learnedCountAll}, masteredCount=${uiState.masteredWordsCount}")
    }

    LaunchedEffect(topicId) {
        android.util.Log.d("TopicDetailScreen", "Loading topic words for topicId: $topicId")
        viewModel.loadTopicWords(topicId)
    }

    // Load more when scrolling near the end
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= uiState.words.size - 3) {
                    viewModel.loadNextPage()
                }
            }
    }

    // Debounce search
    var searchQuery by remember { mutableStateOf(uiState.searchQuery) }
    LaunchedEffect(searchQuery) {
        delay(500)
        if (searchQuery != uiState.searchQuery) {
            viewModel.searchWords(searchQuery)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.border(width = 1.dp, color = TopBarBorder),
                title = {
                    Column {
                        Text(
                            text = "Topic Detail",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (uiState.totalWordsAll > 0) {
                            Text(
                                text = "${uiState.learnedCountAll}/${uiState.totalWordsAll} từ đã học",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Học từ") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Danh sách từ") }
                )
            }

            // Content based on selected tab
            when (selectedTab) {
                0 -> LearningTab(
                    uiState = uiState,
                    viewModel = viewModel,
                    onStartLearning = { wordCount, gameTypes ->
                        onStartLearning(topicId, wordCount)
                    }
                )
                1 -> WordListTab(
                    uiState = uiState,
                    listState = listState,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onFilterHasLearned = { hasLearned ->
                        viewModel.filterByHasLearned(hasLearned)
                    },
                    onRefresh = { viewModel.refresh() }
                )
            }
        }
    }
}

@Composable
fun LearningTab(
    uiState: TopicDetailUiState,
    viewModel: TopicDetailViewModel,
    onStartLearning: (Int, Set<GameType>) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Topic info
            Column {
                Text(
                    text = "Topic: ${uiState.topicId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Setup based on your preferences",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            // Progress indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                CircularProgressIndicator(
                    progress = { uiState.progressPercent / 100f },
                    modifier = Modifier.size(60.dp),
                    color = GradientStart,
                    strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth,
                    trackColor = ProgressIndicatorDefaults.circularTrackColor,
                    strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "${uiState.learnedCountAll}/${uiState.totalWordsAll}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Learned",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            // Number of words section
            var showWordCountDialog by remember { mutableStateOf(false) }
            val wordCountOptions = remember { listOf(3, 5, 10, 15, 20, 25) }
            
            Column {
                Text(
                    text = "Number of words taken",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showWordCountDialog = true },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Words taken for each turn",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${uiState.selectedWordCount} words",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select word count"
                            )
                        }
                    }
                }
            }
            
            // Word count selection dialog
            if (showWordCountDialog) {
                AlertDialog(
                    onDismissRequest = { showWordCountDialog = false },
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Number of words taken")
                            IconButton(onClick = { showWordCountDialog = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close"
                                )
                            }
                        }
                    },
                    text = {
                        Column {
                            wordCountOptions.forEach { count ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setWordCount(count)
                                            showWordCountDialog = false
                                        }
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$count words",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    RadioButton(
                                        selected = uiState.selectedWordCount == count,
                                        onClick = {
                                            viewModel.setWordCount(count)
                                            showWordCountDialog = false
                                        }
                                    )
                                }
                                if (count != wordCountOptions.last()) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showWordCountDialog = false }) {
                            Text("Đóng")
                        }
                    }
                )
            }
        }

        item {
            // Type of word games section
            Column {
                Text(
                    text = "Type of word games",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Choose at least 2 word game types",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GameTypeItem(
                            title = "Nghe điền từ",
                            isSelected = uiState.selectedGameTypes.contains(GameType.LISTEN_FILL),
                            onClick = { viewModel.toggleGameType(GameType.LISTEN_FILL) }
                        )
                        GameTypeItem(
                            title = "Nghe chọn từ",
                            isSelected = uiState.selectedGameTypes.contains(GameType.LISTEN_CHOOSE),
                            onClick = { viewModel.toggleGameType(GameType.LISTEN_CHOOSE) }
                        )
                        GameTypeItem(
                            title = "Đúng/Sai",
                            isSelected = uiState.selectedGameTypes.contains(GameType.TRUE_FALSE),
                            onClick = { viewModel.toggleGameType(GameType.TRUE_FALSE) }
                        )
                        GameTypeItem(
                            title = "Nhìn từ chọn nghĩa",
                            isSelected = uiState.selectedGameTypes.contains(GameType.SEE_WORD_CHOOSE_MEANING),
                            onClick = { viewModel.toggleGameType(GameType.SEE_WORD_CHOOSE_MEANING) }
                        )
                        GameTypeItem(
                            title = "Nhìn nghĩa chọn từ",
                            isSelected = uiState.selectedGameTypes.contains(GameType.SEE_MEANING_CHOOSE_WORD),
                            onClick = { viewModel.toggleGameType(GameType.SEE_MEANING_CHOOSE_WORD) }
                        )
                    }
                }
            }
        }

        item {
            // Start learning button
            Button(
                onClick = {
                    if (uiState.selectedGameTypes.size >= 2) {
                        onStartLearning(uiState.selectedWordCount, uiState.selectedGameTypes)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.selectedGameTypes.size >= 2 && !uiState.isLoadingStudyWords,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GradientStart
                )
            ) {
                if (uiState.isLoadingStudyWords) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "Bắt đầu học",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GameTypeItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = isSelected,
            onCheckedChange = { onClick() }
        )
    }
}

@Composable
fun WordListTab(
    uiState: com.example.lingora_fe.user.vocabulary.presentation.viewmodel.TopicDetailUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterHasLearned: (Boolean?) -> Unit,
    onRefresh: () -> Unit
) {
    var showFilterDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search and Filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = "Tìm kiếm từ...",
                modifier = Modifier.weight(1f)
            )
            
            // Filter button using FilterButton component
            com.example.lingora_fe.admin.common.presentation.components.FilterButton(
                onClick = { showFilterDialog = true },
                hasActiveFilters = uiState.hasLearnedFilter != null,
                modifier = Modifier
            )
        }

        // Progress stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${uiState.learnedCountAll}/${uiState.totalWordsAll} từ đã học",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${uiState.masteredWordsCount} từ đã thành thạo",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Active filter chip
        if (uiState.hasLearnedFilter != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = true,
                    onClick = { onFilterHasLearned(null) },
                    label = {
                        Text(
                            if (uiState.hasLearnedFilter == true) "Đã học" else "Chưa học"
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        // Word List
        when {
            uiState.isLoading && uiState.words.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.words.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.error,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = onRefresh) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            uiState.words.isEmpty() && !uiState.isLoading && uiState.error == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không có từ nào trong chủ đề này",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.words.size) { index ->
                        val word = uiState.words[index]
                        WordListItem(
                            word = word.word,
                            meaning = word.meaning,
                            phonetic = word.phonetic,
                            cefrLevel = word.cefrLevel,
                            type = word.type,
                            hasProgress = word.progress != null,
                            status = word.progress?.status?.value ?: "NEW"
                        )
                    }

                    // Loading indicator at the end
                    if (uiState.isLoading && uiState.words.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }

    // Filter dialog
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Lọc từ") },
            text = {
                Column {
                    TextButton(onClick = {
                        onFilterHasLearned(null)
                        showFilterDialog = false
                    }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Tất cả")
                                Text(
                                    text = "Tất cả các từ trong chủ đề này",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (uiState.hasLearnedFilter == null) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    TextButton(onClick = {
                        onFilterHasLearned(true)
                        showFilterDialog = false
                    }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Đã học")
                                Text(
                                    text = "Chỉ hiển thị các từ đã học",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (uiState.hasLearnedFilter == true) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    TextButton(onClick = {
                        onFilterHasLearned(false)
                        showFilterDialog = false
                    }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Chưa học")
                                Text(
                                    text = "Chỉ hiển thị các từ chưa học",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (uiState.hasLearnedFilter == false) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }
}

@Composable
fun WordListItem(
    word: String,
    meaning: String?,
    phonetic: String?,
    cefrLevel: String,
    type: String,
    hasProgress: Boolean,
    status: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = word,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (phonetic != null) {
                        Text(
                            text = phonetic,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                meaning?.let { meaningText ->
                    Text(
                        text = meaningText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = cefrLevel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = type,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (hasProgress) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (status) {
                        "LEARNING" -> Color(0xFFFFA726)
                        "REVIEWING" -> Color(0xFF42A5F5)
                        "MASTERED" -> Color(0xFF66BB6A)
                        "FORGOTTEN" -> Color(0xFFEF5350)
                        else -> Color(0xFF9E9E9E)
                    }.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


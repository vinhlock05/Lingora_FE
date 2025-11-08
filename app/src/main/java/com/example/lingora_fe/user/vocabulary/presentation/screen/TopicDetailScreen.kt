package com.example.lingora_fe.user.vocabulary.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lingora_fe.admin.common.presentation.components.SearchBar
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.TopBarBorder
import com.example.lingora_fe.user.vocabulary.presentation.components.QuestionTypeSelector
import com.example.lingora_fe.user.vocabulary.presentation.components.WordCountSelector
import com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType
import com.example.lingora_fe.user.vocabulary.presentation.viewmodel.TopicDetailUiState
import com.example.lingora_fe.user.vocabulary.presentation.viewmodel.TopicDetailViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    topicId: Int,
    topicName: String,
    onBackClick: () -> Unit,
    onStartLearning: (Int, Int, Set<GameType>) -> Unit, // topicId, wordCount, gameTypes
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
                        Text(
                            text = "Chi tiết chủ đề",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
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
                    topicName = topicName,
                    uiState = uiState,
                    viewModel = viewModel,
                    onStartLearning = { wordCount, gameTypes ->
                        onStartLearning(topicId, wordCount, gameTypes)
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
    topicName: String,
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
            // Header section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Phần tiêu đề bên trái
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Topic: $topicName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Điều chỉnh các tùy chọn bên dưới để bắt đầu phiên học từ của bạn.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Progress indicator bên phải
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Track (nền mờ)
                    CircularProgressIndicator(
                        progress = { uiState.progressPercent / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = GradientStart,
                        trackColor = GradientStart.copy(alpha = 0.2f),
                        strokeWidth = 6.dp
                    )

                    // Text giữa vòng tròn
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${uiState.learnedCountAll}/${uiState.totalWordsAll}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "đã học",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

        }

        item {
            WordCountSelector(
                selectedWordCount = uiState.selectedWordCount,
                onSelectCount = { count ->
                    viewModel.setWordCount(count)
                }
            )
        }

        item {
            // Type of word games section
            QuestionTypeSelector(
                selectedTypes = uiState.selectedGameTypes,
                onToggle = { viewModel.toggleGameType(it) }
            )
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
                            cefrLevel = word.cefrLevel,
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

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = {
                Text(
                    text = "Lọc từ vựng",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filterOptions = listOf(
                        Triple("Tất cả", "Tất cả các từ trong chủ đề này", null),
                        Triple("Đã học", "Chỉ hiển thị các từ đã học", true),
                        Triple("Chưa học", "Chỉ hiển thị các từ chưa học", false)
                    )

                    filterOptions.forEach { (title, subtitle, value) ->
                        val isSelected = uiState.hasLearnedFilter == value
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onFilterHasLearned(value)
                                    showFilterDialog = false
                                },
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            else
                                Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showFilterDialog = false },
                ) {
                    Text("Đóng")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

}

@Composable
fun WordListItem(
    word: String,
    cefrLevel: String,
    hasProgress: Boolean,
    status: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp) // shadow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: word • CEFR
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = word,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = cefrLevel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right: status chip (optional)
            if (hasProgress) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (status) {
                        "LEARNING" -> Color(0xFFFFA726)
                        "REVIEWING" -> Color(0xFF42A5F5)
                        "MASTERED" -> Color(0xFF66BB6A)
                        "FORGOTTEN" -> Color(0xFFEF5350)
                        else -> Color(0xFF9E9E9E)
                    }.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

}


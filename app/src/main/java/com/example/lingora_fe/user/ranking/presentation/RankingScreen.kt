package com.example.lingora_fe.user.ranking.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lingora_fe.user.ranking.domain.model.LeaderboardEntry
import com.example.lingora_fe.user.ranking.domain.model.XpHistoryEntry
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.user.ranking.presentation.components.ClassroomPicker
import com.example.lingora_fe.user.ranking.presentation.components.LeaderboardItem
import com.example.lingora_fe.user.ranking.presentation.components.MyRankCard
import com.example.lingora_fe.user.ranking.presentation.components.PeriodChips
import com.example.lingora_fe.user.ranking.presentation.components.RankingColors
import com.example.lingora_fe.user.ranking.presentation.components.XpHistoryItem

private enum class RankingTab(val labelVi: String) {
    GLOBAL("Toàn cầu"),
    CLASSROOM("Lớp học"),
    HISTORY("Lịch sử XP")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    onBackClick: () -> Unit,
    viewModel: RankingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(RankingTab.GLOBAL) }

    // Auto-refresh whenever the screen returns to the foreground — covers the
    // "user learned a flashcard, backed out, came back to ranking" flow.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onScreenResumed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            RankingTab.GLOBAL -> {
                if (state.globalEntries.isEmpty() && !state.isLoadingGlobal) {
                    viewModel.loadGlobalLeaderboard(page = 1)
                }
            }
            RankingTab.CLASSROOM -> {
                if (state.joinedClassrooms.isEmpty() && !state.isLoadingJoinedClassrooms) {
                    viewModel.loadJoinedClassrooms()
                }
            }
            RankingTab.HISTORY -> {
                if (state.historyEntries.isEmpty() && !state.isLoadingHistory) {
                    viewModel.loadXpHistory(page = 1)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bảng xếp hạng",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = RankingColors.ScreenBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(RankingColors.ScreenBackground)
        ) {
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Color.White,
                contentColor = GradientStart,
                indicator = { positions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(positions[selectedTab.ordinal]),
                        color = GradientStart
                    )
                }
            ) {
                RankingTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                tab.labelVi,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    RankingTab.GLOBAL -> Icons.Filled.Public
                                    RankingTab.CLASSROOM -> Icons.Filled.Group
                                    RankingTab.HISTORY -> Icons.Filled.History
                                },
                                contentDescription = null
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                RankingTab.GLOBAL -> GlobalTab(state = state, viewModel = viewModel)
                RankingTab.CLASSROOM -> ClassroomTab(state = state, viewModel = viewModel)
                RankingTab.HISTORY -> HistoryTab(state = state, viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlobalTab(state: RankingState, viewModel: RankingViewModel) {
    val listState = rememberLazyListState()
    val myUserId = state.myStats?.userId

    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            last >= state.globalEntries.lastIndex - 3 &&
                state.globalEntries.isNotEmpty() &&
                state.globalPage < state.globalTotalPages
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMoreGlobal()
    }

    // Pull-to-refresh binds to initial-load spinner: the indicator shows while
    // page-1 is fetching, and the downstream list shows its own loaders for
    // pagination.
    val isRefreshing = state.isLoadingGlobal && state.globalEntries.isNotEmpty()
    val pullState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshGlobal() },
        state = pullState,
        modifier = Modifier.fillMaxSize()
    ) {
        LeaderboardList(
            listState = listState,
            header = {
                MyRankCard(stats = state.myStats, period = state.period)
                Spacer(modifier = Modifier.height(12.dp))
                PeriodChips(selected = state.period, onSelected = viewModel::setPeriod)
                if (state.globalError != null) {
                    ErrorBanner(message = state.globalError, onRetry = viewModel::refreshGlobal)
                }
            },
            entries = state.globalEntries,
            myUserId = myUserId,
            isInitialLoading = state.isLoadingGlobal && state.globalEntries.isEmpty(),
            isLoadingMore = state.isLoadingGlobal && state.globalEntries.isNotEmpty(),
            emptyText = "Chưa có ai xuất hiện trên bảng xếp hạng cho khoảng thời gian này.",
            showLevel = true,
            showStreak = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassroomTab(state: RankingState, viewModel: RankingViewModel) {
    val listState = rememberLazyListState()
    val myUserId = state.myStats?.userId

    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            last >= state.classroomBoard.lastIndex - 3 &&
                state.classroomBoard.isNotEmpty() &&
                state.classroomPage < state.classroomTotalPages
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMoreClassroom()
    }

    if (state.isLoadingJoinedClassrooms && state.joinedClassrooms.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.joinedClassrooms.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.Group,
            title = "Bạn chưa tham gia lớp học nào",
            subtitle = "Hãy tham gia một lớp để xem bảng xếp hạng riêng của lớp đó.",
            onRetry = viewModel::refreshClassroom
        )
        return
    }

    val isRefreshing = state.isLoadingClassroomBoard && state.classroomBoard.isNotEmpty()
    val pullState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshClassroom() },
        state = pullState,
        modifier = Modifier.fillMaxSize()
    ) {
    LeaderboardList(
        listState = listState,
        header = {
            ClassroomPicker(
                classrooms = state.joinedClassrooms,
                selectedId = state.selectedClassroomId,
                onSelect = viewModel::selectClassroom
            )
            Spacer(modifier = Modifier.height(8.dp))
            PeriodChips(selected = state.period, onSelected = viewModel::setPeriod)
            ClassroomSummaryCard(
                className = state.classroomInfo?.name,
                rankText = state.myClassroomStats
                    ?.rankFor(state.period)
                    ?.let { "#$it" }
                    ?: "Chưa xếp hạng",
                xp = state.myClassroomStats?.xpFor(state.period) ?: 0,
                periodLabel = when (state.period) {
                    com.example.lingora_fe.user.ranking.domain.model.RankingPeriod.WEEKLY -> "Tuần này"
                    com.example.lingora_fe.user.ranking.domain.model.RankingPeriod.MONTHLY -> "Tháng này"
                    com.example.lingora_fe.user.ranking.domain.model.RankingPeriod.ALLTIME -> "Toàn thời gian"
                }
            )
            if (state.classroomBoardError != null) {
                ErrorBanner(message = state.classroomBoardError, onRetry = viewModel::refreshClassroom)
            }
        },
        entries = state.classroomBoard,
        myUserId = myUserId,
        isInitialLoading = state.isLoadingClassroomBoard && state.classroomBoard.isEmpty(),
        isLoadingMore = state.isLoadingClassroomBoard && state.classroomBoard.isNotEmpty(),
        emptyText = "Chưa có thành viên nào trong lớp xuất hiện trên bảng xếp hạng.",
        showLevel = false,
        showStreak = false
    )
    }
}

@Composable
private fun ClassroomSummaryCard(
    className: String?,
    rankText: String,
    xp: Int,
    periodLabel: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = RankingColors.CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = className ?: "Lớp học",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = RankingColors.TextPrimary
            )
            Text(
                text = periodLabel,
                style = MaterialTheme.typography.labelMedium,
                color = RankingColors.TextMuted
            )
            Text(
                text = "Hạng của bạn: $rankText",
                style = MaterialTheme.typography.bodyMedium,
                color = RankingColors.TextSecondary
            )
            Text(
                text = "$xp XP",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GradientStart,
                textAlign = TextAlign.Start
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryTab(state: RankingState, viewModel: RankingViewModel) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            last >= state.historyEntries.lastIndex - 3 &&
                state.historyEntries.isNotEmpty() &&
                state.historyPage < state.historyTotalPages
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMoreHistory()
    }

    if (state.isLoadingHistory && state.historyEntries.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.historyError != null && state.historyEntries.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.History,
            title = "Không thể tải lịch sử XP",
            subtitle = state.historyError,
            onRetry = viewModel::refreshHistory
        )
        return
    }

    if (state.historyEntries.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.History,
            title = "Chưa có hoạt động XP",
            subtitle = "Hãy học flashcard, làm bài kiểm tra hoặc đăng nhập hằng ngày để bắt đầu kiếm XP.",
            onRetry = viewModel::refreshHistory
        )
        return
    }

    val isRefreshing = state.isLoadingHistory && state.historyEntries.isNotEmpty()
    val pullState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshHistory() },
        state = pullState,
        modifier = Modifier.fillMaxSize()
    ) {
        XpHistoryList(
            listState = listState,
            entries = state.historyEntries,
            isLoadingMore = state.isLoadingHistory && state.historyEntries.isNotEmpty()
        )
    }
}

@Composable
private fun LeaderboardList(
    listState: androidx.compose.foundation.lazy.LazyListState,
    header: @Composable () -> Unit,
    entries: List<LeaderboardEntry>,
    myUserId: Int?,
    isInitialLoading: Boolean,
    isLoadingMore: Boolean,
    emptyText: String,
    showLevel: Boolean,
    showStreak: Boolean
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item { header() }

        if (isInitialLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
        } else if (entries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emptyText,
                        color = RankingColors.TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(items = entries, key = { it.userId }) { entry ->
                LeaderboardItem(
                    entry = entry,
                    isMe = entry.userId == myUserId,
                    showLevel = showLevel,
                    showStreak = showStreak
                )
            }
            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun XpHistoryList(
    listState: androidx.compose.foundation.lazy.LazyListState,
    entries: List<XpHistoryEntry>,
    isLoadingMore: Boolean
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        items(items = entries, key = { it.id }) { entry ->
            XpHistoryItem(entry = entry)
        }
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = message,
            color = RankingColors.ErrorText,
            style = MaterialTheme.typography.bodySmall
        )
        BrandOutlinedButton(onClick = onRetry, label = "Thử lại")
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String?,
    onRetry: (() -> Unit)? = null
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RankingColors.TextPlaceholder,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = RankingColors.TextSecondary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = RankingColors.TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            if (onRetry != null) {
                BrandOutlinedButton(onClick = onRetry, label = "Thử lại")
            }
        }
    }
}

/**
 * OutlinedButton variant that uses the brand green instead of the Material
 * theme primary (which is currently purple). Keeps call sites short.
 */
@Composable
private fun BrandOutlinedButton(onClick: () -> Unit, label: String) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(1.dp, GradientStart),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = GradientStart)
    ) { Text(label) }
}

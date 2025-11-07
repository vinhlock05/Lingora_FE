package com.example.lingora_fe.user.practice.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.navigation.Route
import com.example.lingora_fe.user.practice.presentation.viewmodel.VocabularyReviewUiState
import com.example.lingora_fe.user.practice.presentation.viewmodel.VocabularyReviewViewModel
import com.example.lingora_fe.user.vocabulary.domain.repository.StatisticItem
import com.example.lingora_fe.user.vocabulary.presentation.components.QuestionTypeSelector
import com.example.lingora_fe.user.vocabulary.presentation.components.WordCountSelector
import com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyReviewScreen(
    navController: NavController,
    viewModel: VocabularyReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow("refreshReviewSummary", false)?.collect { shouldRefresh ->
            if (shouldRefresh) {
                viewModel.loadProgressSummary()
                savedStateHandle.set("refreshReviewSummary", false)
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
            }
            viewModel.resetError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ôn tập từ vựng",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MainText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GradientStart)
                    }
                }

                !uiState.hasLearnedWords -> {
                    EmptyReviewState()
                }

                else -> {
                    ReviewSummaryContent(
                        uiState = uiState,
                        onSelectCount = { count -> viewModel.setWordCount(count) },
                        onToggleType = viewModel::toggleGameType,
                        onStartReview = {
                            val route = Route.reviewPractice(
                                limit = uiState.selectedWordCount,
                                gameTypes = uiState.selectedGameTypes.toParamString()
                            )
                            navController.navigate(route)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewSummaryContent(
    uiState: VocabularyReviewUiState,
    onSelectCount: (Int) -> Unit,
    onToggleType: (GameType) -> Unit,
    onStartReview: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ProgressSummaryCard(
            totalLearned = uiState.totalLearnWord,
            statistics = uiState.statistics
        )

        WordCountSelector(
            selectedWordCount = uiState.selectedWordCount,
            onSelectCount = onSelectCount
        )

        QuestionTypeSelector(
            selectedTypes = uiState.selectedGameTypes,
            onToggle = onToggleType
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onStartReview,
            enabled = uiState.canStartReview,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
        ) {
            Text(
                text = "Ôn tập",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ProgressSummaryCard(
    totalLearned: Int,
    statistics: List<StatisticItem>
) {
    // Ensure we always have 5 levels (1-5), fill missing ones with 0 wordCount
    val allLevels = remember(statistics) {
        val statsMap = statistics.associateBy { it.srsLevel }
        (1..5).map { level ->
            statsMap[level] ?: StatisticItem(srsLevel = level, wordCount = 0)
        }
    }
    
    val maxValue = allLevels.maxOfOrNull { it.wordCount }?.coerceAtLeast(1) ?: 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header with gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Text(
                    text = "$totalLearned từ đã học",
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 20.sp)
                )
            }
            
            // Chart section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Cấp độ ghi nhớ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                // Bar chart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    allLevels.forEachIndexed { index, stat ->
                        LevelBar(
                            statistic = stat,
                            maxValue = maxValue,
                            index = index
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelBar(
    statistic: StatisticItem,
    maxValue: Int,
    index: Int
) {
    val heightFraction = if (maxValue > 0) {
        (statistic.wordCount.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val color = levelColorFor(statistic.srsLevel, index)
    val isMaster = statistic.srsLevel == 5

    Column(
        modifier = Modifier.width(52.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Word count at top
        Text(
            text = statistic.wordCount.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        // Bar container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xCBE4E4E7)),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Filled bar
            if (heightFraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(heightFraction)
                        .clip(RoundedCornerShape(18.dp))
                        .background(color)
                )
            }
        }
        
        // Level label with Master icon
        if (isMaster) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Master",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC94A),
                    modifier = Modifier.size(14.dp)
                )
            }
        } else {
            Text(
                text = "Lv.${statistic.srsLevel}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyReviewState() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bạn chưa có từ nào để ôn",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MainText,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Hãy học thêm từ mới trước khi quay lại ôn tập nhé!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}


private fun levelColorFor(level: Int, fallbackIndex: Int): Color {
    return when (level) {
        1 -> Color(0xFF63E28C)
        2 -> Color(0xFF3BA8F6)
        3 -> Color(0xFF9C8CFF)
        4 -> Color(0xFFFF8BA0)
        5 -> Color(0xFFFFC94A)
        else -> listOf(
            Color(0xFF60A5FA),
            Color(0xFF34D399),
            Color(0xFFF472B6),
            Color(0xFFF97316)
        )[fallbackIndex % 4]
    }
}

private fun Set<GameType>.toParamString(): String {
    return this.joinToString(separator = ",") { it.name }
}


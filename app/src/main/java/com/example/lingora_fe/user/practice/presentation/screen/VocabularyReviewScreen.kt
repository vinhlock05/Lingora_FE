package com.example.lingora_fe.user.practice.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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
                viewModel.loadWordsForReview()
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
                uiState.isLoading || uiState.isLoadingReviewWords -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GradientStart)
                    }
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

        if (!uiState.hasReviewWords) {
            // Hiển thị thông báo nếu chưa có từ cần ôn tập
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Chưa có từ nào cần ôn tập",
                    style = MaterialTheme.typography.titleLarge,
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
        } else {
            // Hiển thị đầy đủ các component nếu có từ cần ôn tập
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
}

@Composable
fun ProgressSummaryCard(
    totalLearned: Int,
    statistics: List<StatisticItem>
) {
    // Đảm bảo luôn có 5 cấp độ (1–5)
    val allLevels = remember(statistics) {
        val statsMap = statistics.associateBy { it.srsLevel }
        (1..5).map { level ->
            statsMap[level] ?: StatisticItem(srsLevel = level, wordCount = 0)
        }
    }

    val maxValue = allLevels.maxOfOrNull { it.wordCount }?.coerceAtLeast(1) ?: 1

    // Màu cột theo cấp độ (tương tự ProgressChartView)
    val barColors = listOf(
        Color(0xFFF44336), // đỏ
        Color(0xFFFFC107), // vàng
        Color(0xFF03A9F4), // xanh dương nhạt
        Color(0xFF3F51B5), // xanh dương đậm
        Color(0xFF4CAF50)  // xanh lá
    )

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔹 Tiêu đề
            Text(
                buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D47A1)
                        )
                    ) {
                        append("$totalLearned")
                    }
                    append("  từ đã học chia theo cấp độ")
                },
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 🔹 Biểu đồ cột
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                allLevels.forEachIndexed { index, stat ->
                    val ratio = stat.wordCount.toFloat() / maxValue
                    val barHeight = if (stat.wordCount == 0) 8.dp else 180.dp * ratio

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            "${stat.wordCount} từ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .height(barHeight)
                                .width(28.dp)
                                .background(
                                    color = barColors.getOrElse(index) { Color.Gray },
                                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                                )
                        )
                    }
                }
            }

            // 🔹 Đường ngang dưới cột
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFDDDDDD))
                    .padding(top = 2.dp, start = 20.dp, end = 20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 Label cho các cấp độ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                allLevels.forEachIndexed { index, _ ->
                    Text(
                        text = if (index+1 == 5) "Master" else "${index + 1}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = if (index+1!=5) Modifier.width(28.dp) else Modifier.wrapContentHeight(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
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


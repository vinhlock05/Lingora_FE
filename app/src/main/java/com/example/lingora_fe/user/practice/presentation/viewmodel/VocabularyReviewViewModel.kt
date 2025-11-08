package com.example.lingora_fe.user.practice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.user.vocabulary.domain.repository.ProgressRepository
import com.example.lingora_fe.user.vocabulary.domain.repository.StatisticItem
import com.example.lingora_fe.user.vocabulary.domain.repository.WordRepository
import com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private val DEFAULT_GAME_TYPES = setOf(
    GameType.LISTEN_FILL,
    GameType.LISTEN_CHOOSE,
    GameType.TRUE_FALSE,
    GameType.SEE_WORD_CHOOSE_MEANING,
    GameType.SEE_MEANING_CHOOSE_WORD
)

data class VocabularyReviewUiState(
    val isLoading: Boolean = false,
    val isLoadingReviewWords: Boolean = false,
    val error: String? = null,
    val totalLearnWord: Int = 0,
    val reviewWordsCount: Int = 0,
    val statistics: List<StatisticItem> = emptyList(),
    val selectedWordCount: Int = 5,
    val selectedGameTypes: Set<GameType> = DEFAULT_GAME_TYPES
) {
    val hasLearnedWords: Boolean get() = totalLearnWord > 0
    val hasReviewWords: Boolean get() = reviewWordsCount > 0
    val maxSelectableWordCount: Int get() = if (hasReviewWords) reviewWordsCount.coerceAtMost(totalLearnWord) else totalLearnWord
    val canStartReview: Boolean get() = hasReviewWords && selectedGameTypes.size >= 2
}

@HiltViewModel
class VocabularyReviewViewModel @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VocabularyReviewUiState(isLoading = true, isLoadingReviewWords = true))
    val uiState: StateFlow<VocabularyReviewUiState> = _uiState.asStateFlow()

    init {
        loadProgressSummary()
        loadWordsForReview()
    }

    fun loadProgressSummary() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            progressRepository.getProgressSummary().fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error,
                        totalLearnWord = 0,
                        statistics = emptyList(),
                        selectedWordCount = 0
                    )
                },
                ifRight = { summary ->
                    val adjustedWordCount = when {
                        summary.totalLearnedWord <= 0 -> 0
                        _uiState.value.selectedWordCount in 1..summary.totalLearnedWord -> _uiState.value.selectedWordCount
                        else -> summary.totalLearnedWord.coerceAtMost(DEFAULT_WORD_COUNT)
                    }.coerceIn(0, summary.totalLearnedWord)

                    val initialCount = if (summary.totalLearnedWord > 0) {
                        adjustedWordCount.takeIf { it > 0 } ?: summary.totalLearnedWord.coerceAtMost(DEFAULT_WORD_COUNT)
                    } else {
                        0
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        totalLearnWord = summary.totalLearnedWord,
                        statistics = summary.statistics,
                        selectedWordCount = initialCount
                    )
                }
            )
        }
    }

    fun setWordCount(value: Int) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(selectedWordCount = value)
    }

    fun toggleGameType(gameType: GameType) {
        val currentState = _uiState.value
        val currentTypes = currentState.selectedGameTypes
        val nextTypes = if (currentTypes.contains(gameType)) {
            currentTypes - gameType
        } else {
            currentTypes + gameType
        }
        _uiState.value = currentState.copy(selectedGameTypes = nextTypes)
    }

    fun loadWordsForReview() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingReviewWords = true, error = null)
            // Gọi API với limit lớn để lấy số lượng từ cần ôn tập
            wordRepository.getWordsForReview(limit = 1000, page = 1).fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingReviewWords = false,
                        error = error.message ?: "Failed to load words for review",
                        reviewWordsCount = 0,
                        selectedWordCount = 0
                    )
                },
                ifRight = { words ->
                    val reviewCount = words.size
                    // Điều chỉnh selectedWordCount nếu vượt quá số lượng từ cần ôn tập
                    val currentState = _uiState.value
                    
                    _uiState.value = _uiState.value.copy(
                        isLoadingReviewWords = false,
                        error = null,
                        reviewWordsCount = reviewCount
                    )
                }
            )
        }
    }

    fun resetError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    companion object {
        private const val DEFAULT_WORD_COUNT = 10
    }
}


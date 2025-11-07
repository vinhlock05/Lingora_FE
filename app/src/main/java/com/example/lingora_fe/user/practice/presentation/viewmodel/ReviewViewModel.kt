package com.example.lingora_fe.user.practice.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.user.vocabulary.domain.model.WordWithProgress
import com.example.lingora_fe.user.vocabulary.domain.repository.ProgressRepository
import com.example.lingora_fe.user.vocabulary.domain.repository.WordRepository
import com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

private val DEFAULT_REVIEW_TYPES = setOf(
    GameType.LISTEN_FILL,
    GameType.LISTEN_CHOOSE,
    GameType.TRUE_FALSE,
    GameType.SEE_WORD_CHOOSE_MEANING,
    GameType.SEE_MEANING_CHOOSE_WORD
)

data class ReviewUiState(
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val words: List<WordWithProgress> = emptyList(),
    val selectedGameTypes: Set<GameType> = DEFAULT_REVIEW_TYPES,
    val limit: Int = 0
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val progressRepository: ProgressRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val limit: Int = savedStateHandle.get<Int>("limit") ?: DEFAULT_LIMIT
    private val gameTypesParam: String? = savedStateHandle.get<String>("types")

    private val initialGameTypes: Set<GameType> = parseGameTypes(gameTypesParam)

    private val _uiState = MutableStateFlow(
        ReviewUiState(
            isLoading = true,
            selectedGameTypes = initialGameTypes,
            limit = limit
        )
    )
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        loadWordsForReview(limit)
    }

    fun loadWordsForReview(limit: Int = this.limit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            wordRepository.getWordsForReview(limit = limit, page = 1).fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load words for review",
                        words = emptyList()
                    )
                },
                ifRight = { words ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        words = words
                    )
                }
            )
        }
    }

    fun updateWordProgress(
        wrongCounts: Map<Int, Int>,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true)
            val now = currentUtcIsoString()
            val payload = _uiState.value.words.map { word ->
                Triple(word.id, wrongCounts[word.id] ?: 0, now)
            }

            progressRepository.updateWordProgress(payload).fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(isUpdating = false)
                    onResult(false, error)
                },
                ifRight = {
                    _uiState.value = _uiState.value.copy(isUpdating = false)
                    onResult(true, null)
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun parseGameTypes(param: String?): Set<GameType> {
        if (param.isNullOrBlank()) return DEFAULT_REVIEW_TYPES
        val tokens = param.split(',')
        val result = tokens.mapNotNull { token ->
            runCatching { GameType.valueOf(token) }.getOrNull()
        }.toSet()
        return if (result.isEmpty()) DEFAULT_REVIEW_TYPES else result
    }

    private fun currentUtcIsoString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return dateFormat.format(Date())
    }

    companion object {
        private const val DEFAULT_LIMIT = 20
    }
}

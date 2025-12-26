package com.example.lingora_fe.user.vocabulary.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.getOrElse
import com.example.lingora_fe.user.vocabulary.domain.model.Word
import com.example.lingora_fe.user.vocabulary.domain.model.WordWithProgress
import com.example.lingora_fe.user.vocabulary.domain.repository.ProgressRepository
import com.example.lingora_fe.user.vocabulary.domain.repository.TopicRepository
import com.example.lingora_fe.user.vocabulary.domain.repository.WordRepository
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

data class TopicDetailUiState(
    val topicId: Int = 0,
    val totalWordsAll: Int = 0,
    val learnedCountAll: Int = 0,
    val masteredWordsCount: Int = 0,
    val progressPercent: Float = 0f,
    val completed: Boolean = false,
    val words: List<WordWithProgress> = emptyList(),
    val studyWords: List<Word> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingStudyWords: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalWordsFiltered: Int = 0,
    val searchQuery: String = "",
    val hasLearnedFilter: Boolean? = null,
    val selectedWordCount: Int = 15,
    val selectedGameTypes: Set<GameType> = setOf(
        GameType.LISTEN_FILL,
        GameType.LISTEN_CHOOSE,
        GameType.TRUE_FALSE,
        GameType.SEE_WORD_CHOOSE_MEANING,
        GameType.SEE_MEANING_CHOOSE_WORD,
        GameType.PRONUNCIATION
    )
)

enum class GameType {
    LISTEN_FILL,           // Nghe điền từ
    LISTEN_CHOOSE,        // Nghe chọn từ
    TRUE_FALSE,           // Đúng/Sai
    SEE_WORD_CHOOSE_MEANING,  // Nhìn từ chọn nghĩa
    SEE_MEANING_CHOOSE_WORD,  // Nhìn nghĩa chọn từ
    PRONUNCIATION             // Luyện phát âm
}

@HiltViewModel
class TopicDetailViewModel @Inject constructor(
    private val topicRepository: TopicRepository,
    private val wordRepository: WordRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopicDetailUiState())
    val uiState: StateFlow<TopicDetailUiState> = _uiState.asStateFlow()

    fun loadTopicWords(topicId: Int, page: Int = 1, search: String? = null, hasLearned: Boolean? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                topicId = topicId,
                isLoading = true,
                error = null
            )
            
            val searchQuery = search ?: _uiState.value.searchQuery
            val hasLearnedFilter = hasLearned ?: _uiState.value.hasLearnedFilter
            
            wordRepository.getTopicWordsWithProgress(
                topicId = topicId,
                limit = 10,
                page = page,
                search = searchQuery.ifEmpty { null },
                hasLearned = hasLearnedFilter
            ).fold(
                ifLeft = { error ->
                    android.util.Log.e("TopicDetailViewModel", "Error loading topic words: $error")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                },
                ifRight = { (meta, words) ->
                    android.util.Log.d("TopicDetailViewModel", "Loaded ${words.size} words, meta: $meta")
                    val allWords = if (page == 1) words else _uiState.value.words + words
                    // Calculate mastered words count from all loaded words
                    // Note: This counts only from loaded words. For accurate total, backend should provide this.
                    val masteredCount = allWords.count { 
                        it.progress?.status == com.example.lingora_fe.user.vocabulary.domain.model.WordStatus.MASTERED 
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        totalWordsAll = meta.totalWordsAll,
                        learnedCountAll = meta.learnedCountAll,
                        masteredWordsCount = masteredCount,
                        progressPercent = meta.progressPercent,
                        completed = meta.completed,
                        words = allWords,
                        isLoading = false,
                        error = null,
                        currentPage = meta.currentPage,
                        totalPages = meta.totalPages,
                        totalWordsFiltered = meta.totalWordsFiltered,
                        searchQuery = searchQuery,
                        hasLearnedFilter = hasLearnedFilter
                    )
                }
            )
        }
    }

    fun loadWordsForStudy(topicId: Int, count: Int) {
        viewModelScope.launch {
            android.util.Log.d("TopicDetailViewModel", "Loading words for study: topicId=$topicId, count=$count")
            _uiState.value = _uiState.value.copy(
                isLoadingStudyWords = true,
                error = null
            )
            
            topicRepository.getWordsForStudy(topicId, count).fold(
                ifLeft = { error ->
                    android.util.Log.e("TopicDetailViewModel", "Error loading words for study: $error")
                    _uiState.value = _uiState.value.copy(
                        isLoadingStudyWords = false,
                        error = error
                    )
                },
                ifRight = { words ->
                    android.util.Log.d("TopicDetailViewModel", "Loaded ${words.size} words for study")
                    if (words.isEmpty()) {
                        android.util.Log.w("TopicDetailViewModel", "No words returned from API!")
                    }
                    _uiState.value = _uiState.value.copy(
                        studyWords = words,
                        isLoadingStudyWords = false,
                        error = null
                    )
                }
            )
        }
    }

    fun searchWords(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadTopicWords(
            topicId = _uiState.value.topicId,
            page = 1,
            search = query
        )
    }

    fun filterByHasLearned(hasLearned: Boolean?) {
        _uiState.value = _uiState.value.copy(hasLearnedFilter = hasLearned)
        loadTopicWords(
            topicId = _uiState.value.topicId,
            page = 1,
            hasLearned = hasLearned
        )
    }

    fun setWordCount(count: Int) {
        _uiState.value = _uiState.value.copy(selectedWordCount = count)
    }

    fun toggleGameType(gameType: GameType) {
        val currentTypes = _uiState.value.selectedGameTypes
        val newTypes = if (currentTypes.contains(gameType)) {
            currentTypes - gameType
        } else {
            currentTypes + gameType
        }
        _uiState.value = _uiState.value.copy(selectedGameTypes = newTypes)
        Log.d("TopicDetailViewModel", "Toggled game type: $gameType, new types: $newTypes")
    }

    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState.currentPage < currentState.totalPages && !currentState.isLoading) {
            loadTopicWords(
                topicId = currentState.topicId,
                page = currentState.currentPage + 1
            )
        }
    }

    fun refresh() {
        val currentState = _uiState.value
        loadTopicWords(
            topicId = currentState.topicId,
            page = 1,
            search = currentState.searchQuery.ifEmpty { null },
            hasLearned = currentState.hasLearnedFilter
        )
    }

    fun createWordProgressAfterLearning(wordIds: List<Int>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            progressRepository.createWordProgress(wordIds).fold(
                ifLeft = { error ->
                    onError(error)
                },
                ifRight = {
                    onSuccess()
                }
            )
        }
    }

    fun updateWordProgressAfterReview(
        wordProgressList: List<Triple<Int, Int, Date>>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            
            val requestList = wordProgressList.map { (wordId, wrongCount, reviewedDate) ->
                Triple(wordId, wrongCount, dateFormat.format(reviewedDate))
            }
            
            progressRepository.updateWordProgress(requestList).fold(
                ifLeft = { error ->
                    onError(error)
                },
                ifRight = {
                    onSuccess()
                }
            )
        }
    }
}


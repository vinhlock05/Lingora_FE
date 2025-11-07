package com.example.lingora_fe.admin.word.presentation

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.admin.word.domain.model.Word
import com.example.lingora_fe.admin.word.domain.repository.WordFilterOptions
import com.example.lingora_fe.admin.word.domain.repository.WordRepository
import com.example.lingora_fe.admin.topic.domain.repository.TopicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordManagementViewModel @Inject constructor(
    private val repository: WordRepository,
    private val topicRepository: TopicRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(WordManagementState())
    val state: StateFlow<WordManagementState> = _state.asStateFlow()

    private val _formState = MutableStateFlow(WordFormState())
    val formState: StateFlow<WordFormState> = _formState.asStateFlow()

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("lingora_prefs", Context.MODE_PRIVATE)

    private var currentTopicId: Int? = null

    fun onEvent(event: WordManagementEvent) {
        when (event) {
            is WordManagementEvent.LoadAll -> loadAll(event.page)
            is WordManagementEvent.LoadInTopic -> loadInTopic(event.topicId, event.page)
            is WordManagementEvent.Search -> search(event.query)
            is WordManagementEvent.SortBy -> sortBy(event.sort)
            WordManagementEvent.ClearFilters -> clearFilters()

            is WordManagementEvent.LoadDetails -> loadDetails(event.wordId)
            WordManagementEvent.ClearSelected -> clearSelected()

            is WordManagementEvent.Create -> create(event)
            is WordManagementEvent.Update -> update(event)
            is WordManagementEvent.Delete -> delete(event.wordId)
            is WordManagementEvent.RemoveFromTopic -> removeFromTopic(event.wordId)

            is WordManagementEvent.LoadUnclassified -> loadUnclassified(event.page)
            is WordManagementEvent.AttachExisting -> attachExisting(event.wordId, event.topicId)

            WordManagementEvent.ClearError -> _state.value = _state.value.copy(error = null)
            WordManagementEvent.ClearActionMessages -> _state.value = _state.value.copy(actionError = null, actionSuccess = null)
        }
    }

    private fun loadAll(page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            currentTopicId = null
            val token = getToken() ?: return@launch
            val filter = WordFilterOptions(
                search = _state.value.searchQuery.takeIf { it.isNotBlank() },
                sort = _state.value.selectedSort,
                page = page,
                limit = 20,
                cefrLevel = _state.value.cefrFilter,
                type = _state.value.typeFilter
            )
            repository.getAllWords(token, filter)
                .onRight { m ->
                    _state.value = _state.value.copy(
                        words = m.words,
                        currentPage = m.currentPage,
                        totalPages = m.totalPages,
                        total = m.total,
                        isLoading = false,
                        error = null
                    )
                }
                .onLeft { f -> _state.value = _state.value.copy(isLoading = false, error = f.message) }
        }
    }

    private fun loadInTopic(topicId: Int, page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            currentTopicId = topicId
            val token = getToken() ?: return@launch
            
            // Load topic info
            topicRepository.getTopicById(token, topicId)
                .onRight { topic ->
                    _state.value = _state.value.copy(currentTopic = topic)
                }
                .onLeft { /* Ignore topic load error, continue with words */ }
            
            val filter = WordFilterOptions(
                search = _state.value.searchQuery.takeIf { it.isNotBlank() },
                sort = _state.value.selectedSort,
                page = page,
                limit = 20,
                cefrLevel = _state.value.cefrFilter,
                type = _state.value.typeFilter
            )
            repository.getTopicWords(token, topicId, filter)
                .onRight { m ->
                    _state.value = _state.value.copy(
                        words = m.words,
                        currentPage = m.currentPage,
                        totalPages = m.totalPages,
                        total = m.total,
                        totalWords = m.totalWords,
                        isLoading = false,
                        error = null
                    )
                }
                .onLeft { f -> _state.value = _state.value.copy(isLoading = false, error = f.message) }
        }
    }

    private fun search(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        currentTopicId?.let { loadInTopic(it) } ?: loadAll()
    }

    private fun sortBy(sort: String?) {
        _state.value = _state.value.copy(selectedSort = sort)
        currentTopicId?.let { loadInTopic(it) } ?: loadAll()
    }

    private fun clearFilters() {
        _state.value = _state.value.copy(searchQuery = "", selectedSort = null, cefrFilter = null, typeFilter = null)
        currentTopicId?.let { loadInTopic(it) } ?: loadAll()
    }

    fun setCefrFilter(level: String?) { _state.value = _state.value.copy(cefrFilter = level); currentTopicId?.let { loadInTopic(it) } ?: loadAll() }
    fun setTypeFilter(type: String?) { _state.value = _state.value.copy(typeFilter = type); currentTopicId?.let { loadInTopic(it) } ?: loadAll() }

    private fun loadDetails(wordId: Int) {
        viewModelScope.launch {
            val token = getToken() ?: return@launch
            repository.getWordById(token, wordId)
                .onRight { w -> _state.value = _state.value.copy(selectedWord = w) }
        }
    }

    private fun clearSelected() { _state.value = _state.value.copy(selectedWord = null) }

    private fun create(e: WordManagementEvent.Create) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCreating = true)
            val token = getToken() ?: return@launch
            val word = Word(
                id = 0,
                word = e.word,
                phonetic = e.phonetic,
                cefrLevel = e.cefrLevel,
                type = e.type,
                meaning = e.meaning,
                example = e.example,
                exampleTranslation = e.exampleTranslation,
                audioUrl = e.audioUrl,
                imageUrl = e.imageUrl,
                topicId = e.topicId,
                createdAt = null,
                updatedAt = null,
                deletedAt = null,
            )
            repository.createWord(token, word)
                .onRight {
                    _state.value = _state.value.copy(isCreating = false, actionSuccess = "Word created")
                    currentTopicId?.let { loadInTopic(it) } ?: loadAll()
                }
                .onLeft { f -> _state.value = _state.value.copy(isCreating = false, actionError = f.message) }
        }
    }

    private fun update(e: WordManagementEvent.Update) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUpdating = true)
            val token = getToken() ?: return@launch
            val base = _state.value.selectedWord ?: return@launch
            val word = base.copy(
                word = e.word ?: base.word,
                meaning = e.meaning ?: base.meaning,
                phonetic = e.phonetic ?: base.phonetic,
                cefrLevel = e.cefrLevel ?: base.cefrLevel,
                type = e.type ?: base.type,
                example = e.example ?: base.example,
                exampleTranslation = e.exampleTranslation ?: base.exampleTranslation,
                audioUrl = e.audioUrl ?: base.audioUrl,
                imageUrl = e.imageUrl ?: base.imageUrl,
                topicId = e.topicId ?: base.topicId
            )
            repository.updateWord(token, e.wordId, word)
                .onRight {
                    _state.value = _state.value.copy(isUpdating = false, actionSuccess = "Word updated")
                    currentTopicId?.let { loadInTopic(it) } ?: loadAll()
                }
                .onLeft { f -> _state.value = _state.value.copy(isUpdating = false, actionError = f.message) }
        }
    }

    private fun delete(wordId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDeleting = true)
            val token = getToken() ?: return@launch
            repository.deleteWord(token, wordId)
                .onRight {
                    _state.value = _state.value.copy(isDeleting = false, actionSuccess = "Word deleted")
                    loadAll(_state.value.currentPage)
                }
                .onLeft { f -> _state.value = _state.value.copy(isDeleting = false, actionError = f.message) }
        }
    }

    private fun removeFromTopic(wordId: Int) {
        viewModelScope.launch {
            val token = getToken() ?: return@launch
            _state.value = _state.value.copy(isUpdating = true, actionError = null)
            val base = repository.getWordById(token, wordId).orNull() ?: run {
                _state.value = _state.value.copy(isUpdating = false, actionError = "Word not found")
                return@launch
            }
            val cleared = base.copy(topicId = null)
            Log.d("WordManagementViewModel", "Removing word $wordId from topic, updating to: $cleared")
            repository.updateWord(token, wordId, cleared)
                .onRight {
                    _state.value = _state.value.copy(isUpdating = false, actionSuccess = "Word removed from topic")
                    // Reload the topic words to refresh the list
                    currentTopicId?.let { topicId ->
                        loadInTopic(topicId, _state.value.currentPage)
                    }
                }
                .onLeft { f -> 
                    _state.value = _state.value.copy(
                        isUpdating = false, 
                        actionError = f.message ?: "Failed to remove word from topic"
                    )
                }
        }
    }

    private fun loadUnclassified(page: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingUnclassified = true)
            val token = getToken() ?: return@launch
            val filter = WordFilterOptions(page = page, hasTopic = false)
            repository.getAllWords(token, filter)
                .onRight { m ->
                    _state.value = _state.value.copy(
                        unclassifiedWords = m.words,
                        unclassifiedCurrentPage = m.currentPage,
                        unclassifiedTotalPages = m.totalPages,
                        isLoadingUnclassified = false
                    )
                }
                .onLeft { f -> _state.value = _state.value.copy(isLoadingUnclassified = false, actionError = f.message) }
        }
    }

    private fun attachExisting(wordId: Int, topicId: Int) {
        viewModelScope.launch {
            val token = getToken() ?: return@launch
            val base = repository.getWordById(token, wordId).orNull() ?: return@launch
            val updated = base.copy(topicId = topicId)
            repository.updateWord(token, wordId, updated)
                .onRight {
                    _state.value = _state.value.copy(actionSuccess = "Word attached to topic")
                    currentTopicId?.let { loadInTopic(it) }
                }
                .onLeft { f -> _state.value = _state.value.copy(actionError = f.message) }
        }
    }

    fun setFormTopicId(topicId: Int?) { _formState.value = _formState.value.copy(topicId = topicId).validate() }
    fun updateFormState(newState: WordFormState) { _formState.value = newState.validate() }
    fun setActionError(error: String?) { _state.value = _state.value.copy(actionError = error) }

    private fun getToken(): String? = sharedPreferences.getString("access_token", null)
}



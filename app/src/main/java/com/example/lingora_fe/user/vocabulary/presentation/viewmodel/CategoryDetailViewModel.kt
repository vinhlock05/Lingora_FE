package com.example.lingora_fe.user.vocabulary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.getOrElse
import com.example.lingora_fe.user.vocabulary.domain.model.TopicProgress
import com.example.lingora_fe.user.vocabulary.domain.repository.TopicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryDetailUiState(
    val categoryId: Int = 0,
    val categoryName: String = "",
    val categoryDescription: String = "",
    val totalTopics: Int = 0,
    val completedTopics: Int = 0,
    val progressPercent: Float = 0f,
    val completed: Boolean = false,
    val topics: List<TopicProgress> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val searchQuery: String = "",
    val sortQuery: String = ""
)

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val topicRepository: TopicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryDetailUiState())
    val uiState: StateFlow<CategoryDetailUiState> = _uiState.asStateFlow()

    fun loadCategoryTopics(categoryId: Int, page: Int = 1, search: String? = null, sort: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                categoryId = categoryId,
                isLoading = true,
                error = null
            )
            
            val searchQuery = search ?: _uiState.value.searchQuery
            val sortQuery = sort ?: _uiState.value.sortQuery
            
            topicRepository.getCategoryTopicsWithProgress(
                categoryId = categoryId,
                limit = 10,
                page = page,
                search = searchQuery.ifEmpty { null },
                sort = sortQuery.ifEmpty { null }
            ).fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                },
                ifRight = { (meta, topics) ->
                    _uiState.value = _uiState.value.copy(
                        categoryName = meta.name,
                        categoryDescription = meta.description,
                        totalTopics = meta.totalTopics,
                        completedTopics = meta.completedTopics,
                        progressPercent = meta.progressPercent,
                        completed = meta.completed,
                        topics = if (page == 1) topics else _uiState.value.topics + topics,
                        isLoading = false,
                        error = null,
                        currentPage = meta.currentPage,
                        totalPages = meta.totalPages,
                        searchQuery = searchQuery,
                        sortQuery = sortQuery
                    )
                }
            )
        }
    }

    fun searchTopics(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadCategoryTopics(
            categoryId = _uiState.value.categoryId,
            page = 1,
            search = query
        )
    }

    fun sortTopics(sort: String) {
        _uiState.value = _uiState.value.copy(sortQuery = sort)
        loadCategoryTopics(
            categoryId = _uiState.value.categoryId,
            page = 1,
            sort = sort
        )
    }

    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState.currentPage < currentState.totalPages && !currentState.isLoading) {
            loadCategoryTopics(
                categoryId = currentState.categoryId,
                page = currentState.currentPage + 1
            )
        }
    }

    fun refresh() {
        val currentState = _uiState.value
        loadCategoryTopics(
            categoryId = currentState.categoryId,
            page = 1,
            search = currentState.searchQuery.ifEmpty { null },
            sort = currentState.sortQuery.ifEmpty { null }
        )
    }
}


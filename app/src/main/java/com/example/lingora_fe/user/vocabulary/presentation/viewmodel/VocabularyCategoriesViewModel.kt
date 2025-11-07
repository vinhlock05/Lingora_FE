package com.example.lingora_fe.user.vocabulary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.getOrElse
import com.example.lingora_fe.user.vocabulary.domain.model.CategoryProgress
import com.example.lingora_fe.user.vocabulary.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VocabularyCategoriesUiState(
    val categories: List<CategoryProgress> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val total: Int = 0,
    val searchQuery: String = ""
)

@HiltViewModel
class VocabularyCategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VocabularyCategoriesUiState())
    val uiState: StateFlow<VocabularyCategoriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories(page: Int = 1, search: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val searchQuery = search ?: _uiState.value.searchQuery
            categoryRepository.getCategoriesWithProgress(
                limit = 20,
                page = page,
                search = searchQuery.ifEmpty { null }
            ).fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                },
                ifRight = { (categories, meta) ->
                    _uiState.value = _uiState.value.copy(
                        categories = if (page == 1) categories else _uiState.value.categories + categories,
                        isLoading = false,
                        error = null,
                        currentPage = meta.currentPage,
                        totalPages = meta.totalPages,
                        total = meta.total,
                        searchQuery = searchQuery
                    )
                }
            )
        }
    }

    fun searchCategories(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadCategories(page = 1, search = query)
    }

    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState.currentPage < currentState.totalPages && !currentState.isLoading) {
            loadCategories(page = currentState.currentPage + 1)
        }
    }

    fun refresh() {
        loadCategories(page = 1, search = _uiState.value.searchQuery.ifEmpty { null })
    }
}


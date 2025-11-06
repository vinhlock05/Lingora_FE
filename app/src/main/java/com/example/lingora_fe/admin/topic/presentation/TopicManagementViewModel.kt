package com.example.lingora_fe.admin.topic.presentation

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.admin.topic.domain.model.CreateTopicData
import com.example.lingora_fe.admin.topic.domain.model.TopicFilterOptions
import com.example.lingora_fe.admin.topic.domain.model.TopicSortOption
import com.example.lingora_fe.admin.topic.domain.model.UpdateTopicData
import com.example.lingora_fe.admin.topic.domain.repository.TopicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicManagementViewModel @Inject constructor(
    private val repository: TopicRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(TopicManagementState())
    val state: StateFlow<TopicManagementState> = _state.asStateFlow()

    private val _formState = MutableStateFlow(TopicFormState())
    val formState: StateFlow<TopicFormState> = _formState.asStateFlow()

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("lingora_prefs", Context.MODE_PRIVATE)

    private var currentCategoryId: Int? = null

    fun onEvent(event: TopicManagementEvent) {
        when (event) {
            is TopicManagementEvent.LoadCategoryTopics -> loadCategoryTopics(event.categoryId, event.page)
            is TopicManagementEvent.SearchTopics -> searchTopics(event.query)
            is TopicManagementEvent.SortBy -> sortBy(event.sortOption)
            is TopicManagementEvent.ClearFilters -> clearFilters()
            is TopicManagementEvent.RefreshTopics -> refreshTopics(event.categoryId)
            
            is TopicManagementEvent.LoadTopicDetails -> loadTopicDetails(event.topicId)
            is TopicManagementEvent.ClearSelectedTopic -> clearSelectedTopic()
            
            is TopicManagementEvent.CreateTopic -> createTopic(
                name = event.name,
                description = event.description,
                categoryId = event.categoryId
            )
            
            is TopicManagementEvent.UpdateTopic -> updateTopic(
                topicId = event.topicId,
                name = event.name,
                description = event.description,
                categoryId = event.categoryId
            )
            
            is TopicManagementEvent.DeleteTopic -> deleteTopic(event.topicId)
            is TopicManagementEvent.RemoveFromCategory -> removeFromCategory(event.topicId)
            
            // Standalone topic events
            is TopicManagementEvent.LoadAllTopics -> loadAllTopics(event.page)
            is TopicManagementEvent.RefreshAllTopics -> refreshAllTopics()
            
            // Unclassified topics events
            is TopicManagementEvent.LoadUnclassifiedTopics -> loadUnclassifiedTopics(event.page)
            is TopicManagementEvent.AddExistingTopic -> addExistingTopicToCategory(event.topicId, event.categoryId)
            
            is TopicManagementEvent.ClearError -> clearError()
            is TopicManagementEvent.ClearActionMessages -> clearActionMessages()
        }
    }

    private fun loadCategoryTopics(categoryId: Int, page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            currentCategoryId = categoryId
            
            val token = getToken() ?: run {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Authentication token not found"
                )
                return@launch
            }

            val filterOptions = TopicFilterOptions(
                search = _state.value.searchQuery.takeIf { it.isNotBlank() },
                sort = _state.value.selectedSort?.apiValue,
                page = page,
                limit = 20
            )

            repository.getCategoryWithTopics(token, categoryId, filterOptions)
                .onRight { categoryWithTopics ->
                    _state.value = _state.value.copy(
                        categoryWithTopics = categoryWithTopics,
                        topics = categoryWithTopics.topics,
                        currentPage = categoryWithTopics.currentPage,
                        totalPages = categoryWithTopics.totalPages,
                        totalTopics = categoryWithTopics.totalTopics,
                        isLoading = false,
                        error = null
                    )
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = failure.message
                    )
                }
        }
    }

    private fun searchTopics(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        currentCategoryId?.let { loadCategoryTopics(it, page = 1) }
    }

    private fun sortBy(sortOption: TopicSortOption?) {
        _state.value = _state.value.copy(selectedSort = sortOption)
        currentCategoryId?.let { loadCategoryTopics(it, page = 1) }
    }

    private fun clearFilters() {
        _state.value = _state.value.copy(
            searchQuery = "",
            selectedSort = null
        )
        currentCategoryId?.let { loadCategoryTopics(it, page = 1) }
    }

    private fun refreshTopics(categoryId: Int) {
        loadCategoryTopics(categoryId, _state.value.currentPage)
    }

    private fun loadTopicDetails(topicId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isTopicDetailsLoading = true)
            
            val token = getToken() ?: return@launch

            repository.getTopicById(token, topicId)
                .onRight { topic ->
                    _state.value = _state.value.copy(
                        selectedTopic = topic,
                        isTopicDetailsLoading = false
                    )
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isTopicDetailsLoading = false,
                        error = failure.message
                    )
                }
        }
    }

    private fun clearSelectedTopic() {
        _state.value = _state.value.copy(selectedTopic = null)
    }

    private fun createTopic(
        name: String,
        description: String,
        categoryId: Int?
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCreating = true, actionError = null)
            
            val token = getToken() ?: return@launch

            val topicData = CreateTopicData(
                name = name,
                description = description,
                categoryId = categoryId
            )

            repository.createTopic(token, topicData)
                .onRight {
                    _state.value = _state.value.copy(
                        isCreating = false,
                        actionSuccess = "Topic created successfully"
                    )
                    currentCategoryId?.let { loadCategoryTopics(it) }
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isCreating = false,
                        actionError = failure.message
                    )
                }
        }
    }

    private fun updateTopic(
        topicId: Int,
        name: String?,
        description: String?,
        categoryId: Int?
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUpdating = true, actionError = null)
            
            val token = getToken() ?: return@launch

            val topicData = UpdateTopicData(
                name = name,
                description = description,
                categoryId = categoryId
            )

            repository.updateTopic(token, topicId, topicData)
                .onRight {
                    _state.value = _state.value.copy(
                        isUpdating = false,
                        actionSuccess = "Topic updated successfully"
                    )
                    currentCategoryId?.let { loadCategoryTopics(it) }
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isUpdating = false,
                        actionError = failure.message
                    )
                }
        }
    }

    private fun deleteTopic(topicId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDeleting = true, actionError = null)
            
            val token = getToken() ?: return@launch

            repository.deleteTopic(token, topicId)
                .onRight {
                    _state.value = _state.value.copy(
                        isDeleting = false,
                        actionSuccess = "Topic deleted successfully"
                    )
                    loadAllTopics() // Reload standalone topics
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isDeleting = false,
                        actionError = failure.message
                    )
                }
        }
    }

    private fun removeFromCategory(topicId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDeleting = true, actionError = null)
            
            val token = getToken() ?: return@launch

            // Get current topic to preserve name and description
            val currentTopic = repository.getTopicById(token, topicId).orNull()
            if (currentTopic == null) {
                _state.value = _state.value.copy(
                    isDeleting = false,
                    actionError = "Topic not found"
                )
                return@launch
            }

            // Update topic to set categoryId = null (remove from category)
            // Keep name and description unchanged
            val updateData = UpdateTopicData(
                name = currentTopic.name,
                description = currentTopic.description,
                categoryId = null
            )
            repository.updateTopic(token, topicId, updateData)
                .onRight {
                    _state.value = _state.value.copy(
                        isDeleting = false,
                        actionSuccess = "Topic removed from category"
                    )
                    currentCategoryId?.let { loadCategoryTopics(it) }
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isDeleting = false,
                        actionError = failure.message
                    )
                }
        }
    }

    private fun loadAllTopics(page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            currentCategoryId = null // Clear category context
            
            val token = getToken() ?: run {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Authentication token not found"
                )
                return@launch
            }

            val filterOptions = TopicFilterOptions(
                search = _state.value.searchQuery.takeIf { it.isNotBlank() },
                sort = _state.value.selectedSort?.apiValue,
                page = page,
                limit = 20
            )

            repository.getAllTopics(token, filterOptions)
                .onRight { metadata ->
                    _state.value = _state.value.copy(
                        topics = metadata.topics,
                        currentPage = metadata.currentPage,
                        totalPages = metadata.totalPages,
                        totalTopics = metadata.total,
                        categoryWithTopics = null, // Clear category info
                        isLoading = false,
                        error = null
                    )
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = failure.message
                    )
                }
        }
    }

    private fun refreshAllTopics() {
        loadAllTopics(_state.value.currentPage)
    }

    private fun loadUnclassifiedTopics(page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingUnclassified = true)
            
            val token = getToken() ?: run {
                _state.value = _state.value.copy(
                    isLoadingUnclassified = false,
                    actionError = "Authentication token not found"
                )
                return@launch
            }

            // Get all topics with hasCategory = false filter
            val filterOptions = TopicFilterOptions(
                search = null,
                sort = null,
                page = page,
                limit = 20, // Standard pagination
                hasCategory = false // Only get unclassified topics
            )

            repository.getAllTopics(token, filterOptions)
                .onRight { metadata ->
                    _state.value = _state.value.copy(
                        unclassifiedTopics = metadata.topics,
                        unclassifiedCurrentPage = metadata.currentPage,
                        unclassifiedTotalPages = metadata.totalPages,
                        isLoadingUnclassified = false
                    )
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isLoadingUnclassified = false,
                        actionError = failure.message
                    )
                }
        }
    }

    private fun addExistingTopicToCategory(topicId: Int, categoryId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUpdating = true, actionError = null)
            
            val token = getToken() ?: return@launch

            // Update topic to add categoryId
            val updateData = UpdateTopicData(categoryId = categoryId)
            repository.updateTopic(token, topicId, updateData)
                .onRight {
                    _state.value = _state.value.copy(
                        isUpdating = false,
                        actionSuccess = "Topic added to category successfully"
                    )
                    currentCategoryId?.let { loadCategoryTopics(it) }
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isUpdating = false,
                        actionError = failure.message
                    )
                }
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun clearActionMessages() {
        _state.value = _state.value.copy(
            actionSuccess = null,
            actionError = null
        )
    }

    // Form state management
    fun updateFormState(formState: TopicFormState) {
        _formState.value = formState.validate()
    }

    fun resetFormState() {
        _formState.value = TopicFormState()
    }

    fun loadTopicIntoForm(topicId: Int) {
        viewModelScope.launch {
            val token = getToken() ?: return@launch

            repository.getTopicById(token, topicId)
                .onRight { topic ->
                    _formState.value = TopicFormState(
                        name = topic.name,
                        description = topic.description,
                        categoryId = topic.category?.id
                    ).validate()
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(error = failure.message)
                }
        }
    }

    fun setFormCategoryId(categoryId: Int?) {
        _formState.value = _formState.value.copy(categoryId = categoryId).validate()
    }

    private fun getToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }
}


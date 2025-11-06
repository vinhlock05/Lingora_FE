package com.example.lingora_fe.admin.topic.presentation

import com.example.lingora_fe.admin.topic.domain.model.CategoryWithTopics
import com.example.lingora_fe.admin.topic.domain.model.Topic
import com.example.lingora_fe.admin.topic.domain.model.TopicSortOption

// Main UI State for Category's Topics
data class TopicManagementState(
    val categoryWithTopics: CategoryWithTopics? = null,
    val topics: List<Topic> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalTopics: Int = 0,
    
    // Filters
    val searchQuery: String = "",
    val selectedSort: TopicSortOption? = null,
    
    // Selected topic for details/edit
    val selectedTopic: Topic? = null,
    val isTopicDetailsLoading: Boolean = false,
    
    // Action states
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,
    
    val actionSuccess: String? = null,
    val actionError: String? = null,
    
    // Unclassified topics for adding to category
    val unclassifiedTopics: List<Topic> = emptyList(),
    val isLoadingUnclassified: Boolean = false,
    val unclassifiedCurrentPage: Int = 1,
    val unclassifiedTotalPages: Int = 1
)

// Form State for Create/Edit Topic
data class TopicFormState(
    val name: String = "",
    val description: String = "",
    val categoryId: Int? = null,
    
    // Validation errors
    val nameError: String? = null,
    val descriptionError: String? = null,
    val categoryIdError: String? = null,
    
    val isValid: Boolean = false
) {
    fun validate(): TopicFormState {
        val nameErr = when {
            name.isBlank() -> "Name is required"
            name.length < 3 -> "Name must be at least 3 characters"
            else -> null
        }
        
        val descriptionErr = when {
            description.isBlank() -> "Description is required"
            description.length < 10 -> "Description must be at least 10 characters"
            else -> null
        }
        
        val categoryIdErr = when {
            categoryId == null || categoryId == 0 -> "Category is required"
            else -> null
        }
        
        return copy(
            nameError = nameErr,
            descriptionError = descriptionErr,
            categoryIdError = categoryIdErr,
            isValid = nameErr == null && descriptionErr == null && categoryIdErr == null
        )
    }
}

// UI Events
sealed class TopicManagementEvent {
    // List events
    data class LoadCategoryTopics(val categoryId: Int, val page: Int = 1) : TopicManagementEvent()
    data class SearchTopics(val query: String) : TopicManagementEvent()
    data class SortBy(val sortOption: TopicSortOption?) : TopicManagementEvent()
    object ClearFilters : TopicManagementEvent()
    data class RefreshTopics(val categoryId: Int) : TopicManagementEvent()
    
    // Topic details
    data class LoadTopicDetails(val topicId: Int) : TopicManagementEvent()
    object ClearSelectedTopic : TopicManagementEvent()
    
    // CRUD operations
    data class CreateTopic(
        val name: String,
        val description: String,
        val categoryId: Int?
    ) : TopicManagementEvent()
    
    data class UpdateTopic(
        val topicId: Int,
        val name: String?,
        val description: String?,
        val categoryId: Int?
    ) : TopicManagementEvent()
    
    data class DeleteTopic(val topicId: Int) : TopicManagementEvent()
    
    // Nested view actions (remove from category instead of delete)
    data class RemoveFromCategory(val topicId: Int) : TopicManagementEvent()
    
    // Standalone view actions
    data class LoadAllTopics(val page: Int = 1) : TopicManagementEvent()
    object RefreshAllTopics : TopicManagementEvent()
    
    // Unclassified topics actions
    data class LoadUnclassifiedTopics(val page: Int = 1) : TopicManagementEvent()
    data class AddExistingTopic(val topicId: Int, val categoryId: Int) : TopicManagementEvent()
    
    // UI actions
    object ClearError : TopicManagementEvent()
    object ClearActionMessages : TopicManagementEvent()
}


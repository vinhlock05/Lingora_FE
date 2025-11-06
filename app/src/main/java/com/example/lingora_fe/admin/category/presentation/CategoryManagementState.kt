package com.example.lingora_fe.admin.category.presentation

import com.example.lingora_fe.admin.category.domain.model.Category
import com.example.lingora_fe.admin.category.domain.model.CategorySortOption

// Main UI State
data class CategoryManagementState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalCategories: Int = 0,
    
    // Filters
    val searchQuery: String = "",
    val selectedSort: CategorySortOption? = null,
    
    // Selected category for details/edit
    val selectedCategory: Category? = null,
    val isCategoryDetailsLoading: Boolean = false,
    
    // Action states
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,
    
    val actionSuccess: String? = null,
    val actionError: String? = null
)

// Form State for Create/Edit
data class CategoryFormState(
    val name: String = "",
    val description: String = "",
    
    // Validation errors
    val nameError: String? = null,
    val descriptionError: String? = null,
    
    val isValid: Boolean = false
) {
    fun validate(): CategoryFormState {
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
        
        return copy(
            nameError = nameErr,
            descriptionError = descriptionErr,
            isValid = nameErr == null && descriptionErr == null
        )
    }
}

// UI Events
sealed class CategoryManagementEvent {
    // List events
    data class LoadCategories(val page: Int = 1) : CategoryManagementEvent()
    data class SearchCategories(val query: String) : CategoryManagementEvent()
    data class SortBy(val sortOption: CategorySortOption?) : CategoryManagementEvent()
    object ClearFilters : CategoryManagementEvent()
    object RefreshCategories : CategoryManagementEvent()
    
    // Category details
    data class LoadCategoryDetails(val categoryId: Int) : CategoryManagementEvent()
    object ClearSelectedCategory : CategoryManagementEvent()
    
    // CRUD operations
    data class CreateCategory(
        val name: String,
        val description: String
    ) : CategoryManagementEvent()
    
    data class UpdateCategory(
        val categoryId: Int,
        val name: String?,
        val description: String?
    ) : CategoryManagementEvent()
    
    data class DeleteCategory(val categoryId: Int) : CategoryManagementEvent()
    
    // UI actions
    object ClearError : CategoryManagementEvent()
    object ClearActionMessages : CategoryManagementEvent()
}


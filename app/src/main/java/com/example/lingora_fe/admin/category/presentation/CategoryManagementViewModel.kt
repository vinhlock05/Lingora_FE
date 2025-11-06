package com.example.lingora_fe.admin.category.presentation

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.admin.category.domain.model.CategoryFilterOptions
import com.example.lingora_fe.admin.category.domain.model.CategorySortOption
import com.example.lingora_fe.admin.category.domain.model.CreateCategoryData
import com.example.lingora_fe.admin.category.domain.model.UpdateCategoryData
import com.example.lingora_fe.admin.category.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val repository: CategoryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryManagementState())
    val state: StateFlow<CategoryManagementState> = _state.asStateFlow()

    private val _formState = MutableStateFlow(CategoryFormState())
    val formState: StateFlow<CategoryFormState> = _formState.asStateFlow()

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("lingora_prefs", Context.MODE_PRIVATE)

    init {
        loadCategories()
    }

    fun onEvent(event: CategoryManagementEvent) {
        when (event) {
            is CategoryManagementEvent.LoadCategories -> loadCategories(event.page)
            is CategoryManagementEvent.SearchCategories -> searchCategories(event.query)
            is CategoryManagementEvent.SortBy -> sortBy(event.sortOption)
            is CategoryManagementEvent.ClearFilters -> clearFilters()
            is CategoryManagementEvent.RefreshCategories -> refreshCategories()
            
            is CategoryManagementEvent.LoadCategoryDetails -> loadCategoryDetails(event.categoryId)
            is CategoryManagementEvent.ClearSelectedCategory -> clearSelectedCategory()
            
            is CategoryManagementEvent.CreateCategory -> createCategory(
                name = event.name,
                description = event.description
            )
            
            is CategoryManagementEvent.UpdateCategory -> updateCategory(
                categoryId = event.categoryId,
                name = event.name,
                description = event.description
            )
            
            is CategoryManagementEvent.DeleteCategory -> deleteCategory(event.categoryId)
            
            is CategoryManagementEvent.ClearError -> clearError()
            is CategoryManagementEvent.ClearActionMessages -> clearActionMessages()
        }
    }

    private fun loadCategories(page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val token = getToken() ?: run {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Authentication token not found"
                )
                return@launch
            }

            val filterOptions = CategoryFilterOptions(
                search = _state.value.searchQuery.takeIf { it.isNotBlank() },
                sort = _state.value.selectedSort?.apiValue,
                page = page,
                limit = 20
            )

            repository.getAllCategories(token, filterOptions)
                .onRight { metadata ->
                    _state.value = _state.value.copy(
                        categories = metadata.categories,
                        currentPage = metadata.currentPage,
                        totalPages = metadata.totalPages,
                        totalCategories = metadata.total,
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

    private fun searchCategories(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        loadCategories(page = 1)
    }

    private fun sortBy(sortOption: CategorySortOption?) {
        _state.value = _state.value.copy(selectedSort = sortOption)
        loadCategories(page = 1)
    }

    private fun clearFilters() {
        _state.value = _state.value.copy(
            searchQuery = "",
            selectedSort = null
        )
        loadCategories(page = 1)
    }

    private fun refreshCategories() {
        loadCategories(_state.value.currentPage)
    }

    private fun loadCategoryDetails(categoryId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCategoryDetailsLoading = true)
            
            val token = getToken() ?: return@launch

            repository.getCategoryById(token, categoryId)
                .onRight { category ->
                    _state.value = _state.value.copy(
                        selectedCategory = category,
                        isCategoryDetailsLoading = false
                    )
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isCategoryDetailsLoading = false,
                        error = failure.message
                    )
                }
        }
    }

    private fun clearSelectedCategory() {
        _state.value = _state.value.copy(selectedCategory = null)
    }

    private fun createCategory(
        name: String,
        description: String
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCreating = true, actionError = null)
            
            val token = getToken() ?: return@launch

            val categoryData = CreateCategoryData(
                name = name,
                description = description
            )

            repository.createCategory(token, categoryData)
                .onRight {
                    _state.value = _state.value.copy(
                        isCreating = false,
                        actionSuccess = "Category created successfully"
                    )
                    loadCategories()
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isCreating = false,
                        actionError = failure.message
                    )
                }
        }
    }

    private fun updateCategory(
        categoryId: Int,
        name: String?,
        description: String?
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUpdating = true, actionError = null)
            
            val token = getToken() ?: return@launch

            val categoryData = UpdateCategoryData(
                name = name,
                description = description
            )

            repository.updateCategory(token, categoryId, categoryData)
                .onRight {
                    _state.value = _state.value.copy(
                        isUpdating = false,
                        actionSuccess = "Category updated successfully"
                    )
                    loadCategories()
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isUpdating = false,
                        actionError = failure.message
                    )
                }
        }
    }

    private fun deleteCategory(categoryId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDeleting = true, actionError = null)
            
            val token = getToken() ?: return@launch

            repository.deleteCategory(token, categoryId)
                .onRight {
                    _state.value = _state.value.copy(
                        isDeleting = false,
                        actionSuccess = "Category deleted successfully"
                    )
                    loadCategories()
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isDeleting = false,
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
    fun updateFormState(formState: CategoryFormState) {
        _formState.value = formState.validate()
    }

    fun resetFormState() {
        _formState.value = CategoryFormState()
    }

    fun loadCategoryIntoForm(categoryId: Int) {
        viewModelScope.launch {
            val token = getToken() ?: return@launch

            repository.getCategoryById(token, categoryId)
                .onRight { category ->
                    _formState.value = CategoryFormState(
                        name = category.name,
                        description = category.description
                    ).validate()
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(error = failure.message)
                }
        }
    }

    private fun getToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }
}


package com.example.lingora_fe.user.studyset.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.core.network.TokenManager
import com.example.lingora_fe.user.forum.domain.repository.ForumRepository
import com.example.lingora_fe.user.studyset.domain.model.StudySet
import com.example.lingora_fe.user.studyset.domain.model.StudySetFilterOptions
import com.example.lingora_fe.user.studyset.domain.model.StudySetStatus
import com.example.lingora_fe.user.studyset.domain.model.StudySetVisibility
import com.example.lingora_fe.user.studyset.domain.repository.StudySetRepository
import com.example.lingora_fe.user.studyset.presentation.StudySetListUiState
import com.example.lingora_fe.user.studyset.presentation.StudySetTab
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudySetListViewModel @Inject constructor(
    private val repository: StudySetRepository,
    private val forumRepository: ForumRepository,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudySetListUiState())
    val uiState: StateFlow<StudySetListUiState> = _uiState.asStateFlow()

    val currentUserId: Int
        get() = tokenManager.getUserId() ?: -1

    init {
        loadStudySets()
    }

    fun loadStudySets(page: Int = 1) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val filterOptions = StudySetFilterOptions(
                search = _uiState.value.searchQuery.ifEmpty { null },
                visibility = _uiState.value.selectedVisibility,
                status = if (_uiState.value.selectedTab == StudySetTab.STORE) StudySetStatus.PUBLISHED else null,
                page = page,
                limit = 10
            )

            val result = if (_uiState.value.selectedTab == StudySetTab.STORE) {
                repository.getAllStudySets(token, filterOptions)
            } else {
                repository.getOwnStudySets(token, filterOptions)
            }

            result.fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                },
                ifRight = { metadata ->
                    _uiState.value = _uiState.value.copy(
                        studySets = if (page == 1) metadata.studySets else _uiState.value.studySets + metadata.studySets,
                        isLoading = false,
                        error = null,
                        currentPage = metadata.currentPage,
                        totalPages = metadata.totalPages,
                        total = metadata.total
                    )
                }
            )
        }
    }

    fun searchStudySets(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadStudySets(page = 1)
    }

    fun switchTab(tab: StudySetTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab, searchQuery = "")
        loadStudySets(page = 1)
    }

    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState.currentPage < currentState.totalPages && !currentState.isLoading) {
            loadStudySets(page = currentState.currentPage + 1)
        }
    }

    fun refresh() {
        loadStudySets(page = 1)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun checkAccessAndNavigate(studySetId: Int, onSuccess: (Int) -> Unit) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            
            _uiState.value = _uiState.value.copy(isCheckingAccess = true, purchaseError = null)
            
            repository.getStudySetById(token, studySetId).fold(
                ifLeft = { error ->
                    // If error (likely 403 Forbidden), show purchase modal
                    val studySet = _uiState.value.studySets.find { it.id == studySetId }
                    if (studySet != null) {
                        _uiState.value = _uiState.value.copy(
                            isCheckingAccess = false,
                            showPurchaseModal = true,
                            purchaseStudySet = studySet,
                            purchaseError = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isCheckingAccess = false,
                            error = error.message
                        )
                    }
                },
                ifRight = { studySet ->
                    // Success - can access, navigate to detail
                    _uiState.value = _uiState.value.copy(isCheckingAccess = false)
                    onSuccess(studySetId)
                }
            )
        }
    }

    fun showPurchaseModal(studySet: StudySet) {
        _uiState.value = _uiState.value.copy(
            showPurchaseModal = true,
            purchaseStudySet = studySet,
            purchaseError = null
        )
    }

    fun hidePurchaseModal() {
        _uiState.value = _uiState.value.copy(
            showPurchaseModal = false,
            purchaseStudySet = null,
            purchaseError = null
        )
    }

    fun buyStudySet(studySetId: Int, onPaymentUrlReceived: (String) -> Unit) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            
            _uiState.value = _uiState.value.copy(isPurchasing = true, purchaseError = null)
            
            repository.buyStudySet(token, studySetId).fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(
                        isPurchasing = false,
                        purchaseError = error.message
                    )
                },
                ifRight = { response ->
                    if (response.isFree) {
                        // Free study set - close modal and refresh list
                        _uiState.value = _uiState.value.copy(
                            isPurchasing = false,
                            showPurchaseModal = false,
                            purchaseStudySet = null
                        )
                        refresh()
                    } else {
                        // Paid study set - open payment URL
                        response.paymentUrl?.let { paymentUrl ->
                            _uiState.value = _uiState.value.copy(
                                isPurchasing = false,
                                showPurchaseModal = false,
                                purchaseStudySet = null
                            )
                            onPaymentUrlReceived(paymentUrl)
                        } ?: run {
                            _uiState.value = _uiState.value.copy(
                                isPurchasing = false,
                                purchaseError = "Không thể tạo link thanh toán"
                            )
                        }
                    }
                }
            )
        }
    }

    fun toggleLike(studySetId: Int) {
        viewModelScope.launch {
            val currentStudySets = _uiState.value.studySets
            val targetStudySet = currentStudySets.find { it.id == studySetId } ?: return@launch
            
            val isCurrentlyLiked = targetStudySet.isAlreadyLike
            val delta = if (isCurrentlyLiked) -1 else 1
            
            // Optimistic update
            val updatedStudySets = currentStudySets.map { studySet ->
                if (studySet.id == studySetId) {
                    studySet.copy(
                        isAlreadyLike = !isCurrentlyLiked,
                        likeCount = (studySet.likeCount + delta).coerceAtLeast(0)
                    )
                } else studySet
            }
            
            _uiState.value = _uiState.value.copy(studySets = updatedStudySets)
            
            val action = if (isCurrentlyLiked) {
                forumRepository.unlike(studySetId, "STUDY_SET")
            } else {
                forumRepository.like(studySetId, "STUDY_SET")
            }
            
            action.fold(
                ifLeft = { error ->
                    // Revert optimistic update on error
                    _uiState.value = _uiState.value.copy(
                        studySets = currentStudySets,
                        error = error.message ?: "Failed to toggle like"
                    )
                },
                ifRight = {
                    // Refresh to get accurate state
                    loadStudySets(_uiState.value.currentPage)
                }
            )
        }
    }

    fun deleteStudySet(studySetId: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            _uiState.value = _uiState.value.copy(deletingStudySetId = studySetId, error = null)

            repository.deleteStudySet(token, studySetId).fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(
                        deletingStudySetId = null,
                        error = error.message
                    )
                },
                ifRight = {
                    _uiState.value = _uiState.value.copy(
                        studySets = _uiState.value.studySets.filterNot { it.id == studySetId },
                        deletingStudySetId = null
                    )
                    onSuccess()
                }
            )
        }
    }
    
    fun verifyPayment(vnpParams: Map<String, String>, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: run {
                onResult(false, "Không tìm thấy token xác thực")
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.verifyVNPayPayment(token, vnpParams).fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onResult(false, "Xác thực thất bại: ${error.message}")
                },
                ifRight = { response ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    if (response.success) {
                        onResult(true, response.message ?: "Thanh toán thành công!")
                    } else {
                        onResult(false, response.message ?: "Thanh toán thất bại")
                    }
                }
            )
        }
    }
}


package com.example.lingora_fe.admin.withdrawal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.admin.withdrawal.domain.model.AdminWithdrawalFilterOptions
import com.example.lingora_fe.admin.withdrawal.domain.repository.AdminWithdrawalRepository
import com.example.lingora_fe.user.withdrawal.domain.model.WithdrawalStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Admin Withdrawal Management screens
 */
@HiltViewModel
class WithdrawalManagementViewModel @Inject constructor(
    private val repository: AdminWithdrawalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WithdrawalManagementState())
    val state: StateFlow<WithdrawalManagementState> = _state.asStateFlow()

    init {
        loadWithdrawals()
    }

    /**
     * Load withdrawal list with pagination
     */
    fun loadWithdrawals(page: Int = 1) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val filterOptions = AdminWithdrawalFilterOptions(
                page = page,
                limit = 10,
                status = _state.value.selectedStatus,
                userId = _state.value.selectedUserId,
                sort = "-createdAt"
            )

            repository.getAllWithdrawals(filterOptions).fold(
                ifLeft = { failure ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = failure.message
                    )}
                },
                ifRight = { metadata ->
                    _state.update { it.copy(
                        isLoading = false,
                        withdrawals = metadata.withdrawals,
                        currentPage = metadata.currentPage,
                        totalPages = metadata.totalPages,
                        totalWithdrawals = metadata.total
                    )}
                }
            )
        }
    }

    /**
     * Filter by status
     */
    fun filterByStatus(status: WithdrawalStatus?) {
        _state.update { it.copy(selectedStatus = status) }
        loadWithdrawals(page = 1)
    }

    /**
     * Filter by user ID
     */
    fun filterByUserId(userId: Int?) {
        _state.update { it.copy(selectedUserId = userId) }
        loadWithdrawals(page = 1)
    }

    /**
     * Clear filters
     */
    fun clearFilters() {
        _state.update { it.copy(
            selectedStatus = null,
            selectedUserId = null
        )}
        loadWithdrawals(page = 1)
    }

    /**
     * Load withdrawal detail
     */
    fun loadWithdrawalDetail(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingDetail = true, error = null) }

            repository.getWithdrawalById(id).fold(
                ifLeft = { failure ->
                    _state.update { it.copy(
                        isLoadingDetail = false,
                        error = failure.message
                    )}
                },
                ifRight = { withdrawal ->
                    _state.update { it.copy(
                        isLoadingDetail = false,
                        selectedWithdrawal = withdrawal
                    )}
                }
            )
        }
    }

    /**
     * Clear selected withdrawal
     */
    fun clearSelectedWithdrawal() {
        _state.update { it.copy(selectedWithdrawal = null) }
    }

    /**
     * Approve withdrawal
     */
    fun approveWithdrawal(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isApproving = true, actionError = null) }

            repository.approveWithdrawal(id).fold(
                ifLeft = { failure ->
                    _state.update { it.copy(
                        isApproving = false,
                        actionError = failure.message
                    )}
                },
                ifRight = { withdrawal ->
                    _state.update { it.copy(
                        isApproving = false,
                        successMessage = "Đã duyệt yêu cầu rút tiền #${withdrawal.id}",
                        selectedWithdrawal = withdrawal
                    )}
                    loadWithdrawals(_state.value.currentPage)
                }
            )
        }
    }

    // Dialog management
    fun showRejectDialog(id: Int) {
        _state.update { it.copy(
            showRejectDialog = true,
            dialogWithdrawalId = id,
            dialogReason = ""
        )}
    }

    fun hideRejectDialog() {
        _state.update { it.copy(
            showRejectDialog = false,
            dialogWithdrawalId = null,
            dialogReason = ""
        )}
    }

    fun showCompleteDialog(id: Int) {
        _state.update { it.copy(
            showCompleteDialog = true,
            dialogWithdrawalId = id,
            dialogTransactionReference = ""
        )}
    }

    fun hideCompleteDialog() {
        _state.update { it.copy(
            showCompleteDialog = false,
            dialogWithdrawalId = null,
            dialogTransactionReference = ""
        )}
    }

    fun showFailDialog(id: Int) {
        _state.update { it.copy(
            showFailDialog = true,
            dialogWithdrawalId = id,
            dialogReason = ""
        )}
    }

    fun hideFailDialog() {
        _state.update { it.copy(
            showFailDialog = false,
            dialogWithdrawalId = null,
            dialogReason = ""
        )}
    }

    fun updateDialogReason(reason: String) {
        _state.update { it.copy(dialogReason = reason) }
    }

    fun updateDialogTransactionReference(ref: String) {
        _state.update { it.copy(dialogTransactionReference = ref) }
    }

    /**
     * Reject withdrawal
     */
    fun rejectWithdrawal() {
        val id = _state.value.dialogWithdrawalId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isRejecting = true, actionError = null) }

            repository.rejectWithdrawal(id, _state.value.dialogReason.ifBlank { null }).fold(
                ifLeft = { failure ->
                    _state.update { it.copy(
                        isRejecting = false,
                        actionError = failure.message
                    )}
                },
                ifRight = { withdrawal ->
                    _state.update { it.copy(
                        isRejecting = false,
                        successMessage = "Đã từ chối yêu cầu rút tiền #${withdrawal.id}",
                        selectedWithdrawal = withdrawal,
                        showRejectDialog = false,
                        dialogWithdrawalId = null,
                        dialogReason = ""
                    )}
                    loadWithdrawals(_state.value.currentPage)
                }
            )
        }
    }

    /**
     * Complete withdrawal
     */
    fun completeWithdrawal() {
        val id = _state.value.dialogWithdrawalId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isCompleting = true, actionError = null) }

            repository.completeWithdrawal(id, _state.value.dialogTransactionReference.ifBlank { null }).fold(
                ifLeft = { failure ->
                    _state.update { it.copy(
                        isCompleting = false,
                        actionError = failure.message
                    )}
                },
                ifRight = { withdrawal ->
                    _state.update { it.copy(
                        isCompleting = false,
                        successMessage = "Đã hoàn thành chuyển khoản #${withdrawal.id}",
                        selectedWithdrawal = withdrawal,
                        showCompleteDialog = false,
                        dialogWithdrawalId = null,
                        dialogTransactionReference = ""
                    )}
                    loadWithdrawals(_state.value.currentPage)
                }
            )
        }
    }

    /**
     * Fail withdrawal
     */
    fun failWithdrawal() {
        val id = _state.value.dialogWithdrawalId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isFailing = true, actionError = null) }

            repository.failWithdrawal(id, _state.value.dialogReason.ifBlank { null }).fold(
                ifLeft = { failure ->
                    _state.update { it.copy(
                        isFailing = false,
                        actionError = failure.message
                    )}
                },
                ifRight = { withdrawal ->
                    _state.update { it.copy(
                        isFailing = false,
                        successMessage = "Đã đánh dấu thất bại #${withdrawal.id}",
                        selectedWithdrawal = withdrawal,
                        showFailDialog = false,
                        dialogWithdrawalId = null,
                        dialogReason = ""
                    )}
                    loadWithdrawals(_state.value.currentPage)
                }
            )
        }
    }

    // Message clearing functions
    fun clearError() {
        _state.update { it.copy(error = null, actionError = null) }
    }

    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }

    /**
     * Refresh all data
     */
    fun refresh() {
        loadWithdrawals(_state.value.currentPage)
    }
}

package com.example.lingora_fe.user.withdrawal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.user.withdrawal.domain.model.CreateWithdrawalData
import com.example.lingora_fe.user.withdrawal.domain.model.WithdrawalFilterOptions
import com.example.lingora_fe.user.withdrawal.domain.model.WithdrawalStatus
import com.example.lingora_fe.user.withdrawal.domain.repository.WithdrawalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for User Withdrawal screens
 */
@HiltViewModel
class WithdrawalViewModel @Inject constructor(
    private val repository: WithdrawalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WithdrawalState())
    val state: StateFlow<WithdrawalState> = _state.asStateFlow()

    init {
        loadBalance()
        loadWithdrawals()
    }

    /**
     * Load user's balance information
     */
    fun loadBalance() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingBalance = true, error = null) }
            
            repository.getBalance().fold(
                ifLeft = { failure ->
                    _state.update { it.copy(
                        isLoadingBalance = false,
                        error = failure.message
                    )}
                },
                ifRight = { balance ->
                    _state.update { it.copy(
                        isLoadingBalance = false,
                        balance = balance
                    )}
                }
            )
        }
    }

    /**
     * Load withdrawal history with pagination
     */
    fun loadWithdrawals(page: Int = 1) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingWithdrawals = true, error = null) }
            
            val filterOptions = WithdrawalFilterOptions(
                page = page,
                limit = 10,
                status = _state.value.selectedStatus,
                sort = "-createdAt"
            )
            
            repository.getMyWithdrawals(filterOptions).fold(
                ifLeft = { failure ->
                    _state.update { it.copy(
                        isLoadingWithdrawals = false,
                        error = failure.message
                    )}
                },
                ifRight = { metadata ->
                    _state.update { it.copy(
                        isLoadingWithdrawals = false,
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
     * Filter withdrawals by status
     */
    fun filterByStatus(status: WithdrawalStatus?) {
        _state.update { it.copy(selectedStatus = status) }
        loadWithdrawals(page = 1)
    }

    /**
     * Create a new withdrawal request
     */
    fun createWithdrawal() {
        // Validate form first
        val validatedState = _state.value.validateForm()
        _state.value = validatedState
        
        if (!validatedState.isFormValid) {
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isCreating = true, error = null) }
            
            val data = CreateWithdrawalData(
                amount = _state.value.amount.toLong(),
                bankName = _state.value.bankName,
                bankAccountNumber = _state.value.bankAccountNumber,
                bankAccountName = _state.value.bankAccountName.uppercase(),
                bankBranch = _state.value.bankBranch.ifBlank { null }
            )
            
            repository.createWithdrawal(data).fold(
                ifLeft = { failure ->
                    _state.update { it.copy(
                        isCreating = false,
                        error = failure.message
                    )}
                },
                ifRight = { withdrawal ->
                    _state.update { it.copy(
                        isCreating = false,
                        successMessage = "Tạo yêu cầu rút tiền thành công!",
                        // Reset form
                        amount = "",
                        bankName = "",
                        bankAccountNumber = "",
                        bankAccountName = "",
                        bankBranch = "",
                        amountError = null,
                        bankNameError = null,
                        bankAccountNumberError = null,
                        bankAccountNameError = null
                    )}
                    // Refresh balance and list
                    loadBalance()
                    loadWithdrawals()
                }
            )
        }
    }

    /**
     * Load a specific withdrawal detail
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

    // Form field update functions
    fun updateAmount(value: String) {
        _state.update { it.copy(amount = value, amountError = null) }
    }

    fun updateBankName(value: String) {
        _state.update { it.copy(bankName = value, bankNameError = null) }
    }

    fun updateBankAccountNumber(value: String) {
        _state.update { it.copy(bankAccountNumber = value, bankAccountNumberError = null) }
    }

    fun updateBankAccountName(value: String) {
        _state.update { it.copy(bankAccountName = value, bankAccountNameError = null) }
    }

    fun updateBankBranch(value: String) {
        _state.update { it.copy(bankBranch = value) }
    }

    // Message clearing functions
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }

    /**
     * Refresh all data
     */
    fun refresh() {
        loadBalance()
        loadWithdrawals()
    }
}

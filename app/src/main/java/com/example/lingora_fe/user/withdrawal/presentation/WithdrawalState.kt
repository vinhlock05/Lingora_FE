package com.example.lingora_fe.user.withdrawal.presentation

import com.example.lingora_fe.user.withdrawal.domain.model.Balance
import com.example.lingora_fe.user.withdrawal.domain.model.Withdrawal
import com.example.lingora_fe.user.withdrawal.domain.model.WithdrawalStatus

/**
 * UI State for User Withdrawal screens
 */
data class WithdrawalState(
    // Balance information
    val balance: Balance? = null,
    val isLoadingBalance: Boolean = false,

    // Withdrawal list
    val withdrawals: List<Withdrawal> = emptyList(),
    val isLoadingWithdrawals: Boolean = false,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalWithdrawals: Int = 0,

    // Filter
    val selectedStatus: WithdrawalStatus? = null,

    // Create withdrawal form fields
    val amount: String = "",
    val bankName: String = "",
    val bankAccountNumber: String = "",
    val bankAccountName: String = "",
    val bankBranch: String = "",
    val isCreating: Boolean = false,

    // Form validation errors
    val amountError: String? = null,
    val bankNameError: String? = null,
    val bankAccountNumberError: String? = null,
    val bankAccountNameError: String? = null,

    // Selected withdrawal detail
    val selectedWithdrawal: Withdrawal? = null,
    val isLoadingDetail: Boolean = false,

    // Messages
    val error: String? = null,
    val successMessage: String? = null
) {
    /**
     * Validate form fields for creating withdrawal
     */
    fun validateForm(): WithdrawalState {
        val amountLong = amount.toLongOrNull()
        
        val amountErr = when {
            amount.isBlank() -> "Vui lòng nhập số tiền"
            amountLong == null -> "Số tiền không hợp lệ"
            amountLong < 50000 -> "Số tiền tối thiểu là 50,000 VND"
            amountLong > 50000000 -> "Số tiền tối đa là 50,000,000 VND"
            balance != null && amountLong > balance.availableBalance -> "Số dư không đủ"
            else -> null
        }
        
        val bankNameErr = if (bankName.isBlank()) "Vui lòng chọn ngân hàng" else null
        val bankAccountNumberErr = if (bankAccountNumber.isBlank()) "Vui lòng nhập số tài khoản" else null
        val bankAccountNameErr = if (bankAccountName.isBlank()) "Vui lòng nhập tên chủ tài khoản" else null
        
        return copy(
            amountError = amountErr,
            bankNameError = bankNameErr,
            bankAccountNumberError = bankAccountNumberErr,
            bankAccountNameError = bankAccountNameErr
        )
    }
    
    /**
     * Check if form is valid
     */
    val isFormValid: Boolean
        get() = amountError == null && bankNameError == null && 
                bankAccountNumberError == null && bankAccountNameError == null &&
                amount.isNotBlank() && bankName.isNotBlank() &&
                bankAccountNumber.isNotBlank() && bankAccountName.isNotBlank()
}

/**
 * Common Vietnamese banks for dropdown
 */
val vietnameseBanks = listOf(
    "Vietcombank",
    "Techcombank",
    "BIDV",
    "VietinBank",
    "Agribank",
    "MB Bank",
    "ACB",
    "Sacombank",
    "VPBank",
    "TPBank",
    "SHB",
    "HDBank",
    "OCB",
    "SeABank",
    "Eximbank",
    "LienVietPostBank",
    "VIB",
    "MSB",
    "NCB",
    "BacABank",
    "KienLongBank",
    "PVcomBank",
    "Nam A Bank",
    "Viet Capital Bank"
)

package com.example.lingora_fe.admin.withdrawal.presentation

import com.example.lingora_fe.admin.withdrawal.domain.model.AdminWithdrawal
import com.example.lingora_fe.user.withdrawal.domain.model.WithdrawalStatus

/**
 * UI State for Admin Withdrawal Management screens
 */
data class WithdrawalManagementState(
    // Withdrawal list
    val withdrawals: List<AdminWithdrawal> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalWithdrawals: Int = 0,

    // Filters
    val selectedStatus: WithdrawalStatus? = null,
    val selectedUserId: Int? = null,
    val searchQuery: String = "",

    // Selected withdrawal detail
    val selectedWithdrawal: AdminWithdrawal? = null,
    val isLoadingDetail: Boolean = false,

    // Action states
    val isApproving: Boolean = false,
    val isRejecting: Boolean = false,
    val isCompleting: Boolean = false,
    val isFailing: Boolean = false,

    // Action dialog states
    val showRejectDialog: Boolean = false,
    val showCompleteDialog: Boolean = false,
    val showFailDialog: Boolean = false,
    val dialogWithdrawalId: Int? = null,
    val dialogReason: String = "",
    val dialogTransactionReference: String = "",

    // Action messages
    val successMessage: String? = null,
    val actionError: String? = null
)

/**
 * Sort options for admin withdrawal list
 */
enum class WithdrawalSortOption(val displayName: String, val apiValue: String) {
    CREATED_AT_DESC("Mới nhất", "-createdAt"),
    CREATED_AT_ASC("Cũ nhất", "+createdAt"),
    AMOUNT_DESC("Số tiền: Cao → Thấp", "-amount"),
    AMOUNT_ASC("Số tiền: Thấp → Cao", "+amount")
}

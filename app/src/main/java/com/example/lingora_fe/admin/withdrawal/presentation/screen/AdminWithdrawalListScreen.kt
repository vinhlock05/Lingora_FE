package com.example.lingora_fe.admin.withdrawal.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.lingora_fe.admin.withdrawal.domain.model.AdminWithdrawal
import com.example.lingora_fe.admin.withdrawal.presentation.WithdrawalManagementViewModel
import com.example.lingora_fe.admin.common.presentation.components.*
import com.example.lingora_fe.core.ui.theme.*
import com.example.lingora_fe.user.withdrawal.domain.model.WithdrawalStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWithdrawalListScreen(
    navController: NavHostController,
    viewModel: WithdrawalManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showFilterDialog by remember { mutableStateOf(false) }

    // Load data on first launch
    LaunchedEffect(Unit) {
        viewModel.loadWithdrawals()
    }

    // Show error snackbar
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Show success snackbar
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
    // Search Bar and Filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChange = { 
                            viewModel.searchWithdrawals(it) 
                        },
                        placeholder = "Search withdrawal...",
                        modifier = Modifier.weight(1f)
                    )
                    
                    FilterButton(
                        onClick = { showFilterDialog = true },
                        hasActiveFilters = state.selectedStatus != null
                    )
                }

                // Active Filters
                if (state.selectedStatus != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.selectedStatus?.let { status ->
                            val label = when(status) {
                                WithdrawalStatus.PENDING -> "Pending"
                                WithdrawalStatus.PROCESSING -> "Processing"
                                WithdrawalStatus.COMPLETED -> "Completed"
                                WithdrawalStatus.REJECTED -> "Rejected"
                                WithdrawalStatus.FAILED -> "Failed"
                            }
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.filterByStatus(null) },
                                label = { 
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
                                    ) 
                                },
                                trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GradientStart.copy(alpha = 0.2f),
                                    selectedLabelColor = GradientStart
                                )
                            )
                        }
                    }
                }

                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total: ${state.totalWithdrawals} requests",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Page ${state.currentPage}/${state.totalPages}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NavBarText
                    )
                }

                // Content
                when {
                    state.isLoading && state.withdrawals.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    state.error != null && state.withdrawals.isEmpty() -> {
                        ErrorContent(
                            message = state.error ?: "Unknown error",
                            onRetry = { viewModel.refresh() }
                        )
                    }
                    state.withdrawals.isEmpty() -> {
                        EmptyContent(
                            message = "No withdrawal requests found",
                            icon = Icons.Default.AccountBalanceWallet
                        )
                    }
                    else -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.withdrawals, key = { it.id }) { withdrawal ->
                                    WithdrawalCard(
                                        withdrawal = withdrawal,
                                        onClick = { navController.navigate("admin_withdrawal/${withdrawal.id}") },
                                        onApprove = { viewModel.approveWithdrawal(withdrawal.id) },
                                        onReject = { viewModel.showRejectDialog(withdrawal.id) },
                                        onComplete = { viewModel.showCompleteDialog(withdrawal.id) },
                                        isApproving = state.isApproving,
                                        isCompleting = state.isCompleting
                                    )
                                }

                                // Pagination
                                if (state.totalPages > 1) {
                                    item {
                                        PaginationControls(
                                            currentPage = state.currentPage,
                                            totalPages = state.totalPages,
                                            onPageChange = { page -> viewModel.loadWithdrawals(page) }
                                        )
                                    }
                                }
                            }

                            // Loading Overlay
                            if (state.isLoading && state.withdrawals.isNotEmpty()) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.TopCenter)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Filter Dialog
    if (showFilterDialog) {
        WithdrawalFilterDialog(
            selectedStatus = state.selectedStatus,
            onDismiss = { showFilterDialog = false },
            onApply = { status ->
                viewModel.filterByStatus(status)
                showFilterDialog = false
            },
            onClear = {
                viewModel.filterByStatus(null)
                showFilterDialog = false
            }
        )
    }

    // Reject Dialog
    if (state.showRejectDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideRejectDialog() },
            title = { Text("Reject Withdrawal") },
            text = {
                Column {
                    Text(
                        text = "Please enter rejection reason (optional):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NavBarText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.dialogReason,
                        onValueChange = { viewModel.updateDialogReason(it) },
                        placeholder = { Text("Enter reason...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.rejectWithdrawal() },
                    enabled = !state.isRejecting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    if (state.isRejecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Reject")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideRejectDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Complete Dialog
    if (state.showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideCompleteDialog() },
            title = { Text("Complete Transfer") },
            text = {
                Column {
                    Text(
                        text = "Enter transaction reference (optional):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NavBarText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.dialogTransactionReference,
                        onValueChange = { viewModel.updateDialogTransactionReference(it) },
                        placeholder = { Text("Transaction ID...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.completeWithdrawal() },
                    enabled = !state.isCompleting,
                    colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
                ) {
                    if (state.isCompleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Complete")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideCompleteDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun WithdrawalFilterDialog(
    selectedStatus: WithdrawalStatus?,
    onDismiss: () -> Unit,
    onApply: (WithdrawalStatus?) -> Unit,
    onClear: () -> Unit
) {
    var tempStatus by remember { mutableStateOf(selectedStatus) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Withdrawals") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Status", style = MaterialTheme.typography.titleSmall)
                val statuses = listOf(
                    WithdrawalStatus.PENDING to "Pending",
                    WithdrawalStatus.PROCESSING to "Processing",
                    WithdrawalStatus.COMPLETED to "Completed",
                    WithdrawalStatus.REJECTED to "Rejected",
                    WithdrawalStatus.FAILED to "Failed"
                )
                
                statuses.forEach { (status, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempStatus = if (tempStatus == status) null else status }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tempStatus == status,
                            onClick = { tempStatus = if (tempStatus == status) null else status },
                            colors = RadioButtonDefaults.colors(selectedColor = GradientStart)
                        )
                        Text(label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onApply(tempStatus) },
                colors = ButtonDefaults.textButtonColors(contentColor = GradientStart)
            ) {
                Text("Apply", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClear) { Text("Clear All") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

@Composable
private fun WithdrawalCard(
    withdrawal: AdminWithdrawal,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit,
    isApproving: Boolean,
    isCompleting: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Amount and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatCurrency(withdrawal.amount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "#${withdrawal.id} • ${formatDate(withdrawal.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NavBarText
                    )
                }
                WithdrawalStatusBadge(status = withdrawal.status)
            }

            Divider()

            // User Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = NavBarText
                )
                Text(
                    text = "${withdrawal.user.username} (${withdrawal.user.email})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NavBarText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bank Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AccountBalance,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = NavBarText
                )
                Text(
                    text = "${withdrawal.bankName} - ${withdrawal.bankAccountNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NavBarText
                )
            }

            // Action Buttons for PENDING status
            if (withdrawal.status == WithdrawalStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFDC2626)
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Reject")
                    }
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GradientStart
                        ),
                        enabled = !isApproving
                    ) {
                        if (isApproving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Approve")
                        }
                    }
                }
            }

            // Action Buttons for PROCESSING status
            if (withdrawal.status == WithdrawalStatus.PROCESSING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GradientStart
                        ),
                        enabled = !isCompleting
                    ) {
                        if (isCompleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Complete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WithdrawalStatusBadge(status: WithdrawalStatus) {
    val (backgroundColor, textColor, label) = when (status) {
        WithdrawalStatus.PENDING -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Pending")
        WithdrawalStatus.PROCESSING -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "Processing")
        WithdrawalStatus.COMPLETED -> Triple(Color(0xFFE8F5E9), Color(0xFF1B5E20), "Completed")
        WithdrawalStatus.REJECTED -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Rejected")
        WithdrawalStatus.FAILED -> Triple(Color(0xFFFFEBEE), Color(0xFFB71C1C), "Failed")
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatCurrency(amount: Long): String {
    val format = NumberFormat.getInstance(Locale("vi", "VN"))
    return "${format.format(amount)} VND"
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))
    return sdf.format(date)
}

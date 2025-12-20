package com.example.lingora_fe.admin.withdrawal.presentation.screen

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.lingora_fe.admin.withdrawal.presentation.WithdrawalManagementViewModel
import com.example.lingora_fe.core.ui.theme.*
import com.example.lingora_fe.user.withdrawal.domain.model.WithdrawalStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWithdrawalDetailScreen(
    withdrawalId: Int,
    navController: NavHostController,
    viewModel: WithdrawalManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load withdrawal detail
    LaunchedEffect(withdrawalId) {
        viewModel.loadWithdrawalDetail(withdrawalId)
    }

    // Show error snackbar
    LaunchedEffect(state.actionError) {
        state.actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Show success snackbar and navigate back
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoadingDetail) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            state.selectedWithdrawal?.let { withdrawal ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = getStatusBackgroundColor(withdrawal.status)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = getStatusIcon(withdrawal.status),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = getStatusTextColor(withdrawal.status)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = getStatusLabel(withdrawal.status),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = getStatusTextColor(withdrawal.status)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = formatCurrency(withdrawal.amount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )
                        }
                    }

                    // User Information Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "User Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            DetailInfoRow(
                                icon = Icons.Default.Person,
                                label = "Username",
                                value = withdrawal.user.username
                            )
                            DetailInfoRow(
                                icon = Icons.Default.Email,
                                label = "Email",
                                value = withdrawal.user.email
                            )
                            DetailInfoRow(
                                icon = Icons.Default.Tag,
                                label = "User ID",
                                value = "#${withdrawal.user.id}"
                            )
                        }
                    }

                    // Bank Information Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Bank Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            DetailInfoRow(
                                icon = Icons.Default.AccountBalance,
                                label = "Bank",
                                value = withdrawal.bankName
                            )
                            DetailInfoRow(
                                icon = Icons.Default.CreditCard,
                                label = "Account Number",
                                value = withdrawal.bankAccountNumber
                            )
                            DetailInfoRow(
                                icon = Icons.Default.Person,
                                label = "Account Holder",
                                value = withdrawal.bankAccountName
                            )
                            withdrawal.bankBranch?.let { branch ->
                                DetailInfoRow(
                                    icon = Icons.Default.LocationOn,
                                    label = "Branch",
                                    value = branch
                                )
                            }
                        }
                    }

                    // Transaction Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Transaction Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            DetailInfoRow(
                                icon = Icons.Default.Tag,
                                label = "Request ID",
                                value = "#${withdrawal.id}"
                            )
                            DetailInfoRow(
                                icon = Icons.Default.CalendarToday,
                                label = "Created At",
                                value = formatDate(withdrawal.createdAt)
                            )
                            DetailInfoRow(
                                icon = Icons.Default.Update,
                                label = "Last Updated",
                                value = formatDate(withdrawal.updatedAt)
                            )
                            withdrawal.transactionReference?.let { ref ->
                                DetailInfoRow(
                                    icon = Icons.Default.Receipt,
                                    label = "Transaction Reference",
                                    value = ref
                                )
                            }
                            withdrawal.processedBy?.let { adminId ->
                                DetailInfoRow(
                                    icon = Icons.Default.AdminPanelSettings,
                                    label = "Processed By",
                                    value = "Admin #$adminId"
                                )
                            }
                        }
                    }

                    // Rejection Reason Card
                    if (withdrawal.status == WithdrawalStatus.REJECTED ||
                        withdrawal.status == WithdrawalStatus.FAILED
                    ) {
                        withdrawal.rejectionReason?.let { reason ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFEBEE)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFC62828)
                                    )
                                    Column {
                                        Text(
                                            text = if (withdrawal.status == WithdrawalStatus.REJECTED)
                                                "Rejection Reason" else "Failure Reason",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFFC62828)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = reason,
                                            fontSize = 14.sp,
                                            color = Color(0xFF424242)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Action Buttons based on status
                    when (withdrawal.status) {
                        WithdrawalStatus.PENDING -> {
                            ActionButtonsForPending(
                                onApprove = { viewModel.approveWithdrawal(withdrawal.id) },
                                onReject = { viewModel.showRejectDialog(withdrawal.id) },
                                isApproving = state.isApproving
                            )
                        }
                        WithdrawalStatus.PROCESSING -> {
                            ActionButtonsForProcessing(
                                onComplete = { viewModel.showCompleteDialog(withdrawal.id) },
                                onFail = { viewModel.showFailDialog(withdrawal.id) }
                            )
                        }
                        else -> {}
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Reject Dialog
    if (state.showRejectDialog) {
        ActionDialog(
            title = "Reject Request",
            inputLabel = "Rejection Reason",
            inputValue = state.dialogReason,
            onInputChange = { viewModel.updateDialogReason(it) },
            onConfirm = { viewModel.rejectWithdrawal() },
            onDismiss = { viewModel.hideRejectDialog() },
            isLoading = state.isRejecting,
            confirmButtonColor = Color(0xFFDC2626)
        )
    }

    // Complete Dialog
    if (state.showCompleteDialog) {
        ActionDialog(
            title = "Complete Transfer",
            inputLabel = "Transaction Reference",
            inputValue = state.dialogTransactionReference,
            onInputChange = { viewModel.updateDialogTransactionReference(it) },
            onConfirm = { viewModel.completeWithdrawal() },
            onDismiss = { viewModel.hideCompleteDialog() },
            isLoading = state.isCompleting,
            confirmButtonColor = GradientStart
        )
    }

    // Fail Dialog
    if (state.showFailDialog) {
        ActionDialog(
            title = "Mark as Failed",
            inputLabel = "Failure Reason",
            inputValue = state.dialogReason,
            onInputChange = { viewModel.updateDialogReason(it) },
            onConfirm = { viewModel.failWithdrawal() },
            onDismiss = { viewModel.hideFailDialog() },
            isLoading = state.isFailing,
            confirmButtonColor = Color(0xFFDC2626)
        )
    }
}

@Composable
private fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = NavBarText
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = NavBarText
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ActionButtonsForPending(
    onApprove: () -> Unit,
    onReject: () -> Unit,
    isApproving: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onReject,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFDC2626)
            )
        ) {
            Icon(Icons.Default.Close, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reject")
        }
        Button(
            onClick = onApprove,
            modifier = Modifier.weight(1f),
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
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Approve")
            }
        }
    }
}

@Composable
private fun ActionButtonsForProcessing(
    onComplete: () -> Unit,
    onFail: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onFail,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFDC2626)
            )
        ) {
            Icon(Icons.Default.Error, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Failed")
        }
        Button(
            onClick = onComplete,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = GradientStart
            )
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Complete")
        }
    }
}

@Composable
private fun ActionDialog(
    title: String,
    inputLabel: String,
    inputValue: String,
    onInputChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    confirmButtonColor: Color
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = "$inputLabel (optional):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NavBarText
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = onInputChange,
                    placeholder = { Text("Enter...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = confirmButtonColor)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Confirm")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getStatusBackgroundColor(status: WithdrawalStatus): Color {
    return when (status) {
        WithdrawalStatus.PENDING -> Color(0xFFFFF3E0)
        WithdrawalStatus.PROCESSING -> Color(0xFFE3F2FD)
        WithdrawalStatus.COMPLETED -> Color(0xFFE8F5E9)
        WithdrawalStatus.REJECTED, WithdrawalStatus.FAILED -> Color(0xFFFFEBEE)
    }
}

private fun getStatusTextColor(status: WithdrawalStatus): Color {
    return when (status) {
        WithdrawalStatus.PENDING -> Color(0xFFE65100)
        WithdrawalStatus.PROCESSING -> Color(0xFF1565C0)
        WithdrawalStatus.COMPLETED -> Color(0xFF1B5E20)
        WithdrawalStatus.REJECTED, WithdrawalStatus.FAILED -> Color(0xFFC62828)
    }
}

private fun getStatusIcon(status: WithdrawalStatus): ImageVector {
    return when (status) {
        WithdrawalStatus.PENDING -> Icons.Default.Schedule
        WithdrawalStatus.PROCESSING -> Icons.Default.Sync
        WithdrawalStatus.COMPLETED -> Icons.Default.CheckCircle
        WithdrawalStatus.REJECTED -> Icons.Default.Cancel
        WithdrawalStatus.FAILED -> Icons.Default.Error
    }
}

private fun getStatusLabel(status: WithdrawalStatus): String {
    return when (status) {
        WithdrawalStatus.PENDING -> "Pending"
        WithdrawalStatus.PROCESSING -> "Processing"
        WithdrawalStatus.COMPLETED -> "Completed"
        WithdrawalStatus.REJECTED -> "Rejected"
        WithdrawalStatus.FAILED -> "Failed"
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

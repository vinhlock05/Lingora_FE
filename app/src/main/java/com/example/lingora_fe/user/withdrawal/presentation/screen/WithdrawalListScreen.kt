package com.example.lingora_fe.user.withdrawal.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.lingora_fe.core.ui.theme.ArimoFontFamily
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.navigation.Route
import com.example.lingora_fe.user.withdrawal.domain.model.Withdrawal
import com.example.lingora_fe.user.withdrawal.domain.model.WithdrawalStatus
import com.example.lingora_fe.user.withdrawal.presentation.WithdrawalViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalListScreen(
    navController: NavHostController,
    viewModel: WithdrawalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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

    // Refresh data when screen becomes visible (after navigating back from create)
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rút tiền",
                        fontFamily = ArimoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Route.WithdrawalCreate.route) },
                containerColor = GradientStart
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Tạo yêu cầu rút tiền",
                    tint = Color.White
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Balance Card
            item {
                BalanceCard(
                    balance = state.balance,
                    isLoading = state.isLoadingBalance,
                    onRefresh = { viewModel.loadBalance() }
                )
            }

            // Status Filter
            item {
                StatusFilterChips(
                    selectedStatus = state.selectedStatus,
                    onStatusSelected = { viewModel.filterByStatus(it) }
                )
            }

            // Withdrawal History Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lịch sử rút tiền",
                        fontFamily = ArimoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${state.totalWithdrawals} yêu cầu",
                        fontFamily = ArimoFontFamily,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            // Loading indicator
            if (state.isLoadingWithdrawals) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Empty state
            if (!state.isLoadingWithdrawals && state.withdrawals.isEmpty()) {
                item {
                    EmptyWithdrawalState()
                }
            }

            // Withdrawal list
            items(state.withdrawals) { withdrawal ->
                WithdrawalListItem(
                    withdrawal = withdrawal,
                    onClick = { navController.navigate(Route.withdrawalDetail(withdrawal.id)) }
                )
            }

            // Pagination
            if (state.totalPages > 1) {
                item {
                    PaginationControls(
                        currentPage = state.currentPage,
                        totalPages = state.totalPages,
                        onPageChange = { viewModel.loadWithdrawals(it) }
                    )
                }
            }

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun BalanceCard(
    balance: com.example.lingora_fe.user.withdrawal.domain.model.Balance?,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(20.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Số dư khả dụng",
                            color = Color.White.copy(alpha = 0.8f),
                            fontFamily = ArimoFontFamily,
                            fontSize = 14.sp
                        )
                        IconButton(onClick = onRefresh) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Color.White
                            )
                        }
                    }
                    
                    Text(
                        text = formatCurrency(balance?.availableBalance ?: 0),
                        color = Color.White,
                        fontFamily = ArimoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BalanceInfoItem(
                            label = "Tổng thu nhập",
                            value = formatCurrency(balance?.totalEarnings ?: 0)
                        )
                        BalanceInfoItem(
                            label = "Đang chờ xử lý",
                            value = formatCurrency(balance?.pendingWithdrawal ?: 0)
                        )
                        BalanceInfoItem(
                            label = "Đã rút",
                            value = formatCurrency(balance?.withdrawnAmount ?: 0)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontFamily = ArimoFontFamily,
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontFamily = ArimoFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun StatusFilterChips(
    selectedStatus: WithdrawalStatus?,
    onStatusSelected: (WithdrawalStatus?) -> Unit
) {
    // All statuses from backend enum
    val filterableStatuses = listOf(
        null to "Tất cả",
        WithdrawalStatus.PENDING to "Chờ xử lý",
        WithdrawalStatus.PROCESSING to "Đang xử lý",
        WithdrawalStatus.COMPLETED to "Hoàn thành",
        WithdrawalStatus.REJECTED to "Từ chối",
        WithdrawalStatus.FAILED to "Thất bại"
    )
    
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filterableStatuses.size) { index ->
            val (status, label) = filterableStatuses[index]
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                label = {
                    Text(
                        text = label,
                        fontSize = 12.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GradientStart,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun WithdrawalListItem(
    withdrawal: Withdrawal,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatCurrency(withdrawal.amount),
                    fontFamily = ArimoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${withdrawal.bankName} - ${withdrawal.bankAccountNumber}",
                    fontFamily = ArimoFontFamily,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatDate(withdrawal.createdAt),
                    fontFamily = ArimoFontFamily,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            StatusBadge(status = withdrawal.status)
        }
    }
}

@Composable
fun StatusBadge(status: WithdrawalStatus) {
    val (backgroundColor, textColor) = when (status) {
        WithdrawalStatus.PENDING -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        WithdrawalStatus.PROCESSING -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        WithdrawalStatus.COMPLETED -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
        WithdrawalStatus.REJECTED -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        WithdrawalStatus.FAILED -> Color(0xFFFFEBEE) to Color(0xFFB71C1C)
    }
    
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = textColor,
            fontFamily = ArimoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun EmptyWithdrawalState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chưa có yêu cầu rút tiền nào",
            fontFamily = ArimoFontFamily,
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Nhấn nút + để tạo yêu cầu mới",
            fontFamily = ArimoFontFamily,
            fontSize = 14.sp,
            color = Color.Gray.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onPageChange(currentPage - 1) },
            enabled = currentPage > 1
        ) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
        }
        
        Text(
            text = "$currentPage / $totalPages",
            fontFamily = ArimoFontFamily,
            fontWeight = FontWeight.Medium
        )
        
        IconButton(
            onClick = { onPageChange(currentPage + 1) },
            enabled = currentPage < totalPages
        ) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next")
        }
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

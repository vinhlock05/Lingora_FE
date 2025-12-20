package com.example.lingora_fe.user.withdrawal.presentation.screen

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
import androidx.navigation.NavHostController
import com.example.lingora_fe.core.ui.theme.ArimoFontFamily
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.user.withdrawal.domain.model.WithdrawalStatus
import com.example.lingora_fe.user.withdrawal.presentation.WithdrawalViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalDetailScreen(
    withdrawalId: Int,
    navController: NavHostController,
    viewModel: WithdrawalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load withdrawal detail
    LaunchedEffect(withdrawalId) {
        viewModel.loadWithdrawalDetail(withdrawalId)
    }

    // Show error snackbar
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chi tiết rút tiền",
                        fontFamily = ArimoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        viewModel.clearSelectedWithdrawal()
                        navController.popBackStack() 
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
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
                                .padding(20.dp),
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
                                text = withdrawal.status.displayName,
                                fontFamily = ArimoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = getStatusTextColor(withdrawal.status)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = formatCurrency(withdrawal.amount),
                                fontFamily = ArimoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )
                        }
                    }

                    // Bank Information Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Thông tin ngân hàng",
                                fontFamily = ArimoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            
                            DetailInfoRow(
                                icon = Icons.Default.AccountBalance,
                                label = "Ngân hàng",
                                value = withdrawal.bankName
                            )
                            
                            DetailInfoRow(
                                icon = Icons.Default.CreditCard,
                                label = "Số tài khoản",
                                value = withdrawal.bankAccountNumber
                            )
                            
                            DetailInfoRow(
                                icon = Icons.Default.Person,
                                label = "Chủ tài khoản",
                                value = withdrawal.bankAccountName
                            )
                            
                            withdrawal.bankBranch?.let { branch ->
                                DetailInfoRow(
                                    icon = Icons.Default.LocationOn,
                                    label = "Chi nhánh",
                                    value = branch
                                )
                            }
                        }
                    }

                    // Transaction Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Thông tin giao dịch",
                                fontFamily = ArimoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            
                            DetailInfoRow(
                                icon = Icons.Default.Tag,
                                label = "Mã yêu cầu",
                                value = "#${withdrawal.id}"
                            )
                            
                            DetailInfoRow(
                                icon = Icons.Default.CalendarToday,
                                label = "Ngày tạo",
                                value = formatDate(withdrawal.createdAt)
                            )
                            
                            DetailInfoRow(
                                icon = Icons.Default.Update,
                                label = "Cập nhật lần cuối",
                                value = formatDate(withdrawal.updatedAt)
                            )
                            
                            withdrawal.transactionReference?.let { ref ->
                                DetailInfoRow(
                                    icon = Icons.Default.Receipt,
                                    label = "Mã giao dịch NH",
                                    value = ref
                                )
                            }
                        }
                    }

                    // Rejection Reason Card (if rejected)
                    if (withdrawal.status == WithdrawalStatus.REJECTED || 
                        withdrawal.status == WithdrawalStatus.FAILED) {
                        withdrawal.rejectionReason?.let { reason ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFEBEE)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
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
                                                "Lý do từ chối" else "Lý do thất bại",
                                            fontFamily = ArimoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFFC62828)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = reason,
                                            fontFamily = ArimoFontFamily,
                                            fontSize = 14.sp,
                                            color = Color(0xFF424242)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Status Timeline
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Trạng thái xử lý",
                                fontFamily = ArimoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            StatusTimeline(currentStatus = withdrawal.status)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
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
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontFamily = ArimoFontFamily,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontFamily = ArimoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun StatusTimeline(currentStatus: WithdrawalStatus) {
    // Status flow: PENDING -> PROCESSING -> COMPLETED
    val steps = listOf(
        "PENDING" to "Chờ xử lý",
        "PROCESSING" to "Đang xử lý",
        "COMPLETED" to "Hoàn thành"
    )
    
    val currentIndex = when (currentStatus) {
        WithdrawalStatus.PENDING -> 0
        WithdrawalStatus.PROCESSING -> 1
        WithdrawalStatus.COMPLETED -> 2
        WithdrawalStatus.REJECTED, WithdrawalStatus.FAILED -> -1
    }
    
    if (currentStatus == WithdrawalStatus.REJECTED || currentStatus == WithdrawalStatus.FAILED) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Cancel,
                contentDescription = null,
                tint = Color(0xFFC62828),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (currentStatus == WithdrawalStatus.REJECTED) 
                    "Yêu cầu đã bị từ chối" else "Giao dịch thất bại",
                fontFamily = ArimoFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFC62828)
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, (_, label) ->
                val isCompleted = index <= currentIndex
                val isCurrent = index == currentIndex
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isCurrent) GradientStart else Color(0xFF4CAF50)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(6.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFFE0E0E0)
                            ) {
                                Box(
                                    modifier = Modifier.size(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontFamily = ArimoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontFamily = ArimoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCompleted) Color.Black else Color.Gray
                    )
                }
            }
        }
    }
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

private fun formatCurrency(amount: Long): String {
    val format = NumberFormat.getInstance(Locale("vi", "VN"))
    return "${format.format(amount)} VND"
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))
    return sdf.format(date)
}

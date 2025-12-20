package com.example.lingora_fe.user.withdrawal.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.lingora_fe.core.ui.theme.ArimoFontFamily
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.user.withdrawal.presentation.WithdrawalViewModel
import com.example.lingora_fe.user.withdrawal.presentation.vietnameseBanks
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWithdrawalScreen(
    navController: NavHostController,
    viewModel: WithdrawalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showBankDropdown by remember { mutableStateOf(false) }

    // Show error snackbar
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Navigate back on success
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
            navController.popBackStack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tạo yêu cầu rút tiền",
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Available Balance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(GradientStart, GradientEnd)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Số dư khả dụng",
                            color = Color.White.copy(alpha = 0.8f),
                            fontFamily = ArimoFontFamily,
                            fontSize = 14.sp
                        )
                        Text(
                            text = formatCurrency(state.balance?.availableBalance ?: 0),
                            color = Color.White,
                            fontFamily = ArimoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                }
            }

            // Amount Input
            OutlinedTextField(
                value = state.amount,
                onValueChange = { viewModel.updateAmount(it.filter { c -> c.isDigit() }) },
                label = { Text("Số tiền (VND)") },
                placeholder = { Text("Nhập số tiền cần rút") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = ThousandSeparatorTransformation(),
                isError = state.amountError != null,
                supportingText = {
                    Column {
                        state.amountError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                        Text(
                            "Tối thiểu: 50,000 VND - Tối đa: 50,000,000 VND",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                leadingIcon = {
                    Icon(Icons.Default.AttachMoney, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Bank Name Dropdown
            ExposedDropdownMenuBox(
                expanded = showBankDropdown,
                onExpandedChange = { showBankDropdown = it }
            ) {
                OutlinedTextField(
                    value = state.bankName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ngân hàng") },
                    placeholder = { Text("Chọn ngân hàng") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    isError = state.bankNameError != null,
                    supportingText = state.bankNameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBankDropdown)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.AccountBalance, contentDescription = null)
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = showBankDropdown,
                    onDismissRequest = { showBankDropdown = false }
                ) {
                    vietnameseBanks.forEach { bank ->
                        DropdownMenuItem(
                            text = { Text(bank) },
                            onClick = {
                                viewModel.updateBankName(bank)
                                showBankDropdown = false
                            }
                        )
                    }
                }
            }

            // Bank Account Number
            OutlinedTextField(
                value = state.bankAccountNumber,
                onValueChange = { viewModel.updateBankAccountNumber(it.filter { c -> c.isDigit() }) },
                label = { Text("Số tài khoản") },
                placeholder = { Text("Nhập số tài khoản ngân hàng") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.bankAccountNumberError != null,
                supportingText = state.bankAccountNumberError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                leadingIcon = {
                    Icon(Icons.Default.CreditCard, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Bank Account Name
            OutlinedTextField(
                value = state.bankAccountName,
                onValueChange = { viewModel.updateBankAccountName(it.uppercase()) },
                label = { Text("Tên chủ tài khoản") },
                placeholder = { Text("VD: NGUYEN VAN A") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.bankAccountNameError != null,
                supportingText = {
                    Column {
                        state.bankAccountNameError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                        Text(
                            "Viết hoa, không dấu, giống chính xác trên thẻ ngân hàng",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Bank Branch (Optional)
            OutlinedTextField(
                value = state.bankBranch,
                onValueChange = { viewModel.updateBankBranch(it) },
                label = { Text("Chi nhánh (không bắt buộc)") },
                placeholder = { Text("VD: Chi nhánh Hà Nội") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Submit Button
            Button(
                onClick = { viewModel.createWithdrawal() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isCreating,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GradientStart
                )
            ) {
                if (state.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gửi yêu cầu rút tiền",
                        fontFamily = ArimoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = GradientStart
                    )
                    Column {
                        Text(
                            text = "Lưu ý",
                            fontFamily = ArimoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Yêu cầu sẽ được xử lý trong vòng 1-3 ngày làm việc\n" +
                                    "• Vui lòng kiểm tra kỹ thông tin ngân hàng trước khi gửi\n" +
                                    "• Liên hệ hỗ trợ nếu có vấn đề với giao dịch",
                            fontFamily = ArimoFontFamily,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun formatCurrency(amount: Long): String {
    val format = NumberFormat.getInstance(Locale("vi", "VN"))
    return "${format.format(amount)} VND"
}

/**
 * VisualTransformation to display numbers with thousand separators
 * Example: 1000000 -> 1.000.000
 */
class ThousandSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val formattedText = formatWithThousandSeparator(originalText)
        
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Count dots before this position
                val digitsBeforeOffset = originalText.take(offset)
                val formattedBeforeOffset = formatWithThousandSeparator(digitsBeforeOffset)
                return formattedBeforeOffset.length
            }
            
            override fun transformedToOriginal(offset: Int): Int {
                // Remove dots to get original position
                val textBeforeOffset = formattedText.take(offset)
                return textBeforeOffset.replace(".", "").length
            }
        }
        
        return TransformedText(
            text = AnnotatedString(formattedText),
            offsetMapping = offsetMapping
        )
    }
    
    private fun formatWithThousandSeparator(text: String): String {
        if (text.isEmpty()) return text
        val reversed = text.reversed()
        val withSeparators = reversed.chunked(3).joinToString(".")
        return withSeparators.reversed()
    }
}

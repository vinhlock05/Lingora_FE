package com.example.lingora_fe.user.common.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.admin.report.domain.model.*
import com.example.lingora_fe.admin.report.presentation.ReportManagementViewModel
import com.example.lingora_fe.core.ui.theme.GradientStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReportDialog(
    targetType: TargetType,
    targetId: Int,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: ReportManagementViewModel = hiltViewModel()
) {
    val dialogState by viewModel.createReportDialogState.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Báo cáo vi phạm") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Report Type Dropdown
                var expanded by remember { mutableStateOf(false) }
                
                Text("Loại vi phạm", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = dialogState.selectedReportType?.displayName ?: "Chọn loại vi phạm",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Loại vi phạm *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = dialogState.reportTypeError != null,
                        supportingText = {
                            if (dialogState.reportTypeError != null) {
                                Text(
                                    dialogState.reportTypeError!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ReportType.entries.forEach { reportType ->
                            DropdownMenuItem(
                                text = { Text(reportType.displayName) },
                                onClick = {
                                    viewModel.updateCreateReportDialogState(
                                        dialogState.copy(selectedReportType = reportType)
                                    )
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Reason Text Field
                OutlinedTextField(
                    value = dialogState.reason,
                    onValueChange = {
                        if (it.length <= 500) {
                            viewModel.updateCreateReportDialogState(
                                dialogState.copy(reason = it)
                            )
                        }
                    },
                    label = {
                        Text(
                            if (dialogState.selectedReportType == ReportType.OTHER)
                                "Lý do *"
                            else
                                "Chi tiết bổ sung (Tùy chọn)"
                        )
                    },
                    placeholder = { Text("Mô tả vấn đề...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    isError = dialogState.reasonError != null,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (dialogState.reasonError != null) {
                                Text(
                                    dialogState.reasonError!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }
                            Text(
                                "${dialogState.reason.length}/500",
                                color = if (dialogState.reason.length > 500)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                // Info text
                Text(
                    "Báo cáo của bạn sẽ được xem xét bởi đội ngũ kiểm duyệt. Báo cáo sai có thể dẫn đến hành động với tài khoản của bạn.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (dialogState.isValid && dialogState.selectedReportType != null) {
                        val reportData = CreateReportData(
                            targetType = targetType,
                            targetId = targetId,
                            reportType = dialogState.selectedReportType!!,
                            reason = dialogState.reason.takeIf { it.isNotBlank() }
                        )
                        viewModel.createReport(reportData)
                        onSuccess()
                    }
                },
                enabled = dialogState.isValid && !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GradientStart
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Gửi báo cáo", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !state.isLoading
            ) {
                Text("Hủy")
            }
        }
    )
}

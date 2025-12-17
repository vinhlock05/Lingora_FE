package com.example.lingora_fe.admin.report.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.admin.report.domain.model.*
import com.example.lingora_fe.admin.report.presentation.HandleReportDialogState
import com.example.lingora_fe.admin.report.presentation.ReportManagementViewModel
import com.example.lingora_fe.core.ui.theme.GradientStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandleReportDialog(
    reportId: Int,
    onDismiss: () -> Unit,
    viewModel: ReportManagementViewModel = hiltViewModel()
) {
    val dialogState by viewModel.handleDialogState.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Handle Report") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Selection
                Text("Report Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = dialogState.selectedStatus == ReportStatus.ACCEPTED,
                        onClick = {
                            viewModel.updateHandleDialogState(
                                dialogState.copy(selectedStatus = ReportStatus.ACCEPTED)
                            )
                        },
                        label = { Text("Accept") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GradientStart.copy(alpha = 0.2f),
                            selectedLabelColor = GradientStart
                        )
                    )
                    FilterChip(
                        selected = dialogState.selectedStatus == ReportStatus.REJECTED,
                        onClick = {
                            viewModel.updateHandleDialogState(
                                dialogState.copy(selectedStatus = ReportStatus.REJECTED)
                            )
                        },
                        label = { Text("Reject") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GradientStart.copy(alpha = 0.2f),
                            selectedLabelColor = GradientStart
                        )
                    )
                }

                // Actions (only if ACCEPTED)
                if (dialogState.selectedStatus == ReportStatus.ACCEPTED) {
                    Divider()
                    
                    // Content Action - Toggle
                    Text("Content Action", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.updateHandleDialogState(
                                    dialogState.copy(deleteContent = !dialogState.deleteContent)
                                )
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Delete Content", fontWeight = FontWeight.Medium)
                            Text(
                                "Soft delete (admin can still view)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = dialogState.deleteContent,
                            onCheckedChange = {
                                viewModel.updateHandleDialogState(
                                    dialogState.copy(deleteContent = it)
                                )
                            }
                        )
                    }

                    Divider()

                    // User Action - Radio Buttons
                    Text("User Action (Select one)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // WARN_USER
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateHandleDialogState(
                                        dialogState.copy(selectedUserAction = ReportActionType.WARN_USER)
                                    )
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = dialogState.selectedUserAction == ReportActionType.WARN_USER,
                                onClick = {
                                    viewModel.updateHandleDialogState(
                                        dialogState.copy(selectedUserAction = ReportActionType.WARN_USER)
                                    )
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Warn User", fontWeight = FontWeight.Medium)
                                Text(
                                    "Send warning notification",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // SUSPEND_USER
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateHandleDialogState(
                                        dialogState.copy(selectedUserAction = ReportActionType.SUSPEND_USER)
                                    )
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = dialogState.selectedUserAction == ReportActionType.SUSPEND_USER,
                                onClick = {
                                    viewModel.updateHandleDialogState(
                                        dialogState.copy(selectedUserAction = ReportActionType.SUSPEND_USER)
                                    )
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Suspend User", fontWeight = FontWeight.Medium)
                                Text(
                                    "Temporarily suspend account",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Duration field (only for SUSPEND)
                        if (dialogState.selectedUserAction == ReportActionType.SUSPEND_USER) {
                            OutlinedTextField(
                                value = dialogState.suspensionDuration,
                                onValueChange = {
                                    viewModel.updateHandleDialogState(
                                        dialogState.copy(suspensionDuration = it)
                                    )
                                },
                                label = { Text("Duration (days)") },
                                placeholder = { Text("1-365") },
                                supportingText = {
                                    if (dialogState.durationError != null) {
                                        Text(
                                            dialogState.durationError!!,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    } else {
                                        Text("Enter number of days (1-365)")
                                    }
                                },
                                isError = dialogState.durationError != null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 48.dp),
                                singleLine = true
                            )
                        }

                        // BAN_USER
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateHandleDialogState(
                                        dialogState.copy(selectedUserAction = ReportActionType.BAN_USER)
                                    )
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = dialogState.selectedUserAction == ReportActionType.BAN_USER,
                                onClick = {
                                    viewModel.updateHandleDialogState(
                                        dialogState.copy(selectedUserAction = ReportActionType.BAN_USER)
                                    )
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Ban User", fontWeight = FontWeight.Medium)
                                Text(
                                    "Permanently ban user",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Reason field (optional)
                    OutlinedTextField(
                        value = dialogState.actionReason,
                        onValueChange = {
                            viewModel.updateHandleDialogState(
                                dialogState.copy(actionReason = it)
                            )
                        },
                        label = { Text("Reason (Optional)") },
                        placeholder = { Text("Add a note about this action...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validatedState = dialogState.validate()
                    viewModel.updateHandleDialogState(validatedState)
                    
                    if (!validatedState.isValid) return@Button
                    
                    // Build actions array based on new logic
                    val actions: List<ReportAction>? = if (dialogState.selectedStatus == ReportStatus.ACCEPTED) {
                        buildList<ReportAction> {
                            // Add DELETE_CONTENT if toggle is ON
                            if (dialogState.deleteContent) {
                                add(ReportAction(
                                    type = ReportActionType.DELETE_CONTENT,
                                    reason = dialogState.actionReason.takeIf { it.isNotBlank() },
                                    duration = null
                                ))
                            }
                            
                            // Always add selected user action
                            add(ReportAction(
                                type = dialogState.selectedUserAction,
                                reason = dialogState.actionReason.takeIf { it.isNotBlank() },
                                duration = if (dialogState.selectedUserAction == ReportActionType.SUSPEND_USER) {
                                    dialogState.suspensionDuration.toIntOrNull()
                                } else null
                            ))
                        }
                    } else null
                    
                    val handleData = HandleReportData(
                        status = dialogState.selectedStatus,
                        actions = actions
                    )
                    
                    viewModel.handleReport(reportId, handleData)
                },
                enabled = dialogState.isValid && !state.isHandlingReport,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GradientStart
                )
            ) {
                if (state.isHandlingReport) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Submit", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !state.isHandlingReport
            ) {
                Text("Cancel")
            }
        }
    )
}

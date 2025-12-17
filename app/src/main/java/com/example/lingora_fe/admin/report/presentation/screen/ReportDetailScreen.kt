package com.example.lingora_fe.admin.report.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.admin.report.domain.model.*
import com.example.lingora_fe.admin.report.presentation.ReportManagementViewModel
import com.example.lingora_fe.admin.report.presentation.components.HandleReportDialog
import com.example.lingora_fe.admin.report.presentation.components.ReportStatusBadge
import com.example.lingora_fe.admin.report.presentation.components.ReportTypeBadge
import com.example.lingora_fe.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportId: Int,
    navController: androidx.navigation.NavController,
    viewModel: ReportManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(reportId) {
        viewModel.loadReportDetail(reportId)
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isReportDetailsLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.selectedReport == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Report not found")
                    }
                }
                else -> {
                    val report = state.selectedReport!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Report Info Card
                        ReportInfoCard(report = report)

                        // Target Details Card - fetched separately
                        TargetContentCard(
                            targetContent = state.targetContent,
                            isLoading = state.isTargetContentLoading,
                            error = state.targetContentError,
                        )

                        // Report History
                        ReportHistoryCard(
                            reportHistory = report.reportHistory,
                            totalReports = report.totalReports
                        )

                        // Action Buttons
                        if (report.status == ReportStatus.PENDING) {
                            ActionButtons(
                                onAccept = { viewModel.showHandleDialog(report.id) },
                                onReject = { 
                                    viewModel.updateReportStatus(
                                        report.id,
                                        ReportStatus.REJECTED
                                    )
                                },
                                onDelete = { showDeleteDialog = true }
                            )
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Report Status: ${report.status.value}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Button(
                                        onClick = { showDeleteDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Handle Report Dialog
    if (state.showHandleDialog && state.handleDialogReportId != null) {
        HandleReportDialog(
            reportId = state.handleDialogReportId!!,
            onDismiss = { viewModel.hideHandleDialog() },
            viewModel = viewModel
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, "Warning", tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Report") },
            text = { Text("Are you sure you want to delete this report? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReport(reportId)
                        showDeleteDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Snackbar Messages
    LaunchedEffect(state.actionSuccess, state.actionError) {
        state.actionSuccess?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearActionMessages()
        }
        state.actionError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
            viewModel.clearActionMessages()
        }
    }
}

@Composable
fun ReportInfoCard(report: ReportDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Report Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Divider()

            // Report Type and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Report Type", style = MaterialTheme.typography.labelMedium, color = NavBarText)
                    Spacer(modifier = Modifier.height(4.dp))
                    ReportTypeBadge(reportType = report.reportType)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Status", style = MaterialTheme.typography.labelMedium, color = NavBarText)
                    Spacer(modifier = Modifier.height(4.dp))
                    ReportStatusBadge(status = report.status)
                }
            }

            // Target Type
            InfoRow(label = "Target Type", value = report.targetType.displayName)

            // Reason
            if (!report.reason.isNullOrBlank()) {
                Column {
                    Text("Reason", style = MaterialTheme.typography.labelMedium, color = NavBarText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        report.reason,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            Divider()

            // Reporter Info
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(GradientStart, GradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = report.createdBy.username.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text("Reported by", style = MaterialTheme.typography.labelMedium, color = NavBarText)
                    Text(
                        report.createdBy.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    report.createdBy.email?.let { email ->
                        Text(email, style = MaterialTheme.typography.bodySmall, color = NavBarText)
                    }
                }
            }

            // Created Date
            InfoRow(
                label = "Reported on",
                value = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault()).format(report.createdAt)
            )
        }
    }
}

@Composable
fun TargetContentCard(
    targetContent: TargetContent?,
    isLoading: Boolean,
    error: String?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Reported Content",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Show target ID for reference
                if (targetContent != null) {
                    Text(
                        when (targetContent) {
                            is TargetContent.PostContent -> "Post ID: ${targetContent.post.id}"
                            is TargetContent.StudySetContent -> "StudySet ID: ${targetContent.studySet.id}"
                            is TargetContent.CommentContent -> "Comment ID: ${targetContent.comment.id}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = NavBarText
                    )
                }
            }

            Divider()

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                error != null -> {
                    Text(
                        "Error loading content: $error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                targetContent is TargetContent.PostContent -> {
                    val post = targetContent.post
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Title:", style = MaterialTheme.typography.labelMedium, color = NavBarText)
                        Text(post.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        
                        Text("Content:", style = MaterialTheme.typography.labelMedium, color = NavBarText)
                        Text(
                            post.content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Text("Author:", style = MaterialTheme.typography.labelMedium, color = NavBarText)
                        post.createdBy?.username?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    }
                }
                
                targetContent is TargetContent.StudySetContent -> {
                    val studySet = targetContent.studySet
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Title:", style = MaterialTheme.typography.labelMedium, color = NavBarText)
                        Text(studySet.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        
                        studySet.description?.let { desc ->
                            Text("Description:", style = MaterialTheme.typography.labelMedium, color = NavBarText)
                            Text(
                                desc,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Text("Owner:", style = MaterialTheme.typography.labelMedium, color = NavBarText)
                        Text(studySet.owner.username, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                
                targetContent is TargetContent.CommentContent -> {
                    val comment = targetContent.comment
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Comment:", style = MaterialTheme.typography.labelMedium, color = NavBarText)
                        Text(
                            comment.content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Text("Author:", style = MaterialTheme.typography.labelMedium, color = NavBarText)
                        comment.createdBy?.username?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    }
                }
                
                else -> {
                    Text(
                        "Content has been deleted or is no longer available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
fun ReportHistoryCard(reportHistory: List<Report>, totalReports: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF009688)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Report history",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF026259)
                ) {
                    Text(
                        totalReports.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Text(
                text = if (totalReports > 1) "$totalReports reports for this content" else "$totalReports report for this content",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            
            if (reportHistory.isNotEmpty()) {
                Divider()
                
                var isHistoryExpanded by remember { mutableStateOf(false) }
                val displayedHistory = if (isHistoryExpanded || reportHistory.size <= 5) {
                    reportHistory
                } else {
                    reportHistory.take(5)
                }
                
                // Report History List
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    displayedHistory.forEach { historyReport ->
                        ReportHistoryItem(historyReport)
                    }
                    
                    // Show More/Less button if more than 5 items
                    if (reportHistory.size > 5) {
                        TextButton(
                            onClick = { isHistoryExpanded = !isHistoryExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (isHistoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isHistoryExpanded) 
                                    "Show Less" 
                                else 
                                    "Show More (${reportHistory.size - 5} more)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportHistoryItem(report: Report) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Reporter and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(GradientStart, GradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = report.createdBy.username.take(1).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(
                            report.createdBy.username,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(report.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = NavBarText
                        )
                    }
                }
                ReportStatusBadge(status = report.status)
            }
            
            // Report Type
            ReportTypeBadge(reportType = report.reportType)
            
            // Reason
            if (report.reason != null) {
                Text(
                    report.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = NavBarText,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun ActionButtons(
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009688)
                    )
                ) {
                    Icon(Icons.Default.Check, "Accept", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Handle")
                }
                
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, "Reject", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete Report")
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = NavBarText)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

package com.example.lingora_fe.admin.report.presentation.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.admin.report.domain.model.*
import com.example.lingora_fe.admin.report.presentation.ReportManagementViewModel
import com.example.lingora_fe.admin.common.presentation.components.*
import com.example.lingora_fe.admin.report.presentation.components.ReportStatusBadge
import com.example.lingora_fe.admin.report.presentation.components.ReportTypeBadge
import com.example.lingora_fe.core.ui.theme.*
import com.example.lingora_fe.util.DateFormatHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    onNavigateToDetail: (Int) -> Unit,
    viewModel: ReportManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    var hasInitiallyLoaded by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadReports()
        hasInitiallyLoaded = true
    }
    
    // Auto-reload when filters change (skip initial load)
    LaunchedEffect(
        state.selectedStatus,
        state.selectedTargetType,
        state.selectedReportType,
        state.createdByFilter,
        state.selectedSort
    ) {
        if (hasInitiallyLoaded) {
            viewModel.loadReports(page = 1)
        }
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Tìm kiếm theo lý do báo cáo...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                viewModel.updateSearchQuery("")
                                viewModel.performSearch()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { viewModel.performSearch() }
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Filter and Sort Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    FilterButton(
                        onClick = { showFilterDialog = true },
                        hasActiveFilters = state.selectedStatus != null || 
                                         state.selectedTargetType != null || 
                                         state.selectedReportType != null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SortButton(
                        onClick = { showSortMenu = true },
                        hasActiveSort = state.selectedSort != null
                    )
                }

                // Active Filters
                if (state.selectedStatus != null || state.selectedTargetType != null || state.selectedReportType != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.selectedStatus?.let { status ->
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.filterByStatus(null) },
                                label = { Text(status.displayName, style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)) },
                                trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GradientStart.copy(alpha = 0.2f),
                                    selectedLabelColor = GradientStart
                                )
                            )
                        }
                        state.selectedTargetType?.let { targetType ->
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.filterByTargetType(null) },
                                label = { Text(targetType.displayName, style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)) },
                                trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GradientStart.copy(alpha = 0.2f),
                                    selectedLabelColor = GradientStart
                                )
                            )
                        }
                        state.selectedReportType?.let { reportType ->
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.filterByReportType(null) },
                                label = { Text(reportType.displayName, style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)) },
                                trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GradientStart.copy(alpha = 0.2f),
                                    selectedLabelColor = GradientStart
                                )
                            )
                        }
                    }
                }

                // Report List
                when {
                    state.isLoading && state.reports.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    state.error != null && state.reports.isEmpty() -> {
                        ErrorContent(
                            message = state.error!!,
                            onRetry = { viewModel.refreshReports() }
                        )
                    }
                    state.reports.isEmpty() -> {
                        EmptyContent(message = "No reports found", icon = Icons.Default.Report)
                    }
                    else -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.reports, key = { it.id }) { report ->
                                    ReportCard(
                                        report = report,
                                        onClick = { onNavigateToDetail(report.id) }
                                    )
                                }

                                // Pagination
                                if (state.totalPages > 1) {
                                    item {
                                        PaginationControls(
                                            currentPage = state.currentPage,
                                            totalPages = state.totalPages,
                                            onPageChange = { page ->
                                                viewModel.loadReports(page)
                                            }
                                        )
                                    }
                                }
                            }

                            // Loading Overlay
                            if (state.isLoading && state.reports.isNotEmpty()) {
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

    // Dialogs
    if (showFilterDialog) {
        ReportFilterDialog(
            selectedStatus = state.selectedStatus,
            selectedTargetType = state.selectedTargetType,
            selectedReportType = state.selectedReportType,
            onDismiss = { showFilterDialog = false },
            onApply = { status, targetType, reportType ->
                viewModel.filterByStatus(status)
                viewModel.filterByTargetType(targetType)
                viewModel.filterByReportType(reportType)
                showFilterDialog = false
            },
            onClear = {
                viewModel.clearFilters()
                showFilterDialog = false
            }
        )
    }

    if (showSortMenu) {
        ReportSortDialog(
            selectedSort = state.selectedSort,
            onDismiss = { showSortMenu = false },
            onSelectSort = { sort ->
                viewModel.sortBy(sort)
                showSortMenu = false
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReportCard(
    report: Report,
    onClick: () -> Unit
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
            // Header: Report Type and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReportTypeBadge(reportType = report.reportType)
                ReportStatusBadge(status = report.status)
            }

            // Target Type and ID
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (report.targetType) {
                        TargetType.POST -> Icons.Default.Article
                        TargetType.STUDY_SET -> Icons.Default.LibraryBooks
                        TargetType.COMMENT -> Icons.Default.Comment
                    },
                    contentDescription = null,
                    tint = NavBarText,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "${report.targetType.displayName} #${report.targetId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NavBarText
                )
            }

            // Target Info Preview
            report.targetInfo?.let { targetInfo ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    targetInfo.title?.let { title ->
                        Text(
                            title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    targetInfo.contentPreview?.let { preview ->
                        if (report.targetType == TargetType.COMMENT)
                        {
                            Text(
                                preview,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        else {
                            Text(
                                preview,
                                style = MaterialTheme.typography.bodySmall,
                                color = NavBarText,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    report.reason?.takeIf { it.isNotBlank() }?.let { reason ->
                        Text(
                            "Reason: $reason",
                            style = MaterialTheme.typography.bodySmall,
                            color = NavBarText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }

                    Divider()

                    Spacer(modifier = Modifier.height(12.dp))

                    // Footer: Reporter and Date
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
                            Text(
                                report.createdBy.username,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            DateFormatHelper.formatTimeAgo(report.createdAt.toInstant().toString()),
                            style = MaterialTheme.typography.bodySmall,
                            color = NavBarText
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportFilterDialog(
    selectedStatus: ReportStatus?,
    selectedTargetType: TargetType?,
    selectedReportType: ReportType?,
    onDismiss: () -> Unit,
    onApply: (ReportStatus?, TargetType?, ReportType?) -> Unit,
    onClear: () -> Unit
) {
    var tempStatus by remember { mutableStateOf(selectedStatus) }
    var tempTargetType by remember { mutableStateOf(selectedTargetType) }
    var tempReportType by remember { mutableStateOf(selectedReportType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Reports") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Filter
                Text("Status", style = MaterialTheme.typography.titleSmall)
                ReportStatus.values().forEach { status ->
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
                        Text(status.displayName, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Divider()

                // Target Type Filter
                Text("Target Type", style = MaterialTheme.typography.titleSmall)
                TargetType.values().forEach { targetType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempTargetType = if (tempTargetType == targetType) null else targetType }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tempTargetType == targetType,
                            onClick = { tempTargetType = if (tempTargetType == targetType) null else targetType },
                            colors = RadioButtonDefaults.colors(selectedColor = GradientStart)
                        )
                        Text(targetType.displayName, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Divider()

                // Report Type Filter
                Text("Report Type", style = MaterialTheme.typography.titleSmall)
                ReportType.values().forEach { reportType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempReportType = if (tempReportType == reportType) null else reportType }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tempReportType == reportType,
                            onClick = { tempReportType = if (tempReportType == reportType) null else reportType },
                            colors = RadioButtonDefaults.colors(selectedColor = GradientStart)
                        )
                        Text(reportType.displayName, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onApply(tempStatus, tempTargetType, tempReportType) },
                colors = ButtonDefaults.textButtonColors(contentColor = GradientStart)
            ) {
                Text("Apply", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClear) {
                    Text("Clear All")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun ReportSortDialog(
    selectedSort: ReportSortOption?,
    onDismiss: () -> Unit,
    onSelectSort: (ReportSortOption?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort Reports") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportSortOption.values().forEach { sortOption ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSort(sortOption) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSort == sortOption,
                            onClick = { onSelectSort(sortOption) },
                            colors = RadioButtonDefaults.colors(selectedColor = GradientStart)
                        )
                        Text(
                            text = sortOption.displayName,
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = GradientStart)
            ) {
                Text("Done", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            if (selectedSort != null) {
                TextButton(onClick = { onSelectSort(null) }) {
                    Text("Clear")
                }
            }
        }
    )
}

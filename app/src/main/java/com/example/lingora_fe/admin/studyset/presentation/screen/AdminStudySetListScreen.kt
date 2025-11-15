package com.example.lingora_fe.admin.studyset.presentation.screen

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.admin.common.presentation.components.*
import com.example.lingora_fe.admin.studyset.presentation.AdminStudySetManagementEvent
import com.example.lingora_fe.admin.studyset.presentation.AdminStudySetManagementViewModel
import com.example.lingora_fe.admin.studyset.presentation.StudySetFilter
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.user.studyset.domain.model.StudySet
import com.example.lingora_fe.user.studyset.domain.model.StudySetStatus
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudySetListScreen(
    onNavigateToDetail: (Int) -> Unit,
    viewModel: AdminStudySetManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showRejectDialog by remember { mutableStateOf<Int?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.onEvent(AdminStudySetManagementEvent.LoadStudySets())
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) {
        val bottomPadding = it.calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)
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
                            viewModel.onEvent(AdminStudySetManagementEvent.SearchStudySets(it)) 
                        },
                        placeholder = "Tìm kiếm học liệu...",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StudySetFilter.values().forEach { filter ->
                        FilterChip(
                            selected = state.filterStatus == filter,
                            onClick = { 
                                viewModel.onEvent(
                                    AdminStudySetManagementEvent.FilterByStatus(
                                        if (state.filterStatus == filter) null else filter
                                    )
                                )
                            },
                            label = { 
                                Text(
                                    filter.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
                                ) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GradientStart.copy(alpha = 0.2f),
                                selectedLabelColor = GradientStart
                            )
                        )
                    }
                }

                // Study Set List
                when {
                    state.isLoading && state.studySets.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    state.error != null && state.studySets.isEmpty() -> {
                        ErrorContent(
                            message = state.error!!,
                            onRetry = { viewModel.onEvent(AdminStudySetManagementEvent.RefreshStudySets) }
                        )
                    }
                    state.studySets.isEmpty() -> {
                        EmptyContent(message = "Không tìm thấy học liệu nào", icon = Icons.Default.School)
                    }
                    else -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.studySets, key = { it.id }) { studySet ->
                                    AdminStudySetCard(
                                        studySet = studySet,
                                        onClick = { onNavigateToDetail(studySet.id) },
                                        onApprove = {
                                            viewModel.onEvent(AdminStudySetManagementEvent.ApproveStudySet(studySet.id))
                                        },
                                        onReject = {
                                            showRejectDialog = studySet.id
                                        }
                                    )
                                }

                                // Pagination
                                if (state.totalPages > 1) {
                                    item {
                                        PaginationControls(
                                            currentPage = state.currentPage,
                                            totalPages = state.totalPages,
                                            onPageChange = { page ->
                                                viewModel.onEvent(AdminStudySetManagementEvent.LoadStudySets(page))
                                            }
                                        )
                                    }
                                }
                            }

                            // Loading Overlay
                            if (state.isLoading && state.studySets.isNotEmpty()) {
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

    // Reject Dialog
    showRejectDialog?.let { studySetId ->
        AlertDialog(
            onDismissRequest = { 
                showRejectDialog = null
                rejectReason = ""
            },
            title = { Text("Từ chối học liệu") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bạn có chắc chắn muốn từ chối học liệu này?")
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Lý do (tùy chọn)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(
                            AdminStudySetManagementEvent.RejectStudySet(
                                studySetId,
                                rejectReason.takeIf { it.isNotBlank() }
                            )
                        )
                        showRejectDialog = null
                        rejectReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Từ chối")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRejectDialog = null
                    rejectReason = ""
                }) {
                    Text("Hủy")
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
            viewModel.onEvent(AdminStudySetManagementEvent.ClearActionMessages)
        }
        state.actionError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
            viewModel.onEvent(AdminStudySetManagementEvent.ClearActionMessages)
        }
    }
}

@Composable
fun AdminStudySetCard(
    studySet: StudySet,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val isPendingApproval = studySet.status == StudySetStatus.PENDING_APPROVAL
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Text(
                text = studySet.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MainText
            )

            // Description
            if (!studySet.description.isNullOrBlank()) {
                Text(
                    text = studySet.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NavBarText,
                    maxLines = 2
                )
            }

            // Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val cardCount = studySet.totalFlashcards ?: studySet.flashcards.size
                    if (cardCount > 0) {
                        BadgeChip(text = "$cardCount thẻ")
                    }
                    val quizCount = studySet.totalQuizzes ?: studySet.quizzes.size
                    if (quizCount > 0) {
                        BadgeChip(text = "$quizCount câu hỏi")
                    }
                    StatusBadge(status = studySet.status)
                }

                // Price
                if (studySet.price > 0) {
                    Text(
                        text = formatPrice(studySet.price),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GradientStart,
                        fontSize = 16.sp
                    )
                } else {
                    Text(
                        text = "Miễn phí",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        fontSize = 16.sp
                    )
                }
            }

            // Owner and Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tác giả: ${studySet.owner.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NavBarText
                )
                Text(
                    text = "${studySet.likeCount} lượt thích",
                    style = MaterialTheme.typography.bodySmall,
                    color = NavBarText
                )
            }

            // Action Buttons (only if pending approval)
            if (isPendingApproval) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        )
                    ) {
                        Text("Duyệt")
                    }
                    Button(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Từ chối")
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFFE0F2FE),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF0369A1),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun StatusBadge(status: StudySetStatus) {
    val (text, backgroundColor, textColor) = when (status) {
        StudySetStatus.PUBLISHED -> Triple("Đã duyệt", Color(0xFFDCFCE7), Color(0xFF10B981))
        StudySetStatus.PENDING_APPROVAL -> Triple("Chờ duyệt", Color(0xFFFFF4E6), Color(0xFFF59E0B))
        StudySetStatus.DRAFT -> Triple("Nháp", Color(0xFFE5E7EB), Color(0xFF6B7280))
        StudySetStatus.REJECTED -> Triple("Đã từ chối", Color(0xFFFEE2E2), Color(0xFFEF4444))
        StudySetStatus.ARCHIVED -> Triple("Đã lưu trữ", Color(0xFFE5E7EB), Color(0xFF6B7280))
    }
    
    BadgeChip(text = text, color = backgroundColor, textColor = textColor)
}

@Composable
private fun BadgeChip(text: String, color: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(
                color = color,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontSize = 12.sp
        )
    }
}

private fun formatPrice(price: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(price)}₫"
}


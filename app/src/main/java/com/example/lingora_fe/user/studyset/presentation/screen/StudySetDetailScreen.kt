package com.example.lingora_fe.user.studyset.presentation.screen

import android.content.Intent
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.R
import com.example.lingora_fe.user.forum.presentation.CommentInputSection
import com.example.lingora_fe.user.forum.presentation.CommentThreadItem
import com.example.lingora_fe.user.studyset.presentation.viewmodel.StudySetDetailViewModel
import com.example.lingora_fe.user.studyset.presentation.PaymentWebViewActivity
import com.example.lingora_fe.user.common.presentation.components.CreateReportDialog
import com.example.lingora_fe.admin.report.domain.model.TargetType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySetDetailScreen(
    onBackClick: () -> Unit,
    onStartFlashcard: (Int) -> Unit,
    onStartQuiz: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    onDeleteSuccess: () -> Unit = {},
    viewModel: StudySetDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Calculate isOwner based on current state - only check when studySet is loaded
    val currentUserId = remember { viewModel.getCurrentUserId() }
    val isOwner = remember(uiState.studySet, currentUserId) {
        val studySet = uiState.studySet
        currentUserId != null && studySet != null && studySet.owner.id == currentUserId
    }

    LaunchedEffect(Unit) {
        viewModel.loadStudySet()
    }
    
    Log.d("StudySetDetailScreen", "isOwner: $isOwner, studySet: ${uiState.studySet?.id}, currentUserId: $currentUserId")

    val context = LocalContext.current

    var showVerifyingDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var verificationSuccess by remember { mutableStateOf(false) }
    var verificationMessage by remember { mutableStateOf("") }

    val paymentLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            android.app.Activity.RESULT_OK -> {
                val needVerification = result.data?.getBooleanExtra("needVerification", false) ?: false
                
                if (needVerification) {
                    val vnpParams = mutableMapOf<String, String>()
                    result.data?.extras?.keySet()?.forEach { key ->
                        if (key.startsWith("vnp_")) {
                            result.data?.getStringExtra(key)?.let { value ->
                                vnpParams[key] = value
                            }
                        }
                    }
                    
                    showVerifyingDialog = true
                    
                    viewModel.verifyPayment(vnpParams) { success, message ->
                        showVerifyingDialog = false
                        verificationSuccess = success
                        verificationMessage = message
                        showResultDialog = true
                    }
                } else {
                    val isSuccess = result.data?.getBooleanExtra("isSuccess", false) ?: false
                    val responseCode = result.data?.getStringExtra("vnp_ResponseCode")
                    
                    verificationSuccess = isSuccess && responseCode == "00"
                    verificationMessage = if (verificationSuccess) {
                        "Thanh toán thành công!"
                    } else {
                        "Thanh toán thất bại (Mã lỗi: $responseCode)"
                    }
                    showResultDialog = true
                }
            }
            android.app.Activity.RESULT_CANCELED -> {
                verificationSuccess = false
                verificationMessage = "Đã hủy thanh toán"
                showResultDialog = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết học liệu") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    uiState.studySet?.let { studySet ->
                        var menuExpanded by remember { mutableStateOf(false) }
                        var showReportDialog by remember { mutableStateOf(false) }
                        
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Options")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                if (isOwner) {
                                    DropdownMenuItem(
                                        text = { Text("Chỉnh sửa") },
                                        onClick = {
                                            menuExpanded = false
                                            onEditClick(studySet.id)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Xóa", color = Color(0xFFDC2626)) },
                                        onClick = {
                                            menuExpanded = false
                                            showDeleteDialog = true
                                        }
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = { Text("Báo cáo vi phạm") },
                                        onClick = {
                                            menuExpanded = false
                                            showReportDialog = true
                                        }
                                    )
                                }
                            }
                        }
                        
                        if (showReportDialog) {
                            CreateReportDialog(
                                targetType = TargetType.STUDY_SET,
                                targetId = studySet.id,
                                onDismiss = { showReportDialog = false },
                                onSuccess = { 
                                    showReportDialog = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Đã báo cáo vi phạm thành công",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            CommentInputSection(
                commentText = uiState.commentText,
                replyContext = uiState.replyContext,
                onCommentTextChange = { viewModel.updateCommentText(it) },
                onSubmitComment = {
                    viewModel.submitComment {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Bình luận đã được đăng",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                },
                onCancelReply = { viewModel.cancelReply() },
                onCommentLimitExceeded = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Bình luận tối đa 256 ký tự",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                isSubmitting = uiState.isSubmittingComment
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.studySet == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.studySet == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            uiState.studySet != null -> {
                val studySet = uiState.studySet!!
                val isAccess = studySet.isPurchased == true || studySet.price == 0 || isOwner

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Color.White
                        )
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Study Set Info Card
                    StudySetInfoCard(
                        studySet = studySet,
                        isLiking = uiState.isLiking,
                        onLikeClick = { viewModel.toggleLike(studySet.id) }
                    )

                    if (isAccess) {
                        Text(
                            text = "Chọn chế độ học",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MainText
                        )

                        LearningModeCard(
                            icon = R.drawable.ic_flashcard,
                            title = "Flashcard",
                            description = "Học từ vựng qua thẻ ghi nhớ",
                            buttonText = "Bắt đầu Flashcard",
                            onClick = { onStartFlashcard(studySet.id) },
                            enabled = studySet.flashcards.isNotEmpty()
                        )

                        LearningModeCard(
                            icon = R.drawable.ic_quiz,
                            title = "Quiz",
                            description = "Kiểm tra kiến thức với câu hỏi trắc nghiệm",
                            buttonText = "Bắt đầu Quiz",
                            onClick = { onStartQuiz(studySet.id) },
                            enabled = studySet.quizzes.isNotEmpty()
                        )
                    } else {
                        Button(
                            onClick = {
                                viewModel.buyStudySet { paymentUrl ->
                                    val intent = Intent(
                                        context,
                                        PaymentWebViewActivity::class.java
                                    ).apply {
                                        putExtra(PaymentWebViewActivity.EXTRA_PAYMENT_URL, paymentUrl)
                                        putExtra(
                                            PaymentWebViewActivity.EXTRA_STUDY_SET_ID,
                                            studySet.id
                                        )
                                    }
                                    paymentLauncher.launch(intent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isPurchasing,
                            colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
                        ) {
                            if (uiState.isPurchasing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text(text = "Mua ngay để học")
                            }
                        }
                    }

                    val totalComments = uiState.commentThreads.sumOf { 1 + it.replies.size }
                    if (totalComments > 0) {
                        Text(
                            text = "Bình luận ($totalComments)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MainText
                        )

                        uiState.commentThreads.forEach { thread ->
                            CommentThreadItem(
                                thread = thread,
                                currentUserId = uiState.currentUserId,
                                editingCommentId = uiState.editingCommentId,
                                editingCommentText = uiState.editingCommentText,
                                isUpdatingComment = uiState.isUpdatingComment,
                                onToggleReplies = { viewModel.toggleRepliesVisibility(thread.parent.id) },
                                onParentLike = { viewModel.toggleCommentLike(thread.parent.id) },
                                onReplyParent = {
                                    viewModel.startReply(
                                        parentId = thread.parent.id,
                                        targetUsername = thread.parent.createdBy?.username ?: "Người dùng"
                                    )
                                },
                                onReplyChild = { reply ->
                                    viewModel.startReply(
                                        parentId = thread.parent.id,
                                        targetUsername = reply.createdBy?.username ?: "Người dùng"
                                    )
                                },
                                onChildLike = { reply ->
                                    viewModel.toggleCommentLike(reply.id)
                                },
                                onEditParent = { viewModel.startEditComment(thread.parent) },
                                onEditChild = { comment -> viewModel.startEditComment(comment) },
                                onEditingTextChange = viewModel::updateEditingCommentText,
                                onSaveEdit = { viewModel.submitEditComment() },
                                onCancelEdit = { viewModel.cancelEditComment() }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa học liệu") },
            text = { Text("Bạn có chắc chắn muốn xóa học liệu này? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteStudySet {
                            onDeleteSuccess()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    if (showVerifyingDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text("Đang xác thực thanh toán")
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Vui lòng đợi...")
                }
            },
            confirmButton = { }
        )
    }
    
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = {
                showResultDialog = false
                if (verificationSuccess) {
                    viewModel.refresh()
                }
            },
            icon = {
                Text(
                    text = if (verificationSuccess) "✅" else "❌",
                    style = MaterialTheme.typography.displayMedium
                )
            },
            title = {
                Text(
                    text = if (verificationSuccess) "Thành công!" else "Thất bại",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = verificationMessage,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResultDialog = false
                        if (verificationSuccess) {
                            viewModel.refresh()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (verificationSuccess) GradientStart else MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun StudySetInfoCard(
    studySet: com.example.lingora_fe.user.studyset.domain.model.StudySet,
    isLiking: Boolean = false,
    onLikeClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = studySet.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MainText
            )

            if (!studySet.description.isNullOrBlank()) {
                Text(
                    text = studySet.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NavBarText
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TagChip(text = "${studySet.flashcards.size} thẻ")
                TagChip(text = "${studySet.quizzes.size} câu hỏi")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tác giả: ${studySet.owner.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NavBarText
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${studySet.commentCount} bình luận",
                        style = MaterialTheme.typography.bodySmall,
                        color = NavBarText,
                        fontSize = 14.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onLikeClick)
                    ) {
                        if (isLiking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (studySet.isAlreadyLike) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (studySet.isAlreadyLike) Color(0xFFEF4444) else NavBarText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "${studySet.likeCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NavBarText,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LearningModeCard(
    @DrawableRes icon: Int,
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Unspecified
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = NavBarText
                )
            }
        }
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = GradientStart
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(10.dp)
        ) {
            Text(buttonText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TagChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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



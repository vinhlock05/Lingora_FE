package com.example.lingora_fe.user.forum.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.navigation.Route
import com.example.lingora_fe.user.forum.domain.model.Comment
import com.example.lingora_fe.user.forum.domain.model.Post
import com.example.lingora_fe.user.forum.domain.model.PostStatus
import com.example.lingora_fe.user.common.presentation.components.CreateReportDialog
import com.example.lingora_fe.admin.report.domain.model.TargetType
import kotlinx.coroutines.launch

private const val COMMENT_MAX_LENGTH = 256

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: Int,
    navController: NavController,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val parentEntry = navController.previousBackStackEntry
    val currentEntry = navController.currentBackStackEntry
    val reloadDetailState = currentEntry?.savedStateHandle
        ?.getStateFlow("shouldReloadPostDetail", false)
        ?.collectAsState()
    
    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }

    LaunchedEffect(reloadDetailState?.value) {
        if (reloadDetailState?.value == true) {
            viewModel.loadPost(postId)
            currentEntry?.savedStateHandle?.set("shouldReloadPostDetail", false)
        }
    }
    
    val navigateBack: () -> Unit = {
        parentEntry?.savedStateHandle?.set("shouldRefreshPosts", true)
        navController.popBackStack()
    }
    
    BackHandler(onBack = navigateBack)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chi tiết bài viết",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MainText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            CommentInputSection(
                commentText = state.commentText,
                replyContext = state.replyContext,
                onCommentTextChange = { viewModel.updateCommentText(it) },
                onSubmitComment = {
                    viewModel.submitComment(postId) {
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
                            message = "Bình luận tối đa ${COMMENT_MAX_LENGTH} ký tự",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                isSubmitting = state.isSubmittingComment
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            state.isLoading && state.post == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null && state.post == null -> {
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
                            text = state.error ?: "Có lỗi xảy ra",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadPost(postId) }) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            else -> {
                state.post?.let { post ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = paddingValues.calculateTopPadding(),
                                bottom = paddingValues.calculateBottomPadding() + 16.dp,
                                start = 0.dp,
                                end = 0.dp
                            )
                            .background(Color.White),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Post Content
                        item {
                            PostDetailCard(
                                post = post,
                                onLikeClick = { viewModel.toggleLike(post.id) },
                                isLiking = state.isLiking,
                                isOwner = post.createdBy?.id == state.currentUserId,
                                onEditPost = { navController.navigate(Route.editPost(post.id)) },
                                onDeletePost = {
                                    viewModel.deletePost(post.id) {
                                        parentEntry?.savedStateHandle?.set("shouldRefreshPosts", true)
                                        navigateBack()
                                    }
                                },
                                onChangeStatus = { targetStatus ->
                                    viewModel.updatePostStatus(post.id, targetStatus)
                                }
                            )
                        }
                        
                        val totalComments = state.commentThreads.sumOf { 1 + it.replies.size }
                        
                        item {
                            Text(
                                text = "Bình luận ($totalComments)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MainText,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        items(state.commentThreads, key = { it.parent.id }) { thread ->
                            CommentThreadItem(
                                thread = thread,
                                currentUserId = state.currentUserId,
                                editingCommentId = state.editingCommentId,
                                editingCommentText = state.editingCommentText,
                                isUpdatingComment = state.isUpdatingComment,
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
                                onSaveEdit = { viewModel.submitEditComment(post.id) },
                                onCancelEdit = { viewModel.cancelEditComment() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostDetailCard(
    post: Post,
    onLikeClick: () -> Unit,
    isLiking: Boolean,
    isOwner: Boolean,
    onEditPost: () -> Unit,
    onDeletePost: () -> Unit,
    onChangeStatus: (PostStatus) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // User Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(GradientStart, GradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.createdBy?.username?.firstOrNull()?.uppercase() ?: "U",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    
                    Column {
                        Text(
                            text = post.createdBy?.username ?: "Unknown",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MainText
                        )
                        Text(
                            text = formatTimeAgo(post.createdAt),
                            fontSize = 12.sp,
                            color = NavBarText
                        )
                    }
                }
                
                if (isOwner) {
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Chỉnh sửa") },
                                onClick = {
                                    menuExpanded = false
                                    onEditPost()
                                }
                            )
                            if (post.status == PostStatus.ARCHIVED) {
                                DropdownMenuItem(
                                    text = { Text("Bỏ lưu trữ") },
                                    onClick = {
                                        menuExpanded = false
                                        onChangeStatus(PostStatus.PUBLISHED)
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Lưu trữ") },
                                    onClick = {
                                        menuExpanded = false
                                        onChangeStatus(PostStatus.ARCHIVED)
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Xóa", color = Color(0xFFDC2626)) },
                                onClick = {
                                    menuExpanded = false
                                    onDeletePost()
                                }
                            )
                        }
                    }
                } else {
                    // Show report option for non-owners
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
                            DropdownMenuItem(
                                text = { Text("Báo cáo vi phạm") },
                                onClick = {
                                    menuExpanded = false
                                    showReportDialog = true
                                }
                            )
                        }
                    }
                    
                    if (showReportDialog) {
                        CreateReportDialog(
                            targetType = TargetType.POST,
                            targetId = post.id,
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
            }
            
            // Title
            Text(
                text = post.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MainText
            )
            
            // Content
            Text(
                text = post.content,
                fontSize = 14.sp,
                color = NavBarText,
                lineHeight = 20.sp
            )

            // Thumbnails
            if (post.thumbnails.isNotEmpty()) {
                val thumbnails = post.thumbnails
                if (thumbnails.size == 1) {
                    AsyncImage(
                        model = thumbnails.first(),
                        contentDescription = "Post image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = thumbnails[0],
                            contentDescription = "Post image",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = thumbnails.getOrNull(1),
                                contentDescription = "Post image",
                                modifier = Modifier.matchParentSize()
                            )
                            if (thumbnails.size > 2) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Color.Black.copy(alpha = 0.45f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${thumbnails.size - 2}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Topic and Tags
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                post.topic?.let { topic ->
                    Surface(
                        color = Color(0xFFDBEAFE),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = getTopicLabel(topic),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E40AF)
                        )
                    }
                }
                
                if (post.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        post.tags.forEach { tag ->
                            Surface(
                                color = Color(0xFFF3F4F6),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    color = NavBarText
                                )
                            }
                        }
                    }
                }
            }
            
            // Engagement Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onLikeClick)
                    ) {
                        if (isLiking) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Icon(
                                if (post.isAlreadyLike) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (post.isAlreadyLike) Color(0xFFEF4444) else NavBarText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = post.likeCount.toString(),
                            fontSize = 14.sp,
                            color = NavBarText
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Comment,
                            contentDescription = "Comment",
                            tint = NavBarText,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = post.commentCount.toString(),
                            fontSize = 14.sp,
                            color = NavBarText
                        )
                    }
                }
                
                IconButton(onClick = { /* Share */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = NavBarText)
                }
            }
        }
    }
}

@Composable
fun CommentThreadItem(
    thread: CommentThread,
    currentUserId: Int?,
    editingCommentId: Int?,
    editingCommentText: String,
    isUpdatingComment: Boolean,
    onToggleReplies: () -> Unit,
    onParentLike: () -> Unit,
    onReplyParent: () -> Unit,
    onReplyChild: (Comment) -> Unit,
    onChildLike: (Comment) -> Unit,
    onEditParent: () -> Unit,
    onEditChild: (Comment) -> Unit,
    onEditingTextChange: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val isParentEditable = thread.parent.createdBy?.id == currentUserId
        val isParentEditing = editingCommentId == thread.parent.id
        CommentCard(
            comment = thread.parent,
            isChild = false,
            isEditable = isParentEditable,
            isEditing = isParentEditing,
            editingText = if (isParentEditing) editingCommentText else "",
            isUpdating = isUpdatingComment && isParentEditing,
            onLikeClick = onParentLike,
            onReplyClick = onReplyParent,
            onEditClick = onEditParent,
            onEditingTextChange = onEditingTextChange,
            onSaveEdit = onSaveEdit,
            onCancelEdit = onCancelEdit
        )
        
        if (thread.replies.isNotEmpty()) {
            TextButton(
                onClick = onToggleReplies,
                modifier = Modifier.padding(start = 52.dp)
            ) {
                Text(
                    text = if (thread.areRepliesVisible) "Ẩn phản hồi" else "Xem tất cả ${thread.replies.size} phản hồi",
                    color = Color(0xFF2563EB)
                )
            }
        }
        
        if (thread.areRepliesVisible && thread.replies.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(start = 52.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                thread.replies.forEach { reply ->
                    val isReplyEditable = reply.createdBy?.id == currentUserId
                    val isReplyEditing = editingCommentId == reply.id
                    CommentCard(
                        comment = reply,
                        isChild = true,
                        isEditable = isReplyEditable,
                        isEditing = isReplyEditing,
                        editingText = if (isReplyEditing) editingCommentText else "",
                        isUpdating = isUpdatingComment && isReplyEditing,
                        onLikeClick = { onChildLike(reply) },
                        onReplyClick = { onReplyChild(reply) },
                        onEditClick = { onEditChild(reply) },
                        onEditingTextChange = onEditingTextChange,
                        onSaveEdit = onSaveEdit,
                        onCancelEdit = onCancelEdit
                    )
                }
            }
        }
    }
}

@Composable
fun CommentCard(
    comment: Comment,
    isChild: Boolean,
    isEditable: Boolean,
    isEditing: Boolean,
    editingText: String,
    isUpdating: Boolean,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onEditClick: () -> Unit,
    onEditingTextChange: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(if (isChild) 32.dp else 38.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = comment.createdBy?.username?.firstOrNull()?.uppercase() ?: "U",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = if (isChild) 14.sp else 16.sp
            )
        }
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = Color(0xFFF5F7FA),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = comment.createdBy?.username ?: "Unknown",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MainText
                    )
                    if (isEditing) {
                        BasicTextField(
                            value = editingText,
                            onValueChange = onEditingTextChange,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = MainText,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(MainText),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = formatCommentContent(comment.content),
                            fontSize = 14.sp,
                            color = NavBarText,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimeAgo(comment.createdAt),
                        fontSize = 12.sp,
                        color = NavBarText
                    )
                    Text(
                        text = "Phản hồi",
                        color = Color(0xFF2563EB),
                        fontSize = 13.sp,
                        modifier = Modifier.clickable(onClick = onReplyClick)
                    )
                    if (isEditable && !isEditing) {
                        Text(
                            text = "Chỉnh sửa",
                            color = Color(0xFF2563EB),
                            fontSize = 13.sp,
                            modifier = Modifier.clickable(onClick = onEditClick)
                        )
                    }
                    if (!isEditable && !isEditing) {
                        val reportScope = rememberCoroutineScope()
                        var showReportDialog by remember { mutableStateOf(false) }
                        Text(
                            text = "Báo cáo",
                            color = Color(0xFFEF4444),
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { showReportDialog = true }
                        )
                        if (showReportDialog) {
                            CreateReportDialog(
                                targetType = TargetType.COMMENT,
                                targetId = comment.id,
                                onDismiss = { showReportDialog = false },
                                onSuccess = { 
                                    showReportDialog = false
                                    reportScope.launch {
                                        androidx.compose.material3.SnackbarHostState().showSnackbar(
                                            message = "Đã báo cáo vi phạm thành công",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onLikeClick)
                ) {
                    Icon(
                        if (comment.isAlreadyLike) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (comment.isAlreadyLike) Color(0xFFEF4444) else NavBarText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = comment.likeCount.toString(),
                        fontSize = 13.sp,
                        color = NavBarText
                    )
                }
            }
            
            if (isEditing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onCancelEdit,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NavBarText)
                    ) {
                        Text("Hủy")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSaveEdit,
                        enabled = !isUpdating,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Lưu", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentInputSection(
    commentText: String,
    replyContext: ReplyContext?,
    onCommentTextChange: (String) -> Unit,
    onSubmitComment: () -> Unit,
    onCancelReply: () -> Unit,
    onCommentLimitExceeded: () -> Unit,
    isSubmitting: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .imePadding()
    ) {
        replyContext?.let {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đang trả lời ${it.targetUsername}",
                    fontWeight = FontWeight.SemiBold,
                    color = MainText,
                    fontSize = 13.sp
                )
                IconButton(
                    onClick = onCancelReply,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancel reply",
                        tint = NavBarText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF3F4F6))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = commentText,
                onValueChange = {
                    if (it.length <= COMMENT_MAX_LENGTH) {
                        onCommentTextChange(it)
                    } else {
                        onCommentLimitExceeded()
                    }
                },
                modifier = Modifier.weight(1f),
                cursorBrush = SolidColor(MainText),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = MainText,
                    fontSize = 14.sp
                ),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    autoCorrect = true,
                    keyboardType = KeyboardType.Text
                ),
                decorationBox = { innerTextField ->
                    if (commentText.isBlank()) {
                        Text(
                            text = if (replyContext != null) "Viết phản hồi..." else "Viết bình luận...",
                            color = NavBarText,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            )
            
            IconButton(
                onClick = onSubmitComment,
                enabled = commentText.isNotBlank() && !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Gửi bình luận",
                        tint = Color(0xFF2563EB)
                    )
                }
            }
        }
    }
}

private fun formatCommentContent(content: String) = buildAnnotatedString {
    val regex = Regex("^@(\\S+)\\s?(.*)")
    val match = regex.find(content)
    if (match != null) {
        val username = match.groupValues[1]
        val remaining = match.groupValues[2]
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MainText)) {
            append(username)
        }
        if (remaining.isNotBlank()) {
            append(" ")
            append(remaining)
        }
    } else {
        append(content)
    }
}

package com.example.lingora_fe.user.forum.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.lingora_fe.user.common.presentation.components.CreateReportDialog
import com.example.lingora_fe.user.forum.domain.model.Post
import com.example.lingora_fe.user.forum.domain.model.PostStatus
import com.example.lingora_fe.user.forum.domain.model.PostTopic
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun ForumScreen(
    navController: NavController,
    viewModel: ForumViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val shouldRefreshPostsState = savedStateHandle?.getStateFlow("shouldRefreshPosts", false)?.collectAsState()

    LaunchedEffect(state.searchInput, state.searchTags) {
        delay(500)
        viewModel.applySearchFilters()
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= state.posts.size - 3) {
                    viewModel.loadMore()
                }
            }
    }

    LaunchedEffect(shouldRefreshPostsState?.value) {
        if (shouldRefreshPostsState?.value == true) {
            viewModel.refresh()
            savedStateHandle["shouldRefreshPosts"] = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F9F4))
    ) {
        // Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TagSearchBar(
                text = state.searchInput,
                tags = state.searchTags,
                onTextChange = { viewModel.onSearchInputChange(it) },
                onRemoveTag = { viewModel.removeSearchTag(it) }
            )

            OwnerFilterSection(
                currentUserId = state.currentUserId,
                showMyPosts = state.showMyPosts,
                currentStatus = state.myPostsStatus,
                onShowAll = { viewModel.setShowMyPosts(false) },
                onShowMine = { viewModel.setShowMyPosts(true) },
                onStatusChange = { viewModel.setMyPostsStatus(it) }
            )
        }

        // Topic Filter
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TopicFilterChip(
                    label = "Tất cả",
                    isSelected = state.selectedTopic == null,
                    onClick = { viewModel.selectTopic(null) }
                )
                TopicFilterChip(
                    label = "Tổng hợp",
                    isSelected = state.selectedTopic == PostTopic.GENERAL,
                    onClick = { viewModel.selectTopic(PostTopic.GENERAL) }
                )
                TopicFilterChip(
                    label = "Ngữ pháp",
                    isSelected = state.selectedTopic == PostTopic.GRAMMAR,
                    onClick = { viewModel.selectTopic(PostTopic.GRAMMAR) }
                )
                TopicFilterChip(
                    label = "Từ vựng",
                    isSelected = state.selectedTopic == PostTopic.VOCABULARY,
                    onClick = { viewModel.selectTopic(PostTopic.VOCABULARY) }
                )
                TopicFilterChip(
                    label = "Speaking",
                    isSelected = state.selectedTopic == PostTopic.SPEAKING,
                    onClick = { viewModel.selectTopic(PostTopic.SPEAKING) }
                )
                TopicFilterChip(
                    label = "Listening",
                    isSelected = state.selectedTopic == PostTopic.LISTENING,
                    onClick = { viewModel.selectTopic(PostTopic.LISTENING) }
                )
                TopicFilterChip(
                    label = "Reading",
                    isSelected = state.selectedTopic == PostTopic.READING,
                    onClick = { viewModel.selectTopic(PostTopic.READING) }
                )
                TopicFilterChip(
                    label = "Writing",
                    isSelected = state.selectedTopic == PostTopic.WRITING,
                    onClick = { viewModel.selectTopic(PostTopic.WRITING) }
                )
            }
        }

        // Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                state.isLoading && state.posts.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GradientStart)
                    }
                }
                state.error != null && state.posts.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = state.error ?: "Có lỗi xảy ra",
                                color = NavBarText,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { viewModel.refresh() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GradientStart
                                )
                            ) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.posts) { post ->
                            PostCard(
                                post = post,
                                currentUserId = state.currentUserId,
                                isViewingArchived = state.showMyPosts && state.myPostsStatus == PostStatus.ARCHIVED,
                                onClick = { navController.navigate(Route.postDetail(post.id)) },
                                onLikeClick = { viewModel.togglePostLike(post.id) },
                                onEditPost = { navController.navigate(Route.editPost(post.id)) },
                                onChangeStatus = { status -> viewModel.updatePostStatus(post.id, status) },
                                onDeletePost = { viewModel.deletePost(post.id) }
                            )
                        }

                        if (state.isLoading && state.posts.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = GradientStart,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopicFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) GradientStart else Color(0xFFF1F5F9),
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.White else NavBarText,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun TagSearchBar(
    text: String,
    tags: List<String>,
    onTextChange: (String) -> Unit,
    onRemoveTag: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.5.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { tag ->
                        SearchTagChip(tag = tag, onRemove = { onRemoveTag(tag) })
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = NavBarText,
                    modifier = Modifier.size(20.dp)
                )
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFF1E293B),
                        fontSize = 14.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(GradientStart),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (text.isEmpty() && tags.isEmpty()) {
                            Text(
                                text = "Tìm kiếm bài viết... (gõ #tag)",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
    }
}

@Composable
fun OwnerFilterSection(
    currentUserId: Int?,
    showMyPosts: Boolean,
    currentStatus: PostStatus,
    onShowAll: () -> Unit,
    onShowMine: () -> Unit,
    onStatusChange: (PostStatus) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF1F5F9)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterButton(
                    text = "Khám phá",
                    isSelected = !showMyPosts,
                    onClick = onShowAll,
                    modifier = Modifier.weight(1f)
                )
                FilterButton(
                    text = "Của tôi",
                    isSelected = showMyPosts,
                    onClick = onShowMine,
                    enabled = currentUserId != null,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (showMyPosts) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip(
                    text = "Công khai",
                    isSelected = currentStatus == PostStatus.PUBLISHED,
                    onClick = { onStatusChange(PostStatus.PUBLISHED) }
                )
                StatusChip(
                    text = "Lưu trữ",
                    isSelected = currentStatus == PostStatus.ARCHIVED,
                    onClick = { onStatusChange(PostStatus.ARCHIVED) }
                )
            }
        }
    }
}

@Composable
fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color.White else Color.Transparent,
        modifier = modifier.height(36.dp),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                color = if (enabled) MainText else NavBarText,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) GradientStart else Color(0xFFF1F5F9),
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) Color.White else NavBarText,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun SearchTagChip(
    tag: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE0F2FE)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "#$tag",
                color = Color(0xFF0369A1),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove tag",
                tint = Color(0xFF0369A1),
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = onRemove)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostCard(
    post: Post,
    currentUserId: Int?,
    isViewingArchived: Boolean,
    onClick: () -> Unit,
    onLikeClick: () -> Unit,
    onEditPost: () -> Unit,
    onChangeStatus: (PostStatus) -> Unit,
    onDeletePost: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        var showImageViewer by remember { mutableStateOf(false) }
        var selectedImageIndex by remember { mutableStateOf(0) }
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
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
                            fontSize = 16.sp
                        )
                    }

                    Column {
                        Text(
                            text = post.createdBy?.username ?: "Unknown",
                            fontWeight = FontWeight.SemiBold,
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

                val isOwner = post.createdBy?.id == currentUserId
                var menuExpanded by remember { mutableStateOf(false) }
                var showReportDialog by remember { mutableStateOf(false) }
                
                // Always show menu (Edit/Delete for owner, Report for non-owner)
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF757575)
                        )
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
                                    onEditPost()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(if (isViewingArchived || post.status == PostStatus.ARCHIVED)
                                        "Bỏ lưu trữ" else "Lưu trữ")
                                },
                                onClick = {
                                    menuExpanded = false
                                    val targetStatus = if (isViewingArchived || post.status == PostStatus.ARCHIVED)
                                        PostStatus.PUBLISHED else PostStatus.ARCHIVED
                                    onChangeStatus(targetStatus)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Xóa", color = Color(0xFFEF4444)) },
                                onClick = {
                                    menuExpanded = false
                                    onDeletePost()
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
                
                // Report Dialog for non-owners
                if (showReportDialog) {
                    CreateReportDialog(
                        targetType = com.example.lingora_fe.admin.report.domain.model.TargetType.POST,
                        targetId = post.id,
                        onDismiss = { showReportDialog = false },
                        onSuccess = { 
                            showReportDialog = false
                            // Could show success message here
                        }
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = post.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MainText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )

                Text(
                    text = post.content,
                    fontSize = 14.sp,
                    color = NavBarText,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            }

            // Images
            if (post.thumbnails.isNotEmpty()) {
                ImageSection(
                    thumbnails = post.thumbnails,
                    onImageClick = { index ->
                        selectedImageIndex = index
                        showImageViewer = true
                    }
                )
            }
            
            // Fullscreen Image Viewer
            if (showImageViewer) {
                FullscreenImageViewer(
                    images = post.thumbnails,
                    initialIndex = selectedImageIndex,
                    onDismiss = { showImageViewer = false }
                )
            }

            // Tags & Topic
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                post.topic?.let { topic ->
                    Surface(
                        color = Color(0xFFE0F2FE),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = getTopicLabel(topic),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0369A1),
                            letterSpacing = 0.3.sp
                        )
                    }
                }

                if (post.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        post.tags.take(3).forEach { tag ->
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    color = NavBarText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        if (post.tags.size > 3) {
                            Text(
                                text = "+${post.tags.size - 3}",
                                fontSize = 11.sp,
                                color = NavBarText,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }

            // Engagement
            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EngagementButton(
                        icon = if (post.isAlreadyLike) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        count = post.likeCount,
                        isActive = post.isAlreadyLike,
                        onClick = onLikeClick
                    )

                    EngagementButton(
                        icon = Icons.Default.Comment,
                        count = post.commentCount,
                        isActive = false,
                        onClick = onClick
                    )
                }

//                IconButton(
//                    onClick = { /* Share */ },
//                    modifier = Modifier.size(36.dp)
//                ) {
//                    Icon(
//                        Icons.Default.Share,
//                        contentDescription = "Share",
//                        tint = NavBarText,
//                        modifier = Modifier.size(20.dp)
//                    )
//                }
            }
        }
    }
}

@Composable
fun ImageSection(
    thumbnails: List<String>,
    onImageClick: (Int) -> Unit = {}
) {
    if (thumbnails.size == 1) {
        AsyncImage(
            model = thumbnails.first(),
            contentDescription = "Post image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .clip(RoundedCornerShape(0.dp))
                .clickable { onImageClick(0) }
        )
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            AsyncImage(
                model = thumbnails[0],
                contentDescription = "Post image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onImageClick(0) }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onImageClick(1) }
            ) {
                AsyncImage(
                    model = thumbnails.getOrNull(1),
                    contentDescription = "Post image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                if (thumbnails.size > 2) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${thumbnails.size - 2}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EngagementButton(
    icon: ImageVector,
    count: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) Color(0xFFEF4444) else NavBarText,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = count.toString(),
            fontSize = 14.sp,
            color = NavBarText,
            fontWeight = FontWeight.Medium
        )
    }
}

fun getTopicLabel(topic: PostTopic): String {
    return when (topic) {
        PostTopic.GENERAL -> "Tổng hợp"
        PostTopic.VOCABULARY -> "Từ vựng"
        PostTopic.GRAMMAR -> "Ngữ pháp"
        PostTopic.LISTENING -> "Listening"
        PostTopic.SPEAKING -> "Speaking"
        PostTopic.READING -> "Reading"
        PostTopic.WRITING -> "Writing"
    }
}

fun formatTimeAgo(date: Date?): String {
    if (date == null) return ""

    val now = Date()
    val diff = now.time - date.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "$days ngày trước"
        hours > 0 -> "$hours giờ trước"
        minutes > 0 -> "$minutes phút trước"
        else -> "Vừa xong"
    }
}
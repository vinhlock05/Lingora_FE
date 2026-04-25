package com.example.lingora_fe.user.classroom.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.user.classroom.presentation.components.ClassroomColors
import com.example.lingora_fe.user.classroom.util.ClassroomMemberStatus
import com.example.lingora_fe.user.classroom.util.ClassroomStatus
import com.example.lingora_fe.navigation.Route
import com.example.lingora_fe.user.classroom.domain.model.Classroom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomListScreen(
    navController: NavController,
    viewModel: ClassroomListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val joinSuccessClassroomId by viewModel.joinSuccessEvent.collectAsState()

    LaunchedEffect(joinSuccessClassroomId) {
        joinSuccessClassroomId?.let { id ->
            navController.navigate(Route.classroomDetail(id.toString()))
            viewModel.clearJoinSuccessEvent()
        }
    }

    // Keep the `savedStateHandle` bridge alive for now so deep-links from the
    // old top-bar shortcut (if any cached backstacks exist) still work.
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentBackStackEntry) {
        val entry = currentBackStackEntry
        entry?.savedStateHandle?.get<Boolean>("showJoinDialog")?.let { show ->
            if (show) {
                viewModel.showJoinDialog()
                entry.savedStateHandle.remove<Boolean>("showJoinDialog")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClassroomColors.ScreenBackground)
    ) {
        // Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ClassroomColors.HeaderSurface)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search bar + compact "Tham gia bằng mã" action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = ClassroomColors.NeutralSurface,
                    border = BorderStroke(1.5.dp, ClassroomColors.NeutralBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
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
                            value = state.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = ClassroomColors.TextPrimary,
                                fontSize = 14.sp
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(ClassroomColors.BrandPrimary),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (state.searchQuery.isEmpty()) {
                                    Text(
                                        text = "Tìm kiếm lớp học...",
                                        color = ClassroomColors.TextPlaceholder,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    viewModel.onSearchQueryChange("")
                                    viewModel.applySearch()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Xóa",
                                    tint = NavBarText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Compact brand-outlined "Tham gia bằng mã" trigger. Keeps the
                // action discoverable without squeezing the top-bar title.
                JoinByCodeButton(
                    onClick = { viewModel.showJoinDialog() }
                )
            }

            // Tab Filters
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
                        isSelected = state.selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterButton(
                        text = "Của tôi",
                        isSelected = state.selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Status Filter Chips (Only for "My Classrooms")
            if (state.selectedTab == 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("Tất cả", "Đã tạo", "Đã tham gia")
                    filters.forEachIndexed { index, title ->
                        FilterChip(
                            selected = state.selectedStatusFilter == index,
                            onClick = { viewModel.onStatusFilterChange(index) },
                            label = { Text(title, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ClassroomColors.BrandPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = ClassroomColors.CardSurface,
                                labelColor = ClassroomColors.TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = state.selectedStatusFilter == index,
                                borderColor = ClassroomColors.NeutralBorder,
                                selectedBorderColor = ClassroomColors.BrandPrimary
                            )
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = ClassroomColors.BrandPrimary
                    )
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = state.error ?: "Đã xảy ra lỗi",
                            color = ClassroomColors.Danger,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = ClassroomColors.BrandPrimaryStrong
                            )
                        ) {
                            Text("Thử lại")
                        }
                    }
                }
                state.classrooms.isEmpty() -> {
                    Text(
                        text = "Không tìm thấy lớp học nào",
                        modifier = Modifier.align(Alignment.Center),
                        color = ClassroomColors.TextMuted
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.classrooms, key = { it.id }) { classroom ->
                            ClassroomItemCard(
                                classroom = classroom,
                                currentUserId = state.currentUserId,
                                onClick = {
                                    val isTeacher = classroom.teacher?.id == state.currentUserId
                                    if (state.selectedTab == 0 && !isTeacher) {
                                        when (classroom.myStatus) {
                                            ClassroomMemberStatus.ACTIVE -> {
                                                navController.navigate(Route.classroomDetail(classroom.id.toString()))
                                            }
                                            ClassroomMemberStatus.PENDING -> {
                                                viewModel.onEvent(ClassroomListEvent.ShowToast("Yêu cầu tham gia của bạn đang chờ duyệt"))
                                            }
                                            else -> {
                                                viewModel.promptJoinPublicClass(classroom)
                                            }
                                        }
                                    } else {
                                        navController.navigate(Route.classroomDetail(classroom.id.toString()))
                                    }
                                },
                                onEdit = {
                                    navController.navigate(Route.createClassroomWithId(classroom.id.toString()))
                                },
                                onArchive = {
                                    viewModel.archiveClassroom(classroom.id)
                                },
                                onDelete = { viewModel.deleteClassroom(classroom.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showJoinDialog) {
        JoinClassroomDialog(
            joinCode = state.joinCode,
            onJoinCodeChange = { viewModel.onJoinCodeChange(it) },
            joinError = state.joinError,
            isJoining = state.isJoining,
            onConfirm = { viewModel.joinByCode() },
            onDismiss = { viewModel.dismissJoinDialog() }
        )
    }

    val publicClass = state.publicClassToJoin
    if (publicClass != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelJoinPublicClass() },
            title = {
                Text(
                    text = "Tham gia lớp học",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Bạn có chắc chắn muốn tham gia lớp học \"${publicClass.name}\" không?")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.joinPublicClass() },
                    modifier = Modifier.padding(start = 8.dp),
                    enabled = !state.isJoining,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ClassroomColors.BrandPrimary,
                        contentColor = Color.White
                    )
                ) {
                    if (state.isJoining) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Tham gia")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.cancelJoinPublicClass() },
                    enabled = !state.isJoining,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = ClassroomColors.TextSecondary
                    )
                ) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun JoinByCodeButton(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = ClassroomColors.BrandSoftSurface,
        border = BorderStroke(1.dp, ClassroomColors.BrandPrimary),
        modifier = Modifier.height(46.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Login,
                contentDescription = null,
                tint = ClassroomColors.BrandPrimaryStrong,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Tham gia",
                color = ClassroomColors.BrandPrimaryStrong,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ClassroomItemCard(
    classroom: Classroom,
    currentUserId: Int?,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isTeacher = currentUserId != null && classroom.teacher?.id == currentUserId

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ClassroomColors.CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(ClassroomColors.CoverPlaceholder),
                contentAlignment = Alignment.Center
            ) {
                if (classroom.coverImageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(classroom.coverImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Ảnh bìa lớp học",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(text = "Ảnh bìa", color = ClassroomColors.TextMuted)
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = classroom.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MainText,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (classroom.isPublic)
                            ClassroomColors.PublicChipBackground
                        else
                            ClassroomColors.PrivateChipBackground,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (classroom.isPublic) "Công khai" else "Riêng tư",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (classroom.isPublic)
                                ClassroomColors.PublicChipText
                            else
                                ClassroomColors.PrivateChipText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Box {
                        IconButton(
                            onClick = { expanded = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Tùy chọn",
                                tint = ClassroomColors.TextMuted
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            if (isTeacher) {
                                DropdownMenuItem(
                                    text = { Text("Chỉnh sửa") },
                                    onClick = {
                                        expanded = false
                                        onEdit()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Lưu trữ") },
                                    onClick = {
                                        expanded = false
                                        onArchive()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Xóa", color = ClassroomColors.Danger) },
                                    onClick = {
                                        expanded = false
                                        onDelete()
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Báo cáo") },
                                    onClick = { expanded = false }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Giáo viên: ${classroom.teacher?.username ?: "Ẩn danh"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClassroomColors.TextSecondary
                    )
                    Text(
                        text = "Mã: ${classroom.code}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClassroomColors.TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${classroom.totalMembers} / ${classroom.maxStudents} học viên",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClassroomColors.TextMuted
                    )
                    Text(
                        text = when (classroom.status) {
                            ClassroomStatus.ACTIVE -> "Đang mở"
                            ClassroomStatus.ARCHIVED -> "Đã lưu trữ"
                            ClassroomStatus.DRAFT -> "Bản nháp"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (classroom.status == ClassroomStatus.ACTIVE)
                            ClassroomColors.ActiveStatusText
                        else
                            ClassroomColors.TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) ClassroomColors.CardSurface else Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) MainText else NavBarText,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun JoinClassroomDialog(
    joinCode: String,
    onJoinCodeChange: (String) -> Unit,
    joinError: String?,
    isJoining: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tham gia lớp học") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = joinCode,
                    onValueChange = onJoinCodeChange,
                    label = { Text("Mã lớp học") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isJoining,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ClassroomColors.BrandPrimary,
                        focusedLabelColor = ClassroomColors.BrandPrimaryStrong,
                        cursorColor = ClassroomColors.BrandPrimary
                    )
                )
                if (joinError != null) {
                    Text(
                        text = joinError,
                        color = ClassroomColors.Danger,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isJoining,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClassroomColors.BrandPrimary,
                    contentColor = Color.White
                )
            ) {
                if (isJoining) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                }
                Text("Tham gia")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isJoining,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ClassroomColors.TextSecondary
                )
            ) {
                Text("Hủy")
            }
        }
    )
}

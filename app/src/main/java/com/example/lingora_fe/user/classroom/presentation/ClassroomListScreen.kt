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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Close
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
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F9F4))
    ) {
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

        // Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.5.dp, Color(0xFFE2E8F0))
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
                            color = Color(0xFF1E293B),
                            fontSize = 14.sp
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(GradientStart),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (state.searchQuery.isEmpty()) {
                                Text(
                                    text = "Tìm kiếm lớp học...",
                                    color = Color(0xFF94A3B8),
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
                                contentDescription = "Clear",
                                tint = NavBarText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
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
                                selectedContainerColor = Color(0xFF5CB85C),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = MainText
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
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = state.error ?: "Đã xảy ra lỗi",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = { viewModel.refresh() }) {
                            Text("Thử lại")
                        }
                    }
                }
                state.classrooms.isEmpty() -> {
                    Text(
                        text = "Không tìm thấy lớp học nào",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
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
                                        // Tab "Của tôi" or tab "Khám phá" but is Teacher
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

    // Join Classroom Dialog
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
                    enabled = !state.isJoining
                ) {
                    if (state.isJoining) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Tham gia")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelJoinPublicClass() }, enabled = !state.isJoining) {
                    Text("Hủy")
                }
            }
        )
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFFE0E0E0)),
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
                    Text(text = "Ảnh bìa", color = Color.Gray)
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
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (classroom.isPublic) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (classroom.isPublic) "Công khai" else "Riêng tư",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (classroom.isPublic) Color(0xFF2E7D32) else Color(0xFFE65100)
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
                                tint = Color.Gray
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
                                    text = { Text("Xóa", color = Color.Red) },
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
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Mã: ${classroom.code}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
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
                        color = Color.Gray
                    )
                    Text(
                        text = when (classroom.status) {
                            ClassroomStatus.ACTIVE -> "Đang mở"
                            ClassroomStatus.ARCHIVED -> "Đã lưu trữ"
                            ClassroomStatus.DRAFT -> "Bản nháp"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (classroom.status == ClassroomStatus.ACTIVE)
                            Color(0xFF5CB85C)
                        else
                            Color.Gray
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
        color = if (isSelected) Color.White else Color.Transparent,
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
                    singleLine = true
                )
                if (joinError != null) {
                    Text(
                        text = joinError,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isJoining
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
                enabled = !isJoining
            ) {
                Text("Hủy")
            }
        }
    )
}

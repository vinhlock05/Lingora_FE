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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.navigation.Route
import com.example.lingora_fe.user.classroom.domain.model.Classroom
import com.example.lingora_fe.user.classroom.util.ClassroomStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomListScreen(
    navController: NavController,
    viewModel: ClassroomListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F9F4))
    ) {
        // Header: Search + Tabs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                        TextButton(
                            onClick = {
                                viewModel.onSearchQueryChange("")
                                viewModel.applySearch()
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Xóa", fontSize = 12.sp, color = NavBarText)
                        }
                    }
                }
            }

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
                                onClick = {
                                    navController.navigate(
                                        Route.classroomDetail(classroom.id.toString())
                                    )
                                },
                                onDelete = { viewModel.deleteClassroom(classroom.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassroomItemCard(
    classroom: Classroom,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

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
                Text(text = "Ảnh bìa", color = Color.Gray)
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
                            DropdownMenuItem(
                                text = { Text("Xóa", color = Color.Red) },
                                onClick = {
                                    expanded = false
                                    onDelete()
                                }
                            )
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
                            MaterialTheme.colorScheme.primary
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
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color.White else Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
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

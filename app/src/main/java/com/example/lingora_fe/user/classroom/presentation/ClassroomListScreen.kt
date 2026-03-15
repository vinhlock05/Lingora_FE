package com.example.lingora_fe.user.classroom.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import androidx.navigation.NavController
import com.example.lingora_fe.navigation.Route
import com.example.lingora_fe.user.classroom.domain.model.Classroom

val mockClassrooms = listOf(
    Classroom(
        id = "1",
        name = "Luyện thi IELTS Target 7.0+",
        code = "IELTS70",
        creatorName = "Nguyễn Văn A",
        studentCount = 25,
        maxStudents = 30,
        description = "Lớp học chuyên sâu luyện kỹ năng Speaking & Writing.",
        status = "ACTIVE",
        isPublic = true
    ),
    Classroom(
        id = "2",
        name = "Tiếng Anh Giao Tiếp Cơ Bản",
        code = "COMM101",
        creatorName = "Trần B",
        studentCount = 120,
        maxStudents = 150,
        description = "Dành cho người mới bắt đầu muốn tự tin giao tiếp.",
        status = "ACTIVE",
        isPublic = true
    ),
    Classroom(
        id = "3",
        name = "Ngữ pháp Căn Bản",
        code = "GRAMMAR1",
        creatorName = "Lê C",
        studentCount = 45,
        maxStudents = 50,
        description = "Hệ thống lại toàn bộ điểm ngữ pháp quan trọng.",
        status = "CLOSED",
        isPublic = false
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomListScreen(
    navController: NavController
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Khám phá, 1: Của tôi

    val filteredClassrooms = remember(searchQuery, selectedTab, mockClassrooms) {
        mockClassrooms.filter {
            val matchesTab = if (selectedTab == 0) it.isPublic else true // Nếu là "Của tôi", show hết của tôi mong muốn. Tạm thời show hết
            val matchesSearch = it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true)
            matchesTab && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F9F4))
    ) {
        // Header Section for Search and Tabs
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
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFF1E293B),
                                fontSize = 14.sp
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(GradientStart),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Tìm kiếm lớp học...",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                // Filter Tabs
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
                            isSelected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.weight(1f)
                        )
                        FilterButton(
                            text = "Của tôi",
                            isSelected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredClassrooms) { classroom ->
                        ClassroomItemCard(
                            classroom = classroom,
                            onClick = { navController.navigate(Route.classroomDetail(classroom.id)) }
                        )
                    }
                }
            }
        }
}

@Composable
fun ClassroomItemCard(
    classroom: Classroom,
    onClick: () -> Unit
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
            // Mock Cover Image Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Cover Image", color = Color.Gray)
            }
            
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
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
                        IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Tùy chọn", tint = Color.Gray)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sửa") },
                                onClick = { expanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Xóa", color = Color.Red) },
                                onClick = { expanded = false }
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
                        text = "Người tạo: ${classroom.creatorName}",
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
                        text = "${classroom.studentCount} / ${classroom.maxStudents} học viên",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = if (classroom.status == "ACTIVE") "Đang mở" else "Đã đóng",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (classroom.status == "ACTIVE") MaterialTheme.colorScheme.primary else Color.Red
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
    androidx.compose.material3.Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        color = if (isSelected) Color.White else Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) com.example.lingora_fe.core.ui.theme.MainText else com.example.lingora_fe.core.ui.theme.NavBarText,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

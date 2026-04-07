package com.example.lingora_fe.user.classroom.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.lingora_fe.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClassroomScreen(
    navController: NavController,
    viewModel: CreateClassroomViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.onCoverImageUriChange(it.toString()) }
    }

    LaunchedEffect(state.isSuccess, state.createdClassroomId) {
        if (state.isSuccess && state.createdClassroomId != null) {
            navController.navigate(Route.classroomDetail(state.createdClassroomId.toString())) {
                val routeToPop = if (state.isEditMode) Route.EditClassroom.route else Route.CreateClassroom.route
                popUpTo(routeToPop) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Chỉnh sửa lớp học" else "Tạo lớp học mới") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image Picker Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8F5E9))
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (state.coverImageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(state.coverImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Preview cover image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit photo",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Add cover photo",
                            tint = Color(0xFF5CB85C),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Thêm ảnh bìa (tùy chọn)",
                            color = Color(0xFF5CB85C),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Thông tin cơ bản", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Tên lớp học *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.error != null && state.name.isBlank()
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Mô tả lớp học") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4
            )

            OutlinedTextField(
                value = state.maxStudents?.toString() ?: "",
                onValueChange = { viewModel.onMaxStudentsChange(it) },
                label = { Text("Số học viên tối đa") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Status Dropdown
            var statusExpanded by remember { mutableStateOf(false) }
            val statusOptions = com.example.lingora_fe.user.classroom.util.ClassroomStatus.entries
            
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = when(state.status) {
                        com.example.lingora_fe.user.classroom.util.ClassroomStatus.ACTIVE -> "Đang hoạt động"
                        com.example.lingora_fe.user.classroom.util.ClassroomStatus.DRAFT -> "Bản nháp"
                        com.example.lingora_fe.user.classroom.util.ClassroomStatus.ARCHIVED -> "Lưu trữ"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Trạng thái") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    statusOptions.forEach { statusOption ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when(statusOption) {
                                        com.example.lingora_fe.user.classroom.util.ClassroomStatus.ACTIVE -> "Đang hoạt động"
                                        com.example.lingora_fe.user.classroom.util.ClassroomStatus.DRAFT -> "Bản nháp"
                                        com.example.lingora_fe.user.classroom.util.ClassroomStatus.ARCHIVED -> "Lưu trữ"
                                    }
                                )
                            },
                            onClick = {
                                viewModel.onStatusChange(statusOption)
                                statusExpanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Công khai lớp học")
                Switch(
                    checked = state.isPublic,
                    onCheckedChange = { viewModel.onIsPublicChange(it) }
                )
            }

            state.error?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.submit(context) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5CB85C))
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (state.isEditMode) "Cập nhật Lớp học" else "Tạo Lớp Học")
                }
            }
        }
    }
}

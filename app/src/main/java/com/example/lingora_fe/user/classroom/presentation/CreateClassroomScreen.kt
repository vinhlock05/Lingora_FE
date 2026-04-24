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
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.navigation.Route
import com.example.lingora_fe.user.classroom.presentation.components.ClassroomColors

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
        if (state.isSuccess) {
            if (state.isEditMode) {
                // Return to detail screen
                navController.popBackStack()
            } else if (state.createdClassroomId != null) {
                // Navigate to detail of newly created classroom
                navController.navigate(Route.classroomDetail(state.createdClassroomId.toString())) {
                    popUpTo(Route.CreateClassroom.route) { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        containerColor = ClassroomColors.ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditMode) "Chỉnh sửa lớp học" else "Tạo lớp học mới",
                        fontWeight = FontWeight.SemiBold,
                        color = MainText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MainText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
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
                    .background(ClassroomColors.BrandSoftSurface)
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
                            tint = ClassroomColors.BrandPrimaryStrong,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Thêm ảnh bìa (tùy chọn)",
                            color = ClassroomColors.BrandPrimaryStrong,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Thông tin cơ bản", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MainText)

            val brandFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ClassroomColors.BrandPrimary,
                focusedLabelColor = ClassroomColors.BrandPrimaryStrong,
                cursorColor = ClassroomColors.BrandPrimary
            )

            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Tên lớp học *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.error != null && state.name.isBlank(),
                colors = brandFieldColors
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Mô tả lớp học") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4,
                colors = brandFieldColors
            )

            OutlinedTextField(
                value = state.maxStudents?.toString() ?: "",
                onValueChange = { viewModel.onMaxStudentsChange(it) },
                label = { Text("Số học viên tối đa") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = brandFieldColors
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
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = brandFieldColors
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
                Text(text = "Công khai lớp học", color = MainText)
                Switch(
                    checked = state.isPublic,
                    onCheckedChange = { viewModel.onIsPublicChange(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ClassroomColors.BrandPrimary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = ClassroomColors.NeutralBorder
                    )
                )
            }

            state.error?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = ClassroomColors.Danger,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.submit(context) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClassroomColors.BrandPrimary,
                    contentColor = Color.White
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (state.isEditMode) "Cập nhật Lớp học" else "Tạo Lớp Học",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

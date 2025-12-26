package com.example.lingora_fe.user.profile.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.util.FileUploadHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel
) {
    val context = LocalContext.current
    val profileState by viewModel.profileState.collectAsState()
    val editState by viewModel.editProfileState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Form state
    var username by remember { mutableStateOf(profileState.user?.username ?: "") }
    var email by remember { mutableStateOf(profileState.user?.email ?: "") }
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    
    // Update form when profile loads
    LaunchedEffect(profileState.user) {
        profileState.user?.let {
            username = it.username
            email = it.email
        }
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedAvatarUri = uri
    }
    
    // Validation
    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val hasChanges = username != profileState.user?.username || 
                     email != profileState.user?.email || 
                     selectedAvatarUri != null
    val canSave = hasChanges && username.isNotBlank() && email.isNotBlank() && isEmailValid
    
    // Show success dialog
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(editState.updateSuccess) {
        if (editState.updateSuccess) {
            showSuccessDialog = true
        }
    }
    
    // Clear state when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearEditProfileState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chỉnh sửa thông tin",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ảnh đại diện",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MainText,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Avatar with camera overlay
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clickable { imagePickerLauncher.launch("image/*") }
                        ) {
                            // Avatar image
                            if (selectedAvatarUri != null) {
                                // Show local preview
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(selectedAvatarUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Avatar preview",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (!profileState.user?.avatar.isNullOrBlank() && profileState.user?.avatar != "N/A") {
                                // Show current avatar from URL
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(profileState.user?.avatar)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Current avatar",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Default avatar with initial letter
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(GradientStart, GradientEnd)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = profileState.user?.username?.first()?.uppercase() ?: "U",
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            
                            // Camera icon overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GradientStart),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change avatar",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = "Nhấn để thay đổi ảnh",
                            fontSize = 13.sp,
                            color = NavBarText,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
                // Form Fields
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Username field
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Tên người dùng") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = GradientStart
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GradientStart,
                                focusedLabelColor = GradientStart
                            ),
                            singleLine = true
                        )
                        
                        // Email field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = GradientStart
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GradientStart,
                                focusedLabelColor = GradientStart,
                                errorBorderColor = Color.Red
                            ),
                            isError = email.isNotBlank() && !isEmailValid,
                            supportingText = if (email.isNotBlank() && !isEmailValid) {
                                { Text("Email không hợp lệ", color = Color.Red) }
                            } else null,
                            singleLine = true
                        )
                    }
                }
                
                // Error message
                editState.error?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFF991B1B),
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Save button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            var avatarUrl: String? = null
                            
                            // Upload avatar if selected
                            if (selectedAvatarUri != null) {
                                viewModel.setUploadingAvatar(true)
                                FileUploadHelper.uploadImage(
                                    context = context,
                                    imageUri = selectedAvatarUri!!,
                                    folder = "lingora/avatars"
                                ).fold(
                                    ifLeft = { failure ->
                                        viewModel.setUploadingAvatar(false)
                                        // Error is handled by state
                                    },
                                    ifRight = { url ->
                                        avatarUrl = url
                                        viewModel.setUploadingAvatar(false)
                                    }
                                )
                            }
                            
                            // Only proceed if avatar upload succeeded or no avatar was selected
                            if (selectedAvatarUri == null || avatarUrl != null) {
                                viewModel.updateProfile(
                                    username = if (username != profileState.user?.username) username else null,
                                    email = if (email != profileState.user?.email) email else null,
                                    avatarUrl = avatarUrl,
                                    onSuccess = {
                                        // Success dialog will be shown via LaunchedEffect
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = canSave && !editState.isUpdating && !editState.isUploadingAvatar,
                    colors = ButtonDefaults.buttonColors(containerColor = GradientStart),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (editState.isUpdating || editState.isUploadingAvatar) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (editState.isUploadingAvatar) "Đang tải ảnh..." else "Đang lưu...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    } else {
                        Text(
                            text = "Lưu thay đổi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
    
    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onBackClick()
            },
            title = {
                Text(
                    "Thành công",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Thông tin đã được cập nhật thành công!")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GradientStart),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

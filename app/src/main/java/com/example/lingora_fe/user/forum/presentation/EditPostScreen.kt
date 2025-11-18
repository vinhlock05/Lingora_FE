package com.example.lingora_fe.user.forum.presentation

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.util.Constant
import com.example.lingora_fe.util.FileUploadHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostScreen(
    navController: NavController,
    viewModel: EditPostViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val parentEntry = navController.previousBackStackEntry
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                viewModel.setUploading(true)
                val sharedPrefs = context.getSharedPreferences("lingora_prefs", Context.MODE_PRIVATE)
                val token = sharedPrefs.getString("access_token", null)
                if (token != null) {
                    FileUploadHelper.uploadImage(context, it, token, Constant.BASE_URL)
                        .fold(
                            ifLeft = { error ->
                                snackbarHostState.showSnackbar(
                                    message = error.message ?: "Không thể tải ảnh",
                                    duration = SnackbarDuration.Short
                                )
                            },
                            ifRight = { url ->
                                viewModel.addThumbnail(url)
                            }
                        )
                } else {
                    snackbarHostState.showSnackbar(
                        message = "Vui lòng đăng nhập lại",
                        duration = SnackbarDuration.Short
                    )
                }
                viewModel.setUploading(false)
            }
        }
    }
    
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            parentEntry?.savedStateHandle?.set("shouldReloadPostDetail", true)
            parentEntry?.savedStateHandle?.set("shouldRefreshPosts", true)
            navController.popBackStack()
            viewModel.consumeSuccess()
        }
    }
    
    if (state.isLoadingData) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chỉnh sửa bài viết",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MainText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.submitEdit() },
                        enabled = !state.isSaving && state.title.isNotBlank() && state.content.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Save",
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (state.isSaving) "Đang lưu..." else "Lưu thay đổi",
                                color = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9FAFB))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Thông tin bài viết",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MainText
                    )
                    
                    Column {
                        Text(
                            text = "Tiêu đề bài viết",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MainText,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = state.title,
                            onValueChange = viewModel::updateTitle,
                            placeholder = { Text("Nhập tiêu đề bài viết...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Text(
                            text = "${state.titleCharCount}/150 ký tự",
                            fontSize = 12.sp,
                            color = NavBarText,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Chủ đề",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MainText,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        TopicDropdown(
                            selectedTopic = state.selectedTopic,
                            onTopicSelected = viewModel::selectTopic
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Nội dung",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MainText,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = state.content,
                            onValueChange = viewModel::updateContent,
                            placeholder = { Text("Chia sẻ câu hỏi, kinh nghiệm, hoặc thảo luận...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            maxLines = Int.MAX_VALUE
                        )
                        Text(
                            text = "${state.contentCharCount}/2000 ký tự",
                            fontSize = 12.sp,
                            color = NavBarText,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "# Tags (Tối đa 5)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MainText
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var tagInput by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = tagInput,
                            onValueChange = { tagInput = it },
                            placeholder = { Text("Thêm tag...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (tagInput.isNotBlank()) {
                                    viewModel.addTag(tagInput)
                                    tagInput = ""
                                }
                            },
                            enabled = state.tags.size < 5 && tagInput.isNotBlank()
                        ) {
                            Text("Thêm")
                        }
                    }
                    
                    if (state.tags.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.tags) { tag ->
                                Surface(
                                    color = Color(0xFFF3F4F6),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("#$tag", color = NavBarText)
                                        IconButton(
                                            onClick = { viewModel.removeTag(tag) },
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Text(
                        text = "Tags phổ biến:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MainText
                    )
                    
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(viewModel.getPopularTags()) { tag ->
                            val isSelected = state.tags.contains(tag)
                            PopularTagChip(
                                tag = tag,
                                onClick = {
                                    if (isSelected) {
                                        viewModel.removeTag(tag)
                                    } else if (state.tags.size < 5) {
                                        viewModel.addTag(tag)
                                    }
                                },
                                isSelected = isSelected
                            )
                        }
                    }
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Hình ảnh",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MainText
                    )
                    
                    if (state.thumbnails.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.thumbnails.size) { index ->
                                ThumbnailItem(
                                    imageUrl = state.thumbnails[index],
                                    onRemove = { viewModel.removeThumbnail(index) }
                                )
                            }
                        }
                    }
                    
                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        enabled = !state.isUploading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Thêm hình ảnh")
                        }
                    }
                }
            }
        }
    }
}

package com.example.lingora_fe.user.forum.presentation

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.user.forum.domain.model.PostTopic
import com.example.lingora_fe.util.Constant
import com.example.lingora_fe.util.FileUploadHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    viewModel: CreatePostViewModel = hiltViewModel(),
    onPostCreated: (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUris = selectedImageUris + it
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.reset()
    }
    
    LaunchedEffect(state.error) {
        state.error?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Long
                )
            }
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.consumeSuccess()
            onPostCreated?.invoke() ?: navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tạo bài viết mới",
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
                        onClick = {
                            scope.launch {
                                if (state.isLoading || isUploading) return@launch
                                isUploading = true
                                val uploadedUrls = mutableListOf<String>()
                                
                                for (uri in selectedImageUris) {
                                    FileUploadHelper.uploadImage(context, uri)
                                        .fold(
                                            ifLeft = { error ->
                                                snackbarHostState.showSnackbar(
                                                    message = error.message ?: "Upload failed",
                                                    duration = SnackbarDuration.Short
                                                )
                                            },
                                            ifRight = { imageUrl ->
                                                uploadedUrls.add(imageUrl)
                                            }
                                        )
                                }
                                
                                viewModel.setThumbnails(uploadedUrls)
                                viewModel.createPost()
                                isUploading = false
                            }
                        },
                        enabled = !state.isLoading && !isUploading && state.title.isNotEmpty() && state.content.isNotEmpty() && state.selectedTopic != null,
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
                                contentDescription = "Post",
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Đăng bài", color = Color.White)
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
            // Post Information Section
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
                    
                    // Title Input
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
                            onValueChange = { viewModel.updateTitle(it) },
                            placeholder = { Text("Nhập tiêu đề bài viết...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            maxLines = 1
                        )
                        Text(
                            text = "${state.titleCharCount}/150 ký tự",
                            fontSize = 12.sp,
                            color = NavBarText,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // Topic Selection
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
                            onTopicSelected = { viewModel.selectTopic(it) }
                        )
                    }
                    
                    // Content Input
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
                            onValueChange = { viewModel.updateContent(it) },
                            placeholder = { Text("Chia sẻ câu hỏi, kinh nghiệm, hoặc thảo luận...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            maxLines = 10
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
            
            // Tags Section
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
                    
                    // Add Tag Input
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
                    
                    // Selected Tags
                    if (state.tags.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(state.tags) { tag ->
                                TagChip(
                                    tag = tag,
                                    onRemove = { viewModel.removeTag(tag) }
                                )
                            }
                        }
                    }
                    
                    // Popular Tags
                    Text(
                        text = "Tags phổ biến:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MainText
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.getPopularTags()) { tag ->
                            val isSelected = tag in state.tags
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
            
            // Thumbnails Section
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
                    
                    if (selectedImageUris.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(selectedImageUris.size) { index ->
                                ThumbnailItem(
                                    imageUrl = selectedImageUris[index].toString(),
                                    onRemove = {
                                        selectedImageUris = selectedImageUris.filterIndexed { i, _ -> i != index }
                                    }
                                )
                            }
                        }
                    }
                    
                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        enabled = !isUploading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Image")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Thêm hình ảnh")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDropdown(
    selectedTopic: PostTopic?,
    onTopicSelected: (PostTopic?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val topics = listOf(
        null to "Chọn chủ đề...",
        PostTopic.GRAMMAR to "Ngữ pháp",
        PostTopic.VOCABULARY to "Từ vựng",
        PostTopic.SPEAKING to "Speaking",
        PostTopic.LISTENING to "Listening",
        PostTopic.READING to "Reading",
        PostTopic.WRITING to "Writing",
        PostTopic.GENERAL to "Tổng hợp"
    )
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedTopic?.let { getTopicLabel(it) } ?: "Chọn chủ đề...",
            onValueChange = { },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            topics.forEach { (topic, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onTopicSelected(topic)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TagChip(
    tag: String,
    onRemove: () -> Unit
) {
    Surface(
        color = Color(0xFFF3F4F6),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$tag",
                fontSize = 14.sp,
                color = NavBarText
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(14.dp),
                    tint = NavBarText
                )
            }
        }
    }
}

@Composable
fun PopularTagChip(
    tag: String,
    onClick: () -> Unit,
    isSelected: Boolean
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text("#$tag") },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF3B82F6),
            containerColor = Color(0xFFF3F4F6),
            selectedLabelColor = Color.White,
            labelColor = NavBarText
        )
    )
}

@Composable
fun ThumbnailItem(
    imageUrl: String,
    onRemove: () -> Unit
) {
    Box {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Thumbnail",
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .background(Color.Red, CircleShape)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}


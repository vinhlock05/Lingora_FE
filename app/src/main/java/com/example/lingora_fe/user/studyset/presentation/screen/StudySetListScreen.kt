package com.example.lingora_fe.user.studyset.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.admin.common.presentation.components.SearchBar
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.navigation.Route
import com.example.lingora_fe.user.studyset.domain.model.StudySet
import com.example.lingora_fe.user.studyset.presentation.components.StudySetCard
import com.example.lingora_fe.user.studyset.presentation.viewmodel.StudySetListViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StudySetListScreen(
    onStudySetClick: (Int) -> Unit,
    onCreateClick: () -> Unit,
    navController: NavController? = null,
    viewModel: StudySetListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var studySetPendingDelete by remember { mutableStateOf<StudySet?>(null) }
    
    // Refresh when coming back from create/edit screen
    val savedStateHandle = navController?.currentBackStackEntry?.savedStateHandle
    
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow<Boolean>("refreshStudySetList", false)?.collect { shouldRefresh ->
            if (shouldRefresh) {
                viewModel.refresh()
                savedStateHandle.set("refreshStudySetList", false)
            }
        }
    }

    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow<String?>("studySetMessage", null)?.collect { message ->
            if (!message.isNullOrBlank()) {
                snackbarHostState.showSnackbar(message)
                savedStateHandle.set("studySetMessage", null)
            }
        }
    }

    // Load more when scrolling near the end
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= uiState.studySets.size - 3) {
                    viewModel.loadNextPage()
                }
            }
    }

    // Debounce search
    var searchQuery by remember { mutableStateOf(uiState.searchQuery) }
    LaunchedEffect(searchQuery) {
        delay(500)
        if (searchQuery != uiState.searchQuery) {
            viewModel.searchStudySets(searchQuery)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GradientStart.copy(alpha = 0.06f),
                        GradientEnd.copy(alpha = 0.02f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabButton(
                text = "Kho học liệu",
                isSelected = uiState.selectedTab == com.example.lingora_fe.user.studyset.presentation.StudySetTab.STORE,
                onClick = { viewModel.switchTab(com.example.lingora_fe.user.studyset.presentation.StudySetTab.STORE) },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                text = "Của tôi",
                isSelected = uiState.selectedTab == com.example.lingora_fe.user.studyset.presentation.StudySetTab.MINE,
                onClick = { viewModel.switchTab(com.example.lingora_fe.user.studyset.presentation.StudySetTab.MINE) },
                modifier = Modifier.weight(1f)
            )
        }

        // Create Button (only for "Của tôi" tab)
        if (uiState.selectedTab == com.example.lingora_fe.user.studyset.presentation.StudySetTab.MINE) {
            Button(
                onClick = onCreateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GradientStart
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tạo học liệu mới",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = if (uiState.selectedTab == com.example.lingora_fe.user.studyset.presentation.StudySetTab.STORE) {
                    "Tìm kiếm học liệu..."
                } else {
                    "Tìm kiếm học liệu của tôi..."
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Content
        when {
            uiState.isLoading && uiState.studySets.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.studySets.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.studySets) { studySet ->
                        if (uiState.selectedTab == com.example.lingora_fe.user.studyset.presentation.StudySetTab.MINE) {
                            com.example.lingora_fe.user.studyset.presentation.components.MyStudySetCard(
                                studySet = studySet,
                                onClick = {
                                    onStudySetClick(studySet.id)
                                },
                                onLikeClick = {
                                    viewModel.toggleLike(studySet.id)
                                },
                                onEditClick = {
                                    navController?.navigate(Route.studySetEdit(studySet.id))
                                },
                                onDeleteClick = {
                                    studySetPendingDelete = studySet
                                }
                            )
                        } else {
                            StudySetCard(
                                currentUserId = viewModel.currentUserId,
                                studySet = studySet,
                                onClick = {
                                    onStudySetClick(studySet.id)
                                },
                                onLikeClick = {
                                    viewModel.toggleLike(studySet.id)
                                },
                                onEditClick = {
                                    navController?.navigate(Route.studySetEdit(studySet.id))
                                },
                                onDeleteClick = {
                                    studySetPendingDelete = studySet
                                }
                            )
                        }
                    }
                }
            }
        }

        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }

    if (studySetPendingDelete != null) {
        val target = studySetPendingDelete!!
        val isDeleting = uiState.deletingStudySetId == target.id
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) {
                    studySetPendingDelete = null
                }
            },
            title = { Text("Xóa học liệu") },
            text = { Text("Bạn có chắc chắn muốn xóa học liệu \"${target.title}\"? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteStudySet(target.id) {
                            val deletedTitle = target.title
                            studySetPendingDelete = null
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Đã xóa học liệu \"$deletedTitle\"")
                            }
                        }
                    },
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Xóa")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { studySetPendingDelete = null },
                    enabled = !isDeleting
                ) {
                    Text("Hủy")
                }
            }
        )
    }
    
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) GradientStart else Color.White,
        border = if (isSelected) null else BorderStroke(1.5.dp, GradientStart.copy(alpha = 0.3f)),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else GradientStart
            )
        }
    }
}


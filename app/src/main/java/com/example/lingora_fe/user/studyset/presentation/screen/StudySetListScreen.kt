package com.example.lingora_fe.user.studyset.presentation.screen

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.admin.common.presentation.components.SearchBar
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.user.studyset.presentation.components.StudySetCard
import com.example.lingora_fe.user.studyset.presentation.viewmodel.StudySetListViewModel
import kotlinx.coroutines.delay

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

    Column(
        modifier = modifier.fillMaxSize()
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

        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = if (uiState.selectedTab == com.example.lingora_fe.user.studyset.presentation.StudySetTab.STORE) {
                "Tìm kiếm học liệu..."
            } else {
                "Tìm kiếm học liệu của tôi..."
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

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
                            // Use MyStudySetCard for "Của tôi" tab
                            com.example.lingora_fe.user.studyset.presentation.components.MyStudySetCard(
                                studySet = studySet,
                                onClick = {
                                    // For own study sets, navigate directly without access check
                                    onStudySetClick(studySet.id)
                                }
                            )
                        } else {
                            // Use StudySetCard for "Kho học liệu" tab
                            StudySetCard(
                                studySet = studySet,
                                onClick = {
                                    // Check access first
                                    viewModel.checkAccessAndNavigate(studySet.id) { studySetId ->
                                        onStudySetClick(studySetId)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Purchase Modal
        if (uiState.showPurchaseModal && uiState.purchaseStudySet != null) {
            com.example.lingora_fe.user.studyset.presentation.components.PurchaseModal(
                studySet = uiState.purchaseStudySet,
                isLoading = uiState.isPurchasing || uiState.isCheckingAccess,
                error = uiState.purchaseError,
                onDismiss = { viewModel.hidePurchaseModal() },
                onPurchase = {
                    viewModel.buyStudySet(uiState.purchaseStudySet!!.id) { paymentUrl ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Handle error opening browser
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(
                color = if (isSelected) GradientStart else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else MainText
        )
    }
}


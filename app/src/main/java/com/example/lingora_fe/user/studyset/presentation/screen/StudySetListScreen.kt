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
import androidx.activity.compose.rememberLauncherForActivityResult
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
    
    // Payment verification dialog states
    var showVerifyingDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var verificationSuccess by remember { mutableStateOf(false) }
    var verificationMessage by remember { mutableStateOf("") }
    
    // Activity launcher for payment
    val paymentLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            android.app.Activity.RESULT_OK -> {
                val needVerification = result.data?.getBooleanExtra("needVerification", false) ?: false
                
                if (needVerification) {
                    // Extract all VNPay params
                    val vnpParams = mutableMapOf<String, String>()
                    result.data?.extras?.keySet()?.forEach { key ->
                        if (key.startsWith("vnp_")) {
                            result.data?.getStringExtra(key)?.let { value ->
                                vnpParams[key] = value
                            }
                        }
                    }
                    
                    val responseCode = vnpParams["vnp_ResponseCode"]
                    
                    // Show loading dialog
                    showVerifyingDialog = true
                    
                    // Call backend to verify payment
                    viewModel.verifyPayment(vnpParams) { success, message ->
                        // Hide loading dialog
                        showVerifyingDialog = false
                        
                        // Show result dialog
                        verificationSuccess = success
                        verificationMessage = message
                        showResultDialog = true
                    }
                } else {
                    // Fallback for old flow (shouldn't happen)
                    val isSuccess = result.data?.getBooleanExtra("isSuccess", false) ?: false
                    val responseCode = result.data?.getStringExtra("vnp_ResponseCode")
                    
                    verificationSuccess = isSuccess && responseCode == "00"
                    verificationMessage = if (verificationSuccess) {
                        "Thanh toán thành công!"
                    } else {
                        "Thanh toán thất bại (Mã lỗi: $responseCode)"
                    }
                    showResultDialog = true
                }
            }
            android.app.Activity.RESULT_CANCELED -> {
                verificationSuccess = false
                verificationMessage = "Đã hủy thanh toán"
                showResultDialog = true
            }
        }
    }
    
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
        modifier = modifier.fillMaxSize()
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
                            // Use StudySetCard for "Kho học liệu" tab
                            StudySetCard(
                                currentUserId = viewModel.currentUserId,
                                studySet = studySet,
                                onClick = {
                                    // Check access first
                                    viewModel.checkAccessAndNavigate(studySet.id) { studySetId ->
                                        onStudySetClick(studySetId)
                                    }
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

    // Purchase Modal
    if (uiState.showPurchaseModal && uiState.purchaseStudySet != null) {
        com.example.lingora_fe.user.studyset.presentation.components.PurchaseModal(
            studySet = uiState.purchaseStudySet,
            isLoading = uiState.isPurchasing || uiState.isCheckingAccess,
            error = uiState.purchaseError,
            onDismiss = { viewModel.hidePurchaseModal() },
            onPurchase = {
                viewModel.buyStudySet(uiState.purchaseStudySet!!.id) { paymentUrl ->
                    val intent = Intent(context, com.example.lingora_fe.user.studyset.presentation.PaymentWebViewActivity::class.java).apply {
                        putExtra(com.example.lingora_fe.user.studyset.presentation.PaymentWebViewActivity.EXTRA_PAYMENT_URL, paymentUrl)
                        putExtra(com.example.lingora_fe.user.studyset.presentation.PaymentWebViewActivity.EXTRA_STUDY_SET_ID, uiState.purchaseStudySet!!.id)
                    }
                    paymentLauncher.launch(intent)
                }
            }
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
    
    // Verifying payment dialog (loading)
    if (showVerifyingDialog) {
        AlertDialog(
            onDismissRequest = { }, // Cannot dismiss while verifying
            title = {
                Text("Đang xác thực thanh toán")
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Vui lòng đợi...")
                }
            },
            confirmButton = { }
        )
    }
    
    // Payment result dialog
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = {
                showResultDialog = false
                if (verificationSuccess) {
                    viewModel.refresh()
                }
            },
            icon = {
                Text(
                    text = if (verificationSuccess) "✅" else "❌",
                    style = MaterialTheme.typography.displayMedium
                )
            },
            title = {
                Text(
                    text = if (verificationSuccess) "Thành công!" else "Thất bại",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = verificationMessage,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResultDialog = false
                        if (verificationSuccess) {
                            viewModel.refresh()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (verificationSuccess) GradientStart else MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("OK")
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


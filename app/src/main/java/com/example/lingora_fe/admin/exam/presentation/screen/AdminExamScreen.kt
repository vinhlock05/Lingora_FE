package com.example.lingora_fe.admin.exam.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.admin.common.presentation.components.*
import com.example.lingora_fe.admin.exam.domain.model.AdminExam
import com.example.lingora_fe.admin.exam.domain.model.AdminExamAttempt
import com.example.lingora_fe.admin.exam.presentation.AdminExamState
import com.example.lingora_fe.admin.exam.presentation.AdminExamViewModel
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.NavBarText
import kotlinx.coroutines.launch
import java.util.Locale

private enum class AdminExamTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    EXAMS("Exams", Icons.Default.Description),
    MONITORING("Monitoring", Icons.Default.History)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminExamScreen(
    viewModel: AdminExamViewModel = hiltViewModel(),
    onNavigateToExamDetail: (Int) -> Unit = {},
    onNavigateToAttemptDetail: (Int) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { AdminExamTab.entries.size })
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                contentColor = GradientStart
            ) {
                AdminExamTab.entries.forEachIndexed { index, tab ->
                     Tab(
                         selected = pagerState.currentPage == index,
                         onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                         text = { Text(tab.title) },
                         icon = { Icon(tab.icon, contentDescription = null) }
                     )
                }
            }
            
            LaunchedEffect(pagerState.currentPage) {
                when (AdminExamTab.entries[pagerState.currentPage]) {
                    AdminExamTab.EXAMS -> viewModel.loadExams()
                    AdminExamTab.MONITORING -> viewModel.loadAttempts()
                }
            }
            
            HorizontalPager(
                state = pagerState, 
                modifier = Modifier.weight(1f)
            ) { page ->
                when(AdminExamTab.entries[page]) {
                    AdminExamTab.EXAMS -> ExamListTab(
                        state = state,
                        onSearch = viewModel::onExamSearchQueryChange,
                        onFilterChange = viewModel::onFilterPublishedChange,
                        onImport = viewModel::importExam,
                        getTemplateContent = viewModel::getExamTemplateJson,
                        onPageChange = { viewModel.loadExams(page = it) },
                        onToggleStatus = viewModel::toggleExamStatus,
                        onDelete = viewModel::deleteExam,
                        onNavigateToDetail = onNavigateToExamDetail
                    )
                    AdminExamTab.MONITORING -> {
                        AttemptListTab(
                            state = state,
                            onSearch = viewModel::onAttemptSearchQueryChange,
                            onPageChange = { viewModel.loadAttempts(page = it) },
                            onNavigateToDetail = onNavigateToAttemptDetail
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExamListTab(
    state: AdminExamState,
    onSearch: (String) -> Unit,
    onFilterChange: (Boolean?) -> Unit,
    onImport: (String) -> Unit,
    getTemplateContent: () -> String,
    onPageChange: (Int) -> Unit,
    onToggleStatus: (Int, Boolean) -> Unit,
    onDelete: (Int) -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    var examIdToDelete by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val content = context.contentResolver.openInputStream(it)?.use { stream ->
                    stream.reader().readText()
                }
                content?.let(onImport)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val saveTemplateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
             coroutineScope.launch {
                 try {
                     context.contentResolver.openOutputStream(it)?.use { stream ->
                         stream.write(getTemplateContent().toByteArray())
                     }
                 } catch (e: Exception) {
                     e.printStackTrace()
                 }
             }
        }
    }

    var isImportMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Search Bar (Debounced)
            OutlinedTextField(
                value = state.examSearchQuery,
                onValueChange = onSearch,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search exams...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.filterPublished == null,
                        onClick = { onFilterChange(null) },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = state.filterPublished == true,
                        onClick = { onFilterChange(true) },
                        label = { Text("Published") }
                    )
                    FilterChip(
                        selected = state.filterPublished == false,
                        onClick = { onFilterChange(false) },
                        label = { Text("Draft") }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if(state.isLoading && state.exams.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if(state.exams.isEmpty()) {
                EmptyContent(message = "No exams found")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.exams) { exam ->
                        ExamItem(
                            exam = exam, 
                            onToggleStatus = onToggleStatus, 
                            onDelete = { examIdToDelete = exam.id },
                            onClick = { onNavigateToDetail(exam.id) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                PaginationControls(
                    currentPage = state.examCurrentPage,
                    totalPages = state.examTotalPages,
                    onPageChange = onPageChange
                )
            }
        }

        // FAB with Menu
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { isImportMenuExpanded = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Exam", tint = Color.White)
            }

            DropdownMenu(
                expanded = isImportMenuExpanded,
                onDismissRequest = { isImportMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Import JSON") },
                    onClick = {
                        isImportMenuExpanded = false
                        importLauncher.launch("application/json")
                    },
                    leadingIcon = { Icon(Icons.Outlined.Upload, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Download Template") },
                    onClick = {
                        isImportMenuExpanded = false
                        saveTemplateLauncher.launch("exam_import_template.json")
                    },
                    leadingIcon = { Icon(Icons.Outlined.Download, contentDescription = null) }
                )
            }
        }
    }

    
    if (examIdToDelete != null) {
        AlertDialog(
            onDismissRequest = { examIdToDelete = null },
            title = { Text("Delete Exam") },
            text = { Text("Are you sure you want to delete this exam? This will also delete all related attempts.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        examIdToDelete?.let(onDelete)
                        examIdToDelete = null
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { examIdToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AttemptListTab(
    state: AdminExamState,
    onSearch: (String) -> Unit,
    onPageChange: (Int) -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Search (Debounced)
        OutlinedTextField(
            value = state.attemptSearchQuery,
            onValueChange = onSearch,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search user or exam...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if(state.isLoading && state.attempts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if(state.attempts.isEmpty()) {
             EmptyContent(message = "No attempts found")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.attempts) { attempt ->
                    AttemptItem(attempt = attempt, onClick = { onNavigateToDetail(attempt.id) })
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PaginationControls(
                currentPage = state.attemptCurrentPage,
                totalPages = state.attemptTotalPages,
                onPageChange = onPageChange
            )
        }
    }
}

@Composable
fun ExamItem(
    exam: AdminExam,
    onToggleStatus: (Int, Boolean) -> Unit,
    onDelete: (Int) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ===== Header =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exam.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Code: ${exam.code} • ${exam.type}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NavBarText
                    )
                }

                StatusBadge(isPublished = exam.isPublished)
            }

            // ===== Actions =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onToggleStatus(exam.id, exam.isPublished) }
                ) {
                    Text(if (exam.isPublished) "Unpublish" else "Publish")
                }

                TextButton(
                    onClick = { onDelete(exam.id) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFDC2626)
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }

}

@Composable
fun AttemptItem(
    attempt: AdminExamAttempt,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
             // Avatar placeholder
             Box(
                 modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray.copy(alpha=0.2f)),
                 contentAlignment = Alignment.Center
             ) {
                 Text(
                     text = attempt.username.take(1).uppercase(),
                     fontWeight = FontWeight.Bold
                 )
             }
             
             Spacer(modifier = Modifier.width(12.dp))
             
             Column(modifier = Modifier.weight(1f)) {
                 Text(text = attempt.username, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                 Text(text = attempt.examTitle, style = MaterialTheme.typography.bodySmall, color = NavBarText, maxLines = 1, overflow = TextOverflow.Ellipsis)
             }
             
             Column(horizontalAlignment = Alignment.End) {
                 /* 
                 // Score hidden as requested
                 Text(
                     text = "${String.format(Locale.US, "%.1f", attempt.score)}/100",
                     style = MaterialTheme.typography.titleMedium,
                     fontWeight = FontWeight.Bold,
                     color = if(attempt.score >= 50) Color(0xFF16A34A) else Color(0xFFDC2626)
                 ) 
                 */
                 attempt.submittedAt?.let {
                      Text(text = it.take(10), style = MaterialTheme.typography.bodySmall, color = NavBarText)
                 }
             }
        }
    }
}
@Composable
fun StatusBadge(isPublished: Boolean) {
    val bgColor = if (isPublished) Color(0xFFDCFCE7) else Color(0xFFF3F4F6)
    val textColor = if (isPublished) Color(0xFF16A34A) else Color(0xFF6B7280)

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bgColor
    ) {
        Text(
            text = if (isPublished) "Published" else "Draft",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

package com.example.lingora_fe.admin.user.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.admin.user.domain.model.AdminUser
import com.example.lingora_fe.admin.user.domain.model.SortOption
import com.example.lingora_fe.core.domain.model.ProficiencyLevel
import com.example.lingora_fe.admin.user.domain.model.UserStatus
import com.example.lingora_fe.admin.user.presentation.UserManagementEvent
import com.example.lingora_fe.admin.user.presentation.UserManagementViewModel
import com.example.lingora_fe.admin.common.presentation.components.*
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    onNavigateToCreateUser: () -> Unit,
    onNavigateToEditUser: (Int) -> Unit,
    onNavigateToUserDetails: (Int) -> Unit,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showBanDialog by remember { mutableStateOf<Int?>(null) }
    var showSuspendDialog by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.onEvent(UserManagementEvent.LoadUsers())
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    )
                    .clickable(onClick = onNavigateToCreateUser),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    "Create User",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) {
        val bottomPadding = it.calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

            // Search Bar and Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.onEvent(UserManagementEvent.SearchUsers(it)) },
                    placeholder = "Search by username or email...",
                    modifier = Modifier.weight(1f)
                )
                
                // Filter Button
                FilterButton(
                    onClick = { showFilterDialog = true },
                    hasActiveFilters = state.selectedProficiency != null || state.selectedStatus != null
                )
                
                // Sort Button
                SortButton(
                    onClick = { showSortMenu = true },
                    hasActiveSort = state.selectedSort != null
                )
            }

            // Active Filters and Sort
            if (state.selectedProficiency != null || state.selectedStatus != null || state.selectedSort != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.selectedProficiency?.let { proficiency ->
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.onEvent(UserManagementEvent.FilterByProficiency(null)) },
                            label = { 
                                Text(
                                    proficiency.value,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
                                ) 
                            },
                            trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GradientStart.copy(alpha = 0.2f),
                                selectedLabelColor = GradientStart
                            )
                        )
                    }
                    state.selectedStatus?.let { status ->
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.onEvent(UserManagementEvent.FilterByStatus(null)) },
                            label = { 
                                Text(
                                    status.value,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
                                ) 
                            },
                            trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GradientStart.copy(alpha = 0.2f),
                                selectedLabelColor = GradientStart
                            )
                        )
                    }
                    state.selectedSort?.let { sort ->
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.onEvent(UserManagementEvent.SortBy(null)) },
                            label = { 
                                Text(
                                    "Sort: ${sort.displayName}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
                                ) 
                            },
                            trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GradientStart.copy(alpha = 0.2f),
                                selectedLabelColor = GradientStart
                            )
                        )
                    }
                }
            }

            // User List
            when {
                state.isLoading && state.users.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null && state.users.isEmpty() -> {
                    ErrorContent(
                        message = state.error!!,
                        onRetry = { viewModel.onEvent(UserManagementEvent.RefreshUsers) }
                    )
                }
                state.users.isEmpty() -> {
                    EmptyContent(message = "No users found", icon = Icons.Default.PersonOff)
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.users, key = { it.id }) { user ->
                                UserCard(
                                    user = user,
                                    onClick = { onNavigateToUserDetails(user.id) },
                                    onEdit = { onNavigateToEditUser(user.id) },
                                    onDelete = { showDeleteDialog = user.id },
                                    onRestore = { viewModel.onEvent(UserManagementEvent.RestoreUser(user.id)) },
                                    onBan = { showBanDialog = user.id },
                                    onSuspend = { showSuspendDialog = user.id },
                                    onUnban = { viewModel.onEvent(UserManagementEvent.UnbanUser(user.id)) }
                                )
                            }

                            // Pagination
                            if (state.totalPages > 1) {
                                item {
                                    PaginationControls(
                                        currentPage = state.currentPage,
                                        totalPages = state.totalPages,
                                        onPageChange = { page ->
                                            viewModel.onEvent(UserManagementEvent.LoadUsers(page))
                                        }
                                    )
                                }
                            }
                        }

                        // Loading Overlay
                        if (state.isLoading && state.users.isNotEmpty()) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showFilterDialog) {
        FilterDialog(
            selectedProficiency = state.selectedProficiency,
            selectedStatus = state.selectedStatus,
            onDismiss = { showFilterDialog = false },
            onApply = { proficiency, status ->
                viewModel.onEvent(UserManagementEvent.FilterByProficiency(proficiency))
                viewModel.onEvent(UserManagementEvent.FilterByStatus(status))
                showFilterDialog = false
            },
            onClear = {
                viewModel.onEvent(UserManagementEvent.ClearFilters)
                showFilterDialog = false
            }
        )
    }

    if (showSortMenu) {
        SortDialog(
            selectedSort = state.selectedSort,
            onDismiss = { showSortMenu = false },
            onSelectSort = { sort ->
                viewModel.onEvent(UserManagementEvent.SortBy(sort))
                showSortMenu = false
            }
        )
    }

    showDeleteDialog?.let { userId ->
        DeleteConfirmDialog(
            onConfirm = {
                viewModel.onEvent(UserManagementEvent.DeleteUser(userId))
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }

    // Snackbar Messages
    LaunchedEffect(state.actionSuccess, state.actionError) {
        state.actionSuccess?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.onEvent(UserManagementEvent.ClearActionMessages)
        }
        state.actionError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
            viewModel.onEvent(UserManagementEvent.ClearActionMessages)
        }
    }

    // Ban Dialog
    showBanDialog?.let { userId ->
        BanUserDialog(
            onConfirm = { reason ->
                viewModel.onEvent(UserManagementEvent.BanUser(userId, reason))
                showBanDialog = null
            },
            onDismiss = { showBanDialog = null }
        )
    }

    // Suspend Dialog
    showSuspendDialog?.let { userId ->
        SuspendUserDialog(
            onConfirm = { reason, days ->
                viewModel.onEvent(UserManagementEvent.SuspendUser(userId, reason, days))
                showSuspendDialog = null
            },
            onDismiss = { showSuspendDialog = null }
        )
    }
    }
}

@Composable
fun UserCard(
    user: AdminUser,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRestore: () -> Unit,
    onBan: () -> Unit = {},
    onSuspend: () -> Unit = {},
    onUnban: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar - show user avatar or fallback to initials
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(GradientStart, GradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!user.avatar.isNullOrBlank() && user.avatar != "N/A") {
                        AsyncImage(
                            model = user.avatar,
                            contentDescription = "User Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = user.username.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // User Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = MainText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                        color = NavBarText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        StatusChip(status = user.status)
                        user.proficiency?.let { proficiency ->
                            ProficiencyChip(proficiency = proficiency)
                        }
                    }
                    // Roles
                    Text(
                        text = user.roles.joinToString(", ") { it.name },
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = GradientStart,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
            
            // 3-dot Menu Button
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = NavBarText,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    when (user.status) {
                        "DELETED" -> {
                            DropdownMenuItem(
                                text = { Text("Restore") },
                                onClick = {
                                    showMenu = false
                                    onRestore()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Restore,
                                        contentDescription = null,
                                        tint = GradientStart
                                    )
                                }
                            )
                        }
                        "BANNED", "SUSPENDED" -> {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = GradientStart
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Unban") },
                                onClick = {
                                    showMenu = false
                                    onUnban()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = Color(0xFF22c55e)
                                    )
                                }
                            )
                        }
                        else -> {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = GradientStart
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Suspend") },
                                onClick = {
                                    showMenu = false
                                    onSuspend()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color(0xFFf97316)
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Ban") },
                                onClick = {
                                    showMenu = false
                                    onBan()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Block,
                                        contentDescription = null,
                                        tint = Color(0xFFef4444)
                                    )
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Delete",
                                        color = MaterialTheme.colorScheme.error
                                    ) 
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "ACTIVE" -> GradientStart to GradientStart
        "INACTIVE" -> Color(0xFFeab308) to Color(0xFFeab308)
        "SUSPENDED" -> Color(0xFFf97316) to Color(0xFFf97316)
        "BANNED" -> Color(0xFFef4444) to Color(0xFFef4444)
        "DELETED" -> Color(0xFF6b7280) to Color(0xFF6b7280)
        else -> Color.Gray to Color.Gray
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor.copy(alpha = 0.15f)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ProficiencyChip(proficiency: String) {
    val (backgroundColor, textColor) = when (proficiency) {
        "BEGINNER" -> Color(0xFF2196F3) to Color(0xFF2196F3)
        "INTERMEDIATE" -> Color(0xFFFF9800) to Color(0xFFFF9800)
        "ADVANCED" -> GradientEnd to GradientEnd
        else -> Color.Gray to Color.Gray
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor.copy(alpha = 0.15f)
    ) {
        Text(
            text = proficiency,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Composable
fun FilterDialog(
    selectedProficiency: ProficiencyLevel?,
    selectedStatus: UserStatus?,
    onDismiss: () -> Unit,
    onApply: (ProficiencyLevel?, UserStatus?) -> Unit,
    onClear: () -> Unit
) {
    var tempProficiency by remember { mutableStateOf(selectedProficiency) }
    var tempStatus by remember { mutableStateOf(selectedStatus) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Users") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Proficiency Filter
                Text("Proficiency Level", style = MaterialTheme.typography.titleSmall)
                ProficiencyLevel.values().forEach { level ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempProficiency = if (tempProficiency == level) null else level }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tempProficiency == level,
                            onClick = { tempProficiency = if (tempProficiency == level) null else level },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = GradientStart
                            )
                        )
                        Text(level.value, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Divider()

                // Status Filter
                Text("Status", style = MaterialTheme.typography.titleSmall)
                UserStatus.values().forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempStatus = if (tempStatus == status) null else status }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tempStatus == status,
                            onClick = { tempStatus = if (tempStatus == status) null else status },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = GradientStart
                            )
                        )
                        Text(status.value, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onApply(tempProficiency, tempStatus) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = GradientStart
                )
            ) {
                Text("Apply", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClear) {
                    Text("Clear All")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, "Warning", tint = MaterialTheme.colorScheme.error) },
        title = { Text("Delete User") },
        text = { Text("Are you sure you want to delete this user? This action can be reversed later.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun SortDialog(
    selectedSort: SortOption?,
    onDismiss: () -> Unit,
    onSelectSort: (SortOption?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort Users") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortOption.values().forEach { sortOption ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSort(sortOption) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSort == sortOption,
                            onClick = { onSelectSort(sortOption) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = GradientStart
                            )
                        )
                        Text(
                            text = sortOption.displayName,
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = GradientStart
                )
            ) {
                Text("Done", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            if (selectedSort != null) {
                TextButton(onClick = { onSelectSort(null) }) {
                    Text("Clear")
                }
            }
        }
    )
}

@Composable
fun BanUserDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { 
            Icon(
                Icons.Default.Block, 
                "Ban", 
                tint = Color(0xFFef4444),
                modifier = Modifier.size(32.dp)
            ) 
        },
        title = { 
            Text(
                "Permanently Ban User",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFef4444)
            ) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "User will not be able to login until unbanned by Admin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NavBarText
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Ban Reason *") },
                    placeholder = { Text("Enter the reason for banning...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GradientStart,
                        focusedLabelColor = GradientStart
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason) },
                enabled = reason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFef4444)
                )
            ) {
                Text("Confirm Ban", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SuspendUserDialog(
    onConfirm: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(7) }
    var customDays by remember { mutableStateOf("") }
    var useCustomDays by remember { mutableStateOf(false) }
    
    val durationOptions = listOf(7, 14, 30)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { 
            Icon(
                Icons.Default.Schedule, 
                "Suspend", 
                tint = Color(0xFFf97316),
                modifier = Modifier.size(32.dp)
            ) 
        },
        title = { 
            Text(
                "Temporarily Suspend User",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFf97316)
            ) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "User will not be able to login during the suspension period.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NavBarText
                )
                
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Suspend Reason *") },
                    placeholder = { Text("Enter the reason for suspension...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GradientStart,
                        focusedLabelColor = GradientStart
                    )
                )
                
                Text(
                    "Suspension Duration *",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                // Duration options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    durationOptions.forEach { days ->
                        FilterChip(
                            selected = !useCustomDays && selectedDays == days,
                            onClick = { 
                                selectedDays = days
                                useCustomDays = false
                            },
                            label = { Text("$days days") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFf97316).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFFf97316)
                            )
                        )
                    }
                }
                
                // Custom days option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadioButton(
                        selected = useCustomDays,
                        onClick = { useCustomDays = true },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFFf97316)
                        )
                    )
                    Text("Custom:")
                    OutlinedTextField(
                        value = customDays,
                        onValueChange = { 
                            customDays = it.filter { c -> c.isDigit() }
                            useCustomDays = true
                        },
                        modifier = Modifier.width(80.dp),
                        placeholder = { Text("...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFf97316),
                            focusedLabelColor = Color(0xFFf97316)
                        )
                    )
                    Text("days")
                }
            }
        },
        confirmButton = {
            val finalDays = if (useCustomDays) customDays.toIntOrNull() ?: 0 else selectedDays
            Button(
                onClick = { onConfirm(reason, finalDays) },
                enabled = reason.isNotBlank() && finalDays > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFf97316)
                )
            ) {
                Text("Confirm", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


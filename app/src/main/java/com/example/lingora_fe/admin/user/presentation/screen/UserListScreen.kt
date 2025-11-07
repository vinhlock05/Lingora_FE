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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.admin.user.domain.model.AdminUser
import com.example.lingora_fe.admin.user.domain.model.ProficiencyLevel
import com.example.lingora_fe.admin.user.domain.model.SortOption
import com.example.lingora_fe.admin.user.domain.model.UserStatus
import com.example.lingora_fe.admin.user.presentation.UserManagementEvent
import com.example.lingora_fe.admin.user.presentation.UserManagementViewModel
import com.example.lingora_fe.admin.common.presentation.components.*

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

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateUser,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Create User")
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
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.selectedProficiency?.let { proficiency ->
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.onEvent(UserManagementEvent.FilterByProficiency(null)) },
                            label = { Text(proficiency.value) },
                            trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) }
                        )
                    }
                    state.selectedStatus?.let { status ->
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.onEvent(UserManagementEvent.FilterByStatus(null)) },
                            label = { Text(status.value) },
                            trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) }
                        )
                    }
                    state.selectedSort?.let { sort ->
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.onEvent(UserManagementEvent.SortBy(null)) },
                            label = { Text("Sort: ${sort.displayName}") },
                            trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) }
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
                                    onRestore = { viewModel.onEvent(UserManagementEvent.RestoreUser(user.id)) }
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
    }
}

@Composable
fun UserCard(
    user: AdminUser,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRestore: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.username.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                // User Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        StatusChip(status = user.status)
                        ProficiencyChip(proficiency = user.proficiency)
                    }
                    // Roles
                    Text(
                        text = user.roles.joinToString(", ") { it.name },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (user.status != "DELETED") {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                } else {
                    IconButton(onClick = onRestore) {
                        Icon(Icons.Default.Restore, "Restore", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status) {
        "ACTIVE" -> Color(0xFF4CAF50)
        "INACTIVE" -> Color(0xFFFF9800)
        "BANNED" -> Color(0xFFF44336)
        "DELETED" -> Color(0xFF9E9E9E)
        else -> Color.Gray
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProficiencyChip(proficiency: String) {
    val color = when (proficiency) {
        "BEGINNER" -> Color(0xFF2196F3)
        "INTERMEDIATE" -> Color(0xFFFF9800)
        "ADVANCED" -> Color(0xFF9C27B0)
        else -> Color.Gray
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = proficiency,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
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
                            onClick = { tempProficiency = if (tempProficiency == level) null else level }
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
                            onClick = { tempStatus = if (tempStatus == status) null else status }
                        )
                        Text(status.value, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(tempProficiency, tempStatus) }) {
                Text("Apply")
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
                            onClick = { onSelectSort(sortOption) }
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
            TextButton(onClick = onDismiss) {
                Text("Done")
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


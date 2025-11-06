package com.example.lingora_fe.admin.navigator

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.lingora_fe.admin.category.presentation.screen.CategoryListScreen
import com.example.lingora_fe.admin.category.presentation.screen.CreateEditCategoryScreen
import com.example.lingora_fe.admin.navigator.components.AdminDrawerContent
import com.example.lingora_fe.admin.navigator.components.AdminTopBar
import com.example.lingora_fe.admin.presentation.screen.placeholder.*
import com.example.lingora_fe.admin.topic.presentation.screen.CreateEditTopicScreen
import com.example.lingora_fe.admin.topic.presentation.screen.TopicListScreen
import com.example.lingora_fe.admin.topic.presentation.screen.TopicsInCategoryScreen
import com.example.lingora_fe.admin.user.presentation.screen.CreateEditUserScreen
import com.example.lingora_fe.admin.user.presentation.screen.UserDetailsScreen
import com.example.lingora_fe.admin.user.presentation.screen.UserListScreen
import com.example.lingora_fe.admin.word.presentation.screen.CreateEditWordScreen
import com.example.lingora_fe.admin.word.presentation.screen.WordListScreen
import com.example.lingora_fe.admin.word.presentation.screen.WordsInTopicScreen
import com.example.lingora_fe.auth.presentation.AuthViewModel
import com.example.lingora_fe.navigation.Route
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNavigator(
    rootNavController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "admin_dashboard"
    
    // State for logout confirmation dialog
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isLoggingOut by remember { mutableStateOf(false) }

    // Get user info
    val userName = authState.user?.username ?: "Admin"
    val userEmail = authState.user?.email ?: "admin@lingora.com"

    // Determine if current route is a nested screen
    val isNestedScreen = remember(currentRoute) {
        currentRoute.contains("/")
    }

    // Determine screen title based on current route
    val screenTitle = remember(currentRoute) {
        when {
            currentRoute == "admin_user_management/create" -> "Create User"
            currentRoute.startsWith("admin_user_management/edit") -> "Edit User"
            currentRoute.startsWith("admin_user_management/details") -> "User Details"
            currentRoute.startsWith("admin_user_management") -> "User Management"
            currentRoute == "admin_category_management/create" -> "Create Category"
            currentRoute.startsWith("admin_category_management/edit") -> "Edit Category"
            currentRoute.startsWith("admin_category_management") -> "Category Management"
            currentRoute == "admin_topic_management/create" -> "Create Topic"
            currentRoute.startsWith("admin_topic_management/edit") -> "Edit Topic"
            currentRoute.startsWith("admin_topic_create") -> "Create Topic"
            currentRoute.startsWith("admin_topic_edit") -> "Edit Topic"
            currentRoute.startsWith("admin_topic_management") -> "Topic Management"
            currentRoute.startsWith("admin_category_topics") -> "Topics in Category"
            currentRoute.startsWith("admin_topic_words") -> "Words In Topic"
            currentRoute == "admin_word_management/create" -> "Create Word"
            currentRoute.startsWith("admin_word_management/edit") -> "Edit Word"
            currentRoute.startsWith("admin_word_management") -> "Word Management"
            currentRoute.startsWith("admin_dashboard") -> "Dashboard"
            currentRoute.startsWith("admin_content") -> "Content Management"
            currentRoute.startsWith("admin_forum") -> "Forum Management"
            currentRoute.startsWith("admin_analytics") -> "Analytics"
            currentRoute.startsWith("admin_settings") -> "Settings"
            else -> "Admin Panel"
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminDrawerContent(
                currentRoute = getMainRoute(currentRoute),
                userName = userName,
                userEmail = userEmail,
                onNavigateToItem = { route ->
                    scope.launch {
                        drawerState.close()
                        navController.navigate(route) {
                            // Pop up to start destination to avoid building up a large back stack
                            popUpTo("admin_dashboard") {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onLogout = {
                    scope.launch {
                        drawerState.close()
                    }
                    showLogoutDialog = true
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                AdminTopBar(
                    title = screenTitle,
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isClosed) {
                                drawerState.open()
                            } else {
                                drawerState.close()
                            }
                        }
                    },
                    notificationCount = 0, // TODO: Get actual count
                    onNotificationClick = {
                        // TODO: Navigate to notifications
                    },
                    showBackButton = isNestedScreen,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    actions = {
                        // Add specific actions for nested screens
                        if (currentRoute.startsWith("admin_user_management/details")) {
                            val userId = currentBackStackEntry?.arguments?.getInt("userId")
                            IconButton(onClick = { 
                                userId?.let { navController.navigate("admin_user_management/edit/$it") }
                            }) {
                                Icon(Icons.Default.Edit, "Edit")
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "admin_dashboard",
                modifier = Modifier.padding(paddingValues)
            ) {
                // Dashboard
                composable("admin_dashboard") {
                    DashboardScreen()
                }

                // User Management
                composable("admin_user_management") {
                    UserListScreen(
                        onNavigateToCreateUser = {
                            navController.navigate("admin_user_management/create")
                        },
                        onNavigateToEditUser = { userId ->
                            navController.navigate("admin_user_management/edit/$userId")
                        },
                        onNavigateToUserDetails = { userId ->
                            navController.navigate("admin_user_management/details/$userId")
                        }
                    )
                }

                composable("admin_user_management/create") {
                    CreateEditUserScreen(
                        userId = null,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "admin_user_management/edit/{userId}",
                    arguments = listOf(
                        navArgument("userId") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getInt("userId")
                    CreateEditUserScreen(
                        userId = userId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "admin_user_management/details/{userId}",
                    arguments = listOf(
                        navArgument("userId") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                    UserDetailsScreen(
                        userId = userId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEdit = { id ->
                            navController.navigate("admin_user_management/edit/$id")
                        }
                    )
                }

                // Category Management
                composable("admin_category_management") {
                    CategoryListScreen(
                        onNavigateToCreateCategory = {
                            navController.navigate("admin_category_management/create")
                        },
                        onNavigateToEditCategory = { categoryId ->
                            navController.navigate("admin_category_management/edit/$categoryId")
                        },
                        onNavigateToCategoryTopics = { categoryId ->
                            navController.navigate("admin_category_topics/$categoryId")
                        }
                    )
                }

                composable("admin_category_management/create") {
                    CreateEditCategoryScreen(
                        categoryId = null,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "admin_category_management/edit/{categoryId}",
                    arguments = listOf(
                        navArgument("categoryId") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val categoryId = backStackEntry.arguments?.getInt("categoryId")
                    CreateEditCategoryScreen(
                        categoryId = categoryId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Topic Management (nested under category)
                composable(
                    route = "admin_category_topics/{categoryId}",
                    arguments = listOf(
                        navArgument("categoryId") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 0
                    TopicsInCategoryScreen(
                        categoryId = categoryId,
                        onNavigateToCreateTopic = {
                            navController.navigate("admin_topic_create/$categoryId")
                        },
                        onNavigateToEditTopic = { topicId ->
                            navController.navigate("admin_topic_edit/$categoryId/$topicId")
                        },
                        onNavigateToTopicWords = { topicId ->
                            navController.navigate("admin_topic_words/$topicId")
                        }
                    )
                }
                // Words in a Topic
                composable(
                    route = "admin_topic_words/{topicId}",
                    arguments = listOf(navArgument("topicId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val topicId = backStackEntry.arguments?.getInt("topicId") ?: 0
                    WordsInTopicScreen(
                        topicId = topicId,
                        onNavigateToCreate = { navController.navigate("admin_word_management/create") },
                        onNavigateToEdit = { wordId -> navController.navigate("admin_word_management/edit/$wordId") }
                    )
                }

                composable(
                    route = "admin_topic_create/{categoryId}",
                    arguments = listOf(
                        navArgument("categoryId") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 0
                    CreateEditTopicScreen(
                        topicId = null,
                        categoryId = categoryId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "admin_topic_edit/{categoryId}/{topicId}",
                    arguments = listOf(
                        navArgument("categoryId") { type = NavType.IntType },
                        navArgument("topicId") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 0
                    val topicId = backStackEntry.arguments?.getInt("topicId")
                    CreateEditTopicScreen(
                        topicId = topicId,
                        categoryId = categoryId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Topic Management (standalone)
                composable("admin_topic_management") {
                    TopicListScreen(
                        onNavigateToCreateTopic = {
                            navController.navigate("admin_topic_management/create")
                        },
                        onNavigateToEditTopic = { topicId ->
                            navController.navigate("admin_topic_management/edit/$topicId")
                        },
                        onNavigateToTopicWords = { topicId ->
                            navController.navigate("admin_topic_words/$topicId")
                        }
                    )
                }

                composable("admin_topic_management/create") {
                    CreateEditTopicScreen(
                        topicId = null,
                        categoryId = 0, // Will allow null categoryId for standalone
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "admin_topic_management/edit/{topicId}",
                    arguments = listOf(
                        navArgument("topicId") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val topicId = backStackEntry.arguments?.getInt("topicId")
                    CreateEditTopicScreen(
                        topicId = topicId,
                        categoryId = 0, // Will load from existing topic
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Content Management
                composable("admin_content") {
                    ContentManagementScreen()
                }

                // Word Management (standalone)
                composable("admin_word_management") {
                    WordListScreen(
                        onNavigateToCreate = { navController.navigate("admin_word_management/create") },
                        onNavigateToEdit = { wordId -> navController.navigate("admin_word_management/edit/$wordId") }
                    )
                }
                composable("admin_word_management/create") {
                    CreateEditWordScreen(wordId = null, topicId = null, onNavigateBack = { navController.popBackStack() })
                }
                composable(
                    route = "admin_word_management/edit/{wordId}",
                    arguments = listOf(navArgument("wordId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val wordId = backStackEntry.arguments?.getInt("wordId")
                    CreateEditWordScreen(wordId = wordId, topicId = null, onNavigateBack = { navController.popBackStack() })
                }

                // Forum Management
                composable("admin_forum") {
                    ForumManagementScreen()
                }

                // Analytics
                composable("admin_analytics") {
                    AnalyticsScreen()
                }

                // Settings
                composable("admin_settings") {
                    SettingsScreen()
                }
            }
        }
    }
    
    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Xác nhận đăng xuất") },
            text = { Text("Bạn có chắc chắn muốn đăng xuất?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        if (!isLoggingOut) {
                            isLoggingOut = true
                            scope.launch {
                                try {
                                    // Gọi logout API (không cần đợi)
                                    authViewModel.logout()
                                    // Navigate ngay lập tức sau khi clear data
                                    rootNavController.navigate(Route.AuthNavigation.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                } finally {
                                    isLoggingOut = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Đăng xuất", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Hủy")
                }
            }
        )
    }
}

// Helper function to extract main route from nested routes
private fun getMainRoute(route: String?): String {
    if (route == null) return "admin_dashboard"
    
    return when {
        route.startsWith("admin_user_management") -> "admin_user_management"
        route.startsWith("admin_category_management") || 
        route.startsWith("admin_category_topics") -> "admin_category_management"
        route.startsWith("admin_topic") -> "admin_topic_management"
        route.startsWith("admin_word_management") || 
        route.startsWith("admin_topic_words") -> "admin_word_management"
        route.startsWith("admin_dashboard") -> "admin_dashboard"
        route.startsWith("admin_content") -> "admin_content"
        route.startsWith("admin_forum") -> "admin_forum"
        route.startsWith("admin_analytics") -> "admin_analytics"
        route.startsWith("admin_settings") -> "admin_settings"
        else -> "admin_dashboard"
    }
}


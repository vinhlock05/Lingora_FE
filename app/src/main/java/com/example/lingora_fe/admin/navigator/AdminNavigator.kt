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
import com.example.lingora_fe.admin.navigator.components.AdminDrawerContent
import com.example.lingora_fe.admin.navigator.components.AdminTopBar
import com.example.lingora_fe.admin.presentation.screen.placeholder.*
import com.example.lingora_fe.admin.user.presentation.screen.CreateEditUserScreen
import com.example.lingora_fe.admin.user.presentation.screen.UserDetailsScreen
import com.example.lingora_fe.admin.user.presentation.screen.UserListScreen
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
                        authViewModel.logout()
                        // Clear back stack and navigate to auth
                        rootNavController.navigate(Route.AuthNavigation.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
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

                // Content Management
                composable("admin_content") {
                    ContentManagementScreen()
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
}

// Helper function to extract main route from nested routes
private fun getMainRoute(route: String?): String {
    if (route == null) return "admin_dashboard"
    
    return when {
        route.startsWith("admin_user_management") -> "admin_user_management"
        route.startsWith("admin_dashboard") -> "admin_dashboard"
        route.startsWith("admin_content") -> "admin_content"
        route.startsWith("admin_forum") -> "admin_forum"
        route.startsWith("admin_analytics") -> "admin_analytics"
        route.startsWith("admin_settings") -> "admin_settings"
        else -> "admin_dashboard"
    }
}


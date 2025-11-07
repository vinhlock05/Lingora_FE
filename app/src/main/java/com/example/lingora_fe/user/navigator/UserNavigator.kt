package com.example.lingora_fe.user.navigator

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lingora_fe.navigation.Route
import com.example.lingora_fe.user.components.FloatingActionButton
import com.example.lingora_fe.user.dictionary.presentation.DictionaryScreen
import com.example.lingora_fe.user.forum.presentation.ForumScreen
import com.example.lingora_fe.user.material.presentation.MaterialsScreen
import com.example.lingora_fe.user.navigator.components.BottomNavigationBar
import com.example.lingora_fe.user.navigator.components.UserTopBar
import com.example.lingora_fe.user.notification.presentation.screen.NotificationScreen
import com.example.lingora_fe.user.practice.presentation.*
import com.example.lingora_fe.user.profile.presentation.ProfileScreen
import com.example.lingora_fe.user.vocabulary.presentation.screen.CategoryDetailScreen
import com.example.lingora_fe.user.vocabulary.presentation.screen.LearnWordScreen
import com.example.lingora_fe.user.vocabulary.presentation.screen.TopicDetailScreen
import com.example.lingora_fe.user.vocabulary.presentation.screen.VocabularyCategoriesScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserNavigator(rootNavController: NavHostController) {
    val navController = rememberNavController()
    val backStackState = navController.currentBackStackEntryAsState().value

    var selectedItem by rememberSaveable {
        mutableStateOf(0)
    }

    selectedItem = when (backStackState?.destination?.route) {
        Route.VocabularyTab.route -> 0
        Route.PracticeTab.route -> 1
        Route.MaterialsTab.route -> 2
        Route.DictionaryTab.route -> 3
        Route.ForumTab.route -> 4
        Route.ProfileTab.route -> 5
        else -> 0
    }

    // Hide navbar when in nested routes (category detail, topic detail, etc.)
    val isBottomVisible = remember(backStackState) {
        when (backStackState?.destination?.route) {
            Route.VocabularyTab.route,
            Route.PracticeTab.route,
            Route.MaterialsTab.route,
            Route.DictionaryTab.route,
            Route.ForumTab.route,
            Route.ProfileTab.route -> true

            else -> false
        }
    }

    // Show FAB on specific routes
    val showFAB = remember(backStackState) {
        val route = backStackState?.destination?.route ?: ""
        when {
            route == Route.VocabularyTab.route -> true
            route.startsWith("vocabulary/category/") -> true
            route == Route.PracticeTab.route -> true
            route == Route.MaterialsTab.route -> true
            route == Route.DictionaryTab.route -> true
            route == Route.ForumTab.route -> true
            route == Route.ProfileTab.route -> true
            else -> false
        }
    }

    // FAB action based on current route
    val fabAction: () -> Unit = remember(backStackState) {
        val route = backStackState?.destination?.route ?: ""
        when {
            route == Route.VocabularyTab.route -> { 
                { /* TODO: Chat action for vocabulary screen */ }
            }
            route.startsWith("vocabulary/category/") -> { 
                { /* TODO: Chat action for category detail */ }
            }
            else -> { 
                { /* No action */ }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (isBottomVisible) {
                val title = when (backStackState?.destination?.route) {
                    Route.VocabularyTab.route -> "Học từ vựng"
                    Route.PracticeTab.route -> "Luyện tập"
                    Route.MaterialsTab.route -> "Học liệu"
                    Route.DictionaryTab.route -> "Từ điển"
                    Route.ForumTab.route -> "Diễn đàn"
                    Route.ProfileTab.route -> "Cá nhân"
                    else -> ""
                }
                UserTopBar(
                    title = title,
                    notificationCount = 1, //TODO: get actual count from ViewModel
                    onNotificationClick = { navController.navigate(Route.Notification.route) }
                )
            }
        },
        bottomBar = {
            if (isBottomVisible) {
                BottomNavigationBar(
                    currentRoute = backStackState?.destination?.route ?: Route.VocabularyTab.route,
                    onItemClick = { route ->
                        val index = when (route) {
                            Route.VocabularyTab.route -> 0
                            Route.PracticeTab.route -> 1
                            Route.MaterialsTab.route -> 2
                            Route.DictionaryTab.route -> 3
                            Route.ForumTab.route -> 4
                            Route.ProfileTab.route -> 5
                            else -> 0
                        }

                        if (index == selectedItem) return@BottomNavigationBar

                        selectedItem = index
                        navController.navigate(route) {
                            navController.graph.startDestinationRoute?.let { screenRoute ->
                                popUpTo(screenRoute) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (showFAB) {
                FloatingActionButton(onClick = fabAction)
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        val bottomPadding = paddingValues.calculateBottomPadding()
        val topPadding = paddingValues.calculateTopPadding()
        NavHost(
            navController = navController,
            startDestination = Route.VocabularyTab.route,
            modifier = Modifier.padding(
                bottom = if (isBottomVisible) bottomPadding else 0.dp,
                top = if (isBottomVisible) topPadding else 0.dp
            )
        ) {
            // Notification
            composable(Route.Notification.route) {
                NotificationScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Vocabulary Tab
            composable(Route.VocabularyTab.route) {
                VocabularyCategoriesScreen(
                    onCategoryClick = { categoryId ->
                        navController.navigate(Route.categoryDetail(categoryId))
                    }
                )
            }

            // Vocabulary nested routes
            composable(
                route = Route.CategoryDetail.route,
                arguments = listOf(
                    navArgument("categoryId") {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 0
                CategoryDetailScreen(
                    categoryId = categoryId,
                    onBackClick = { navController.popBackStack() },
                    onTopicClick = { topicId ->
                        navController.navigate(Route.topicDetail(topicId))
                    }
                )
            }

            composable(
                route = Route.TopicDetail.route,
                arguments = listOf(
                    navArgument("topicId") {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val topicId = backStackEntry.arguments?.getInt("topicId") ?: 0
                TopicDetailScreen(
                    topicId = topicId,
                    onBackClick = { navController.popBackStack() },
                    onStartLearning = { topicId, wordCount ->
                        navController.navigate(Route.learnWord(topicId, wordCount))
                    }
                )
            }

            composable(
                route = Route.LearnWord.route,
                arguments = listOf(
                    navArgument("topicId") {
                        type = NavType.IntType
                    },
                    navArgument("wordCount") {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val topicId = backStackEntry.arguments?.getInt("topicId") ?: 0
                val wordCount = backStackEntry.arguments?.getInt("wordCount") ?: 15
                LearnWordScreen(
                    topicId = topicId,
                    wordCount = wordCount,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Practice Tab
            composable(Route.PracticeTab.route) {
                PracticeScreen(navController = navController)
            }

            // Practice nested routes
            composable(Route.PronunciationPractice.route) {
                PronunciationPracticeScreen(navController = navController)
            }

            composable(Route.TestPractice.route) {
                TestPracticeScreen(navController = navController)
            }

            composable(Route.VocabularyReview.route) {
                VocabularyReviewScreen(navController = navController)
            }

            composable(Route.FlashcardPractice.route) {
                FlashcardPracticeScreen(navController = navController)
            }

            composable(Route.QuizPractice.route) {
                QuizPracticeScreen(navController = navController)
            }

            composable(
                route = Route.TestDetail.route,
                arguments = listOf(
                    navArgument("testId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val testId = backStackEntry.arguments?.getString("testId") ?: "1"
                TestDetailScreen(
                    navController = navController,
                    testId = testId
                )
            }

            composable(
                route = Route.ListeningPractice.route,
                arguments = listOf(
                    navArgument("testId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                ListeningPracticeScreen(navController = navController)
            }

            composable(
                route = Route.ReadingPractice.route,
                arguments = listOf(
                    navArgument("testId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                ReadingPracticeScreen(navController = navController)
            }

            composable(
                route = Route.WritingPractice.route,
                arguments = listOf(
                    navArgument("testId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                WritingPracticeScreen(navController = navController)
            }

            // Materials Tab
            composable(Route.MaterialsTab.route) {
                MaterialsScreen()
            }

            // Dictionary Tab
            composable(Route.DictionaryTab.route) {
                DictionaryScreen()
            }

            // Forum Tab
            composable(Route.ForumTab.route) {
                ForumScreen()
            }

            // Profile Tab
            composable(Route.ProfileTab.route) {
                ProfileScreen(rootNavController = rootNavController)
            }
        }
    }
}



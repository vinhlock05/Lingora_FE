package com.example.lingora_fe.user.navigator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.example.lingora_fe.user.forum.presentation.*
import com.example.lingora_fe.user.navigator.components.BottomNavigationBar
import com.example.lingora_fe.user.navigator.components.UserTopBar
import com.example.lingora_fe.user.notification.presentation.screen.NotificationScreen
import com.example.lingora_fe.user.practice.presentation.screen.ListeningPracticeScreen
import com.example.lingora_fe.user.practice.presentation.screen.PracticeScreen
import com.example.lingora_fe.user.practice.presentation.screen.PronunciationPracticeScreen
import com.example.lingora_fe.user.practice.presentation.screen.ReadingPracticeScreen
import com.example.lingora_fe.user.practice.presentation.screen.ReviewScreen
import com.example.lingora_fe.user.practice.presentation.screen.TestDetailScreen
import com.example.lingora_fe.user.practice.presentation.screen.TestPracticeScreen
import com.example.lingora_fe.user.practice.presentation.screen.VocabularyReviewScreen
import com.example.lingora_fe.user.practice.presentation.screen.WritingPracticeScreen
import com.example.lingora_fe.user.profile.presentation.ProfileScreen
import com.example.lingora_fe.user.vocabulary.presentation.screen.CategoryDetailScreen
import com.example.lingora_fe.user.vocabulary.presentation.screen.LearnWordScreen
import com.example.lingora_fe.user.vocabulary.presentation.screen.TopicDetailScreen
import com.example.lingora_fe.user.vocabulary.presentation.screen.VocabularyCategoriesScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserNavigator(
    rootNavController: NavHostController,
    viewModel: AuthRepositoryViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    fun notifyStudySetList(message: String, refresh: Boolean = false) {
        if (message.isBlank()) return
        runCatching {
            val listEntry = navController.getBackStackEntry(Route.StudySetList.route)
            if (refresh) {
                listEntry.savedStateHandle.set("refreshStudySetList", true)
            }
            listEntry.savedStateHandle.set("studySetMessage", message)
        }
    }
    val backStackState = navController.currentBackStackEntryAsState().value
    var isCheckingProficiency by remember { mutableStateOf(true) }
    var hasProficiency by remember { mutableStateOf(false) }
    
    // Check proficiency when entering UserNavigator
    // This must complete BEFORE rendering NavHost to prevent API calls
    LaunchedEffect(Unit) {
        val token = viewModel.tokenManager.getAccessToken()
        val activeRole = viewModel.tokenManager.getActiveRole() ?: viewModel.tokenManager.getUserRole()
        
        // Only check proficiency for LEARNER role, ADMIN doesn't need it
        if (token != null && activeRole != "ADMIN") {
            isCheckingProficiency = true
            
            // Check proficiency
            viewModel.authRepository.getProfile(token).fold(
                ifLeft = {
                    // Error case - allow access to avoid blocking user
                    isCheckingProficiency = false
                    hasProficiency = true // Allow access even on error
                },
                ifRight = { user ->
                    // Success case
                    isCheckingProficiency = false
                    // If proficiency is null or empty, navigate to adaptive test
                    if (user.proficiency.isNullOrBlank()) {
                        // Navigate to adaptive test - don't set hasProficiency to true
                        rootNavController.navigate(Route.AdaptiveTest.route) {
                            popUpTo(Route.UserNavigation.route) { inclusive = false }
                        }
                        // Keep hasProficiency = false to prevent NavHost from rendering
                    } else {
                        // User has proficiency - allow rendering NavHost
                        hasProficiency = true
                    }
                }
            )
        } else {
            // No token or ADMIN role - skip proficiency check and allow rendering
            isCheckingProficiency = false
            hasProficiency = true
        }
    }

    var selectedItem by rememberSaveable {
        mutableStateOf(0)
    }

    selectedItem = when (backStackState?.destination?.route) {
        Route.VocabularyTab.route -> 0
        Route.PracticeTab.route -> 1
        Route.StudySetList.route -> 2
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
            Route.StudySetList.route,
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
                    route == Route.StudySetList.route -> true
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

    // Show loading while checking proficiency
    // Don't render NavHost until proficiency check is complete
    if (isCheckingProficiency || !hasProficiency) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (isBottomVisible) {
                val currentRoute = backStackState?.destination?.route
                val title = when (currentRoute) {
                    Route.VocabularyTab.route -> "Học từ vựng"
                    Route.PracticeTab.route -> "Luyện tập"
                            Route.StudySetList.route -> "Học liệu"
                    Route.DictionaryTab.route -> "Từ điển"
                    Route.ForumTab.route -> "Diễn đàn"
                    Route.ProfileTab.route -> "Cá nhân"
                    else -> ""
                }
                val extraActions: @Composable RowScope.() -> Unit = when (currentRoute) {
                    Route.ForumTab.route -> {
                        {
                            Button(
                                onClick = { navController.navigate(Route.CreatePost.route) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text(
                                    text = "+ Viết bài",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    else -> {
                        { }
                    }
                }
                UserTopBar(
                    title = title,
                    notificationCount = 1, //TODO: get actual count from ViewModel
                    onNotificationClick = { navController.navigate(Route.Notification.route) },
                    extraActions = extraActions
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
                            Route.StudySetList.route -> 2
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
                    onTopicClick = { topicId, topicName ->
                        navController.navigate(Route.topicDetail(topicId, topicName))
                    }
                )
            }

            composable(
                route = Route.TopicDetail.route,
                arguments = listOf(
                    navArgument("topicId") {
                        type = NavType.IntType
                    },
                    navArgument("topicName") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val topicId = backStackEntry.arguments?.getInt("topicId") ?: 0
                val topicName = backStackEntry.arguments?.getString("topicName")
                TopicDetailScreen(
                    topicId = topicId,
                    topicName = topicName!!,
                    onBackClick = { navController.popBackStack() },
                    onStartLearning = { topicId, wordCount, gameTypes ->
                        val gameTypesString = gameTypes.joinToString(separator = ",") { it.name }
                        navController.navigate(Route.learnWord(topicId, wordCount, gameTypesString))
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
                    },
                    navArgument("gameTypes") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val topicId = backStackEntry.arguments?.getInt("topicId") ?: 0
                val wordCount = backStackEntry.arguments?.getInt("wordCount") ?: 15
                val gameTypesString = backStackEntry.arguments?.getString("gameTypes") ?: ""
                val gameTypes = if (gameTypesString.isNotEmpty()) {
                    gameTypesString.split(",").mapNotNull { typeName ->
                        try {
                            com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType.valueOf(typeName)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }.toSet()
                } else {
                    setOf(
                        com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType.LISTEN_FILL,
                        com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType.LISTEN_CHOOSE,
                        com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType.TRUE_FALSE,
                        com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType.SEE_WORD_CHOOSE_MEANING,
                        com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType.SEE_MEANING_CHOOSE_WORD
                    )
                }
                LearnWordScreen(
                    topicId = topicId,
                    wordCount = wordCount,
                    gameTypes = gameTypes,
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

            composable(
                route = Route.ReviewPractice.route,
                arguments = listOf(
                    navArgument("limit") { type = NavType.IntType },
                    navArgument("types") { type = NavType.StringType }
                )
            ) {
                ReviewScreen(navController = navController)
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

            composable(
                route = Route.StudySetDetail.route,
                arguments = listOf(
                    navArgument("studySetId") {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val studySetId = backStackEntry.arguments?.getInt("studySetId") ?: 0
                com.example.lingora_fe.user.studyset.presentation.screen.StudySetDetailScreen(
                    onBackClick = { navController.popBackStack() },
                    onStartFlashcard = {
                        navController.navigate(Route.studySetFlashcard(studySetId))
                    },
                    onStartQuiz = {
                        navController.navigate(Route.studySetQuiz(studySetId))
                    },
                    onEditClick = { id ->
                        navController.navigate(Route.studySetEdit(id))
                    },
                    onDeleteSuccess = {
                        notifyStudySetList("Đã xóa học liệu", refresh = true)
                        navController.popBackStack()
                    }
                )
            }

            composable(Route.StudySetCreate.route) { backStackEntry ->
                com.example.lingora_fe.user.studyset.presentation.screen.CreateEditStudySetScreen(
                    onBackClick = { navController.popBackStack() },
                    onSaveSuccess = { message ->
                        notifyStudySetList(message, refresh = true)
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Route.StudySetEdit.route,
                arguments = listOf(
                    navArgument("studySetId") {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                com.example.lingora_fe.user.studyset.presentation.screen.CreateEditStudySetScreen(
                    onBackClick = { navController.popBackStack() },
                    onSaveSuccess = { message ->
                        notifyStudySetList(message, refresh = true)
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Route.StudySetFlashcard.route,
                arguments = listOf(
                    navArgument("studySetId") {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val studySetId = backStackEntry.arguments?.getInt("studySetId") ?: 0
                com.example.lingora_fe.user.studyset.presentation.screen.StudySetFlashcardScreen(
                    studySetId = studySetId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Route.StudySetQuiz.route,
                arguments = listOf(
                    navArgument("studySetId") {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val studySetId = backStackEntry.arguments?.getInt("studySetId") ?: 0
                com.example.lingora_fe.user.studyset.presentation.screen.StudySetQuizScreen(
                    studySetId = studySetId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // StudySet Tab (replaces Materials Tab)
            composable(Route.StudySetList.route) {
                com.example.lingora_fe.user.studyset.presentation.screen.StudySetListScreen(
                    onStudySetClick = { studySetId ->
                        navController.navigate(Route.studySetDetail(studySetId))
                    },
                    onCreateClick = {
                        navController.navigate(Route.StudySetCreate.route)
                    },
                    navController = navController
                )
            }

            // Dictionary Tab
            composable(Route.DictionaryTab.route) {
                DictionaryScreen()
            }

            // Forum Tab
            composable(Route.ForumTab.route) {
                ForumScreen(
                    navController = navController
                )
            }
            
            // Forum Navigation
            composable(Route.CreatePost.route) {
                val parentEntry = navController.previousBackStackEntry
                val savedStateHandle = parentEntry?.savedStateHandle
                CreatePostScreen(
                    navController = navController,
                    onPostCreated = {
                        savedStateHandle?.set("shouldRefreshPosts", true)
                        navController.popBackStack()
                    }
                )
            }
            
            composable(
                route = Route.PostDetail.route,
                arguments = listOf(
                    navArgument("postId") {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getInt("postId") ?: 0
                PostDetailScreen(
                    postId = postId,
                    navController = navController
                )
            }

            composable(
                route = Route.EditPost.route,
                arguments = listOf(
                    navArgument("postId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                EditPostScreen(navController = navController)
            }

            // Profile Tab
            composable(Route.ProfileTab.route) {
                ProfileScreen(rootNavController = rootNavController)
            }
        }
    }
}



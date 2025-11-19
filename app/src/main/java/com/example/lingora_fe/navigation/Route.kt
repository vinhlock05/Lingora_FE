package com.example.lingora_fe.navigation

sealed class Route(
    val route: String
) {
    // Splash Screen
    object SplashScreen : Route("splashScreen")
    
    // Auth Navigation
    object AuthNavigation : Route("authNavigation")
    object AuthScreen : Route("authScreen")
    object LoginScreen : Route("loginScreen") // Deprecated - use AuthScreen with initialTab = "login"
    object RegisterScreen : Route("registerScreen") // Deprecated - use AuthScreen with initialTab = "register"
    object OTPScreen : Route("otpScreen/{email}")
    object ProficiencySelection : Route("proficiencySelection")
    object AdaptiveTest : Route("adaptiveTest")
    
    // Admin Navigation
    object AdminNavigation : Route("adminNavigation")
    object AdminNavigator : Route("adminNavigator")
    
    // Admin Routes
    object AdminDashboard : Route("admin_dashboard")
    object AdminUserManagement : Route("admin_user_management")
    object AdminUserCreate : Route("admin_user_management/create")
    object AdminUserEdit : Route("admin_user_management/edit/{userId}")
    object AdminUserDetails : Route("admin_user_management/details/{userId}")
    object AdminStudySetManagement : Route("admin_studyset_management")
    object AdminStudySetDetail : Route("admin_studyset_management/detail/{studySetId}")
    object AdminContentManagement : Route("admin_content")
    object AdminForumManagement : Route("admin_forum")
    object AdminAnalytics : Route("admin_analytics")
    object AdminSettings : Route("admin_settings")
    
    // User Navigation
    object UserNavigation : Route("userNavigation")
    object UserNavigator : Route("userNavigator")
    
    // Bottom Navigation Tabs
    object VocabularyTab : Route("vocabulary")
    object PracticeTab : Route("practice")
    object DictionaryTab : Route("dictionary")
    object ForumTab : Route("forum")
    object ProfileTab : Route("profile")
    
    // Notification
    object Notification : Route("notification")
    
    // Vocabulary Navigation
    object CategoryDetail : Route("vocabulary/category/{categoryId}")
    
    // StudySet Navigation
    object StudySetList : Route("studyset/list")
    object StudySetDetail : Route("studyset/detail/{studySetId}")
    object StudySetCreate : Route("studyset/create")
    object StudySetEdit : Route("studyset/edit/{studySetId}")
    object StudySetFlashcard : Route("studyset/{studySetId}/flashcard")
    object StudySetQuiz : Route("studyset/{studySetId}/quiz")
    object TopicDetail : Route("vocabulary/topic/{topicId}?topicName={topicName}")
    object LearnWord : Route("vocabulary/learn/{topicId}/{wordCount}/{gameTypes}")
    object Practice : Route("vocabulary/practice/{topicId}")
    
    // Practice Navigation
    object PronunciationPractice : Route("practice/pronunciation")
    object TestPractice : Route("practice/tests")
    object VocabularyReview : Route("practice/vocabulary_review")
    object FlashcardPractice : Route("practice/flashcard")
    object QuizPractice : Route("practice/quiz")
    object ReviewPractice : Route("practice/review/{limit}/{types}")
    object TestDetail : Route("practice/test/{testId}")
    object ListeningPractice : Route("practice/test/{testId}/listening")
    object ReadingPractice : Route("practice/test/{testId}/reading")
    object WritingPractice : Route("practice/test/{testId}/writing")
    
    // Forum Navigation
    object CreatePost : Route("forum/create")
    object PostDetail : Route("forum/post/{postId}")
    object EditPost : Route("forum/post/{postId}/edit")
    
    companion object {
        // Auth routes
        fun otpScreen(email: String) = "otpScreen/$email"
        
        // Admin routes
        fun adminUserEdit(userId: Int) = "admin_user_management/edit/$userId"
        fun adminUserDetails(userId: Int) = "admin_user_management/details/$userId"
        
        // Vocabulary routes
        fun categoryDetail(categoryId: Int) = "vocabulary/category/$categoryId"
        fun topicDetail(topicId: Int, topicName: String): String {
            val encodedName = android.net.Uri.encode(topicName)
            return "vocabulary/topic/$topicId?topicName=$encodedName"
        }
        fun learnWord(topicId: Int, wordCount: Int, gameTypes: String) = "vocabulary/learn/$topicId/$wordCount/$gameTypes"
        fun practice(topicId: Int) = "vocabulary/practice/$topicId"
        
        // Practice routes
        fun testDetail(testId: String) = "practice/test/$testId"
        fun listeningPractice(testId: String) = "practice/test/$testId/listening"
        fun readingPractice(testId: String) = "practice/test/$testId/reading"
        fun writingPractice(testId: String) = "practice/test/$testId/writing"
        fun reviewPractice(limit: Int, gameTypes: String) = "practice/review/$limit/$gameTypes"
        
        // StudySet routes
        fun studySetDetail(studySetId: Int) = "studyset/detail/$studySetId"
        fun studySetEdit(studySetId: Int) = "studyset/edit/$studySetId"
        fun studySetFlashcard(studySetId: Int) = "studyset/$studySetId/flashcard"
        fun studySetQuiz(studySetId: Int) = "studyset/$studySetId/quiz"
        // Forum routes
        fun postDetail(postId: Int) = "forum/post/$postId"
        fun editPost(postId: Int) = "forum/post/$postId/edit"
    }

}
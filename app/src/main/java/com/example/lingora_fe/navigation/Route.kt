package com.example.lingora_fe.navigation

sealed class Route(
    val route: String
) {
    // Auth Navigation
    object AuthNavigation : Route("authNavigation")
    object AuthScreen : Route("authScreen")
    object LoginScreen : Route("loginScreen") // Deprecated - use AuthScreen with initialTab = "login"
    object RegisterScreen : Route("registerScreen") // Deprecated - use AuthScreen with initialTab = "register"
    object OTPScreen : Route("otpScreen/{email}")
    
    // Admin Navigation
    object AdminNavigation : Route("adminNavigation")
    object AdminNavigator : Route("adminNavigator")
    
    // User Navigation
    object UserNavigation : Route("userNavigation")
    object UserNavigator : Route("userNavigator")
    
    // Bottom Navigation Tabs
    object VocabularyTab : Route("vocabulary")
    object PracticeTab : Route("practice")
    object MaterialsTab : Route("materials")
    object DictionaryTab : Route("dictionary")
    object ForumTab : Route("forum")
    object ProfileTab : Route("profile")
    
    // Notification
    object Notification : Route("notification")
    
    // Vocabulary Navigation
    object CategoryDetail : Route("vocabulary/category/{categoryId}")
    object LearnWord : Route("vocabulary/learn/{topicId}")
    object Practice : Route("vocabulary/practice/{topicId}")
    
    // Practice Navigation
    object PronunciationPractice : Route("practice/pronunciation")
    object TestPractice : Route("practice/tests")
    object VocabularyReview : Route("practice/vocabulary_review")
    object FlashcardPractice : Route("practice/flashcard")
    object QuizPractice : Route("practice/quiz")
    object TestDetail : Route("practice/test/{testId}")
    object ListeningPractice : Route("practice/test/{testId}/listening")
    object ReadingPractice : Route("practice/test/{testId}/reading")
    object WritingPractice : Route("practice/test/{testId}/writing")
    
    companion object {
        // Auth routes
        fun otpScreen(email: String) = "otpScreen/$email"
        
        // Vocabulary routes
        fun categoryDetail(categoryId: Int) = "vocabulary/category/$categoryId"
        fun learnWord(topicId: Int) = "vocabulary/learn/$topicId"
        fun practice(topicId: Int) = "vocabulary/practice/$topicId"
        
        // Practice routes
        fun testDetail(testId: String) = "practice/test/$testId"
        fun listeningPractice(testId: String) = "practice/test/$testId/listening"
        fun readingPractice(testId: String) = "practice/test/$testId/reading"
        fun writingPractice(testId: String) = "practice/test/$testId/writing"
    }

}
package com.example.lingora_fe.navigation

sealed class Route(
    val route: String
) {
    // Auth Navigation
    object AuthNavigation : Route("authNavigation")
    object LoginScreen : Route("loginScreen")
    object RegisterScreen : Route("registerScreen")
    object OTPScreen : Route("otpScreen")
    
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
    object VocabularyCategories : Route("vocabulary/categories")
    object CategoryDetail : Route("vocabulary/category/{categoryId}")
    object TopicDetail : Route("vocabulary/topic/{topicId}")
    object LearnWord : Route("vocabulary/learn/{topicId}")
    object Practice : Route("vocabulary/practice/{topicId}")
    object Quiz : Route("vocabulary/quiz/{topicId}")
    object QuizCompletion : Route("vocabulary/quiz/{topicId}/completion")
    
    companion object {
        fun categoryDetail(categoryId: Int) = "vocabulary/category/$categoryId"
        fun topicDetail(topicId: Int) = "vocabulary/topic/$topicId"
        fun newLesson(topicId: Int) = "vocabulary/topic/$topicId/new-lesson"
        fun learnWord(topicId: Int) = "vocabulary/learn/$topicId"
        fun practice(topicId: Int) = "vocabulary/practice/$topicId"
        fun quiz(topicId: Int) = "vocabulary/quiz/$topicId"
        fun quizCompletion(topicId: Int) = "vocabulary/quiz/$topicId/completion"
    }

}
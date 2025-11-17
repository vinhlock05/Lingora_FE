package com.example.lingora_fe.user.studyset.presentation

import com.example.lingora_fe.user.studyset.domain.model.StudySet
import com.example.lingora_fe.user.studyset.domain.model.StudySetStatus
import com.example.lingora_fe.user.studyset.domain.model.StudySetVisibility

// StudySet List State
data class StudySetListUiState(
    val studySets: List<StudySet> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val total: Int = 0,
    val searchQuery: String = "",
    val selectedTab: StudySetTab = StudySetTab.STORE,
    val selectedVisibility: StudySetVisibility? = null,
    val selectedStatus: StudySetStatus? = null,
    val showPurchaseModal: Boolean = false,
    val purchaseStudySet: StudySet? = null,
    val isCheckingAccess: Boolean = false,
    val purchaseError: String? = null,
    val isPurchasing: Boolean = false
)

enum class StudySetTab {
    STORE, // Kho học liệu
    MINE   // Của tôi
}

// StudySet Detail State
data class StudySetDetailUiState(
    val studySet: StudySet? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// Create/Edit StudySet State
data class StudySetFormUiState(
    val title: String = "",
    val description: String = "",
    val visibility: StudySetVisibility = StudySetVisibility.PRIVATE,
    val isPaid: Boolean = false,
    val price: String = "",
    val flashcards: List<FlashcardFormItem> = emptyList(),
    val quizzes: List<QuizFormItem> = emptyList(),
    val selectedContentTab: ContentTab = ContentTab.FLASHCARD,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val studySetId: Int? = null,
    val saveSuccess: Boolean = false
)

data class FlashcardFormItem(
    val id: Int? = null,
    val frontText: String = "",
    val backText: String = "",
    val example: String = ""
)

data class QuizFormItem(
    val id: Int? = null,
    val type: com.example.lingora_fe.user.studyset.domain.model.QuizType = com.example.lingora_fe.user.studyset.domain.model.QuizType.MULTIPLE_CHOICE,
    val question: String = "",
    val options: List<String> = listOf("", "", "", ""),
    val correctAnswer: String = ""
)

enum class ContentTab {
    FLASHCARD,
    QUIZ
}

// Purchase Modal State
data class PurchaseModalState(
    val studySet: StudySet? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val paymentUrl: String? = null
)

// Flashcard Study State
data class FlashcardStudyUiState(
    val studySet: StudySet? = null,
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val learnedCount: Int = 0
)

// Quiz Study State
data class QuizStudyUiState(
    val studySet: StudySet? = null,
    val currentIndex: Int = 0,
    val selectedAnswers: Map<Int, String> = emptyMap(),
    val showResults: Boolean = false,
    val correctCount: Int = 0,
    val showFeedback: Boolean = false,
    val checkedAnswers: Set<Int> = emptySet()
)


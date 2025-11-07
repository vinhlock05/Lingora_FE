package com.example.lingora_fe.user.vocabulary.presentation.viewmodel

import com.example.lingora_fe.user.vocabulary.domain.model.Word

// Learning phases
enum class LearningPhase {
    LEARN,      // Learning new words with flashcard
    QUIZ        // Quiz testing
}

// Quiz question types (based on GameType from TopicDetailViewModel)
enum class QuestionType {
    LISTEN_FILL,           // Nghe điền từ
    LISTEN_CHOOSE,         // Nghe chọn từ
    TRUE_FALSE,            // Đúng/Sai
    SEE_WORD_CHOOSE_MEANING,  // Nhìn từ chọn nghĩa
    SEE_MEANING_CHOOSE_WORD   // Nhìn nghĩa chọn từ
}

// Quiz question data class
data class QuizQuestion(
    val type: QuestionType,
    val question: String,
    val correctAnswer: String,
    val options: List<String> = emptyList(),
    val word: Word
)

// Learning state
data class LearningState(
    val phase: LearningPhase = LearningPhase.LEARN,
    val currentWordIndex: Int = 0,
    val isFlashcardRevealed: Boolean = false,
    val currentQuestionIndex: Int = 0,
    val selectedAnswer: String? = null,
    val typedAnswer: String = "",
    val isAnswerChecked: Boolean = false,
    val correctAnswers: Int = 0,
    val showCompletionDialog: Boolean = false,
    val showExitDialog: Boolean = false
)


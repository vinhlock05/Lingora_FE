package com.example.lingora_fe.user.vocabulary.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.lingora_fe.user.vocabulary.domain.model.Word

data class VocabularyUiState(
    val currentTopicId: Int? = null,
    val wordsInTopic: List<Word> = emptyList(),
    val currentWordIndex: Int = 1,
    val isPracticeRevealed: Boolean = false,
    val currentQuizIndex: Int = 1,
    val selectedOptionId: Int? = null,
    val showQuizFeedback: Boolean = false,
)

data class QuizItem(
    val questionText: String,
    val options: List<QuizOptionUi>
)

data class QuizOptionUi(
    val id: Int,
    val text: String,
    val isCorrect: Boolean
)

class VocabularyViewModel : ViewModel() {
    var state by mutableStateOf(VocabularyUiState())
        private set

    // Sample data for demo
    private val sampleWords = listOf(
        Word(1, topicId = 101, level = "A1", word = "apple", meaning = "quả táo", example = "I eat an apple.", exampleTranslation = "Tôi ăn một quả táo.", position = 1, audioUrl = "", imageUrl = ""),
        Word(2, topicId = 101, level = "A1", word = "book", meaning = "quyển sách", example = "This is a book.", exampleTranslation = "Đây là một quyển sách.", position = 2, audioUrl = "", imageUrl = ""),
        Word(3, topicId = 101, level = "A1", word = "cat", meaning = "con mèo", example = "The cat sleeps.", exampleTranslation = "Con mèo đang ngủ.", position = 3, audioUrl = "", imageUrl = ""),
        Word(4, topicId = 101, level = "A1", word = "door", meaning = "cánh cửa", example = "Open the door.", exampleTranslation = "Mở cửa ra.", position = 4, audioUrl = "", imageUrl = ""),
        Word(5, topicId = 101, level = "A1", word = "egg", meaning = "trứng", example = "I cook an egg.", exampleTranslation = "Tôi nấu một quả trứng.", position = 5, audioUrl = "", imageUrl = "")
    )

    fun startTopic(topicId: Int) {
        val words = sampleWords.filter { it.topicId == topicId }.sortedBy { it.position }
        state = state.copy(
            currentTopicId = topicId,
            wordsInTopic = words,
            currentWordIndex = 1,
            isPracticeRevealed = false,
            currentQuizIndex = 1,
            selectedOptionId = null,
            showQuizFeedback = false
        )
    }

    fun totalWords(): Int = state.wordsInTopic.size.coerceAtLeast(1)

    fun currentWord(): Word? = state.wordsInTopic.getOrNull(state.currentWordIndex - 1)

    fun nextLearn(): Boolean {
        val next = state.currentWordIndex + 1
        return if (next <= totalWords()) {
            state = state.copy(currentWordIndex = next)
            true
        } else false
    }

    fun previousLearn() {
        val prev = (state.currentWordIndex - 1).coerceAtLeast(1)
        state = state.copy(currentWordIndex = prev)
    }

    // Practice
    fun resetPractice() {
        state = state.copy(currentWordIndex = 1, isPracticeRevealed = false)
    }

    fun revealPracticeCard() {
        state = state.copy(isPracticeRevealed = true)
    }

    fun nextPractice(): Boolean {
        val next = state.currentWordIndex + 1
        return if (next <= totalWords()) {
            state = state.copy(currentWordIndex = next, isPracticeRevealed = false)
            true
        } else false
    }

    fun previousPractice() {
        val prev = (state.currentWordIndex - 1).coerceAtLeast(1)
        state = state.copy(currentWordIndex = prev, isPracticeRevealed = false)
    }

    // Quiz
    fun resetQuiz() {
        state = state.copy(currentQuizIndex = 1, selectedOptionId = null, showQuizFeedback = false)
    }

    fun currentQuizItem(): QuizItem? {
        val word = state.wordsInTopic.getOrNull(state.currentQuizIndex - 1) ?: return null
        val correctText = word.meaning
        val distractors = state.wordsInTopic.filter { it.id != word.id }.shuffled().take(3).map { it.meaning }
        val optionsTexts = (distractors + correctText).shuffled()
        return QuizItem(
            questionText = "${word.word}: nghĩa là gì?",
            options = optionsTexts.mapIndexed { idx, txt -> QuizOptionUi(idx + 1, txt, txt == correctText) }
        )
    }

    fun selectOption(optionId: Int) {
        state = state.copy(selectedOptionId = optionId)
    }

    fun showFeedback() {
        state = state.copy(showQuizFeedback = true)
    }

    fun isCurrentAnswerCorrect(): Boolean {
        val item = currentQuizItem() ?: return false
        val selected = state.selectedOptionId ?: return false
        return item.options.firstOrNull { it.id == selected }?.isCorrect == true
    }

    fun nextQuiz(): Boolean {
        val next = state.currentQuizIndex + 1
        return if (next <= totalWords()) {
            state = state.copy(currentQuizIndex = next, selectedOptionId = null, showQuizFeedback = false)
            true
        } else false
    }
}



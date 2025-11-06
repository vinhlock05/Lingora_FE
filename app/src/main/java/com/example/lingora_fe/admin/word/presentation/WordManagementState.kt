package com.example.lingora_fe.admin.word.presentation

import com.example.lingora_fe.admin.word.domain.model.Word
import com.example.lingora_fe.admin.topic.domain.model.Topic

data class WordManagementState(
    val words: List<Word> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val total: Int = 0,
    val totalWords: Int? = null, // For topic/{id}/words response

    val searchQuery: String = "",
    val selectedSort: String? = null,
    val cefrFilter: String? = null,
    val typeFilter: String? = null,

    val selectedWord: Word? = null,
    val currentTopic: Topic? = null, // Topic info when viewing words in topic

    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,

    val actionSuccess: String? = null,
    val actionError: String? = null,

    // Unclassified words dialog
    val unclassifiedWords: List<Word> = emptyList(),
    val isLoadingUnclassified: Boolean = false,
    val unclassifiedCurrentPage: Int = 1,
    val unclassifiedTotalPages: Int = 1
)

data class WordFormState(
    val id: Int? = null,
    val word: String = "",
    val meaning: String = "",
    val phonetic: String? = null,
    val cefrLevel: String = "A1",
    val type: String = "noun",
    val example: String? = null,
    val exampleTranslation: String? = null,
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val topicId: Int? = null,

    val isValid: Boolean = false,
    val wordError: String? = null,
    val meaningError: String? = null
) {
    fun validate(): WordFormState {
        val wErr = if (word.isBlank()) "Word is required" else null
        val mErr = if (meaning.isBlank()) "Meaning is required" else null
        return copy(
            wordError = wErr,
            meaningError = mErr,
            isValid = wErr == null && mErr == null
        )
    }
}

sealed class WordManagementEvent {
    data class LoadAll(val page: Int = 1): WordManagementEvent()
    data class LoadInTopic(val topicId: Int, val page: Int = 1): WordManagementEvent()
    data class Search(val query: String): WordManagementEvent()
    data class SortBy(val sort: String?): WordManagementEvent()
    object ClearFilters: WordManagementEvent()

    data class LoadDetails(val wordId: Int): WordManagementEvent()
    object ClearSelected: WordManagementEvent()

    data class Create(
        val word: String,
        val meaning: String,
        val phonetic: String?,
        val cefrLevel: String,
        val type: String,
        val example: String?,
        val exampleTranslation: String?,
        val audioUrl: String?,
        val imageUrl: String?,
        val topicId: Int?
    ) : WordManagementEvent()

    data class Update(
        val wordId: Int,
        val word: String?,
        val meaning: String?,
        val phonetic: String?,
        val cefrLevel: String?,
        val type: String?,
        val example: String?,
        val exampleTranslation: String?,
        val audioUrl: String?,
        val imageUrl: String?,
        val topicId: Int?
    ) : WordManagementEvent()

    data class Delete(val wordId: Int): WordManagementEvent()
    data class RemoveFromTopic(val wordId: Int): WordManagementEvent()

    data class LoadUnclassified(val page: Int = 1): WordManagementEvent()
    data class AttachExisting(val wordId: Int, val topicId: Int): WordManagementEvent()

    object ClearError: WordManagementEvent()
    object ClearActionMessages: WordManagementEvent()
}



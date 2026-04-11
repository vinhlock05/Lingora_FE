package com.example.lingora_fe.user.dictionary.presentation

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.user.dictionary.domain.model.TranslateResult
import com.example.lingora_fe.user.dictionary.domain.repository.TranslateRepository
import com.example.lingora_fe.user.scan.data.ml.DetectedObject
import com.example.lingora_fe.user.scan.domain.repository.ScanRepository
import com.example.lingora_fe.user.vocabulary.domain.model.Word
import com.example.lingora_fe.user.vocabulary.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DictionaryState(
    val isDictionaryMode: Boolean = true,

    val searchTerm: String = "",
    val suggestions: List<Word> = emptyList(),
    val selectedWord: Word? = null,
    val isSuggestLoading: Boolean = false,
    val isLookupLoading: Boolean = false,

    // Phrase translation
    val translateText: String = "",
    val translateResult: TranslateResult? = null,
    val isTranslateLoading: Boolean = false,
    val sourceLang: String = "en", // Default: English
    val targetLang: String = "vi", // Default: Vietnamese

    val errorMessage: String? = null
)

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val translateRepository: TranslateRepository,
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DictionaryState())
    val state: StateFlow<DictionaryState> = _state.asStateFlow()

    fun setMode(isDictionary: Boolean) {
        _state.value = _state.value.copy(isDictionaryMode = isDictionary)
    }

    // Dictionary
    fun onSearchTermChanged(term: String) {
        _state.value = _state.value.copy(
            searchTerm = term
        )

        if (term.trim().length < 2) {
            _state.value = _state.value.copy(
                suggestions = emptyList(),
                isSuggestLoading = false
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSuggestLoading = true)
            wordRepository.suggestWords(term.trim(), limit = 10)
                .onRight { words ->
                    _state.value = _state.value.copy(
                        suggestions = words,
                        isSuggestLoading = false
                    )
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isSuggestLoading = false,
                        errorMessage = failure.message ?: "Failed to load suggestions"
                    )
                }
        }
    }

    fun lookupCurrentTerm() {
        val term = _state.value.searchTerm.trim()
        if (term.isBlank()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLookupLoading = true)
            wordRepository.lookupWord(term)
                .onRight { word ->
                    _state.value = _state.value.copy(
                        selectedWord = word,
                        isLookupLoading = false,
                        suggestions = emptyList()
                    )
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isLookupLoading = false,
                        errorMessage = failure.message ?: "Failed to lookup word"
                    )
                }
        }
    }

    fun onSuggestionSelected(word: Word) {
        _state.value = _state.value.copy(
            searchTerm = word.word
        )
        lookupCurrentTerm()
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    // Scan image
    var detectedObjects by mutableStateOf<List<DetectedObject>>(emptyList())
    var currentBitmap by mutableStateOf<Bitmap?>(null)

    fun onImageCaptured(bitmap: Bitmap) {
        currentBitmap = bitmap

        viewModelScope.launch {
            detectedObjects = scanRepository.detect(bitmap)
        }
    }

    fun onObjectSelected(label: String) {
        val keyword = label.lowercase().trim()

        viewModelScope.launch {
            wordRepository.lookupWord(keyword)
                .onRight { word ->
                    _state.update {
                        it.copy(
                            selectedWord = word,
                            searchTerm = keyword
                        )
                    }
                    currentBitmap = null
                    detectedObjects = emptyList()
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        errorMessage = failure.message ?: "Failed to lookup word"
                    )
                }
        }
    }

    // Translate phrase
    fun onTranslateTextChanged(text: String) {
        _state.value = _state.value.copy(translateText = text)
    }

    fun swapLanguages() {
        val currentSource = _state.value.sourceLang
        val currentTarget = _state.value.targetLang
        _state.value = _state.value.copy(
            sourceLang = currentTarget,
            targetLang = currentSource,
            translateResult = null // Clear previous result when swapping
        )
    }

    fun translateCurrentText() {
        val text = _state.value.translateText.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isTranslateLoading = true)
            translateRepository.translatePhrase(
                text,
                _state.value.sourceLang,
                _state.value.targetLang
            )
                .onRight { result ->
                    _state.value = _state.value.copy(
                        translateResult = result,
                        isTranslateLoading = false
                    )
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isTranslateLoading = false,
                        errorMessage = failure.message ?: "Failed to translate phrase"
                    )
                }
        }
    }
}

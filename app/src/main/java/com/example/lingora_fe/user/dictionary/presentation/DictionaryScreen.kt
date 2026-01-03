package com.example.lingora_fe.user.dictionary.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.user.dictionary.presentation.components.DictionarySearchCard
import com.example.lingora_fe.user.dictionary.presentation.components.DictionarySuggestionList
import com.example.lingora_fe.user.dictionary.presentation.components.ModeToggleRow
import com.example.lingora_fe.user.dictionary.presentation.components.TranslateInputCard
import com.example.lingora_fe.user.dictionary.presentation.components.TranslateResultCard
import com.example.lingora_fe.user.dictionary.presentation.components.WordResultCard

private fun languageLabel(code: String): String = when (code.lowercase()) {
    "en" -> "Tiếng Anh"
    "vi" -> "Tiếng Việt"
    else -> code.uppercase()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    viewModel: DictionaryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GradientStart.copy(alpha = 0.06f),
                        GradientEnd.copy(alpha = 0.02f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ModeToggleRow(
                isDictionaryMode = state.isDictionaryMode,
                onModeChange = { viewModel.setMode(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (state.isDictionaryMode) {
                DictionarySearchCard(
                    query = state.searchTerm,
                    onQueryChange = { viewModel.onSearchTermChanged(it) },
                    onSearch = { viewModel.lookupCurrentTerm() }
                )
            } else {
                TranslateInputCard(
                    sourceLanguageLabel = languageLabel(state.sourceLang),
                    targetLanguageLabel = languageLabel(state.targetLang),
                    text = state.translateText,
                    onTextChange = { viewModel.onTranslateTextChanged(it) },
                    onSwapLanguages = { viewModel.swapLanguages() },
                    onTranslate = { viewModel.translateCurrentText() },
                    isTranslating = state.isTranslateLoading
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.isDictionaryMode) {
                    if (state.suggestions.isNotEmpty()) {
                        DictionarySuggestionList(
                            suggestions = state.suggestions,
                            onSuggestionSelected = { viewModel.onSuggestionSelected(it) }
                        )
                    }

                    if (state.isLookupLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GradientStart)
                        }
                    }

                    state.selectedWord?.let { WordResultCard(word = it) }
                } else {
                    state.translateResult?.let {
                        TranslateResultCard(
                            result = it,
                            languageNameResolver = ::languageLabel
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}
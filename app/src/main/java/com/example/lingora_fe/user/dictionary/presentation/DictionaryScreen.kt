package com.example.lingora_fe.user.dictionary.presentation

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
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
import com.example.lingora_fe.user.scan.presentation.ScanScreen
import com.example.lingora_fe.util.uriToBitmap

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

    val context = LocalContext.current
    var showPicker by remember { mutableStateOf(false) }

    var showScan by remember { mutableStateOf(false) }
    var scanBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // 🔔 Error
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    // 📷 Camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            scanBitmap = it
            viewModel.onImageCaptured(it)
            showScan = true
        }
    }

    // 🖼️ Gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bitmap = uriToBitmap(context, it)
            scanBitmap = bitmap
            viewModel.onImageCaptured(bitmap)
            showScan = true
        }
    }

    if (showScan && scanBitmap != null) {
        ScanScreen(
            bitmap = scanBitmap!!,
            objects = viewModel.detectedObjects,
            onObjectClick = { obj ->
                viewModel.onObjectSelected(obj.label)
                showScan = false
                scanBitmap = null
            },
            onClose = {
                showScan = false
                scanBitmap = null
            }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
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

            // MODE
            ModeToggleRow(
                isDictionaryMode = state.isDictionaryMode,
                onModeChange = { viewModel.setMode(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (state.isDictionaryMode) {
                DictionarySearchCard(
                    query = state.searchTerm,
                    onQueryChange = { viewModel.onSearchTermChanged(it) },
                    onSearch = { viewModel.lookupCurrentTerm() },
                    onOpenCamera = { showPicker = true }
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

            // CONTENT
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                if (state.isDictionaryMode) {

                    // suggestions
                    if (state.suggestions.isNotEmpty()) {
                        DictionarySuggestionList(
                            suggestions = state.suggestions,
                            onSuggestionSelected = {
                                viewModel.onSuggestionSelected(it)
                            }
                        )
                    }

                    // loading
                    if (state.isLookupLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GradientStart)
                        }
                    }

                    // result
                    state.selectedWord?.let {
                        WordResultCard(word = it)
                    }

                } else {

                    // translate ONLY
                    state.translateResult?.let {
                        TranslateResultCard(
                            result = it,
                            languageNameResolver = ::languageLabel
                        )
                    }
                }
            }
        }

        // ===================== PICKER =====================
        if (showPicker) {
            ModalBottomSheet(
                onDismissRequest = { showPicker = false }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text("Chọn ảnh")

                    ListItem(
                        headlineContent = { Text("Chụp ảnh") },
                        leadingContent = {
                            Icon(Icons.Default.CameraAlt, null)
                        },
                        modifier = Modifier.clickable {
                            showPicker = false
                            cameraLauncher.launch(null)
                        }
                    )

                    ListItem(
                        headlineContent = { Text("Chọn từ thư viện") },
                        leadingContent = {
                            Icon(Icons.Default.Photo, null)
                        },
                        modifier = Modifier.clickable {
                            showPicker = false
                            galleryLauncher.launch("image/*")
                        }
                    )
                }
            }
        }

        // ===================== SNACKBAR =====================
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}
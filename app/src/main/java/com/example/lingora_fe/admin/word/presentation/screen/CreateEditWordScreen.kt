package com.example.lingora_fe.admin.word.presentation.screen

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.lingora_fe.admin.word.domain.model.CefrLevel
import com.example.lingora_fe.admin.word.domain.model.WordType
import com.example.lingora_fe.admin.word.presentation.*
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.util.FileUploadHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditWordScreen(
    wordId: Int?,
    topicId: Int?,
    onNavigateBack: () -> Unit,
    viewModel: WordManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val form by viewModel.formState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(wordId, topicId) {
        if (wordId != null) {
            viewModel.onEvent(WordManagementEvent.LoadDetails(wordId))
        } else {
            viewModel.setFormTopicId(topicId)
        }
    }

    // When the selectedWord loads, hydrate form
    LaunchedEffect(state.selectedWord) {
        state.selectedWord?.let { w ->
            viewModel.updateFormState(
                form.copy(
                    id = w.id,
                    word = w.word,
                    meaning = w.meaning,
                    vnMeaning = w.vnMeaning,
                    phonetic = w.phonetic,
                    cefrLevel = w.cefrLevel,
                    type = w.type,
                    example = w.example,
                    exampleTranslation = w.exampleTranslation,
                    audioUrl = w.audioUrl,
                    imageUrl = w.imageUrl,
                    topicId = w.topicId
                )
            )
        }
    }

    LaunchedEffect(state.actionSuccess) {
        state.actionSuccess?.let { onNavigateBack() }
    }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("lingora_prefs", Context.MODE_PRIVATE)
    val scope = rememberCoroutineScope()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
        }
    }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedAudioUri = it
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionError) {
        state.actionError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            viewModel.onEvent(WordManagementEvent.ClearActionMessages)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        val bottom = padding.calculateBottomPadding()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottom)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Word
            OutlinedTextField(
                value = form.word,
                onValueChange = { viewModel.updateFormState(form.copy(word = it)) },
                label = { Text("Word *") },
                leadingIcon = {
                    Icon(
                        Icons.Default.TextFields,
                        "Word",
                        tint = GradientStart
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                isError = form.wordError != null,
                supportingText = form.wordError?.let { { Text(it) } }
            )

            // Phonetic
            OutlinedTextField(
                value = form.phonetic ?: "",
                onValueChange = { viewModel.updateFormState(form.copy(phonetic = it.takeIf { it.isNotBlank() })) },
                label = { Text("Phonetic") },
                leadingIcon = {
                    Icon(
                        Icons.Default.VolumeUp,
                        "Phonetic",
                        tint = GradientStart
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            // CEFR Level
            var cefrExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = cefrExpanded,
                onExpandedChange = { cefrExpanded = !cefrExpanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = form.cefrLevel,
                    onValueChange = {},
                    label = { Text("CEFR Level *") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Label,
                            "CEFR Level",
                            tint = GradientStart
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cefrExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = cefrExpanded,
                    onDismissRequest = { cefrExpanded = false }
                ) {
                    CefrLevel.values().forEach { level ->
                        DropdownMenuItem(
                            text = { Text(level.value) },
                            onClick = {
                                viewModel.updateFormState(form.copy(cefrLevel = level.value))
                                cefrExpanded = false
                            }
                        )
                    }
                }
            }

            // Type
            var typeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = !typeExpanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = form.type,
                    onValueChange = {},
                    label = { Text("Type *") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Category,
                            "Type",
                            tint = GradientStart
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    WordType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.value) },
                            onClick = {
                                viewModel.updateFormState(form.copy(type = type.value))
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            // Meaning
            OutlinedTextField(
                value = form.meaning,
                onValueChange = { viewModel.updateFormState(form.copy(meaning = it)) },
                label = { Text("Meaning *") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Description,
                        "Meaning",
                        tint = GradientStart
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                isError = form.meaningError != null,
                supportingText = form.meaningError?.let { { Text(it) } }
            )

            // Vietnamese Meaning
            OutlinedTextField(
                value = form.vnMeaning ?: "",
                onValueChange = { value ->
                    viewModel.updateFormState(form.copy(vnMeaning = value.takeIf { it.isNotBlank() }))
                },
                label = { Text("Vietnamese Meaning") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Language,
                        "Vietnamese Meaning",
                        tint = GradientStart
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            // Example
            OutlinedTextField(
                value = form.example ?: "",
                onValueChange = { viewModel.updateFormState(form.copy(example = it.takeIf { it.isNotBlank() })) },
                label = { Text("Example") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Article,
                        "Example",
                        tint = GradientStart
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            // Example Translation
            OutlinedTextField(
                value = form.exampleTranslation ?: "",
                onValueChange = { viewModel.updateFormState(form.copy(exampleTranslation = it.takeIf { it.isNotBlank() })) },
                label = { Text("Example Translation") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Language,
                        "Example Translation",
                        tint = GradientStart
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            // Image Upload
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Image,
                            "Image",
                            tint = GradientStart,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Image",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = GradientStart
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Display current image or selected image
                        val imageToShow = selectedImageUri?.toString() ?: form.imageUrl
                        if (imageToShow != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageToShow)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Word image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    "No image",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = { imagePicker.launch("image/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(GradientStart, GradientEnd)
                                        ),
                                        shape = RoundedCornerShape(40.dp)
                                    ),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    Icons.Default.Upload, 
                                    "Upload",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Upload Image",
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                            if (imageToShow != null) {
                                TextButton(
                                    onClick = {
                                        selectedImageUri = null
                                        viewModel.updateFormState(form.copy(imageUrl = null))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Remove", fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            // Audio Upload
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val audioToShow = selectedAudioUri?.toString() ?: form.audioUrl
                    if (audioToShow != null) {
                        IconButton(
                            onClick = {
                                try {
                                    mediaPlayer?.release()
                                    mediaPlayer = MediaPlayer().apply {
                                        setDataSource(context, Uri.parse(audioToShow))
                                        prepare()
                                        start()
                                        setOnCompletionListener { release() }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.VolumeUp, 
                                "Play audio",
                                tint = GradientStart,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Audio file selected",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                fontWeight = FontWeight.Medium,
                                color = NavBarText
                            )
                            TextButton(
                                onClick = {
                                    selectedAudioUri = null
                                    viewModel.updateFormState(form.copy(audioUrl = null))
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Remove", fontWeight = FontWeight.Medium)
                            }
                        }
                    } else {
                        Icon(
                            Icons.Default.MusicNote,
                            "No audio",
                            modifier = Modifier.size(32.dp),
                            tint = GradientStart
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "No audio file",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = NavBarText,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Upload an audio file for pronunciation",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = NavBarText.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Button(
                            onClick = { audioPicker.launch("audio/*") },
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(GradientStart, GradientEnd)
                                    ),
                                    shape = RoundedCornerShape(40.dp)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                Icons.Default.Upload, 
                                "Upload",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Upload", 
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Error Message
            state.actionError?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Close, 
                        "Cancel",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        scope.launch {
                            isUploading = true

                            var finalImageUrl = form.imageUrl
                            var finalAudioUrl = form.audioUrl

                            if (selectedImageUri != null) {
                                FileUploadHelper.uploadImage(context, selectedImageUri!!)
                                    .fold(
                                        ifLeft = { error ->
                                            viewModel.setActionError(error.message)
                                            isUploading = false
                                            return@launch
                                        },
                                        ifRight = { imageUrl ->
                                            finalImageUrl = imageUrl
                                        }
                                    )
                            }

                            if (selectedAudioUri != null) {
                                FileUploadHelper.uploadAudio(context, selectedAudioUri!!)
                                    .fold(
                                        ifLeft = { error ->
                                            viewModel.setActionError(error.message)
                                            isUploading = false
                                            return@launch
                                        },
                                        ifRight = { audioUrl ->
                                            finalAudioUrl = audioUrl
                                        }
                                    )
                            }

                            if (wordId == null) {
                                viewModel.onEvent(
                                    WordManagementEvent.Create(
                                        word = form.word,
                                        meaning = form.meaning,
                                        vnMeaning = form.vnMeaning,
                                        phonetic = form.phonetic,
                                        cefrLevel = form.cefrLevel,
                                        type = form.type,
                                        example = form.example,
                                        exampleTranslation = form.exampleTranslation,
                                        audioUrl = finalAudioUrl,
                                        imageUrl = finalImageUrl,
                                        topicId = form.topicId
                                    )
                                )
                            } else {
                                viewModel.onEvent(
                                    WordManagementEvent.Update(
                                        wordId = wordId,
                                        word = form.word,
                                        meaning = form.meaning,
                                        vnMeaning = form.vnMeaning,
                                        phonetic = form.phonetic,
                                        cefrLevel = form.cefrLevel,
                                        type = form.type,
                                        example = form.example,
                                        exampleTranslation = form.exampleTranslation,
                                        audioUrl = finalAudioUrl,
                                        imageUrl = finalImageUrl,
                                        topicId = form.topicId
                                    )
                                )
                            }

                            selectedImageUri = null
                            selectedAudioUri = null
                            isUploading = false
                        }
                    },
                    enabled = form.isValid && !isUploading,
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            alpha = if (form.isValid && !isUploading) 1f else 0.5f
                        }
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(GradientStart, GradientEnd)
                            ),
                            shape = RoundedCornerShape(40.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color.White.copy(alpha = 0.7f),
                        contentColor = Color.White
                    )
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Uploading...",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            if (wordId == null) Icons.Default.Add else Icons.Default.Save, 
                            if (wordId == null) "Create" else "Save",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (wordId == null) "Create" else "Save",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }
}

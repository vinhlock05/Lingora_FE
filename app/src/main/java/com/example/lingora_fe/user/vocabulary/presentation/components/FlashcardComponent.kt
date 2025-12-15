package com.example.lingora_fe.user.vocabulary.presentation.components

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.lingora_fe.R
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.TopBarBorder
import com.example.lingora_fe.user.vocabulary.domain.model.Word

@Composable
fun FlashcardComponent(
    word: Word,
    isRevealed: Boolean,
    onCardClick: () -> Unit,
    onPronunciationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rotated by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val flipPlayer = remember { MediaPlayer.create(context, R.raw.flip) }

    var isFirstLaunch by remember { mutableStateOf(true) }

    LaunchedEffect(isRevealed) {
        if (isFirstLaunch) {
            isFirstLaunch = false
            return@LaunchedEffect  // Không chạy flip lúc mới vào
        }

        rotated = isRevealed
        try {
            flipPlayer.seekTo(0)
            flipPlayer.start()
        } catch (_: Exception) {}
    }
    DisposableEffect(Unit) {
        onDispose { try { flipPlayer.release() } catch (_: Exception) { } }
    }

    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "card_rotation"
    )

    val animateFront by animateFloatAsState(
        targetValue = if (!rotated) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "front_fade"
    )

    val animateBack by animateFloatAsState(
        targetValue = if (rotated) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "back_fade"
    )

    Card(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .height(500.dp)
            .border(BorderStroke(1.dp, Color(0xFFE5E7EB)), shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            if (rotation <= 90f) {
                FlashcardFrontSide(
                    word = word,
                    alpha = animateFront,
                    onPronunciationClick = onPronunciationClick
                )
            } else {
                FlashcardBackSide(
                    word = word,
                    alpha = animateBack,
                    onPronunciationClick = onPronunciationClick,
                    modifier = Modifier.graphicsLayer {
                        rotationY = 180f
                    }
                )
            }
        }
    }
}

@Composable
fun FlashcardFrontSide(
    word: Word,
    alpha: Float,
    onPronunciationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(32.dp)
            .verticalScroll(scrollState)
            .graphicsLayer { this.alpha = alpha },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!word.imageUrl.isNullOrEmpty()) {
            Card(
                modifier = Modifier
                    .size(200.dp)
                    .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(word.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = word.word,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Text(
            text = word.word,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = GradientStart,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        val hasAudio = !word.audioUrl.isNullOrEmpty()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onPronunciationClick,
                enabled = hasAudio,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Pronounce",
                    tint = if (hasAudio) GradientEnd else GradientEnd.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
            word.phonetic?.let { phonetic ->
                Text(
                    text = phonetic,
                    style = MaterialTheme.typography.bodyLarge,
                    color = GradientEnd
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Nhấn để xem nghĩa",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FlashcardBackSide(
    word: Word,
    alpha: Float,
    onPronunciationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(32.dp)
            .verticalScroll(scrollState)
            .graphicsLayer { this.alpha = alpha },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        word.meaning?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (word.example != null || word.exampleTranslation != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        TopBarBorder,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    word.example?.let { example ->
                        Text(
                            text = "\"$example\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    word.exampleTranslation?.let { exampleTranslation ->
                        if (word.example != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text(
                            text = exampleTranslation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val hasAudio = !word.audioUrl.isNullOrEmpty()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onPronunciationClick,
                enabled = hasAudio,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Pronounce",
                    tint = if (hasAudio) GradientEnd else GradientEnd.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
            word.phonetic?.let { phonetic ->
                Text(
                    text = phonetic,
                    style = MaterialTheme.typography.bodyLarge,
                    color = GradientEnd
                )
            }
        }
    }
}


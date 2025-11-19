package com.example.lingora_fe.user.studyset.presentation.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.user.studyset.presentation.viewmodel.StudySetFlashcardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySetFlashcardScreen(
    studySetId: Int,
    onBackClick: () -> Unit,
    viewModel: StudySetFlashcardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val studySet = uiState.studySet
    val flashcards = studySet?.flashcards ?: emptyList()

    if (flashcards.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Không có flashcard nào")
                Button(onClick = onBackClick) {
                    Text("Quay lại")
                }
            }
        }
        return
    }

    val currentFlashcard = flashcards.getOrNull(uiState.currentIndex) ?: return
    val density = LocalDensity.current
    val cameraDistance = 8f * density.density

    // Flip animation
    val rotation by animateFloatAsState(
        targetValue = if (uiState.isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "card_flip"
    )

    val frontAlpha by animateFloatAsState(
        targetValue = if (uiState.isFlipped) 0f else 1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "front_alpha"
    )

    val backAlpha by animateFloatAsState(
        targetValue = if (uiState.isFlipped) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "back_alpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(studySet?.title ?: "Flashcard") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Progress indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Thẻ ${uiState.currentIndex + 1}/${flashcards.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NavBarText
                )
                Text(
                    text = "${uiState.learnedCount}/${flashcards.size} đã học",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NavBarText
                )
            }

            // Flashcard with flip animation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clickable(onClick = { viewModel.flipCard() }),
                contentAlignment = Alignment.Center
            ) {
                // Front side
                if (frontAlpha > 0f) {
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = frontAlpha
                                rotationY = rotation
                                this.cameraDistance = cameraDistance
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(GradientStart, GradientEnd)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = currentFlashcard.frontText,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                
                                Text(
                                    text = "Nhấn để xem nghĩa",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Back side
                if (backAlpha > 0f) {
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = backAlpha
                                rotationY = rotation + 180f
                                this.cameraDistance = cameraDistance
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(GradientStart, GradientEnd)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = currentFlashcard.backText,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                
                                if (!currentFlashcard.example.isNullOrBlank()) {
                                    Text(
                                        text = currentFlashcard.example,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.9f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Flip button
            Button(
                onClick = { viewModel.flipCard() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GradientStart
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (uiState.isFlipped) "Lật lại" else "Lật thẻ")
            }

            // Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { viewModel.previousCard() },
                    enabled = uiState.currentIndex > 0
                ) {
                    Text("< Trước")
                }

                // Dots indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    flashcards.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (index == uiState.currentIndex) GradientStart else Color.Gray.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable { viewModel.goToCard(index) }
                        )
                    }
                }

                TextButton(
                    onClick = { viewModel.nextCard() },
                    enabled = uiState.currentIndex < flashcards.size - 1
                ) {
                    Text("Sau >")
                }
            }
        }
    }
}

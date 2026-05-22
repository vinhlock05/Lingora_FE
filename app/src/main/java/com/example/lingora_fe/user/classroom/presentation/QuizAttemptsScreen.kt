package com.example.lingora_fe.user.classroom.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.user.classroom.domain.model.QuizAttemptWithUser
import com.example.lingora_fe.user.classroom.presentation.components.ClassroomColors
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizAttemptsScreen(
    navController: NavController,
    viewModel: QuizAttemptsViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value

    Scaffold(
        containerColor = ClassroomColors.ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Kết quả học sinh",
                        fontWeight = FontWeight.SemiBold,
                        color = MainText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MainText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ClassroomColors.BrandPrimary)
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyLarge,
                            color = ClassroomColors.Danger
                        )
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ClassroomColors.BrandPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Thử lại")
                        }
                    }
                }
            }

            state.attempts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = ClassroomColors.TextPlaceholder,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Chưa có học sinh nào nộp bài",
                            style = MaterialTheme.typography.bodyLarge,
                            color = ClassroomColors.TextMuted
                        )
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = ClassroomColors.BrandSoftSurface
                    ) {
                        Text(
                            text = "${state.attempts.size} lượt nộp bài",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = ClassroomColors.BrandPrimaryStrong,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.attempts) { attempt ->
                            AttemptCard(attempt = attempt)
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttemptCard(attempt: QuizAttemptWithUser) {
    val scorePercent = (attempt.score * 100).toInt()
    val scoreColor = when {
        scorePercent >= 80 -> ClassroomColors.BrandPrimaryStrong
        scorePercent >= 50 -> Color(0xFFF59E0B)
        else -> ClassroomColors.Danger
    }
    val submittedText = attempt.submittedAt?.let {
        SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(it)
    } ?: "—"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ClassroomColors.CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!attempt.user?.avatar.isNullOrEmpty() && attempt.user?.avatar != "N/A") {
                AsyncImage(
                    model = attempt.user?.avatar,
                    contentDescription = attempt.user?.username,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(ClassroomColors.BrandSoftSurface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (attempt.user?.username?.firstOrNull()?.uppercaseChar() ?: 'S').toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClassroomColors.BrandPrimaryStrong
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = attempt.user?.username ?: "Học sinh",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MainText
                )
                Text(
                    text = "Lần ${attempt.attemptNumber}  •  $submittedText",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassroomColors.TextMuted
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = scoreColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "$scorePercent%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

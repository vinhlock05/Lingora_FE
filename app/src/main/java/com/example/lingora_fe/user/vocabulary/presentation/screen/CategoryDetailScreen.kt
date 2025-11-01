package com.example.lingora_fe.user.vocabulary.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lingora_fe.core.ui.theme.TopBarBorder
import com.example.lingora_fe.user.vocabulary.presentation.components.TopicCard

data class TopicUiState(
    val id: Int,
    val name: String,
    val description: String,
    val level: String,
    val duration: String,
    val learnedWords: Int,
    val totalWords: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryName: String,
    onBackClick: () -> Unit,
    onTopicClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sample data - replace with ViewModel later
    val topics = listOf(
        TopicUiState(
            id = 1,
            name = "Chào hỏi & Giới thiệu",
            description = "Cách chào hỏi và giới thiệu bản thân",
            level = "Beginner",
            duration = "15 phút",
            learnedWords = 30,
            totalWords = 50
        ),
        TopicUiState(
            id = 2,
            name = "Gia đình & Bạn bè",
            description = "Từ vựng về gia đình và mối quan hệ",
            level = "Beginner",
            duration = "20 phút",
            learnedWords = 0,
            totalWords = 60
        ),
        TopicUiState(
            id = 2,
            name = "Gia đình & Bạn bè",
            description = "Từ vựng về gia đình và mối quan hệ",
            level = "Beginner",
            duration = "20 phút",
            learnedWords = 0,
            totalWords = 60
        ),
        TopicUiState(
            id = 2,
            name = "Gia đình & Bạn bè",
            description = "Từ vựng về gia đình và mối quan hệ",
            level = "Beginner",
            duration = "20 phút",
            learnedWords = 0,
            totalWords = 60
        ),
        TopicUiState(
            id = 2,
            name = "Gia đình & Bạn bè",
            description = "Từ vựng về gia đình và mối quan hệ",
            level = "Beginner",
            duration = "20 phút",
            learnedWords = 0,
            totalWords = 60
        ),
        TopicUiState(
            id = 2,
            name = "Gia đình & Bạn bè",
            description = "Từ vựng về gia đình và mối quan hệ",
            level = "Beginner",
            duration = "20 phút",
            learnedWords = 0,
            totalWords = 60
        ),
        TopicUiState(
            id = 2,
            name = "Gia đình & Bạn bè",
            description = "Từ vựng về gia đình và mối quan hệ",
            level = "Beginner",
            duration = "20 phút",
            learnedWords = 0,
            totalWords = 60
        ),
        TopicUiState(
            id = 2,
            name = "Gia đình & Bạn bè",
            description = "Từ vựng về gia đình và mối quan hệ",
            level = "Beginner",
            duration = "20 phút",
            learnedWords = 0,
            totalWords = 60
        ),
        TopicUiState(
            id = 2,
            name = "Gia đình & Bạn bè",
            description = "Từ vựng về gia đình và mối quan hệ",
            level = "Beginner",
            duration = "20 phút",
            learnedWords = 0,
            totalWords = 60
        )
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .border(width = 1.dp, color = TopBarBorder),
                title = {
                    Text(
                        text = categoryName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(topics) { topic ->
                TopicCard(
                    title = topic.name,
                    description = topic.description,
                    learnedWords = topic.learnedWords,
                    totalWords = topic.totalWords,
                    onClick = { onTopicClick(topic.id) }
                )
            }
        }
    }
}


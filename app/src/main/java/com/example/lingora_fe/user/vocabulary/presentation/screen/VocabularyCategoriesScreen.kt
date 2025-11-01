package com.example.lingora_fe.user.vocabulary.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.lingora_fe.user.vocabulary.presentation.components.CategoryCard

data class CategoryUiState(
    val id: Int,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val topicCount: Int,
    val learnedWords: Int,
    val totalWords: Int
)

@Composable
fun VocabularyCategoriesScreen(
    onCategoryClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sample data - replace with ViewModel later
    val categories = listOf(
        CategoryUiState(
            id = 1,
            name = "Giao tiếp hàng ngày",
            description = "Từ vựng thông dụng cho giao tiếp hàng ngày",
            icon = Icons.Default.Chat,
            topicCount = 2,
            learnedWords = 30,
            totalWords = 110
        ),
        CategoryUiState(
            id = 2,
            name = "Công việc",
            description = "Từ vựng chuyên ngành và môi trường làm việc",
            icon = Icons.Default.Work,
            topicCount = 2,
            learnedWords = 20,
            totalWords = 180
        ),
        CategoryUiState(
            id = 3,
            name = "Du lịch",
            description = "Từ vựng cần thiết cho chuyến đi",
            icon = Icons.Default.Flight,
            topicCount = 2,
            learnedWords = 10,
            totalWords = 100
        ),
        CategoryUiState(
            id = 4,
            name = "IELTS",
            description = "Từ vựng ôn thi IELTS",
            icon = Icons.Default.MenuBook,
            topicCount = 2,
            learnedWords = 35,
            totalWords = 160
        ),
        CategoryUiState(
            id = 4,
            name = "IELTS",
            description = "Từ vựng ôn thi IELTS",
            icon = Icons.Default.MenuBook,
            topicCount = 2,
            learnedWords = 35,
            totalWords = 160
        ),
        CategoryUiState(
            id = 4,
            name = "IELTS",
            description = "Từ vựng ôn thi IELTS",
            icon = Icons.Default.MenuBook,
            topicCount = 2,
            learnedWords = 35,
            totalWords = 160
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryCard(
                title = category.name,
                description = category.description,
                topicCount = category.topicCount,
                learnedWords = category.learnedWords,
                totalWords = category.totalWords,
                onClick = { onCategoryClick(category.id) }
            )
        }
    }
}




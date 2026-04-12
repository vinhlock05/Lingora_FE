package com.example.lingora_fe.user.conversation.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lingora_fe.user.conversation.domain.model.ConversationVocabulary

@Composable
fun VocabularyBlock(
    vocabulary: ConversationVocabulary,
    modifier: Modifier = Modifier
) {
    if (vocabulary.highlight.isNullOrBlank() || vocabulary.meaning.isNullOrBlank()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE8F5E9)) // Light green background
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = "📚 Từ vựng: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF388E3C))
            Text(
                text = vocabulary.highlight,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Bold
            )
        }
        
        Text(
            text = vocabulary.meaning,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF1B5E20)
        )
    }
}

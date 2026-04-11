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
import com.example.lingora_fe.user.conversation.domain.model.ConversationImprovement

@Composable
fun ImprovementBlock(
    improvement: ConversationImprovement,
    modifier: Modifier = Modifier
) {
    if (!improvement.hasImprovement || improvement.improved.isNullOrBlank()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE3F2FD)) // Light blue background
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = "✨ Hay hơn: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF1976D2))
        }
        
        Text(
            text = improvement.improved,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF1565C0),
            fontWeight = FontWeight.Medium
        )

        improvement.explanation?.takeIf { it.isNotBlank() }?.let { explanation ->
            Text(
                text = "💡 $explanation",
                style = MaterialTheme.typography.labelMedium,
                color = Color.DarkGray
            )
        }
    }
}

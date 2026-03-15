package com.example.lingora_fe.user.chatbot.presentation.components

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
import com.example.lingora_fe.user.chatbot.domain.model.ConversationCorrection

@Composable
fun CorrectionBlock(
    correction: ConversationCorrection,
    modifier: Modifier = Modifier
) {
    if (!correction.hasError) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFF3E0)) // Light Orange/Yellow
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "❌ ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = correction.original,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFD32F2F) // Red for error
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "✅ ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = correction.corrected,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF388E3C), // Green for correct
                fontWeight = FontWeight.SemiBold
            )
        }
        
        if (correction.explanation.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "💡 ${correction.explanation}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.DarkGray
            )
        }
    }
}

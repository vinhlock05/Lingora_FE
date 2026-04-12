package com.example.lingora_fe.user.conversation.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lingora_fe.user.conversation.domain.model.ConversationCorrection

@Composable
fun CorrectionBlock(
    correction: ConversationCorrection,
    modifier: Modifier = Modifier
) {
    if (!correction.hasError || correction.errors.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFF3E0))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        correction.errors.forEachIndexed { index, error ->
            if (index > 0) HorizontalDivider(color = Color(0xFFFFCC80), thickness = 0.5.dp)

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "❌ ", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = error.wrong,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFD32F2F)
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "✅ ", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = error.correct,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF388E3C),
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (error.explanation.isNotEmpty()) {
                Text(
                    text = "💡 ${error.explanation}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.DarkGray
                )
            }
        }
    }
}

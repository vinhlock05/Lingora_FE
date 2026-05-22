package com.example.lingora_fe.user.ranking.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lingora_fe.user.ranking.domain.model.XpActionType
import com.example.lingora_fe.user.ranking.domain.model.XpHistoryEntry
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val displayFormatter = DateTimeFormatter
    .ofPattern("dd/MM HH:mm", Locale("vi"))
    .withZone(ZoneId.systemDefault())

@Composable
fun XpHistoryItem(
    entry: XpHistoryEntry,
    modifier: Modifier = Modifier
) {
    val (icon, tint) = iconForAction(entry.actionType)
    val isPositive = entry.xpAmount >= 0
    val timeText = formatTime(entry.createdAt)
    val title = entry.actionType.labelVi
    val subtitle = entry.classroomName?.let { "Lớp: $it" }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(RankingColors.CardSurface)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = RankingColors.TextPrimary,
                fontSize = 14.sp,
                maxLines = 2
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = null,
                    tint = RankingColors.TextPlaceholder,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = RankingColors.TextMuted
                )
                if (subtitle != null) {
                    Text(
                        text = "·",
                        color = RankingColors.TextPlaceholder
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = RankingColors.TextMuted,
                        maxLines = 1
                    )
                }
            }
        }

        Text(
            text = if (isPositive) "+${entry.xpAmount} XP" else "${entry.xpAmount} XP",
            color = if (isPositive) RankingColors.XpPositive else RankingColors.XpNegative,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

private fun iconForAction(action: XpActionType): Pair<ImageVector, Color> = when (action) {
    XpActionType.FLASHCARD_LEARNED,
    XpActionType.WORD_MASTERED -> Icons.Filled.Star to RankingColors.ActionFlashcard
    XpActionType.QUIZ_COMPLETED -> Icons.Filled.Quiz to RankingColors.ActionQuiz
    XpActionType.EXAM_COMPLETED -> Icons.Filled.School to RankingColors.ActionExam
    XpActionType.LESSON_COMPLETED -> Icons.Filled.School to RankingColors.ActionLesson
    XpActionType.CLASSROOM_QUIZ -> Icons.Filled.Class to RankingColors.ActionClassroomQuiz
    XpActionType.CLASSROOM_CHAT -> Icons.Filled.Forum to RankingColors.ActionClassroomChat
    XpActionType.CONVERSATION_ENDED -> Icons.Filled.Mic to RankingColors.ActionConversation
    XpActionType.DAILY_LOGIN -> Icons.Filled.Login to RankingColors.ActionLogin
    XpActionType.STREAK_BONUS -> Icons.Filled.Bolt to RankingColors.ActionStreakBonus
    XpActionType.POST_CREATED -> Icons.Filled.Forum to RankingColors.ActionPost
    XpActionType.ADMIN_ADJUSTMENT -> Icons.Filled.MilitaryTech to RankingColors.ActionSystem
    XpActionType.UNKNOWN -> Icons.Filled.History to RankingColors.ActionSystem
}

private fun formatTime(raw: String): String = runCatching {
    val parsed = OffsetDateTime.parse(raw)
    displayFormatter.format(parsed.toInstant())
}.getOrElse { raw }

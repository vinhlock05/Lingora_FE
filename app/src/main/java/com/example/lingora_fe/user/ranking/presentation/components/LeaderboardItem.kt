package com.example.lingora_fe.user.ranking.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.user.ranking.domain.model.LeaderboardEntry

/**
 * One leaderboard row. Top three ranks get a gold/silver/bronze badge, and the
 * caller's own row is highlighted with a subtle gradient border so it stays
 * visible when scrolling through a long board.
 */
@Composable
fun LeaderboardItem(
    entry: LeaderboardEntry,
    isMe: Boolean,
    modifier: Modifier = Modifier,
    showLevel: Boolean = true,
    showStreak: Boolean = true
) {
    val shape = RoundedCornerShape(16.dp)
    val baseModifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
        .clip(shape)

    val finalModifier = if (isMe) {
        baseModifier
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(listOf(GradientStart, GradientEnd)),
                shape = shape
            )
            .background(RankingColors.MyRowSurface)
    } else {
        baseModifier.background(RankingColors.CardSurface)
    }

    Row(
        modifier = finalModifier
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RankBadge(rank = entry.rank)

        Box(
            modifier = Modifier
                .padding(start = 10.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(RankingColors.ChipOutline)
        ) {
            if (!entry.avatar.isNullOrBlank()) {
                AsyncImage(
                    model = entry.avatar,
                    contentDescription = entry.username,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.username.firstOrNull()?.uppercase() ?: "?",
                        color = RankingColors.TextSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = entry.username + if (isMe) " (Bạn)" else "",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = RankingColors.TextPrimary,
                maxLines = 1
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                if (showLevel && entry.level != null) {
                    SubMeta(
                        icon = {
                            Icon(
                                Icons.Filled.MilitaryTech,
                                contentDescription = null,
                                tint = RankingColors.LevelAccent
                            )
                        },
                        text = "Lv ${entry.level}"
                    )
                }
                if (showStreak && (entry.streak ?: 0) > 0) {
                    SubMeta(
                        icon = {
                            Icon(
                                Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = RankingColors.StreakAccent
                            )
                        },
                        text = "${entry.streak}"
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${entry.xp} XP",
                fontWeight = FontWeight.Bold,
                color = RankingColors.TextPrimary,
                fontSize = 15.sp
            )
            Text(
                text = "Tổng ${entry.totalXp}",
                style = MaterialTheme.typography.labelSmall,
                color = RankingColors.TextMuted
            )
        }
    }
}

@Composable
private fun RankBadge(rank: Int) {
    val tone = when (rank) {
        1 -> RankingColors.GoldBadge
        2 -> RankingColors.SilverBadge
        3 -> RankingColors.BronzeBadge
        else -> RankingColors.DefaultBadge
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(tone.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rank.toString(),
            color = tone.foreground,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SubMeta(
    icon: @Composable () -> Unit,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(16.dp)) { icon() }
        Text(
            text = text,
            color = RankingColors.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

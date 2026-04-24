package com.example.lingora_fe.user.ranking.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Bolt
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
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.user.ranking.domain.model.MyRankingStats
import com.example.lingora_fe.user.ranking.domain.model.RankingPeriod

/**
 * The big gradient header card shown above every ranking tab. It surfaces the
 * caller's level + level-up progress, current period rank, and the two main
 * "engagement" KPIs (streak + activity score).
 */
@Composable
fun MyRankCard(
    stats: MyRankingStats?,
    period: RankingPeriod,
    modifier: Modifier = Modifier
) {
    val rank = stats?.rankFor(period)
    val xp = stats?.xpFor(period) ?: 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(listOf(GradientStart, GradientEnd))
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hạng ${period.labelVi.lowercase()}",
                        color = RankingColors.OnGradientSoft,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = rank?.let { "#$it" } ?: "Chưa xếp hạng",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                MetricChip(
                    icon = { Icon(Icons.Filled.Bolt, contentDescription = null, tint = Color.White) },
                    value = xp.toString(),
                    label = "XP"
                )
            }

            if (stats != null) {
                LevelProgressBar(
                    level = stats.level,
                    progress = stats.levelProgress
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatBadge(
                        icon = {
                            Icon(
                                Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = RankingColors.StreakAccent
                            )
                        },
                        label = "Streak",
                        value = "${stats.streak}",
                        modifier = Modifier.weight(1f)
                    )
                    StatBadge(
                        icon = {
                            Icon(
                                Icons.Filled.MilitaryTech,
                                contentDescription = null,
                                tint = RankingColors.OnGradientIconSoft
                            )
                        },
                        label = "Hoạt động (7d)",
                        value = formatActivityScore(stats.activityScore),
                        modifier = Modifier.weight(1f)
                    )
                    StatBadge(
                        icon = {
                            Icon(
                                Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = Color.White
                            )
                        },
                        label = "Tổng XP",
                        value = "${stats.totalXp}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/** 2.86 -> "2.9", 3.0 -> "3". Keeps the badge compact. */
private fun formatActivityScore(value: Float): String =
    if (value % 1f == 0f) value.toInt().toString()
    else "%.1f".format(value)

@Composable
private fun MetricChip(
    icon: @Composable () -> Unit,
    value: String,
    label: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(RankingColors.OnGradientFaint),
            contentAlignment = Alignment.Center,
            content = { icon() }
        )
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = RankingColors.OnGradientSoft,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun StatBadge(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(RankingColors.OnGradientFaint),
            contentAlignment = Alignment.Center,
            content = { icon() }
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = RankingColors.OnGradientSoft,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

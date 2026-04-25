package com.example.lingora_fe.user.ranking.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.lingora_fe.user.ranking.domain.model.LevelProgress

@Composable
fun LevelProgressBar(
    level: Int,
    progress: LevelProgress,
    modifier: Modifier = Modifier
) {
    val animated by animateFloatAsState(
        targetValue = progress.progress.coerceIn(0f, 1f),
        label = "level-progress"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cấp $level",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = " → ",
                color = RankingColors.OnGradientSoft,
                fontSize = 14.sp
            )
            Text(
                text = "Cấp ${level + 1}",
                color = RankingColors.OnGradientSoft,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Box(modifier = Modifier.weight(1f))
            Text(
                text = "${progress.current} / ${progress.needed} XP",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(RankingColors.OnGradientBarTrack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animated)
                    .clip(RoundedCornerShape(50))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.White, RankingColors.OnGradientBarHighlight)
                        )
                    )
            )
        }
    }
}

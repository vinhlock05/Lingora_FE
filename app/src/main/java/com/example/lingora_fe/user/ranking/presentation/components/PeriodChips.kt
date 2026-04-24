package com.example.lingora_fe.user.ranking.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.user.ranking.domain.model.RankingPeriod

@Composable
fun PeriodChips(
    selected: RankingPeriod,
    onSelected: (RankingPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RankingPeriod.values().forEach { period ->
            PeriodChip(
                label = period.labelVi,
                selected = selected == period,
                onClick = { onSelected(period) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PeriodChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    val backgroundModifier = if (selected) {
        Modifier.background(
            brush = Brush.horizontalGradient(listOf(GradientStart, GradientEnd)),
            shape = shape
        )
    } else {
        Modifier
            .background(color = RankingColors.CardSurface, shape = shape)
            .border(width = 1.dp, color = RankingColors.ChipOutline, shape = shape)
    }

    Row(
        modifier = modifier
            .clip(shape)
            .then(backgroundModifier)
            .clickable(onClick = onClick)
            .height(40.dp)
            .padding(PaddingValues(horizontal = 12.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else RankingColors.TextSecondary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

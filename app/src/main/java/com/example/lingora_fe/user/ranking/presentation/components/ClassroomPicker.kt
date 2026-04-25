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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.lingora_fe.user.classroom.domain.model.Classroom

@Composable
fun ClassroomPicker(
    classrooms: List<Classroom>,
    selectedId: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (classrooms.isEmpty()) return

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = classrooms, key = { it.id }) { classroom ->
            ClassroomChip(
                name = classroom.name,
                selected = classroom.id == selectedId,
                onClick = { onSelect(classroom.id) }
            )
        }
    }
}

@Composable
private fun ClassroomChip(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val backgroundModifier = if (selected) {
        Modifier.background(
            brush = Brush.horizontalGradient(listOf(GradientStart, GradientEnd)),
            shape = shape
        )
    } else {
        Modifier
            .background(RankingColors.CardSurface, shape)
            .border(width = 1.dp, color = RankingColors.ChipOutline, shape = shape)
    }

    Row(
        modifier = Modifier
            .clip(shape)
            .then(backgroundModifier)
            .clickable(onClick = onClick)
            .height(36.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = if (selected) Color.White else RankingColors.TextSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

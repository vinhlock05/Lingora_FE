package com.example.lingora_fe.user.studyset.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.user.studyset.domain.model.StudySet
import com.example.lingora_fe.user.studyset.domain.model.StudySetStatus
import com.example.lingora_fe.user.studyset.domain.model.StudySetVisibility
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MyStudySetCard(
    studySet: StudySet,
    onClick: () -> Unit,
    onLikeClick: () -> Unit = {},
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val menuExpanded = remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = studySet.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MainText,
                    modifier = Modifier.weight(1f)
                )

                if (onEditClick != null || onDeleteClick != null) {
                    Box {
                        IconButton(onClick = { menuExpanded.value = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More actions"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded.value,
                            onDismissRequest = { menuExpanded.value = false }
                        ) {
                            onEditClick?.let { edit ->
                                DropdownMenuItem(
                                    text = { Text("Chỉnh sửa") },
                                    onClick = {
                                        menuExpanded.value = false
                                        edit()
                                    }
                                )
                            }
                            onDeleteClick?.let { delete ->
                                DropdownMenuItem(
                                    text = { Text("Xóa") },
                                    onClick = {
                                        menuExpanded.value = false
                                        delete()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Description
            if (!studySet.description.isNullOrBlank()) {
                Text(
                    text = studySet.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NavBarText,
                    maxLines = 2
                )
            }

            // Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Flashcard badge
                    val cardCount = studySet.totalFlashcards ?: studySet.flashcards.size
                    if (cardCount > 0) {
                        BadgeChip(text = "$cardCount thẻ", color = Color(0xFFE0F2FE), textColor = Color(0xFF0369A1))
                    }
                    
                    // Quiz badge
                    val quizCount = studySet.totalQuizzes ?: studySet.quizzes.size
                    if (quizCount > 0) {
                        BadgeChip(text = "$quizCount câu hỏi", color = Color(0xFFE0F2FE), textColor = Color(0xFF0369A1))
                    }
                    
                    // Status badge
                    StatusBadge(
                        visibility = studySet.visibility,
                        status = studySet.status
                    )
                }
                
                // Price
                if (studySet.price > 0) {
                    Text(
                        text = formatPrice(studySet.price),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GradientStart,
                        fontSize = 16.sp
                    )
                } else {
                    Text(
                        text = "Miễn phí",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        fontSize = 16.sp
                    )
                }
            }

            // Like Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onLikeClick)
                ) {
                    Icon(
                        imageVector = if (studySet.isAlreadyLike) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (studySet.isAlreadyLike) Color(0xFFEF4444) else NavBarText,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${studySet.likeCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NavBarText
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(
    text: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = color,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun StatusBadge(
    visibility: StudySetVisibility,
    status: StudySetStatus
) {
    val (text, backgroundColor, textColor) = when {
        visibility == StudySetVisibility.PUBLIC && status == StudySetStatus.PUBLISHED -> 
            Triple("Công khai", Color(0xFFDCFCE7), Color(0xFF10B981))
        visibility == StudySetVisibility.PUBLIC && status != StudySetStatus.PUBLISHED -> 
            Triple("Chờ duyệt", Color(0xFFFFF4E6), Color(0xFFF59E0B))
        else -> 
            Triple("Riêng tư", Color(0xFFE5E7EB), Color(0xFF6B7280))
    }
    
    BadgeChip(
        text = text,
        color = backgroundColor,
        textColor = textColor
    )
}

private fun formatPrice(price: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(price)}₫"
}


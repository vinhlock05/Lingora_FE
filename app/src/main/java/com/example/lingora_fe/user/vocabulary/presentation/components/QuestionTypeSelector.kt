package com.example.lingora_fe.user.vocabulary.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType

@Composable
fun QuestionTypeSelector(
    selectedTypes: Set<GameType>,
    onToggle: (GameType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Tiêu đề phần chọn
        Text(
            text = "Loại câu hỏi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Text(
            text = "Chọn ít nhất hai loại câu hỏi",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B) // Xám nhạt hơn
        )

        // Container danh sách game
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF5F5F5))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            questionTypeOptions.forEachIndexed { index, option ->
                val isSelected = selectedTypes.contains(option.type)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(option.type) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = option.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            text = option.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B) // xám phụ
                        )
                    }

                    Switch(
                        checked = isSelected,
                        onCheckedChange = { onToggle(option.type) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GradientStart,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD1D5DB),
                            uncheckedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                }

                // Divider giữa các dòng
                if (index < questionTypeOptions.lastIndex) {
                    Divider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = Color(0xFFE2E8F0),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}



private data class QuestionTypeOption(
    val type: GameType,
    val title: String,
    val description: String
)

private val questionTypeOptions = listOf(
    QuestionTypeOption(
        type = GameType.LISTEN_FILL,
        title = "Nghe điền từ",
        description = "Nghe phát âm và nhập chính xác từ tiếng Anh"
    ),
    QuestionTypeOption(
        type = GameType.LISTEN_CHOOSE,
        title = "Nghe chọn từ",
        description = "Nghe phát âm và chọn từ đúng từ danh sách"
    ),
    QuestionTypeOption(
        type = GameType.TRUE_FALSE,
        title = "Đúng/Sai",
        description = "Xác định nghĩa tiếng Việt đúng với từ"
    ),
    QuestionTypeOption(
        type = GameType.SEE_WORD_CHOOSE_MEANING,
        title = "Nhìn từ chọn nghĩa",
        description = "Chọn nghĩa đúng cho từ tiếng Anh"
    ),
    QuestionTypeOption(
        type = GameType.SEE_MEANING_CHOOSE_WORD,
        title = "Nhìn nghĩa chọn từ",
        description = "Chọn từ tiếng Anh phù hợp với nghĩa"
    )
)
package com.example.lingora_fe.user.conversation.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PhaseIndicator(
    currentPhase: String,
    modifier: Modifier = Modifier
) {
    val phases = listOf("opening", "developing", "closing", "completed")
    val currentIndex = phases.indexOf(currentPhase.lowercase()).takeIf { it >= 0 } ?: 0
    
    val displayNames = listOf("Mở đầu", "Phát triển", "Kết thúc", "Hoàn thành")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        phases.forEachIndexed { index, _ ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (index <= currentIndex) MaterialTheme.colorScheme.primary 
                            else Color(0xFFE0E0E0)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (index + 1).toString(),
                        color = if (index <= currentIndex) Color.White else Color.Gray,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = displayNames[index],
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index <= currentIndex) MaterialTheme.colorScheme.primary else Color.Gray,
                    fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal
                )
            }
            
            if (index < phases.size - 1) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(30.dp)
                        .height(2.dp)
                        .background(
                            if (index < currentIndex) MaterialTheme.colorScheme.primary 
                            else Color(0xFFE0E0E0)
                        )
                        .align(Alignment.CenterVertically)
                        .offset(y = (-10).dp)
                )
            }
        }
    }
}

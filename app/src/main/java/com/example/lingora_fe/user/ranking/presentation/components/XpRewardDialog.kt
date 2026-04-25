package com.example.lingora_fe.user.ranking.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Celebratory popup shown when the backend has awarded XP to the user.
 * Animations:
 *  - Dialog enters with a spring scale + fade.
 *  - XP counter tweens from 0 → xpDelta over 900ms.
 *  - Level progress bar animates to its new value.
 *  - When `leveledUp` is true, a trophy badge pulses and a "LEVEL UP!"
 *    label appears below the counter.
 *  - 18 small confetti dots fall behind the card for the first 1.6s.
 *
 * Accessibility:
 *  - `dismissOnBackPress = true`, `dismissOnClickOutside = true`, and there's
 *    an explicit "Tuyệt vời!" button so users can close with one tap.
 */
@Composable
fun XpRewardDialog(
    visible: Boolean,
    xpDelta: Int,
    newTotalXp: Int,
    newLevel: Int,
    leveledUp: Boolean,
    sourceActionKey: String?,
    xpPerLevel: Int = 100,
    onDismiss: () -> Unit
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            ConfettiOverlay(modifier = Modifier.fillMaxSize())

            AnimatedVisibility(
                visible = true,
                enter = scaleIn(initialScale = 0.6f) + fadeIn(),
                exit = scaleOut(targetScale = 0.9f) + fadeOut()
            ) {
                RewardCard(
                    xpDelta = xpDelta,
                    newTotalXp = newTotalXp,
                    newLevel = newLevel,
                    leveledUp = leveledUp,
                    sourceActionKey = sourceActionKey,
                    xpPerLevel = xpPerLevel,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun RewardCard(
    xpDelta: Int,
    newTotalXp: Int,
    newLevel: Int,
    leveledUp: Boolean,
    sourceActionKey: String?,
    xpPerLevel: Int,
    onDismiss: () -> Unit
) {
    // --- Counter animation: tween from 0 → xpDelta. ---
    var displayedXp by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(xpDelta) {
        scope.launch {
            val animated = Animatable(0f)
            animated.animateTo(
                targetValue = xpDelta.toFloat(),
                animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
            ) {
                displayedXp = value.toInt()
            }
            // Ensure we settle exactly on xpDelta even after rounding.
            displayedXp = xpDelta
        }
    }

    // --- Level progress bar: XP inside the current level, post-award. ---
    val xpInsideLevel = (newTotalXp - (newLevel - 1) * xpPerLevel)
        .coerceIn(0, xpPerLevel)
    val progressTarget = xpInsideLevel.toFloat() / xpPerLevel.toFloat()
    val progress by animateFloatAsState(
        targetValue = progressTarget.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "reward-level-progress"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 28.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(listOf(GradientStart, GradientEnd))
            )
            .padding(vertical = 28.dp, horizontal = 24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TrophyBadge(leveledUp = leveledUp)

            Text(
                text = if (leveledUp) "Lên cấp!" else "Tuyệt vời!",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )

            if (sourceActionKey != null) {
                Text(
                    text = "Bạn vừa hoàn thành: $sourceActionKey",
                    color = RankingColors.OnGradientSoft,
                    fontSize = 13.sp
                )
            }

            // --- Giant animated XP number. ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = Color(0xFFFFF59D),
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = "+$displayedXp XP",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 40.sp
                )
            }

            // --- Level meta + progress. ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Cấp $newLevel",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Box(modifier = Modifier.weight(1f))
                    Text(
                        text = "$xpInsideLevel / $xpPerLevel XP",
                        color = RankingColors.OnGradientSoft,
                        fontSize = 13.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(RankingColors.OnGradientBarTrack)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(RoundedCornerShape(50))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.White,
                                        RankingColors.OnGradientBarHighlight
                                    )
                                )
                            )
                    )
                }
            }

            Text(
                text = "Tổng XP: $newTotalXp",
                color = RankingColors.OnGradientSoft,
                fontSize = 13.sp
            )

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = GradientStart
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tuyệt vời!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun TrophyBadge(leveledUp: Boolean) {
    // Gentle pulse on level-up; static size otherwise. Always registering the
    // infinite transition simplifies the composition graph and keeps animations
    // predictable across recomposition.
    val transition = rememberInfiniteTransition(label = "trophy-pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (leveledUp) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophy-pulse-scale"
    )

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(RankingColors.OnGradientFaint),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.EmojiEvents,
            contentDescription = null,
            tint = RankingColors.OnGradientIconSoft,
            modifier = Modifier.size((42 * pulseScale).dp)
        )
    }
}

// ─────────────────────── confetti overlay ───────────────────────────

private data class ConfettiPiece(
    val id: Int,
    val startX: Float,
    val startRotation: Float,
    val color: Color,
    val delayMs: Int
)

@Composable
private fun ConfettiOverlay(modifier: Modifier = Modifier) {
    val pieces = remember {
        val colors = listOf(
            Color(0xFFFFD54F),
            Color(0xFFFF7043),
            Color(0xFF64B5F6),
            Color(0xFFA5D6A7),
            Color(0xFFCE93D8),
            Color.White
        )
        (0 until 18).map { i ->
            ConfettiPiece(
                id = i,
                startX = Random.nextFloat(),
                startRotation = Random.nextFloat() * 360f,
                color = colors[i % colors.size],
                delayMs = Random.nextInt(0, 500)
            )
        }
    }

    Box(modifier = modifier) {
        pieces.forEach { piece ->
            ConfettiDot(piece = piece)
        }
    }
}

@Composable
private fun ConfettiDot(piece: ConfettiPiece) {
    val fall = remember { Animatable(0f) }
    val spin = rememberInfiniteTransition(label = "confetti-spin-${piece.id}")
    val rotation by spin.animateFloat(
        initialValue = piece.startRotation,
        targetValue = piece.startRotation + 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti-rotation"
    )

    LaunchedEffect(piece.id) {
        fall.snapTo(0f)
        fall.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1600,
                delayMillis = piece.delayMs,
                easing = LinearEasing
            )
        )
    }

    // Position is computed via fractional offsets on Box to avoid needing an
    // explicit canvas or size lookup.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = (piece.startX * 100).dp,
                top = (fall.value * 400).dp
            )
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .rotate(rotation)
                .clip(RoundedCornerShape(2.dp))
                .background(piece.color)
        )
    }
}

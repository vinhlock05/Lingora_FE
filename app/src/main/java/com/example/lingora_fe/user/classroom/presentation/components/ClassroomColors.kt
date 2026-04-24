package com.example.lingora_fe.user.classroom.presentation.components

import androidx.compose.ui.graphics.Color
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.GradientStart700

/**
 * Centralised colour tokens for the Classroom module.
 *
 * Mirrors the approach used by `RankingColors`: every classroom screen used
 * to reach for one-off hex values (`#5CB85C`, `#81C784`, `#10B981`, `#3B82F6`
 * for the Join button, ...). That made the module drift away from the
 * app-wide green/teal brand palette defined in `core/ui/theme/Color.kt`.
 *
 * Everything UI-facing inside `user/classroom/...` now consumes this object,
 * so retoning the module is a single-file change.
 */
internal object ClassroomColors {

    // --- Brand primaries (re-exported for convenience) -----------------

    val BrandPrimary = GradientStart          // #00BC7D
    val BrandSecondary = GradientEnd          // #00BBA7
    val BrandPrimaryStrong = GradientStart700 // #00A63E — used for text/accents on white

    // --- Surfaces & backgrounds ---------------------------------------

    /** Page background behind cards. Same tone as Ranking module. */
    val ScreenBackground = Color(0xFFF0F9F4)
    val CardSurface = Color.White
    val HeaderSurface = Color.White

    /** Soft brand-tinted surface for selected chips / reply bar / success states. */
    val BrandSoftSurface = Color(0xFFE6F7EF)

    /** Neutral surface used by search-bar fill and muted cards. */
    val NeutralSurface = Color(0xFFF8FAFC)
    val NeutralBorder = Color(0xFFE2E8F0)

    /** Draft / archived cards. */
    val MutedCardSurface = Color(0xFFF5F5F5)

    /** Pending-member warning surface — replaces harsh yellow `#FFF9C4`. */
    val PendingSurface = Color(0xFFFFF7E6)
    val PendingAccent = Color(0xFFB45309)

    /** Cover-image placeholder (before a real image loads). */
    val CoverPlaceholder = Color(0xFFE5F3EB)

    // --- Neutral text -------------------------------------------------

    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF4B5563)
    val TextMuted = Color(0xFF6B7280)
    val TextPlaceholder = Color(0xFF94A3B8)

    // --- Status chips -------------------------------------------------

    /** "Công khai" chip — brand-soft on brand-strong text. */
    val PublicChipBackground = BrandSoftSurface
    val PublicChipText = BrandPrimaryStrong

    /** "Riêng tư" chip — warm amber to feel locked but not alarming. */
    val PrivateChipBackground = Color(0xFFFFF4E5)
    val PrivateChipText = Color(0xFFB45309)

    /** "Đang mở" row text — subtle brand green. */
    val ActiveStatusText = BrandPrimaryStrong

    // --- Chat --------------------------------------------------------

    /** My message bubble. Slightly deeper than BrandPrimary so white text pops. */
    val MyBubble = Color(0xFF059669)

    /** Embedded quote block inside my bubble — darker brand. */
    val MyBubbleQuote = Color(0xFF047857)

    /** Other user's bubble — soft neutral. */
    val OtherBubble = Color(0xFFF3F4F6)

    /** Embedded quote block inside other bubble — one shade deeper. */
    val OtherBubbleQuote = Color(0xFFE5E7EB)

    /** Reply preview bar background (above input). */
    val ReplyBarSurface = BrandSoftSurface

    // --- Error / destructive -----------------------------------------

    val Danger = Color(0xFFDC2626)
    val DangerSoft = Color(0xFFFEE2E2)
}

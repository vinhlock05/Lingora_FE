package com.example.lingora_fe.user.ranking.presentation.components

import androidx.compose.ui.graphics.Color

/**
 * Centralised colour tokens for the Ranking module.
 *
 * The rest of the app uses a green/teal brand palette (see
 * `core/ui/theme/Color.kt`: `GradientStart`, `GradientEnd`, `MainText`, ...).
 * Previously every ranking component reached for random hex values which
 * pushed the screens away from the brand (purple level chips, orange streaks,
 * fuchsia mics, etc). Everything UI-facing inside `user/ranking/...` now
 * consumes this object so a single edit here retones the whole module.
 *
 * Semantic buckets:
 *  - `OnGradient*`: tints used *on top of* the green gradient card (whites and
 *    soft golds so they pop without fighting the brand).
 *  - `Neutral*`: text/border/surface shades (kept as warm greys that read well
 *    against a #F0F9F4 background).
 *  - `Accent*`: semantic accents (streak, level, positive/negative XP).
 *  - `ActionTint*`: per-action-type tints used by `XpHistoryItem`. Re-ordered
 *    so no two adjacent actions share a hue and nothing clashes with brand
 *    green.
 */
internal object RankingColors {

    // --- Surfaces & backgrounds ---------------------------------------

    val ScreenBackground = Color(0xFFF0F9F4)
    val CardSurface = Color.White
    val MyRowSurface = Color(0xFFE6FFF6)
    val ChipOutline = Color(0xFFE5E7EB)

    // --- Neutral text -------------------------------------------------

    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF4B5563)
    val TextMuted = Color(0xFF6B7280)
    val TextPlaceholder = Color(0xFF9CA3AF)

    // --- On-gradient (white + subtle highlights) ----------------------

    val OnGradientSoft = Color.White.copy(alpha = 0.85f)
    val OnGradientFaint = Color.White.copy(alpha = 0.20f)
    val OnGradientBarTrack = Color.White.copy(alpha = 0.25f)
    val OnGradientBarHighlight = Color(0xFFFFF59D) // very light gold
    val OnGradientIconSoft = Color(0xFFFFE082)     // champagne gold — "trophy"

    // --- Accents (kept warm to harmonise with brand green) ------------

    /** Streak flame. Amber reads as "hot" without veering into red. */
    val StreakAccent = Color(0xFFF59E0B)

    /** Level badge. Slightly deeper amber so it distinguishes from streak. */
    val LevelAccent = Color(0xFFD97706)

    /** "Positive XP" text in the history list — on-brand green. */
    val XpPositive = Color(0xFF00A63E) // GradientStart700 in brand palette
    val XpNegative = Color(0xFFDC2626)

    /** Error banner text. */
    val ErrorText = Color(0xFFB91C1C)

    // --- Leaderboard rank badges (podium) -----------------------------

    data class RankBadgeTone(val background: Color, val foreground: Color)

    val GoldBadge = RankBadgeTone(Color(0xFFFFD54F), Color(0xFF4E342E))
    val SilverBadge = RankBadgeTone(Color(0xFFCFD8DC), Color(0xFF263238))
    val BronzeBadge = RankBadgeTone(Color(0xFFFFAB91), Color(0xFF4E342E))
    val DefaultBadge = RankBadgeTone(Color(0xFFE5E7EB), Color(0xFF374151))

    // --- XP action tints (history rows) -------------------------------
    //
    // Deliberately avoids pure purple / pink — those clashed with the
    // green brand. Every tone below has ≥40% luminance contrast against
    // a 12% alpha background and against the white card.

    val ActionFlashcard = Color(0xFFF59E0B)     // amber — matches streak family
    val ActionQuiz = Color(0xFF0EA5E9)          // sky blue
    val ActionExam = Color(0xFF0284C7)          // deeper blue
    val ActionLesson = Color(0xFF10B981)        // emerald
    val ActionClassroomQuiz = Color(0xFF14B8A6) // teal — on-brand
    val ActionClassroomChat = Color(0xFF2563EB) // indigo blue (chat)
    val ActionConversation = Color(0xFF059669)  // brand-green family
    val ActionLogin = Color(0xFF0D9488)         // darker teal
    val ActionStreakBonus = Color(0xFFEF4444)   // red — "bonus, special"
    val ActionPost = Color(0xFF22C55E)          // lime green
    val ActionSystem = Color(0xFF6B7280)        // grey
}

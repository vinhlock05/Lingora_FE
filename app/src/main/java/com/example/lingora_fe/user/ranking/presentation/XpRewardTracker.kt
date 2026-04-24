package com.example.lingora_fe.user.ranking.presentation

import com.example.lingora_fe.user.ranking.domain.model.MyRankingStats
import com.example.lingora_fe.user.ranking.domain.repository.RankingRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Glue between "the user just did something that should earn XP" and
 * `XpEventBus` / the reward popup.
 *
 * Flow:
 *   1. A feature ViewModel (Exam, Flashcard, Conversation, …) successfully
 *      completes a backend call that *will* cause XP to be awarded via the
 *      event bus on the server.
 *   2. The ViewModel calls [observeAfterAction] with a short human label
 *      like "Bài kiểm tra".
 *   3. This tracker calls `GET /rankings/me`, compares the returned
 *      totalXp against the *previous* cached value, and emits an
 *      `XpEventBus.XpAwarded` if there's a positive delta.
 *
 * Why polling and not a push response? The XP award is event-driven
 * on the backend (happens *after* the original transaction commits),
 * so the HTTP response to the exam submit does not include the new XP.
 * Polling once with a small delay gives the listener enough time to
 * finish and keeps the backend API surface unchanged.
 *
 * Idempotency: [lastKnownTotalXp] is only updated from server responses
 * (never from local guesses), so repeated calls of [observeAfterAction]
 * will only fire a popup the first time the server acknowledges the
 * delta. If the user triggers two awards quickly, the tracker collapses
 * them into a single popup with the combined delta — preventing popup
 * spam.
 */
@Singleton
class XpRewardTracker @Inject constructor(
    private val rankingRepository: RankingRepository
) {

    /**
     * Last XP total we observed from the server. Null until the first call.
     * Intentionally nullable so the *very first* snapshot does not fire a
     * bogus popup on app start.
     */
    @Volatile
    private var lastKnownTotalXp: Int? = null

    @Volatile
    private var lastKnownLevel: Int = 1

    /**
     * Call after any action that should result in XP being awarded.
     *
     * Will poll `GET /rankings/me` up to [maxAttempts] times with
     * [initialDelayMs] / [retryDelayMs] spacing, and emit an
     * `XpEventBus.XpAwarded` on the *first* call that sees an increased
     * totalXp. If the server never reports a delta (e.g. daily cap
     * reached), no popup fires — which is the correct UX.
     *
     * @param sourceActionKey Used as popup heading ("Bạn vừa hoàn thành: …").
     */
    suspend fun observeAfterAction(
        sourceActionKey: String? = null,
        initialDelayMs: Long = 600,
        retryDelayMs: Long = 1200,
        maxAttempts: Int = 3
    ) {
        delay(initialDelayMs)
        repeat(maxAttempts) { attempt ->
            val stats = fetchStats() ?: return
            val previous = lastKnownTotalXp
            lastKnownTotalXp = stats.totalXp
            val previousLevel = lastKnownLevel
            lastKnownLevel = stats.level

            if (previous == null) {
                // First snapshot of the session — just cache, no popup.
                return
            }

            val delta = stats.totalXp - previous
            if (delta > 0) {
                XpEventBus.emit(
                    XpEventBus.XpAwarded(
                        xpDelta = delta,
                        newTotalXp = stats.totalXp,
                        newLevel = stats.level,
                        leveledUp = stats.level > previousLevel,
                        sourceActionKey = sourceActionKey
                    )
                )
                return
            }

            if (attempt < maxAttempts - 1) delay(retryDelayMs)
        }
    }

    /**
     * Call on login / app start to seed the cache so the very first XP
     * action fires a popup correctly (without the seed, the tracker has
     * nothing to diff against).
     */
    suspend fun prime() {
        fetchStats()?.let {
            lastKnownTotalXp = it.totalXp
            lastKnownLevel = it.level
        }
    }

    private suspend fun fetchStats(): MyRankingStats? =
        rankingRepository.getMyStats().fold(ifLeft = { null }, ifRight = { it })
}

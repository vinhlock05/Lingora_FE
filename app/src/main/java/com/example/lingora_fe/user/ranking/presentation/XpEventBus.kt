package com.example.lingora_fe.user.ranking.presentation

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * App-wide hot stream used to propagate "XP was awarded on the backend"
 * events from the ViewModels that trigger milestone actions (exam submit,
 * flashcard reviewed, conversation ended, …) to the UI layers that should
 * react: the top-bar level pill, the ranking summary card, and the XP
 * reward popup.
 *
 * Why a singleton instead of a shared ViewModel?
 *  - The producing ViewModels (ExamViewModel, TopicDetailViewModel, ...) and
 *    the consuming composables (RankingScreen, RankingSummaryCard, the host
 *    Activity that shows the popup) do not share a Hilt graph in any
 *    convenient way.
 *  - This is purely a UI-side bus; it does not persist state, so it can be
 *    a plain object.
 *  - `extraBufferCapacity = 4` + `DROP_OLDEST` prevents backpressure if the
 *    consumer has not attached yet, which happens on cold-starts where the
 *    very first event fires before RankingSummaryCard has resumed.
 */
object XpEventBus {

    /**
     * Emitted when the frontend has *observed* a net XP change for the
     * current user. Populated either by:
     *
     *  - the `XpRewardTracker` (preferred): polls `GET /rankings/me` after
     *    a milestone action and emits the delta once the server acknowledges
     *    the new total, OR
     *  - any future backend change that returns `{ xpAwarded, leveledUp }`
     *    directly in the action response.
     *
     * @property xpDelta         Positive XP gained since the last observation.
     * @property newTotalXp      Total XP after this award (snapshot).
     * @property newLevel        User's level after this award.
     * @property leveledUp       True if this award crossed a level boundary.
     * @property sourceActionKey Human-readable label used in popup copy
     *                           ("Bài kiểm tra", "Flashcard", …). Optional.
     */
    data class XpAwarded(
        val xpDelta: Int,
        val newTotalXp: Int,
        val newLevel: Int,
        val leveledUp: Boolean,
        val sourceActionKey: String? = null
    )

    private val _events = MutableSharedFlow<XpAwarded>(
        replay = 0,
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val events: SharedFlow<XpAwarded> = _events.asSharedFlow()

    /**
     * Broadcast an XP award to the UI layer. Safe to call from any coroutine
     * context. If nobody is currently collecting, up to 4 events are buffered
     * so the popup can still fire as soon as the UI resumes.
     */
    fun emit(event: XpAwarded) {
        _events.tryEmit(event)
    }
}

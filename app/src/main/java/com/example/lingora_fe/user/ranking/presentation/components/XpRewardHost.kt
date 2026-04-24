package com.example.lingora_fe.user.ranking.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.user.ranking.presentation.XpEventBus
import com.example.lingora_fe.user.ranking.presentation.XpRewardTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Thin Hilt-injected holder that exists solely so the composable layer can
 * resolve a singleton `XpRewardTracker` without leaking dependency injection
 * into `XpRewardHost`. Priming is one-shot per ViewModel (i.e. per user
 * session in the nav graph).
 */
@HiltViewModel
class XpRewardHostViewModel @Inject constructor(
    private val xpRewardTracker: XpRewardTracker
) : ViewModel() {

    private var primed = false

    fun primeIfNeeded() {
        if (primed) return
        primed = true
        viewModelScope.launch {
            // Seeds the tracker's cached totalXp so the next XP-awarding action
            // can compute a delta. Failing silently is fine — the tracker
            // itself tolerates a missing baseline.
            xpRewardTracker.prime()
        }
    }
}

/**
 * Top-level host that owns the XP reward popup's visibility state. Drop it
 * once into the user-area Scaffold; it collects from `XpEventBus` and
 * renders an `XpRewardDialog` whenever the backend has awarded XP.
 *
 * Consumers do not need to pass anything — the whole XP flow is wired via
 * the event bus singleton. If multiple events arrive in quick succession
 * the *latest* one wins; the dialog updates smoothly instead of stacking
 * multiple popups.
 */
@Composable
fun XpRewardHost() {
    val viewModel: XpRewardHostViewModel = hiltViewModel()
    var pending by remember { mutableStateOf<XpEventBus.XpAwarded?>(null) }

    LaunchedEffect(Unit) {
        viewModel.primeIfNeeded()
        XpEventBus.events.collect { event ->
            pending = event
        }
    }

    val active = pending
    XpRewardDialog(
        visible = active != null,
        xpDelta = active?.xpDelta ?: 0,
        newTotalXp = active?.newTotalXp ?: 0,
        newLevel = active?.newLevel ?: 1,
        leveledUp = active?.leveledUp == true,
        sourceActionKey = active?.sourceActionKey,
        onDismiss = { pending = null }
    )
}

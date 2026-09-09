/*
 * :feature:planter — planter mode.
 *
 * Responsibility: drive the planting order for one field. Each trigger (tap on the big button,
 * volume key, Bluetooth button, or the voice command "plant") records the current fix, starts
 * or finalises a voice note, stores a PLANT event for the current plot and advances. Supports
 * pause/resume (no advance), skip (SKIP event, advance), redo (REDO event replaces the previous
 * plot's fix), and manual correction from the list (CORRECT event). GPS loss and STT loss are
 * banners, never blockers: the event is stored with what is available and flagged.
 *
 * Interface: planterGraph(nav) registers Routes.PLANTER; PlanterViewModel exposes
 * PlanterUiState via StateFlow and accepts PlanterAction (unidirectional data flow).
 * Domain rules live in :core:model; this module owns only UI and orchestration.
 */
package org.fieldcapture.feature.planter

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class PlanterUiState(
    val fieldName: String = "",
    val currentPlotId: String? = null,
    val currentIndex: Int = 0,
    val total: Int = 0,
    val paused: Boolean = false,
    val gpsBanner: String? = null,
    val sttBanner: String? = null,
    val lastTranscript: String? = null,
    val lastAccuracyM: Float? = null,
)

sealed interface PlanterAction {
    data class Trigger(val atMs: Long) : PlanterAction
    data object Pause : PlanterAction
    data object Resume : PlanterAction
    data object Skip : PlanterAction
    data object Redo : PlanterAction
    data class Correct(val eventId: String, val plotId: String) : PlanterAction
    data object Finish : PlanterAction
}

@HiltViewModel
class PlanterViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(PlanterUiState())
    val state: StateFlow<PlanterUiState> = _state.asStateFlow()

    fun onAction(action: PlanterAction) {
        // M1 vertical slice: wire to SessionRepository, LocationTracker.latest(), SpeechEngine.
        _state.value = when (action) {
            PlanterAction.Pause -> _state.value.copy(paused = true)
            PlanterAction.Resume -> _state.value.copy(paused = false)
            else -> _state.value
        }
    }
}

/*
 * :feature:walk — walk mode and the post-walk review.
 *
 * Responsibility: continuous video (VideoRecorder) with a synchronized ≥ 1 Hz track written to
 * sessions/<id>/track.ndjson by :core:location; spoken notes during recording become
 * annotations time-indexed to the video and the track; after stop, WalkAnnotationMapper
 * (:core:model) assigns each annotation to a plot and the review screen lists items with
 * needsReview first, showing the map position, the transcript, the audio clip and the video
 * moment, with a plot picker to fix mis-assignments (stored as MANUAL, manuallyCorrected = true).
 * Photo capture during a walk uses the same current-plot assignment.
 *
 * Interface: walkGraph(nav) registers Routes.WALK and Routes.WALK + "/review/{sessionId}";
 * WalkViewModel exposes WalkUiState and accepts WalkAction.
 */
package org.fieldcapture.feature.walk

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class WalkUiState(
    val recording: Boolean = false,
    val durationMs: Long = 0,
    val fixCount: Int = 0,
    val currentPlotId: String? = null,
    val annotationCount: Int = 0,
    val gpsBanner: String? = null,
    val storageBanner: String? = null,
)

sealed interface WalkAction {
    data object Start : WalkAction
    data object Pause : WalkAction
    data object Resume : WalkAction
    data object Stop : WalkAction
    data class Photo(val noteType: String) : WalkAction
    data class ReassignAnnotation(val annotationId: String, val plotId: String) : WalkAction
}

@HiltViewModel
class WalkViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(WalkUiState())
    val state: StateFlow<WalkUiState> = _state.asStateFlow()

    fun onAction(action: WalkAction) {
        // M2: start RecordingForegroundService(location|microphone|camera), VideoRecorder, SpeechEngine.
        _state.value = when (action) {
            WalkAction.Start -> _state.value.copy(recording = true)
            WalkAction.Stop -> _state.value.copy(recording = false)
            else -> _state.value
        }
    }
}

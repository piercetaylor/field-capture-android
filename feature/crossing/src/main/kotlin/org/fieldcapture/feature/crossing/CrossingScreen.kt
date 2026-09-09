/*
 * :feature:crossing — pollination log.
 *
 * Responsibility: record a cross (female plot/germplasm × male plot/germplasm) with timestamp,
 * pollinator (person), GPS fix, optional voice note, and later follow-up counts (pollinations
 * attempted, pods set, seed count). Parents are chosen from the plot list, by barcode, or by
 * voice ("cross forty-two by seventeen"). Export writes the Intercross-compatible columns
 * (crossID, femaleObsUnitID, maleObsUnitID, timestamp, person, experiment, type) plus this
 * app's extra columns, and can import an Intercross parents CSV (id, name, sex) and wishlist
 * CSV (femaleDbId, maleDbId, femaleName, maleName, wishType, wishMin, wishMax) to seed the
 * parent list and the planned-cross checklist (docs/data-formats.md, "Crossing").
 *
 * Interface: crossingGraph(nav) registers Routes.CROSSING; CrossingViewModel exposes
 * CrossingUiState and accepts CrossingAction. Implemented in M3.
 */
package org.fieldcapture.feature.crossing

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class CrossingUiState(
    val femaleId: String? = null,
    val maleId: String? = null,
    val todayCount: Int = 0,
    val wishlistRemaining: Int = 0,
    val gpsBanner: String? = null,
)

sealed interface CrossingAction {
    data class SetFemale(val id: String) : CrossingAction
    data class SetMale(val id: String) : CrossingAction
    data class Save(val pollinations: Int?, val note: String?) : CrossingAction
    data class FollowUp(val crossId: String, val podsSet: Int?, val seedCount: Int?) : CrossingAction
    data class ImportWishlist(val uri: String) : CrossingAction
}

@HiltViewModel
class CrossingViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(CrossingUiState())
    val state: StateFlow<CrossingUiState> = _state.asStateFlow()

    fun onAction(action: CrossingAction) {
        _state.value = when (action) {
            is CrossingAction.SetFemale -> _state.value.copy(femaleId = action.id)
            is CrossingAction.SetMale -> _state.value.copy(maleId = action.id)
            else -> _state.value
        }
    }
}

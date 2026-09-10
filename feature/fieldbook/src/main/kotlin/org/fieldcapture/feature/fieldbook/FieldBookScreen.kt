/*
 * :feature:fieldbook — fields, plots, import, export and the map.
 *
 * Responsibility: import a Field Book–compatible CSV/XLSX (column picker for unique/primary/
 * secondary IDs, validation report from FieldBookImport), create a grid layout from a surveyed
 * origin (GridLayout), list plots with their coordinate source, show plots/tracks/points on a
 * MapLibre map (plots-only mode works with zero basemap tiles; a basemap region can be
 * downloaded on Wi-Fi via OfflineManager), and run the export dialog (ExportBundle → SAF tree).
 *
 * Interface: fieldbookGraph(nav) registers Routes.FIELDBOOK and sub-routes import/export/map;
 * FieldBookViewModel exposes FieldBookUiState and accepts FieldBookAction.
 */
package org.fieldcapture.feature.fieldbook

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.fieldcapture.model.ImportIssue
import javax.inject.Inject

data class FieldSummary(val fieldId: String, val name: String, val plots: Int, val withCoordinates: Int)

data class FieldBookUiState(
    val fields: List<FieldSummary> = emptyList(),
    val importIssues: List<ImportIssue> = emptyList(),
    val exporting: Boolean = false,
)

sealed interface FieldBookAction {
    data class ImportCsv(val uri: String, val trial: String, val field: String, val uniqueIdColumn: String?) : FieldBookAction
    data class CreateGrid(val trial: String, val field: String) : FieldBookAction
    data class Export(val treeUri: String, val fieldIds: List<String>) : FieldBookAction
}

@HiltViewModel
class FieldBookViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(FieldBookUiState())
    val state: StateFlow<FieldBookUiState> = _state.asStateFlow()

    // The parameter is part of the fixed interface; the body arrives in M1.
    @Suppress("UnusedParameter")
    fun onAction(action: FieldBookAction) {
        // M1: import + plot list; M2: map + export.
        _state.value = _state.value
    }
}

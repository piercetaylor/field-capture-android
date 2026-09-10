/*
 * :feature:integrations — weather, soil, harvest import, GIS layers, optional BrAPI.
 *
 * Responsibility: user-initiated, WorkManager-backed fetches that attach context to a field:
 * Open-Meteo archive/forecast (daily variables per field centroid), Daymet (North America,
 * 1 km, complete calendar years), USDA NRCS Soil Data Access (map unit + dominant component per
 * plot centroid via SDA_Get_Mukey_from_intersection_with_WktWgs84), harvest CSV import with a
 * saved column-mapping profile (Mirus and similar plot-combine exports), and GeoJSON/KML layer
 * import for display. Each fetch stores the request URL and response time so the export is
 * reproducible. No API keys are stored in the repository; the optional Open-Meteo commercial key
 * and any BrAPI token come from the device settings screen (twelve-factor: configuration is
 * supplied at runtime, never in code).
 *
 * Interface: integrationsGraph(nav) registers Routes.INTEGRATIONS; IntegrationsViewModel
 * exposes IntegrationsUiState and accepts IntegrationsAction. Workers: WeatherFetchWorker,
 * SoilFetchWorker (M3).
 */
package org.fieldcapture.feature.integrations

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class IntegrationsUiState(
    val online: Boolean = false,
    val weatherRows: Int = 0,
    val soilRows: Int = 0,
    val harvestRows: Int = 0,
    val layers: List<String> = emptyList(),
    val lastError: String? = null,
)

sealed interface IntegrationsAction {
    data class FetchWeather(val fieldId: String, val source: String, val startDate: String, val endDate: String) : IntegrationsAction
    data class FetchSoil(val fieldId: String) : IntegrationsAction
    data class ImportHarvest(val uri: String, val mappingProfile: String) : IntegrationsAction
    data class ImportLayer(val uri: String) : IntegrationsAction
}

@HiltViewModel
class IntegrationsViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(IntegrationsUiState())
    val state: StateFlow<IntegrationsUiState> = _state.asStateFlow()

    // The parameter is part of the fixed interface; the body arrives in M3.
    @Suppress("UnusedParameter")
    fun onAction(action: IntegrationsAction) {
        // M3: enqueue OneTimeWorkRequests with a NetworkType.CONNECTED constraint.
        _state.value = _state.value
    }
}

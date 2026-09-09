/*
 * :core:model — domain types shared by every module.
 *
 * Responsibility: immutable value types for positions, fixes, plots, annotations, and the
 * identifiers that tie exports together. No Android imports, no I/O.
 *
 * Interface: plain Kotlin data classes. Persistence mapping (Room) lives in :core:data.
 * Naming follows the export CSV columns in docs/data-formats.md so that a field name here
 * is the column name there.
 */
package org.fieldcapture.model

/** WGS 84 position in decimal degrees. Latitude first in code; GeoJSON output is lon,lat. */
data class LatLon(val lat: Double, val lon: Double) {
    init {
        require(lat in -90.0..90.0) { "latitude out of range: $lat" }
        require(lon in -180.0..180.0) { "longitude out of range: $lon" }
    }
}

/** One location fix from the device or an external receiver. Time is Unix epoch milliseconds (UTC). */
data class GpsFix(
    val lat: Double,
    val lon: Double,
    val accuracyM: Float,
    val timestampMs: Long,
    val altitudeM: Double? = null,
    val speedMps: Float? = null,
    val bearingDeg: Float? = null,
    val provider: String = "fused",
) {
    val position: LatLon get() = LatLon(lat, lon)
}

/** Field-book hierarchy: trial > field > plot. Mirrors the photo folder tree. */
data class PlotKey(val trial: String, val field: String, val plotId: String)

/**
 * An experimental unit. [plotId] is the Field Book "unique identifier"; [primaryId] and
 * [secondaryId] are the two ordering columns (typically row/plot or range/row).
 * [attributes] keeps every other import column verbatim so exports can round-trip them.
 */
data class Plot(
    val key: PlotKey,
    val primaryId: String,
    val secondaryId: String,
    val plantingOrder: Int,
    val attributes: Map<String, String> = emptyMap(),
    val centroid: LatLon? = null,
    /** Closed ring, first point not repeated. Empty when only a centroid is known. */
    val polygon: List<LatLon> = emptyList(),
) {
    val plotId: String get() = key.plotId
}

/** How a plot was chosen for an observation. Exported as `assignment_method`. */
enum class AssignmentMethod { POLYGON, NEAREST_CENTROID, PLANTING_ORDER, MANUAL }

/** Result of assigning a position to a plot. */
data class PlotAssignment(
    val plotId: String,
    val method: AssignmentMethod,
    val distanceM: Double,
    /** Distance to the runner-up plot, for flagging ambiguous assignments. Null if no other plot. */
    val runnerUpDistanceM: Double?,
) {
    /** True when the runner-up is within [ratio] of the winner's distance (or both are inside a polygon). */
    fun isAmbiguous(ratio: Double = 1.5): Boolean =
        runnerUpDistanceM != null && method != AssignmentMethod.POLYGON &&
            runnerUpDistanceM <= distanceM * ratio
}

/** A spoken or typed note attached to a moment in time (planter tap, walk-mode utterance). */
data class Annotation(
    val id: String,
    val sessionId: String,
    val timestampMs: Long,
    val transcript: String?,
    val audioPath: String?,
    val sttEngine: String?,
    val sttConfidence: Float?,
    val assignedPlotId: String?,
    val assignmentMethod: AssignmentMethod?,
    val assignmentDistanceM: Double?,
    val manuallyCorrected: Boolean = false,
)

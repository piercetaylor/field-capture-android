/*
 * :core:data — storage layout, export bundle, and repository interfaces.
 *
 * Responsibility: define where files live on the device and what an export folder contains.
 * All capture data is written under the app-specific external files directory (no storage
 * permission, not scanned into the gallery, removed on uninstall). Export copies a self-contained
 * bundle to a user-chosen SAF tree (ACTION_OPEN_DOCUMENT_TREE); nothing is written outside the
 * app sandbox otherwise. Layout mirrors PhenoApps Field Book's folder vocabulary
 * (field_import, field_export, plot_data) so users of both apps see familiar names.
 *
 * Interface: StorageLayout paths, ExportBundle spec, and the repository contracts that features
 * depend on. Implementations arrive in M1/M2 (PLAN.md, "Milestones").
 */
package org.fieldcapture.data

import org.fieldcapture.model.GpsFix
import org.fieldcapture.model.ImportResult
import org.fieldcapture.model.Plot
import java.io.File

object StorageLayout {
    const val FIELD_IMPORT = "field_import"
    const val FIELD_EXPORT = "field_export"
    const val PLOT_DATA = "plot_data"
    const val SESSIONS = "sessions"
    const val DATABASE = "database"
    const val MODELS = "models"
    const val LAYERS = "layers"

    /** Root is `context.getExternalFilesDir(null)` when mounted, else `context.filesDir`. */
    fun root(externalFilesDir: File?, internalFilesDir: File): File = externalFilesDir ?: internalFilesDir

    fun sessionDir(root: File, sessionId: String): File = File(root, "$SESSIONS/$sessionId")

    /** Track sidecar written at ≥ 1 Hz during recording; one line per fix (NDJSON) so a crash loses at most one line. */
    fun trackFile(root: File, sessionId: String): File = File(sessionDir(root, sessionId), "track.ndjson")

    fun videoFile(root: File, sessionId: String): File = File(sessionDir(root, sessionId), "video.mp4")

    fun plotDataDir(root: File): File = File(root, PLOT_DATA)
}

/**
 * Contents of one export (docs/data-formats.md, "Export bundle"). Every file is optional except
 * manifest.json; the user picks the set in the export dialog.
 */
data class ExportBundle(
    val manifest: Boolean = true,
    val plotsCsv: Boolean = true,
    val observationsCsv: Boolean = true,
    val planterEventsCsv: Boolean = true,
    val annotationsCsv: Boolean = true,
    val mediaCsv: Boolean = true,
    val tracksGpx: Boolean = true,
    val plotsGeoJson: Boolean = true,
    val pointsGeoJson: Boolean = true,
    val sqlite: Boolean = true,
    val fieldBookTableCsv: Boolean = false,
    val fieldBookDatabaseCsv: Boolean = false,
    val sampleManifestCsv: Boolean = false,
    val mediaFiles: Boolean = false,
    val weatherCsv: Boolean = false,
    val soilCsv: Boolean = false,
    val harvestCsv: Boolean = false,
    val crossesCsv: Boolean = false,
)

interface FieldRepository {
    suspend fun importFieldBookCsv(fieldId: String, result: ImportResult)
    suspend fun plots(fieldId: String): List<Plot>
    suspend fun markPlanted(plotId: String, fix: GpsFix, plantedAt: Long)
}

interface SessionRepository {
    suspend fun start(type: String, fieldId: String?): String
    suspend fun appendTrack(sessionId: String, fixes: List<GpsFix>)
    suspend fun end(sessionId: String)
    /** Called on app start: any session with endedAt == null is closed and its files reconciled. */
    suspend fun recoverInterrupted()
}

interface ExportRepository {
    /** Writes the bundle under [treeUri] (a SAF document tree). Returns relative paths written. */
    suspend fun export(bundle: ExportBundle, treeUri: String, fieldIds: List<String>): List<String>
}

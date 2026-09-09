/*
 * :core:data — Room entities.
 *
 * Responsibility: the on-device schema. Table and column names are the export column names
 * (docs/data-formats.md), so the SQLite export is a straight copy of this database and every
 * flat CSV is a SELECT over one table. Dimensions: trials, fields, plots, sessions. Facts:
 * planter_events, annotations, observations, media, track_points, weather_daily, soil,
 * harvest, crosses, samples.
 *
 * Interface: entities only. Queries are in FieldCaptureDatabase.kt; mapping to :core:model types
 * is done in repositories so the pure-Kotlin module never sees Room annotations.
 */
package org.fieldcapture.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "trials")
data class TrialEntity(
    @PrimaryKey val trialId: String,
    val name: String,
    val season: String?,
    val crop: String?,
    val locationName: String?,
    val notes: String?,
)

@Entity(
    tableName = "fields",
    foreignKeys = [ForeignKey(TrialEntity::class, ["trialId"], ["trialId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("trialId")],
)
data class FieldEntity(
    @PrimaryKey val fieldId: String,
    val trialId: String,
    val name: String,
    /** IMPORT_CSV, IMPORT_XLSX, GRID, PLANTER — how plot coordinates were obtained. */
    val layoutSource: String,
    val importFileName: String?,
    val uniqueIdColumn: String?,
    val primaryIdColumn: String?,
    val secondaryIdColumn: String?,
    val createdAt: Long,
)

@Entity(
    tableName = "plots",
    foreignKeys = [ForeignKey(FieldEntity::class, ["fieldId"], ["fieldId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("fieldId"), Index(value = ["plotId"], unique = true)],
)
data class PlotEntity(
    @PrimaryKey val plotId: String,
    val fieldId: String,
    val trialId: String,
    val primaryId: String,
    val secondaryId: String,
    val plantingOrder: Int,
    /** JSON object of every extra import column, preserved verbatim for round-tripping. */
    val attributesJson: String,
    val centroidLat: Double?,
    val centroidLon: Double?,
    val coordAccuracyM: Float?,
    /** IMPORT, GRID, PLANTER, MANUAL */
    val coordSource: String?,
    /** GeoJSON Polygon geometry, or null when only a centroid exists. */
    val polygonGeoJson: String?,
    val plantedAt: Long?,
)

@Entity(tableName = "sessions", indices = [Index("fieldId")])
data class SessionEntity(
    @PrimaryKey val sessionId: String,
    /** PLANTER, WALK, PHOTO, CROSSING */
    val type: String,
    val fieldId: String?,
    val startedAt: Long,
    val endedAt: Long?,
    val person: String?,
    val deviceModel: String,
    val appVersion: String,
    val sttEngine: String?,
    val videoPath: String?,
    val trackPath: String?,
    val notes: String?,
)

@Entity(
    tableName = "planter_events",
    foreignKeys = [ForeignKey(SessionEntity::class, ["sessionId"], ["sessionId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("sessionId"), Index("plotId")],
)
data class PlanterEventEntity(
    @PrimaryKey val eventId: String,
    val sessionId: String,
    val seq: Int,
    /** PLANT, SKIP, REDO, PAUSE, RESUME, CORRECT */
    val action: String,
    val plotId: String?,
    val plantingOrder: Int?,
    val timestamp: Long,
    val lat: Double?,
    val lon: Double?,
    val accuracyM: Float?,
    val fixAgeMs: Long?,
    val audioPath: String?,
    val transcript: String?,
    val sttConfidence: Float?,
    val manuallyCorrected: Boolean,
)

@Entity(
    tableName = "annotations",
    foreignKeys = [ForeignKey(SessionEntity::class, ["sessionId"], ["sessionId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("sessionId"), Index("plotId")],
)
data class AnnotationEntity(
    @PrimaryKey val annotationId: String,
    val sessionId: String,
    val timestamp: Long,
    /** Offset into the session video, when one exists. */
    val videoOffsetMs: Long?,
    val transcript: String?,
    val audioPath: String?,
    val sttEngine: String?,
    val sttConfidence: Float?,
    val lat: Double?,
    val lon: Double?,
    val accuracyM: Float?,
    val plotId: String?,
    /** POLYGON, NEAREST_CENTROID, PLANTING_ORDER, MANUAL */
    val assignmentMethod: String?,
    val assignmentDistanceM: Double?,
    val needsReview: Boolean,
    val manuallyCorrected: Boolean,
)

@Entity(tableName = "observations", indices = [Index("plotId"), Index("sessionId"), Index("trait")])
data class ObservationEntity(
    @PrimaryKey val observationId: String,
    val plotId: String,
    val sessionId: String?,
    val annotationId: String?,
    val trait: String,
    val value: String,
    val unit: String?,
    val timestamp: Long,
    val person: String?,
    /** VOICE, MANUAL, IMPORT */
    val source: String,
    val lat: Double?,
    val lon: Double?,
)

@Entity(tableName = "media", indices = [Index("plotId"), Index("sessionId")])
data class MediaEntity(
    @PrimaryKey val mediaId: String,
    /** PHOTO, VIDEO, AUDIO */
    val type: String,
    val plotId: String?,
    val sessionId: String?,
    val noteType: String,
    /** Relative to the storage root; mirrors trial/field/plot/note_type. */
    val path: String,
    val sidecarPath: String?,
    val timestamp: Long,
    val lat: Double?,
    val lon: Double?,
    val accuracyM: Float?,
    val bearingDeg: Float?,
    val sha256: String?,
    val bytes: Long?,
)

@Entity(
    tableName = "track_points",
    foreignKeys = [ForeignKey(SessionEntity::class, ["sessionId"], ["sessionId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["sessionId", "timestamp"])],
)
data class TrackPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val timestamp: Long,
    val lat: Double,
    val lon: Double,
    val accuracyM: Float,
    val altitudeM: Double?,
    val speedMps: Float?,
    val bearingDeg: Float?,
    val provider: String,
)

@Entity(tableName = "weather_daily", primaryKeys = ["fieldId", "date", "source"])
data class WeatherDailyEntity(
    val fieldId: String,
    /** ISO date, local to the field. */
    val date: String,
    /** OPEN_METEO_ARCHIVE, OPEN_METEO_FORECAST, DAYMET, NWS */
    val source: String,
    val tmaxC: Double?,
    val tminC: Double?,
    val precipMm: Double?,
    val et0Mm: Double?,
    val sradMjM2: Double?,
    val vpKpa: Double?,
    val soilMoisture0to7cm: Double?,
    val fetchedAt: Long,
    val requestUrl: String,
)

@Entity(tableName = "soil", primaryKeys = ["fieldId", "mukey", "cokey"])
data class SoilEntity(
    val fieldId: String,
    val plotId: String?,
    val mukey: String,
    val cokey: String,
    val musym: String?,
    val muname: String?,
    val compname: String?,
    val comppctR: Int?,
    val majcompflag: String?,
    val taxclname: String?,
    val fetchedAt: Long,
    val query: String,
)

@Entity(tableName = "harvest", indices = [Index("plotId")])
data class HarvestEntity(
    @PrimaryKey val harvestId: String,
    val plotId: String,
    val harvestedAt: Long?,
    val weightKg: Double?,
    val moisturePct: Double?,
    val testWeight: Double?,
    val yield: Double?,
    val yieldUnit: String?,
    val plotAreaM2: Double?,
    val sourceFile: String,
    val mappingProfile: String,
    val importedAt: Long,
)

@Entity(tableName = "crosses", indices = [Index("femaleId"), Index("maleId")])
data class CrossEntity(
    @PrimaryKey val crossId: String,
    val femaleId: String,
    val maleId: String,
    val sessionId: String?,
    val timestamp: Long,
    val person: String?,
    val lat: Double?,
    val lon: Double?,
    val accuracyM: Float?,
    val pollinations: Int?,
    val podsSet: Int?,
    val seedCount: Int?,
    val crossType: String,
    val notes: String?,
    val audioPath: String?,
)

@Entity(tableName = "samples", indices = [Index("plotId"), Index("plateId")])
data class SampleEntity(
    @PrimaryKey val sampleId: String,
    val plotId: String,
    val plateId: String?,
    val well: String?,
    val tissue: String,
    val collectedAt: Long?,
    val collectedBy: String?,
    val lat: Double?,
    val lon: Double?,
    val notes: String?,
)

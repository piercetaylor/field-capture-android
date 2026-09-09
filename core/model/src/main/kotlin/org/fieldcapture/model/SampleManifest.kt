/*
 * :core:model — genotyping sample manifest (plate layout) export.
 *
 * Responsibility: lay selected plots/plants onto 96-well (or 384-well) plates in a fixed fill
 * order, reserving control wells, and emit the CSV columns defined in docs/data-formats.md,
 * "Sample manifest". The downstream genotype-analysis tools consume this file as-is.
 *
 * Interface: SampleManifest.layout(samples, config) -> List<Well>; SampleManifest.csv(wells).
 * Pure; the app supplies the sample list from the selected plots and the collected-by/time.
 */
package org.fieldcapture.model

data class SampleRequest(
    val sampleId: String,
    val plotId: String,
    val trial: String,
    val field: String,
    val germplasm: String?,
    val tissue: String = "leaf",
    val collectedAtMs: Long?,
    val collectedBy: String?,
    val position: LatLon?,
    val notes: String? = null,
)

data class PlateConfig(
    val plateIdPrefix: String,
    val rows: Int = 8,
    val columns: Int = 12,
    /** Column-major (A01, B01, …, H01, A02 …) matches most liquid-handler pick lists. */
    val columnMajor: Boolean = true,
    /** Wells kept empty or filled with controls, e.g. setOf("H12"). */
    val reservedWells: Set<String> = emptySet(),
    val controlLabel: String = "CONTROL",
) {
    init {
        require(rows in 1..16 && columns in 1..24) { "unsupported plate size" }
    }
}

data class Well(
    val plateId: String,
    val well: String,
    val row: Char,
    val column: Int,
    val sample: SampleRequest?,
    val isControl: Boolean,
)

object SampleManifest {
    val HEADER = listOf(
        "plate_id", "well", "well_row", "well_column", "sample_id", "plot_id", "trial", "field",
        "germplasm", "tissue", "collected_at", "collected_by", "lat", "lon", "notes",
    )

    fun wellName(row: Char, column: Int): String = row + String.format(java.util.Locale.ROOT, "%02d", column)

    fun layout(samples: List<SampleRequest>, config: PlateConfig): List<Well> {
        val order = ArrayList<Pair<Char, Int>>()
        if (config.columnMajor) {
            for (c in 1..config.columns) for (r in 0 until config.rows) order += ('A' + r) to c
        } else {
            for (r in 0 until config.rows) for (c in 1..config.columns) order += ('A' + r) to c
        }
        val out = ArrayList<Well>()
        val remaining = samples.iterator()
        var plate = 0
        while (remaining.hasNext()) {
            plate += 1
            val plateId = "${config.plateIdPrefix}${String.format(java.util.Locale.ROOT, "%03d", plate)}"
            for ((r, c) in order) {
                val name = wellName(r, c)
                out += when {
                    name in config.reservedWells -> Well(plateId, name, r, c, null, isControl = true)
                    remaining.hasNext() -> Well(plateId, name, r, c, remaining.next(), isControl = false)
                    else -> Well(plateId, name, r, c, null, isControl = false)
                }
            }
        }
        return out
    }

    fun csv(wells: List<Well>, config: PlateConfig): String {
        val rows = ArrayList<List<String?>>()
        rows += HEADER
        for (w in wells) {
            val s = w.sample
            rows += listOf(
                w.plateId, w.well, w.row.toString(), w.column.toString(),
                if (w.isControl) config.controlLabel else s?.sampleId,
                s?.plotId, s?.trial, s?.field, s?.germplasm, s?.tissue,
                s?.collectedAtMs?.let { Iso8601.utc(it) }, s?.collectedBy,
                s?.position?.let { Json.num(it.lat) }, s?.position?.let { Json.num(it.lon) }, s?.notes,
            )
        }
        return Csv.write(rows)
    }
}

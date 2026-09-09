/*
 * :core:model — Field Book–compatible field layout import.
 *
 * Responsibility: turn a CSV field file (PhenoApps Field Book column conventions: one
 * unique identifier column, two ordering columns, arbitrary extra attribute columns, optional
 * `geo_coordinates`) into validated Plot objects plus a list of warnings/errors. The contract
 * is documented in docs/data-formats.md, "Field layout import".
 *
 * Interface: FieldBookImport.parse(csvText, spec) -> ImportResult. No I/O; the caller reads
 * the file (SAF/DocumentFile in :core:data) and hands over text. XLSX is converted to CSV rows
 * by :core:data before reaching this parser.
 */
package org.fieldcapture.model

data class FieldBookImportSpec(
    val trial: String,
    val field: String,
    /** Column holding the Field Book "unique identifier". Null = auto-detect (`plot_id`, then first column). */
    val uniqueIdColumn: String? = null,
    /** Ordering columns shown between the navigation arrows in Field Book. Null = auto-detect. */
    val primaryIdColumn: String? = null,
    val secondaryIdColumn: String? = null,
    /** Column with "lat;lon" (Field Book GeoNav) coordinates. */
    val geoCoordinatesColumn: String = FieldBookImport.GEO_COORDINATES,
)

data class ImportIssue(val severity: Severity, val row: Int?, val message: String) {
    enum class Severity { ERROR, WARNING }
}

data class ImportResult(
    val plots: List<Plot>,
    val issues: List<ImportIssue>,
    val uniqueIdColumn: String,
    val primaryIdColumn: String,
    val secondaryIdColumn: String,
    val columns: List<String>,
) {
    val hasErrors: Boolean get() = issues.any { it.severity == ImportIssue.Severity.ERROR }
}

object FieldBookImport {
    const val GEO_COORDINATES = "geo_coordinates"

    /** Characters Field Book rejects in file names and column headers. */
    val ILLEGAL_HEADER_CHARS: Set<Char> = setOf('/', '?', '*', '|', '"')

    private val UNIQUE_ID_CANDIDATES = listOf("plot_id", "unique_id", "observationunitdbid", "plotid", "id")
    private val PRIMARY_CANDIDATES = listOf("row", "range", "primary_id", "block")
    private val SECONDARY_CANDIDATES = listOf("plot", "column", "col", "secondary_id")

    fun parse(csvText: String, spec: FieldBookImportSpec): ImportResult {
        val issues = ArrayList<ImportIssue>()
        val rows = Csv.parse(csvText)
        if (rows.isEmpty()) {
            issues += error(null, "file is empty")
            return ImportResult(emptyList(), issues, "", "", "", emptyList())
        }
        val header = rows.first().map { it.trim() }
        issues += validateHeader(header)

        val uniqueCol = resolve(header, spec.uniqueIdColumn, UNIQUE_ID_CANDIDATES) ?: header.first()
        val primaryCol = resolve(header, spec.primaryIdColumn, PRIMARY_CANDIDATES)
            ?: header.firstOrNull { it != uniqueCol } ?: uniqueCol
        val secondaryCol = resolve(header, spec.secondaryIdColumn, SECONDARY_CANDIDATES)
            ?: header.firstOrNull { it != uniqueCol && it != primaryCol } ?: primaryCol
        if (spec.uniqueIdColumn != null && spec.uniqueIdColumn !in header) {
            issues += error(1, "unique id column '${spec.uniqueIdColumn}' not found")
        }

        val columns = Columns(header, uniqueCol, primaryCol, secondaryCol, spec.geoCoordinatesColumn)
        val seen = HashSet<String>()
        val plots = ArrayList<Plot>()
        rows.drop(1).forEachIndexed { i, raw ->
            val rowNo = i + 2
            if (raw.any { it.isNotBlank() }) {
                parseRow(raw, rowNo, columns, spec, seen, issues)?.let { plots += it.copy(plantingOrder = plots.size + 1) }
            }
        }
        if (plots.isEmpty()) issues += error(null, "no entries found")
        return ImportResult(plots, issues, uniqueCol, primaryCol, secondaryCol, header)
    }

    private class Columns(
        val header: List<String>,
        val uniqueCol: String,
        val primaryCol: String,
        val secondaryCol: String,
        geoCol: String,
    ) {
        val idx: Map<String, Int> = header.withIndex().associate { it.value to it.index }
        val geoIdx: Int? = idx[geoCol]
    }

    private fun error(row: Int?, msg: String) = ImportIssue(ImportIssue.Severity.ERROR, row, msg)

    private fun validateHeader(header: List<String>): List<ImportIssue> {
        val issues = ArrayList<ImportIssue>()
        header.forEachIndexed { i, h ->
            if (h.isBlank()) issues += error(1, "column ${i + 1} has an empty header")
            val bad = h.filter { it in ILLEGAL_HEADER_CHARS }
            if (bad.isNotEmpty()) issues += error(1, "header '$h' contains characters Field Book rejects: $bad")
        }
        header.groupBy { it.lowercase() }.filter { it.value.size > 1 }.keys
            .forEach { issues += error(1, "duplicate header '$it'") }
        return issues
    }

    private fun parseRow(
        raw: List<String>,
        rowNo: Int,
        cols: Columns,
        spec: FieldBookImportSpec,
        seen: MutableSet<String>,
        issues: MutableList<ImportIssue>,
    ): Plot? {
        if (raw.size != cols.header.size) {
            issues += error(rowNo, "expected ${cols.header.size} fields, found ${raw.size}")
            return null
        }
        val cells = raw.map { it.trim() }
        val uid = cells[cols.idx.getValue(cols.uniqueCol)]
        if (uid.isBlank()) {
            issues += error(rowNo, "blank unique identifier")
            return null
        }
        if (!seen.add(uid)) {
            issues += error(rowNo, "duplicate unique identifier '$uid'")
            return null
        }
        val centroid = cols.geoIdx?.let { g ->
            val parsed = parseGeoCoordinates(cells[g])
            parsed.warning?.let { issues += ImportIssue(ImportIssue.Severity.WARNING, rowNo, it) }
            parsed.position
        }
        val attributes = cols.header.indices
            .filter { cols.header[it] != cols.uniqueCol }
            .associate { cols.header[it] to cells[it] }
        return Plot(
            key = PlotKey(spec.trial, spec.field, uid),
            primaryId = cells[cols.idx.getValue(cols.primaryCol)],
            secondaryId = cells[cols.idx.getValue(cols.secondaryCol)],
            plantingOrder = 0,
            attributes = attributes,
            centroid = centroid,
        )
    }

    private fun resolve(header: List<String>, requested: String?, candidates: List<String>): String? {
        if (requested != null) return header.firstOrNull { it == requested }
        val lower = header.associateBy { it.lowercase() }
        return candidates.firstNotNullOfOrNull { lower[it] }
    }

    data class GeoParse(val position: LatLon?, val warning: String?)

    /**
     * Parse Field Book's `geo_coordinates` cell. The documentation states `Lat;Long`; the
     * bundled `rtk_sample.csv` contains `-96.3431604;36.6677618` (lon;lat). Both are accepted:
     * when exactly one ordering yields a valid latitude, that ordering is used; when both are
     * valid the documented lat;lon order is used and a warning is returned.
     */
    fun parseGeoCoordinates(cell: String): GeoParse {
        val t = cell.trim()
        if (t.isEmpty()) return GeoParse(null, null)
        val parts = t.split(';', ',').map { it.trim() }
        if (parts.size != 2) return GeoParse(null, "geo_coordinates '$t' is not two numbers separated by ';'")
        val a = parts[0].toDoubleOrNull()
        val b = parts[1].toDoubleOrNull()
        if (a == null || b == null) return GeoParse(null, "geo_coordinates '$t' is not numeric")
        val latFirstOk = a in -90.0..90.0 && b in -180.0..180.0
        val lonFirstOk = b in -90.0..90.0 && a in -180.0..180.0
        return when {
            latFirstOk && !lonFirstOk -> GeoParse(LatLon(a, b), null)
            lonFirstOk && !latFirstOk -> GeoParse(LatLon(b, a), "geo_coordinates '$t' read as lon;lat")
            latFirstOk && lonFirstOk -> GeoParse(LatLon(a, b), "geo_coordinates '$t' ambiguous; assumed lat;lon")
            else -> GeoParse(null, "geo_coordinates '$t' out of range")
        }
    }
}

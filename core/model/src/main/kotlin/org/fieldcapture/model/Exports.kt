/*
 * :core:model — export serializers and file-naming contracts.
 *
 * Responsibility: produce the byte-exact text of the GeoJSON, GPX and flat-CSV exports and the
 * photo folder/file names described in docs/data-formats.md. Writers are hand-rolled so the
 * module has no dependencies and the output is deterministic (stable key order, fixed number
 * formatting), which keeps the exports diff-friendly for the R/Power BI side.
 *
 * Interface: GeoJson.plots(...), GeoJson.points(...), Gpx.track(...), MediaPath.photo(...).
 * The SQLite export is produced by :core:data (Room database copy) and is not defined here.
 */
package org.fieldcapture.model

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object Iso8601 {
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT).withZone(ZoneOffset.UTC)
    private val compact = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.ROOT).withZone(ZoneOffset.UTC)

    /** UTC, millisecond precision, trailing Z. Used in every CSV/JSON export. */
    fun utc(epochMs: Long): String = fmt.format(Instant.ofEpochMilli(epochMs))

    /** File-name safe UTC timestamp, e.g. 20260904T143012Z. */
    fun compactUtc(epochMs: Long): String = compact.format(Instant.ofEpochMilli(epochMs))
}

object Json {
    fun str(s: String): String = buildString {
        append('"')
        for (c in s) {
            when (c) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (c < ' ') append(String.format(Locale.ROOT, "\\u%04x", c.code)) else append(c)
            }
        }
        append('"')
    }

    fun num(d: Double, decimals: Int = 7): String = String.format(Locale.ROOT, "%.${decimals}f", d)

    fun props(map: Map<String, String?>): String =
        map.entries.joinToString(",", "{", "}") { (k, v) -> str(k) + ":" + (v?.let { str(it) } ?: "null") }
}

/** GeoJSON per RFC 7946: coordinates are [lon, lat], WGS 84, FeatureCollection root. */
object GeoJson {
    private fun position(p: LatLon) = "[${Json.num(p.lon)},${Json.num(p.lat)}]"

    /** Plots as Polygon features when a polygon exists, else Point features at the centroid. */
    fun plots(plots: Collection<Plot>): String {
        val features = plots.mapNotNull { plot ->
            val geometry = when {
                plot.polygon.size >= 3 -> {
                    val ring = (plot.polygon + plot.polygon.first()).joinToString(",") { position(it) }
                    "{\"type\":\"Polygon\",\"coordinates\":[[$ring]]}"
                }
                plot.centroid != null -> "{\"type\":\"Point\",\"coordinates\":${position(plot.centroid)}}"
                else -> return@mapNotNull null
            }
            val props = linkedMapOf<String, String?>(
                "plot_id" to plot.plotId,
                "trial" to plot.key.trial,
                "field" to plot.key.field,
                "primary_id" to plot.primaryId,
                "secondary_id" to plot.secondaryId,
                "planting_order" to plot.plantingOrder.toString(),
            )
            plot.attributes.toSortedMap().forEach { (k, v) -> props.putIfAbsent(k, v) }
            "{\"type\":\"Feature\",\"geometry\":$geometry,\"properties\":${Json.props(props)}}"
        }
        return "{\"type\":\"FeatureCollection\",\"features\":[${features.joinToString(",")}]}"
    }

    /** Point features for planter fixes, annotations or photos. [properties] must already be flat strings. */
    fun points(points: List<Pair<LatLon, Map<String, String?>>>): String {
        val features = points.joinToString(",") { (p, props) ->
            "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":${position(p)}},\"properties\":${Json.props(props)}}"
        }
        return "{\"type\":\"FeatureCollection\",\"features\":[$features]}"
    }
}

/** GPX 1.1 track writer. Accuracy and speed go into an extensions block because GPX 1.1 has no core field for them. */
object Gpx {
    const val NAMESPACE = "http://www.topografix.com/GPX/1/1"
    const val EXT_NAMESPACE = "https://fieldcapture.org/gpx/1"

    fun track(name: String, fixes: List<GpsFix>, creator: String = "field-capture-android"): String = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        append("<gpx version=\"1.1\" creator=\"").append(xml(creator)).append("\" xmlns=\"").append(NAMESPACE)
        append("\" xmlns:fc=\"").append(EXT_NAMESPACE).append("\">\n")
        append("  <trk><name>").append(xml(name)).append("</name><trkseg>\n")
        for (f in fixes.sortedBy { it.timestampMs }) {
            append("    <trkpt lat=\"").append(Json.num(f.lat)).append("\" lon=\"").append(Json.num(f.lon)).append("\">")
            f.altitudeM?.let { append("<ele>").append(Json.num(it, 1)).append("</ele>") }
            append("<time>").append(Iso8601.utc(f.timestampMs)).append("</time>")
            append("<extensions><fc:accuracy_m>").append(Json.num(f.accuracyM.toDouble(), 1)).append("</fc:accuracy_m>")
            f.speedMps?.let { append("<fc:speed_mps>").append(Json.num(it.toDouble(), 2)).append("</fc:speed_mps>") }
            append("<fc:provider>").append(xml(f.provider)).append("</fc:provider></extensions>")
            append("</trkpt>\n")
        }
        append("  </trkseg></trk>\n</gpx>\n")
    }

    private fun xml(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
}

/**
 * Photo/media path contract: `<trial>/<field>/<plot_id>/<note_type>/<timestamp>_<seq>.jpg`.
 * Each segment is sanitized so the folder tree is portable across Android, Windows and macOS,
 * and so it mirrors the export CSV columns trial, field, plot_id, note_type.
 */
object MediaPath {
    private val unsafe = Regex("[^A-Za-z0-9._-]+")

    fun segment(raw: String): String {
        val s = raw.trim().replace(unsafe, "_").trim('_', '.')
        return if (s.isEmpty()) "_" else s.take(80)
    }

    fun photoDir(key: PlotKey, noteType: String): String =
        listOf(key.trial, key.field, key.plotId, noteType).joinToString("/") { segment(it) }

    fun photo(key: PlotKey, noteType: String, timestampMs: Long, seq: Int, ext: String = "jpg"): String =
        photoDir(key, noteType) + "/" + Iso8601.compactUtc(timestampMs) + "_" + String.format(Locale.ROOT, "%02d", seq) + "." + ext

    /** Sidecar JSON sits next to the media file with the same stem. */
    fun sidecar(mediaPath: String): String = mediaPath.substringBeforeLast('.') + ".json"
}

package org.fieldcapture.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExportsTest {
    private val key = PlotKey("2026_YT_Columbia", "Bradford North", "26YT-0042")

    @Test
    fun `geojson writes lon then lat and closes polygon rings`() {
        val plot = Plot(
            key = key, primaryId = "3", secondaryId = "42", plantingOrder = 7,
            attributes = mapOf("pedigree" to "A/B", "rep" to "1"),
            centroid = LatLon(38.9, -92.27),
            polygon = listOf(LatLon(38.9, -92.27), LatLon(38.9, -92.2699), LatLon(38.90003, -92.2699)),
        )
        val json = GeoJson.plots(listOf(plot))
        assertTrue(json.startsWith("{\"type\":\"FeatureCollection\""))
        assertTrue("\"coordinates\":[[[-92.2700000,38.9000000]" in json, json)
        // ring closed: first vertex repeated as last
        assertEquals(2, Regex("\\[-92.2700000,38.9000000\\]").findAll(json).count())
        assertTrue("\"plot_id\":\"26YT-0042\"" in json)
        assertTrue("\"pedigree\":\"A/B\"" in json)
    }

    @Test
    fun `geojson point features escape strings`() {
        val json = GeoJson.points(listOf(LatLon(1.0, 2.0) to mapOf("note" to "say \"hi\"\nnow", "n" to null)))
        assertTrue("\"note\":\"say \\\"hi\\\"\\nnow\"" in json)
        assertTrue("\"n\":null" in json)
        assertTrue("\"coordinates\":[2.0000000,1.0000000]" in json)
    }

    @Test
    fun `gpx track has 1_1 namespace, ISO times and accuracy extension`() {
        val fixes = listOf(
            GpsFix(38.9, -92.27, 3.0f, 1_756_000_000_000L, altitudeM = 230.4, speedMps = 1.3f),
            GpsFix(38.90001, -92.27, 2.5f, 1_756_000_001_000L),
        )
        val gpx = Gpx.track("walk-1", fixes)
        assertTrue("xmlns=\"http://www.topografix.com/GPX/1/1\"" in gpx)
        assertTrue("<trkpt lat=\"38.9000000\" lon=\"-92.2700000\">" in gpx)
        assertTrue("<time>" + Iso8601.utc(1_756_000_000_000L) + "</time>" in gpx)
        assertTrue("<fc:accuracy_m>3.0</fc:accuracy_m>" in gpx)
        assertTrue("<ele>230.4</ele>" in gpx)
        assertEquals(2, Regex("<trkpt ").findAll(gpx).count())
    }

    @Test
    fun `photo path mirrors trial-field-plot-note hierarchy with sanitized segments`() {
        val t = 1_756_000_000_000L // 2025-08-24T01:46:40Z
        val path = MediaPath.photo(key, "lodging score", t, 3)
        assertEquals("2026_YT_Columbia/Bradford_North/26YT-0042/lodging_score/20250824T014640Z_03.jpg", path)
        assertEquals("2026_YT_Columbia/Bradford_North/26YT-0042/lodging_score/20250824T014640Z_03.json", MediaPath.sidecar(path))
        assertEquals("_", MediaPath.segment("///"))
    }

    @Test
    fun `iso8601 is UTC with milliseconds`() {
        assertEquals("2025-08-24T01:46:40.000Z", Iso8601.utc(1_756_000_000_000L))
    }

    @Test
    fun `sample manifest fills column-major, skips reserved wells and spans plates`() {
        val samples = (1..100).map { i ->
            SampleRequest(
                "S%03d".format(i), "P$i", "T", "F", "G$i",
                collectedAtMs = 1_756_000_000_000L, collectedBy = "pt", position = null,
            )
        }
        val cfg = PlateConfig(plateIdPrefix = "PLT", reservedWells = setOf("H12"))
        val wells = SampleManifest.layout(samples, cfg)
        assertEquals(192, wells.size)
        assertEquals("A01", wells[0].well)
        assertEquals("B01", wells[1].well)
        assertEquals("S001", wells[0].sample?.sampleId)
        val h12 = wells.first { it.plateId == "PLT001" && it.well == "H12" }
        assertTrue(h12.isControl)
        val plate2 = wells.filter { it.plateId == "PLT002" }
        assertEquals(5, plate2.count { it.sample != null }) // 100 − 95 usable wells on plate 1
        val csv = SampleManifest.csv(wells, cfg)
        val rows = Csv.parse(csv)
        assertEquals(SampleManifest.HEADER, rows.first())
        assertEquals(193, rows.size)
        assertEquals("CONTROL", rows.first { it[0] == "PLT001" && it[1] == "H12" }[4])
    }
}

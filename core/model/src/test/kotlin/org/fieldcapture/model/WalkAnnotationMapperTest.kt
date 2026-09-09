package org.fieldcapture.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WalkAnnotationMapperTest {
    private val layout = GridLayout(
        origin = LatLon(38.9060, -92.2690),
        rangeBearingDeg = 0.0,
        rows = 4,
        ranges = 2,
        rowSpacingM = 0.76,
        rangeLengthM = 4.0,
        rowsPerPlot = 2,
    )
    private val plots = layout.generate("T1", "F1")

    /** Walk north along plot column 1 at 1 m/s, one fix per second, starting at the south edge. */
    private fun track(seconds: Int, gapAfterSecond: Int? = null): List<GpsFix> =
        (0..seconds).filter { gapAfterSecond == null || it <= gapAfterSecond || it > gapAfterSecond + 8 }.map { s ->
            val p = Geo.fromLocalMeters(layout.origin, 0.76, s.toDouble())
            GpsFix(p.lat, p.lon, accuracyM = 2.5f, timestampMs = 1_000_000L + s * 1000L)
        }

    private fun ann(id: String, tMs: Long) = Annotation(
        id = id, sessionId = "walk-1", timestampMs = tMs, transcript = "note $id", audioPath = null,
        sttEngine = "vosk", sttConfidence = 0.9f, assignedPlotId = null, assignmentMethod = null, assignmentDistanceM = null,
    )

    @Test
    fun `annotation between fixes is interpolated and assigned to the plot under the walker`() {
        val mapped = WalkAnnotationMapper.map(listOf(ann("a", 1_000_000L + 1500)), track(8), plots)
        val m = mapped.single()
        assertNotNull(m.position)
        val (_, north) = Geo.toLocalMeters(layout.origin, m.position.position)
        assertEquals(1.5, north, 0.05)
        assertEquals("R1P1", m.assignment?.plotId)
        assertEquals(AssignmentMethod.POLYGON, m.assignment?.method)
        assertFalse(m.needsReview)
    }

    @Test
    fun `annotation in second range maps to second range plot`() {
        val mapped = WalkAnnotationMapper.map(listOf(ann("b", 1_000_000L + 6000)), track(8), plots)
        assertEquals("R2P1", mapped.single().assignment?.plotId)
    }

    @Test
    fun `annotation inside a GPS gap is positioned but flagged for review`() {
        val t = track(20, gapAfterSecond = 3)
        assertEquals(listOf(1_000_000L + 3000 to 1_000_000L + 12_000), TrackInterpolator.gaps(t))
        val mapped = WalkAnnotationMapper.map(listOf(ann("c", 1_000_000L + 6000)), t, plots)
        val m = mapped.single()
        assertNotNull(m.position)
        assertTrue(m.position.gapMs > TrackInterpolator.GAP_THRESHOLD_MS)
        assertTrue(m.position.accuracyM > 2.5f)
        assertTrue(m.needsReview)
    }

    @Test
    fun `annotation after the track ends clamps to the last fix and is extrapolated`() {
        val mapped = WalkAnnotationMapper.map(listOf(ann("d", 1_000_000L + 30_000)), track(8), plots)
        val m = mapped.single()
        assertTrue(m.position!!.extrapolated)
        assertTrue(m.needsReview)
    }

    @Test
    fun `manual correction wins over automatic assignment`() {
        val corrected = ann("e", 1_000_000L + 1500).copy(assignedPlotId = "R2P2", manuallyCorrected = true)
        val m = WalkAnnotationMapper.map(listOf(corrected), track(8), plots).single()
        assertEquals("R2P2", m.assignment?.plotId)
        assertEquals(AssignmentMethod.MANUAL, m.assignment?.method)
    }
}

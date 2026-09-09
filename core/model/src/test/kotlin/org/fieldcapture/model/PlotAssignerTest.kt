package org.fieldcapture.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlotAssignerTest {
    // Soybean nursery near Columbia, MO: 4 plots across (2-row plots, 0.76 m rows), 3 ranges of 3.6 m with 1.2 m alleys.
    private val layout = GridLayout(
        origin = LatLon(38.9060, -92.2690),
        rangeBearingDeg = 0.0,
        rows = 8,
        ranges = 3,
        rowSpacingM = 0.76,
        rangeLengthM = 3.6,
        alleyM = 1.2,
        rowsPerPlot = 2,
    )
    private val plots = layout.generate(trial = "T1", field = "F1", serpentine = true)

    @Test
    fun `grid produces one plot per row-group and range in serpentine order`() {
        assertEquals(12, plots.size)
        assertEquals(listOf("R1P1", "R1P2", "R1P3", "R1P4", "R2P4", "R2P3", "R2P2", "R2P1", "R3P1"), plots.take(9).map { it.plotId })
        assertEquals((1..12).toList(), plots.map { it.plantingOrder })
    }

    @Test
    fun `centroid of first plot is half a plot width east and half a range north of origin`() {
        val c = plots.first { it.plotId == "R1P1" }.centroid!!
        val (east, north) = Geo.toLocalMeters(layout.origin, c)
        assertEquals(0.76, east, 0.01)
        assertEquals(1.8, north, 0.01)
    }

    @Test
    fun `point inside a plot polygon is assigned by polygon containment`() {
        val target = plots.first { it.plotId == "R2P3" }
        val inside = Geo.fromLocalMeters(layout.origin, 2 * 1.52 + 0.3, 4.8 + 0.5)
        val a = PlotAssigner.assign(inside, plots)
        assertNotNull(a)
        assertEquals(target.plotId, a.plotId)
        assertEquals(AssignmentMethod.POLYGON, a.method)
        assertTrue(!a.isAmbiguous())
    }

    @Test
    fun `point in the alley falls back to nearest centroid and is flagged ambiguous when equidistant`() {
        // Exactly in the alley between range 1 and range 2, centred on plot column 2.
        val alleyPoint = Geo.fromLocalMeters(layout.origin, 1.52 + 0.76, 3.6 + 0.6)
        val a = PlotAssigner.assign(alleyPoint, plots)
        assertNotNull(a)
        assertEquals(AssignmentMethod.NEAREST_CENTROID, a.method)
        assertTrue(a.plotId == "R1P2" || a.plotId == "R2P2")
        assertTrue(a.isAmbiguous())
    }

    @Test
    fun `point far from every plot is not assigned`() {
        val far = Geo.offset(layout.origin, 180.0, 50.0)
        assertNull(PlotAssigner.assign(far, plots))
    }

    @Test
    fun `centroid-only plots are matched within tolerance`() {
        val centroidOnly = plots.map { it.copy(polygon = emptyList()) }
        val near = Geo.offset(centroidOnly[0].centroid!!, 90.0, 0.5)
        val a = PlotAssigner.assign(near, centroidOnly, maxDistanceM = 2.0)
        assertNotNull(a)
        assertEquals("R1P1", a.plotId)
        assertEquals(0.5, a.distanceM, 0.02)
    }

    @Test
    fun `haversine distance matches known value`() {
        // 1 arc-minute of latitude ≈ 1852 m (nautical mile definition; spherical model within 0.5 %).
        val d = Geo.distanceM(LatLon(38.0, -92.0), LatLon(38.0 + 1.0 / 60.0, -92.0))
        assertEquals(1853.0, d, 5.0)
    }
}

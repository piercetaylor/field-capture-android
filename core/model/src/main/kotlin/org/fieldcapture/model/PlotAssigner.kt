/*
 * :core:model — plot assignment and layout generation.
 *
 * Responsibility: decide which plot a position belongs to (polygon containment first, then
 * nearest centroid within a tolerance), and generate a row/range grid of plot centroids and
 * rectangles from a surveyed origin when planter-mode coordinates are not available.
 *
 * Interface: PlotAssigner.assign(...) and GridLayout.generate(...). Deterministic, no I/O.
 * Used by walk mode (annotation → plot), photo capture (auto-fill plot), and the review screen
 * (ambiguity flags). Unit-tested in PlotAssignerTest.
 */
package org.fieldcapture.model

object PlotAssigner {
    /** Default maximum centroid distance accepted as a match. Row spacing in soybean is 0.76 m; plots 3–6 m long. */
    const val DEFAULT_MAX_DISTANCE_M = 6.0

    /**
     * Assign [position] to one of [plots].
     * Order of preference: a plot whose polygon contains the point; otherwise the plot with the
     * nearest centroid if that distance is ≤ [maxDistanceM]; otherwise null.
     */
    fun assign(
        position: LatLon,
        plots: Collection<Plot>,
        maxDistanceM: Double = DEFAULT_MAX_DISTANCE_M,
    ): PlotAssignment? {
        val byDistance = plots
            .mapNotNull { p -> p.centroid?.let { c -> p to Geo.distanceM(c, position) } }
            .sortedBy { it.second }

        val containing = plots.firstOrNull { it.polygon.size >= 3 && Geo.contains(it.polygon, position) }
        return when {
            containing != null -> {
                val d = containing.centroid?.let { Geo.distanceM(it, position) } ?: 0.0
                val runnerUp = byDistance.firstOrNull { it.first.plotId != containing.plotId }?.second
                PlotAssignment(containing.plotId, AssignmentMethod.POLYGON, d, runnerUp)
            }
            byDistance.isEmpty() || byDistance.first().second > maxDistanceM -> null
            else -> {
                val (plot, d) = byDistance.first()
                PlotAssignment(plot.plotId, AssignmentMethod.NEAREST_CENTROID, d, byDistance.getOrNull(1)?.second)
            }
        }
    }
}

/**
 * Row/range grid generator. Ranges run along [rangeBearingDeg]; rows are perpendicular
 * (bearing + 90°). Plot (row r, range g), 1-based, has its centroid at
 * origin + (r − 0.5)·rowSpacing along the row axis + (g − 0.5)·rangeLength along the range axis.
 */
data class GridLayout(
    val origin: LatLon,
    val rangeBearingDeg: Double,
    val rows: Int,
    val ranges: Int,
    val rowSpacingM: Double,
    val rangeLengthM: Double,
    /** Alley between ranges, added to each range step but excluded from the plot rectangle. */
    val alleyM: Double = 0.0,
    /** Number of adjacent rows that make up one plot (e.g., 2-row or 4-row plots). */
    val rowsPerPlot: Int = 1,
) {
    init {
        require(rows > 0 && ranges > 0) { "rows and ranges must be positive" }
        require(rowSpacingM > 0 && rangeLengthM > 0) { "spacing must be positive" }
        require(rowsPerPlot in 1..rows) { "rowsPerPlot must be between 1 and rows" }
        require(rows % rowsPerPlot == 0) { "rows must be a multiple of rowsPerPlot" }
    }

    private val rowBearing get() = (rangeBearingDeg + 90.0) % 360.0
    private val plotWidthM get() = rowSpacingM * rowsPerPlot

    /** Centroid of the plot occupying rows [firstRow, firstRow + rowsPerPlot) in range [range]. */
    fun centroid(plotIndexAcross: Int, range: Int): LatLon {
        val across = (plotIndexAcross - 0.5) * plotWidthM
        val along = (range - 1) * (rangeLengthM + alleyM) + rangeLengthM / 2
        val p = Geo.offset(origin, rowBearing, across)
        return Geo.offset(p, rangeBearingDeg, along)
    }

    /** Rectangle for the plot, as a closed ring without the repeated first vertex. */
    fun polygon(plotIndexAcross: Int, range: Int): List<LatLon> {
        val x0 = (plotIndexAcross - 1) * plotWidthM
        val x1 = x0 + plotWidthM
        val y0 = (range - 1) * (rangeLengthM + alleyM)
        val y1 = y0 + rangeLengthM
        return listOf(x0 to y0, x1 to y0, x1 to y1, x0 to y1).map { (x, y) ->
            Geo.offset(Geo.offset(origin, rowBearing, x), rangeBearingDeg, y)
        }
    }

    /**
     * Generate plots in serpentine or linear planting order. Returns plots with
     * `attributes["row"]` and `attributes["range"]` set, plotId = "R{range}-P{plotAcross}" unless
     * [idFor] is supplied.
     */
    fun generate(
        trial: String,
        field: String,
        serpentine: Boolean = true,
        idFor: (plotAcross: Int, range: Int) -> String = { a, g -> "R${g}P$a" },
    ): List<Plot> {
        val plotsAcross = rows / rowsPerPlot
        val out = ArrayList<Plot>(plotsAcross * ranges)
        var order = 0
        for (g in 1..ranges) {
            val acrossOrder = if (serpentine && g % 2 == 0) (plotsAcross downTo 1) else (1..plotsAcross)
            for (a in acrossOrder) {
                order += 1
                out += Plot(
                    key = PlotKey(trial, field, idFor(a, g)),
                    primaryId = g.toString(),
                    secondaryId = a.toString(),
                    plantingOrder = order,
                    attributes = mapOf("range" to g.toString(), "row" to a.toString()),
                    centroid = centroid(a, g),
                    polygon = polygon(a, g),
                )
            }
        }
        return out
    }
}

/*
 * :core:model — walk-mode annotation → plot mapping.
 *
 * Responsibility: given the GPS track written during a walk (≥ 1 Hz fixes) and the timestamps
 * of spoken annotations, interpolate the position at each annotation time and assign it to a
 * plot with PlotAssigner. Also flags gaps in the track (GPS loss) so the review screen can
 * show which annotations were positioned by interpolation across a gap.
 *
 * Interface: TrackInterpolator.positionAt(track, t) and WalkAnnotationMapper.map(...).
 * Pure functions; the same code runs in unit tests and on the device.
 */
package org.fieldcapture.model

data class InterpolatedPosition(
    val position: LatLon,
    /** Larger of the two bracketing fixes' accuracies, inflated by gap length. */
    val accuracyM: Float,
    /** Milliseconds between the two fixes used; 0 when an exact fix matched. */
    val gapMs: Long,
    val extrapolated: Boolean,
)

object TrackInterpolator {
    /** Gaps longer than this are reported as GPS loss; interpolation across them is flagged. */
    const val GAP_THRESHOLD_MS = 5_000L

    /** Accuracy is inflated by this many metres per second of gap (walking speed ≈ 1.4 m/s). */
    private const val ACCURACY_INFLATION_M_PER_S = 1.4f

    /**
     * Position at time [t]. [track] must be sorted by timestamp. Returns null for an empty track.
     * Times before the first fix or after the last fix clamp to that fix and are marked extrapolated.
     */
    fun positionAt(track: List<GpsFix>, t: Long): InterpolatedPosition? = when {
        track.isEmpty() -> null
        t <= track.first().timestampMs -> clamp(track.first(), gapMs = track.first().timestampMs - t)
        t >= track.last().timestampMs -> clamp(track.last(), gapMs = t - track.last().timestampMs)
        else -> interpolate(track, t)
    }

    private fun clamp(f: GpsFix, gapMs: Long) =
        InterpolatedPosition(f.position, f.accuracyM, gapMs, extrapolated = gapMs > 0)

    private fun interpolate(track: List<GpsFix>, t: Long): InterpolatedPosition {
        var lo = 0
        var hi = track.size - 1
        while (hi - lo > 1) {
            val mid = (lo + hi) / 2
            if (track[mid].timestampMs <= t) lo = mid else hi = mid
        }
        val a = track[lo]
        val b = track[hi]
        if (a.timestampMs == t) return InterpolatedPosition(a.position, a.accuracyM, 0, false)
        val span = (b.timestampMs - a.timestampMs).coerceAtLeast(1)
        val frac = (t - a.timestampMs).toDouble() / span
        val pos = LatLon(
            a.lat + (b.lat - a.lat) * frac,
            a.lon + (b.lon - a.lon) * frac,
        )
        val inflation = if (span > GAP_THRESHOLD_MS) (span / 1000f) * ACCURACY_INFLATION_M_PER_S else 0f
        return InterpolatedPosition(pos, maxOf(a.accuracyM, b.accuracyM) + inflation, span, false)
    }

    /** Pairs of (start, end) timestamps where consecutive fixes are more than [thresholdMs] apart. */
    fun gaps(track: List<GpsFix>, thresholdMs: Long = GAP_THRESHOLD_MS): List<Pair<Long, Long>> =
        track.zipWithNext()
            .filter { (a, b) -> b.timestampMs - a.timestampMs > thresholdMs }
            .map { (a, b) -> a.timestampMs to b.timestampMs }
}

data class MappedAnnotation(
    val annotation: Annotation,
    val position: InterpolatedPosition?,
    val assignment: PlotAssignment?,
) {
    /** Needs human review: no position, no plot, ambiguous plot, or positioned across a GPS gap. */
    val needsReview: Boolean
        get() = position == null || assignment == null || assignment.isAmbiguous() ||
            position.gapMs > TrackInterpolator.GAP_THRESHOLD_MS || position.extrapolated
}

object WalkAnnotationMapper {
    fun map(
        annotations: List<Annotation>,
        track: List<GpsFix>,
        plots: Collection<Plot>,
        maxDistanceM: Double = PlotAssigner.DEFAULT_MAX_DISTANCE_M,
    ): List<MappedAnnotation> {
        val sorted = track.sortedBy { it.timestampMs }
        return annotations.map { ann ->
            if (ann.manuallyCorrected && ann.assignedPlotId != null) {
                val pos = TrackInterpolator.positionAt(sorted, ann.timestampMs)
                MappedAnnotation(ann, pos, PlotAssignment(ann.assignedPlotId, AssignmentMethod.MANUAL, 0.0, null))
            } else {
                val pos = TrackInterpolator.positionAt(sorted, ann.timestampMs)
                val assignment = pos?.let { PlotAssigner.assign(it.position, plots, maxDistanceM) }
                MappedAnnotation(ann, pos, assignment)
            }
        }
    }
}

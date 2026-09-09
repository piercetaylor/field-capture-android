/*
 * :core:model — geodesy helpers.
 *
 * Responsibility: distance, local planar projection, bearing offsets, polygon tests and
 * centroids on WGS 84. Accuracy target is sub-decimetre over field scales (< 2 km), which a
 * spherical model plus a local equirectangular projection satisfies; a full ellipsoidal
 * solution is not needed for plot assignment.
 *
 * Interface: pure functions on LatLon. No I/O, no Android.
 */
package org.fieldcapture.model

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object Geo {
    /** Mean Earth radius in metres (IUGG). */
    const val EARTH_RADIUS_M = 6_371_008.8

    private fun Double.toRad() = this * PI / 180.0
    private fun Double.toDeg() = this * 180.0 / PI

    /** Great-circle distance in metres (haversine). */
    fun distanceM(a: LatLon, b: LatLon): Double {
        val dLat = (b.lat - a.lat).toRad()
        val dLon = (b.lon - a.lon).toRad()
        val h = sin(dLat / 2) * sin(dLat / 2) +
            cos(a.lat.toRad()) * cos(b.lat.toRad()) * sin(dLon / 2) * sin(dLon / 2)
        return 2 * EARTH_RADIUS_M * asin(sqrt(h))
    }

    /** Destination point given start, initial bearing (degrees clockwise from north) and distance. */
    fun offset(from: LatLon, bearingDeg: Double, distanceM: Double): LatLon {
        val d = distanceM / EARTH_RADIUS_M
        val brg = bearingDeg.toRad()
        val lat1 = from.lat.toRad()
        val lon1 = from.lon.toRad()
        val lat2 = asin(sin(lat1) * cos(d) + cos(lat1) * sin(d) * cos(brg))
        val lon2 = lon1 + atan2(sin(brg) * sin(d) * cos(lat1), cos(d) - sin(lat1) * sin(lat2))
        return LatLon(lat2.toDeg(), normalizeLon(lon2.toDeg()))
    }

    private fun normalizeLon(lon: Double): Double = ((lon + 540.0) % 360.0) - 180.0

    /** Local planar coordinates (east, north) in metres relative to [origin]. Valid for small extents. */
    fun toLocalMeters(origin: LatLon, p: LatLon): Pair<Double, Double> {
        val x = (p.lon - origin.lon).toRad() * cos(origin.lat.toRad()) * EARTH_RADIUS_M
        val y = (p.lat - origin.lat).toRad() * EARTH_RADIUS_M
        return x to y
    }

    /** Ray-casting point-in-polygon in local metres. [ring] is a closed ring without the repeated first point. */
    fun contains(ring: List<LatLon>, p: LatLon): Boolean {
        if (ring.size < 3) return false
        val origin = ring.first()
        val (px, py) = toLocalMeters(origin, p)
        val xs = ring.map { toLocalMeters(origin, it) }
        var inside = false
        var j = xs.size - 1
        for (i in xs.indices) {
            val (xi, yi) = xs[i]
            val (xj, yj) = xs[j]
            val crosses = (yi > py) != (yj > py) &&
                px < (xj - xi) * (py - yi) / (yj - yi) + xi
            if (crosses) inside = !inside
            j = i
        }
        return inside
    }

    /** Area-weighted centroid of a simple polygon; falls back to the vertex mean for degenerate rings. */
    fun centroid(ring: List<LatLon>): LatLon {
        require(ring.isNotEmpty()) { "empty ring" }
        if (ring.size < 3) return LatLon(ring.map { it.lat }.average(), ring.map { it.lon }.average())
        val origin = ring.first()
        val pts = ring.map { toLocalMeters(origin, it) }
        var area = 0.0
        var cx = 0.0
        var cy = 0.0
        for (i in pts.indices) {
            val (x0, y0) = pts[i]
            val (x1, y1) = pts[(i + 1) % pts.size]
            val cross = x0 * y1 - x1 * y0
            area += cross
            cx += (x0 + x1) * cross
            cy += (y0 + y1) * cross
        }
        if (area == 0.0) return LatLon(ring.map { it.lat }.average(), ring.map { it.lon }.average())
        area *= 0.5
        cx /= 6 * area
        cy /= 6 * area
        return fromLocalMeters(origin, cx, cy)
    }

    /** Inverse of [toLocalMeters]. */
    fun fromLocalMeters(origin: LatLon, east: Double, north: Double): LatLon {
        val lat = origin.lat + (north / EARTH_RADIUS_M).toDeg()
        val lon = origin.lon + (east / (EARTH_RADIUS_M * cos(origin.lat.toRad()))).toDeg()
        return LatLon(lat, normalizeLon(lon))
    }
}

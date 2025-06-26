package com.youcef_bounaas.athlo.Record.utils

import com.mapbox.geojson.Point
import kotlin.math.*

fun haversineDistance(p1: Point, p2: Point): Double {
    val R = 6371000.0 // Earth radius in meters

    val lat1 = p1.latitude()
    val lon1 = p1.longitude()
    val lat2 = p2.latitude()
    val lon2 = p2.longitude()

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return R * c // distance in meters
}

fun calculateTotalDistanceInKm(segments: List<List<Point>>): Float {
    var total = 0.0
    segments.forEach { segment ->
        for (i in 0 until segment.size - 1) {
            total += haversineDistance(segment[i], segment[i + 1])
        }
    }
    return (total / 1000).toFloat() // meters → km
}

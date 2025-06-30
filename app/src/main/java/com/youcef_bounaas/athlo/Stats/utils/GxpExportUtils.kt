package com.youcef_bounaas.athlo.Stats.utils

import com.youcef_bounaas.athlo.Stats.data.TrackPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone





fun generateGpx(
    pathSegments: List<List<TrackPoint>>,
    runStartTimeMillis: Long,
    distanceKm: Float,
    durationSec: Int,
    avgPace: String
): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val runStartIso = dateFormat.format(Date(runStartTimeMillis))

    val sb = StringBuilder()
    sb.append("""<?xml version="1.0" encoding="UTF-8"?>""").append("\n")
    sb.append("""<gpx version="1.1" creator="ATHLOWS" xmlns="http://www.topografix.com/GPX/1/1">""").append("\n")
    sb.append("  <metadata>\n")
    sb.append("    <time>$runStartIso</time>\n")
    sb.append("    <extensions>\n")
    sb.append("      <athlows:distance_km xmlns:athlows=\"https://athlows.com/gpx\">$distanceKm</athlows:distance_km>\n")
    sb.append("      <athlows:duration_sec xmlns:athlows=\"https://athlows.com/gpx\">$durationSec</athlows:duration_sec>\n")
    sb.append("      <athlows:avg_pace xmlns:athlows=\"https://athlows.com/gpx\">$avgPace</athlows:avg_pace>\n")
    sb.append("    </extensions>\n")
    sb.append("  </metadata>\n")

    sb.append("  <trk>\n")
    sb.append("    <name>ATHLOWS Run $runStartIso</name>\n")
    sb.append("    <trkseg>\n")

    for (segment in pathSegments) {
        for (tp in segment) {
            val lat = tp.point.latitude()
            val lon = tp.point.longitude()
            val timestamp = dateFormat.format(Date(tp.timestamp))
            sb.append("      <trkpt lat=\"$lat\" lon=\"$lon\">\n")
            sb.append("        <time>$timestamp</time>\n")
            sb.append("      </trkpt>\n")
        }
    }

    sb.append("    </trkseg>\n")
    sb.append("  </trk>\n")
    sb.append("</gpx>\n")




    return sb.toString()
}



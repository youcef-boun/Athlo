package com.youcef_bounaas.athlo.Stats.data

import com.mapbox.geojson.Point

data class TrackPoint(
    val point: Point,
    val timestamp: Long  // milliseconds since epoch (UTC)
)
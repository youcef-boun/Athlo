package com.youcef_bounaas.athlo.Record.data

import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow


object LocationBroadcaster {
    private val _locationFlow = MutableSharedFlow<Point>(extraBufferCapacity = 1)
    val locationFlow: SharedFlow<Point> = _locationFlow

    fun broadcast(point: Point) {
        _locationFlow.tryEmit(point)
    }
}

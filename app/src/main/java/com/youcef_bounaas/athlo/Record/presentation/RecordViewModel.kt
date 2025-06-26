package com.youcef_bounaas.athlo.Record.presentation

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.mapbox.geojson.Point
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.youcef_bounaas.athlo.Record.data.LocationBroadcaster
import com.youcef_bounaas.athlo.Record.presentation.service.TrackingService
import com.youcef_bounaas.athlo.Record.utils.calculateTotalDistanceInKm
import com.youcef_bounaas.athlo.Record.utils.haversineDistance
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


enum class TrackingState { IDLE, TRACKING, PAUSED, FINISHED }

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class RecordViewModel(application: Application) : AndroidViewModel(application) {


    private val _centerOnUser = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val centerOnUser: SharedFlow<Unit> = _centerOnUser



    private val _pathSegments = MutableStateFlow<List<List<Point>>>(emptyList())
    val pathSegments: StateFlow<List<List<Point>>> = _pathSegments.asStateFlow()


    private val _trackingState = MutableStateFlow(TrackingState.IDLE)
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private val minDistanceMeters = 5.0



    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private val _isFinished = MutableStateFlow(true)
    val isFinished = _isFinished.asStateFlow()

    private val _timeInSeconds = MutableStateFlow(0)
    val timeInSeconds = _timeInSeconds.asStateFlow()

    private val _distance = MutableStateFlow(0f)
    val distance = _distance.asStateFlow()

    private val _avgPace = MutableStateFlow("-:--") // static for now
    val avgPace = _avgPace.asStateFlow()

    private val _hasZoomedOnStart = MutableStateFlow(false)
    val hasZoomedOnStart = _hasZoomedOnStart.asStateFlow()


    private val _initialCameraMoved = MutableStateFlow(false)
    val initialCameraMoved: StateFlow<Boolean> = _initialCameraMoved














    fun incrementTime() {
        _timeInSeconds.value +=1




    }


    fun startRun() {
        _isRunning.value = true
        _isFinished.value = false
        _timeInSeconds.value = 0
        _distance.value = 0f
        _pathSegments.value = listOf(mutableListOf())
        _trackingState.value = TrackingState.TRACKING
        resetZoomFlag()
        resetInitialCameraMoved()
    }

    fun pauseOrResumeRun() {
        _isRunning.value = !_isRunning.value
        val context = getApplication<Application>().applicationContext
        val action = if (_isRunning.value) TrackingService.ACTION_RESUME else TrackingService.ACTION_PAUSE


        val intent = Intent(action).apply {
            `package` = context.packageName
        }
        context.sendBroadcast(intent)





        Log.d("RecordViewModel", "📡 Sending broadcast: $action")


        _trackingState.value = if (_isRunning.value) {
            // Add a new empty segment when resuming
            _pathSegments.update { it + listOf(emptyList()) }
            TrackingState.TRACKING
        } else {
            TrackingState.PAUSED
        }
    }



    fun finishRun() {
        _isRunning.value = false
        _isFinished.value = true
        _trackingState.value = TrackingState.FINISHED
    }


    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)


    fun addPoint(point: Point) {
        if (_isRunning.value) {
            val last = pathSegments.value.lastOrNull()?.lastOrNull()
            if (last == null || haversineDistance(last, point) > 5.0) {
                _pathSegments.update { segments ->
                    val copy = segments.toMutableList()
                    if (copy.isEmpty()) copy.add(mutableListOf(point))
                    else {
                        val lastSegment = copy.removeLast().toMutableList()
                        lastSegment.add(point)
                        copy.add(lastSegment)
                    }
                    copy
                }
                // ✅ Update distance and pace here
                val dist = calculateTotalDistanceInKm(_pathSegments.value)
                _distance.value = dist

                if (_timeInSeconds.value > 0 && dist > 0.1f) {
                    _avgPace.value = calculateAvgPace(_timeInSeconds.value, dist)
                } else {
                    _avgPace.value = "--:--"
                }
            }
        }
    }


    fun markZoomed() {
        _hasZoomedOnStart.value = true
    }

    fun resetZoomFlag() {
        _hasZoomedOnStart.value = false
    }





    fun markInitialCameraMoved() {
        _initialCameraMoved.value = true
    }

    fun resetInitialCameraMoved() {
        _initialCameraMoved.value = false
    }

    fun requestCenterOnUser() {
        _centerOnUser.tryEmit(Unit)
    }






    companion object {
        var latestInstance: RecordViewModel? = null
    }


    init {
        latestInstance = this
    }


    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun addPointFromService(point: Point) {
        if (_trackingState.value == TrackingState.TRACKING) {
            addPoint(point)
        }
    }


    init {
        viewModelScope.launch {
            LocationBroadcaster.locationFlow.collect { point ->
                addPoint(point)
            }
        }
    }














    fun calculateAvgPace(timeInSeconds: Int, distanceKm: Float): String {
        if (distanceKm == 0f) return "--:--"
        val paceInSecondsPerKm = (timeInSeconds / distanceKm).toInt()
        val minutes = paceInSecondsPerKm / 60
        val seconds = paceInSecondsPerKm % 60
        return String.format("%d:%02d", minutes, seconds)
    }





    /*
        fun clearPath() {
            _pathPoints.value = emptyList()
        }

     */



}

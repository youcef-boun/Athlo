package com.youcef_bounaas.athlo.Record.presentation

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.mapbox.geojson.Point
import androidx.lifecycle.AndroidViewModel
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import com.mapbox.api.geocoding.v5.GeocodingCriteria
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.maps.CameraOptions
import com.youcef_bounaas.athlo.R
import com.youcef_bounaas.athlo.Record.data.LocationBroadcaster
import com.youcef_bounaas.athlo.Record.presentation.service.TrackingService
import com.youcef_bounaas.athlo.Record.utils.calculateTotalDistanceInKm
import com.youcef_bounaas.athlo.Record.utils.haversineDistance
import com.youcef_bounaas.athlo.Stats.data.Run
import com.youcef_bounaas.athlo.Stats.data.TrackPoint
import com.youcef_bounaas.athlo.Stats.utils.generateGpx
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.Instant
import kotlin.coroutines.resume


fun parsePaceToDouble(pace: String): Double? {
    if (pace == "-:--" || pace == "--:--" || pace.isBlank()) return null
    val parts = pace.split(":")
    return if (parts.size == 2) {
        val min = parts[0].toIntOrNull() ?: return null
        val sec = parts[1].toIntOrNull() ?: return null
        min + (sec / 60.0)
    } else {
        pace.toDoubleOrNull()
    }
}

enum class TrackingState { IDLE, TRACKING, PAUSED, FINISHED }

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class RecordViewModel(
    application: Application,
    private val savedStateHandle: androidx.lifecycle.SavedStateHandle
) : AndroidViewModel(application) {

    val accessToken = getApplication<Application>().getString(R.string.mapbox_access_token)


    private val _centerOnUser = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val centerOnUser: SharedFlow<Unit> = _centerOnUser




    private val _pathSegments = MutableStateFlow<List<List<TrackPoint>>>(emptyList())
    val pathSegments: StateFlow<List<List<TrackPoint>>> = _pathSegments.asStateFlow()

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

    private val _avgPace = MutableStateFlow("-:--")
    val avgPace = _avgPace.asStateFlow()

    private val _hasZoomedOnStart = MutableStateFlow(savedStateHandle.get<Boolean>(HAS_ZOOMED_ON_START) ?: false)
    val hasZoomedOnStart = _hasZoomedOnStart.asStateFlow()


    private val _initialCameraMoved = MutableStateFlow(savedStateHandle.get<Boolean>(INITIAL_CAMERA_MOVED) ?: false)
    val initialCameraMoved: StateFlow<Boolean> = _initialCameraMoved


    private val _lastCameraPosition = MutableStateFlow<CameraOptions?>(null)
    val lastCameraPosition: StateFlow<CameraOptions?> = _lastCameraPosition.asStateFlow()



    private val _runStartTimeMillis = MutableStateFlow(savedStateHandle.get<Long>(RUN_START_TIME))
    val runStartTimeMillis: StateFlow<Long?> = _runStartTimeMillis.asStateFlow()



    companion object {
        var latestInstance: RecordViewModel? = null
        private const val RUN_START_TIME = "run_start_time"
        private const val CAMERA_CENTER_LNG = "camera_center_lng"
        private const val CAMERA_CENTER_LAT = "camera_center_lat"
        private const val CAMERA_ZOOM = "camera_zoom"
        private const val CAMERA_BEARING = "camera_bearing"
        private const val CAMERA_PITCH = "camera_pitch"
        private const val INITIAL_CAMERA_MOVED = "initial_camera_moved"
        private const val HAS_ZOOMED_ON_START = "has_zoomed_on_start"
    }

    fun saveCameraPosition(cameraOptions: CameraOptions) {
        _lastCameraPosition.value = cameraOptions
        cameraOptions.center?.let {
            savedStateHandle[CAMERA_CENTER_LNG] = it.longitude()
            savedStateHandle[CAMERA_CENTER_LAT] = it.latitude()
        }
        cameraOptions.zoom?.let { savedStateHandle[CAMERA_ZOOM] = it }
        cameraOptions.bearing?.let { savedStateHandle[CAMERA_BEARING] = it }
        cameraOptions.pitch?.let { savedStateHandle[CAMERA_PITCH] = it }
        // Save to SharedPreferences as well
        saveCameraToPrefs(cameraOptions)
        Log.d("RecordViewModel", "Saving camera: center=${cameraOptions.center}, zoom=${cameraOptions.zoom}, bearing=${cameraOptions.bearing}, pitch=${cameraOptions.pitch}")
    }

    private fun saveCameraToPrefs(cameraOptions: CameraOptions) {

        val prefs = getApplication<Application>().getSharedPreferences("map_camera_prefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            cameraOptions.center?.let {
                putLong(CAMERA_CENTER_LNG, java.lang.Double.doubleToRawLongBits(it.longitude()))
                putLong(CAMERA_CENTER_LAT, java.lang.Double.doubleToRawLongBits(it.latitude()))
            }
            cameraOptions.zoom?.let { putLong(CAMERA_ZOOM, java.lang.Double.doubleToRawLongBits(it)) }
            cameraOptions.bearing?.let { putLong(CAMERA_BEARING, java.lang.Double.doubleToRawLongBits(it)) }
            cameraOptions.pitch?.let { putLong(CAMERA_PITCH, java.lang.Double.doubleToRawLongBits(it)) }
            apply()
        }
    }

    fun getSavedCameraOptions(): CameraOptions? {
        val lng = savedStateHandle.get<Double>(CAMERA_CENTER_LNG)
        val lat = savedStateHandle.get<Double>(CAMERA_CENTER_LAT)
        val zoom = savedStateHandle.get<Double>(CAMERA_ZOOM)
        val bearing = savedStateHandle.get<Double>(CAMERA_BEARING)
        val pitch = savedStateHandle.get<Double>(CAMERA_PITCH)
        if (lng != null && lat != null && zoom != null) {
            return CameraOptions.Builder()
                .center(Point.fromLngLat(lng, lat))
                .zoom(zoom)
                .bearing(bearing ?: 0.0)
                .pitch(pitch ?: 0.0)
                .build()
        } else {
            // Fallback to SharedPreferences
            val prefs = getApplication<Application>().getSharedPreferences("map_camera_prefs", Context.MODE_PRIVATE)
            val lngBits = prefs.getLong(CAMERA_CENTER_LNG, java.lang.Double.doubleToRawLongBits(0.0))
            val latBits = prefs.getLong(CAMERA_CENTER_LAT, java.lang.Double.doubleToRawLongBits(0.0))
            val zoomBits = prefs.getLong(CAMERA_ZOOM, java.lang.Double.doubleToRawLongBits(0.0))
            val bearingBits = prefs.getLong(CAMERA_BEARING, java.lang.Double.doubleToRawLongBits(0.0))
            val pitchBits = prefs.getLong(CAMERA_PITCH, java.lang.Double.doubleToRawLongBits(0.0))
            val lngPref = java.lang.Double.longBitsToDouble(lngBits)
            val latPref = java.lang.Double.longBitsToDouble(latBits)
            val zoomPref = java.lang.Double.longBitsToDouble(zoomBits)
            val bearingPref = java.lang.Double.longBitsToDouble(bearingBits)
            val pitchPref = java.lang.Double.longBitsToDouble(pitchBits)
            // Only restore if zoomPref is not 0 (never saved)
            return if (zoomPref != 0.0) {
                CameraOptions.Builder()
                    .center(Point.fromLngLat(lngPref, latPref))
                    .zoom(zoomPref)
                    .bearing(bearingPref)
                    .pitch(pitchPref)
                    .build()
            } else null
        }


    }







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
        _runStartTimeMillis.value = System.currentTimeMillis()
        savedStateHandle[RUN_START_TIME] = _runStartTimeMillis.value
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
            val now = System.currentTimeMillis()
            val last = pathSegments.value.lastOrNull()?.lastOrNull()?.point
            if (last == null || haversineDistance(last, point) > 5.0) {
                _pathSegments.update { segments ->
                    val copy = segments.toMutableList()
                    val trackPoint = TrackPoint(point, now)
                    if (copy.isEmpty()) copy.add(mutableListOf(trackPoint))
                    else {
                        val lastSegment = copy.removeLast().toMutableList()
                        lastSegment.add(trackPoint)
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
        savedStateHandle[HAS_ZOOMED_ON_START] = true
    }

    fun resetZoomFlag() {
        _hasZoomedOnStart.value = false
        savedStateHandle[HAS_ZOOMED_ON_START] = false
    }





    fun markInitialCameraMoved() {
        _initialCameraMoved.value = true
        savedStateHandle[INITIAL_CAMERA_MOVED] = true
    }

    fun resetInitialCameraMoved() {
        _initialCameraMoved.value = false
        savedStateHandle[INITIAL_CAMERA_MOVED] = false
    }

    fun requestCenterOnUser() {
        _centerOnUser.tryEmit(Unit)
    }







    init {
        Log.d("RecordViewModel", "RecordViewModel created! " + this.hashCode())
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

    fun getStartingPoint(): Point? {
        return pathSegments.value.firstOrNull()?.firstOrNull()?.point
    }

    fun exportCurrentRunToGpx(): String? {
        val segments = pathSegments.value
        val startMillis = runStartTimeMillis.value
        val dist = distance.value
        val duration = timeInSeconds.value
        val pace = avgPace.value

        if (segments.isEmpty() || startMillis == null) return null

        return generateGpx(
            pathSegments = segments,
            runStartTimeMillis = startMillis,
            distanceKm = dist,
            durationSec = duration,
            avgPace = pace
        )
    }





    suspend fun uploadGpxToSupabase(
        supabaseClient: SupabaseClient,
        gpxString: String,
        userId: String
    ): String? {
        val gpxBytes = gpxString.toByteArray(Charsets.UTF_8)
        val fileName = "run_${System.currentTimeMillis()}.gpx"
        val objectPath = "user_${userId}/$fileName"
        val bucket = "gpx-runs"

        // Correct call: objectPath, gpxBytes, upsert = false
        supabaseClient.storage.from(bucket).upload(objectPath, gpxBytes) {
            upsert = false
        }

        // Get public URL
        val publicUrl = supabaseClient.storage.from(bucket).publicUrl(objectPath)
        return publicUrl
    }

    suspend fun insertRunToSupabase(
        supabaseClient: SupabaseClient,
        userId: String,
        runStartTimeMillis: Long,
        distanceKm: Double,
        durationSec: Long,
        avgPace: Double,
        gpxUrl: String,
        city: String?
    ) {
        val dateString = Instant.ofEpochMilli(runStartTimeMillis).toString()


        val run = Run(
            user_id = userId,
            date = dateString,
            distance_km = distanceKm,
            duration_secs = durationSec,
            avg_pace = avgPace,
            gpx_url = gpxUrl,
            city = city
        )

        supabaseClient
            .from("runs")
            .insert(run)
    }

    suspend fun reverseGeocodeStartingPoint(point: Point, accessToken: String): String? =
        suspendCancellableCoroutine { continuation ->
            val client = MapboxGeocoding.builder()
                .accessToken(accessToken)
                .query(Point.fromLngLat(point.longitude(), point.latitude()))
                .geocodingTypes(GeocodingCriteria.TYPE_PLACE, GeocodingCriteria.TYPE_REGION, GeocodingCriteria.TYPE_COUNTRY)
                .build()
            client.enqueueCall(object : retrofit2.Callback<com.mapbox.api.geocoding.v5.models.GeocodingResponse> {
                override fun onResponse(
                    call: retrofit2.Call<com.mapbox.api.geocoding.v5.models.GeocodingResponse>,
                    response: retrofit2.Response<com.mapbox.api.geocoding.v5.models.GeocodingResponse>
                ) {
                    val features = response.body()?.features()
                    val place = features?.firstOrNull()
                    // Compose city, state, country (if available)
                    val city = place?.context()?.find { it.id()?.startsWith("place") == true }?.text()
                        ?: place?.text()
                    val state = place?.context()?.find { it.id()?.startsWith("region") == true }?.text()
                    val country = place?.context()?.find { it.id()?.startsWith("country") == true }?.text()
                    val result = listOfNotNull(city, state, country).joinToString(", ")
                    continuation.resume(if (result.isNotEmpty()) result else null)
                }

                override fun onFailure(
                    call: retrofit2.Call<com.mapbox.api.geocoding.v5.models.GeocodingResponse>,
                    t: Throwable
                ) {
                    continuation.resume(null)
                }
            })
        }



    suspend fun fetchRunsFromSupabase(supabaseClient: SupabaseClient, userId: String): List<Run> {
        return supabaseClient.from("runs")
            .select {
                filter {
                    eq("user_id", userId)
                }
                order("date", Order.DESCENDING)
            }
            .decodeList<Run>()
    }







}

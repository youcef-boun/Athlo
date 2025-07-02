package com.youcef_bounaas.athlo.Record.presentation


import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.DefaultLocationProvider
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.delay
import org.koin.androidx.compose.getViewModel
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.youcef_bounaas.athlo.Record.presentation.service.TrackingService
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.graphicsLayer
import com.mapbox.maps.extension.style.layers.properties.generated.ProjectionName
import com.mapbox.maps.extension.style.projection.generated.setProjection
import com.mapbox.maps.extension.style.projection.generated.Projection
import com.youcef_bounaas.athlo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import org.koin.androidx.compose.get
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen() {
    val viewModel: RecordViewModel = getViewModel()
    val supabaseClient: SupabaseClient = get()

    val isRunning by viewModel.isRunning.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()
    val timeInSeconds = viewModel.timeInSeconds.collectAsState()
    val distance by viewModel.distance.collectAsState()
    val avgPace by viewModel.avgPace.collectAsState()
    var showFinishConfirmation by remember { mutableStateOf(false) }

    val context = LocalContext.current

    var isPanelVisible by remember { mutableStateOf(true) }

    // Animation for sliding metrics panel
    val panelTranslationY by animateFloatAsState(
        targetValue = if (isPanelVisible) 0f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "panelSlide"
    )

    // Timer effect
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isRunning) {
                delay(1000)
                viewModel.incrementTime()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Run",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        // Status Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isRunning) MaterialTheme.colorScheme.primary
                    else if (isFinished) { Color(0x00FF5722)
                    }
                    else Color(0xFFFF2222)
                )
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isRunning) "RUNNING" else if (isFinished) {
                    ""
                } else
                    "STOPPED",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // Map Area with overlay metrics panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    if (MaterialTheme.colorScheme.background == MaterialTheme.colorScheme.surface) {
                        // Light theme - lighter map background
                        Color(0xFFE8F5E8)
                    } else {
                        // Dark theme - darker map background
                        Color(0xFF2C3E50)
                    }
                )
        ) {
            // Fullscreen Map
            MapScreenWithPermissions()

            // Map Controls - always visible on the right side
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 110.dp // Increase this value to move controls higher above the metrics panel
                    ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapControlButton(Icons.Default.Layers)
                MapControlButton(Icons.Default.Rotate90DegreesCcw, "3D")
                MapControlButton(Icons.Default.MyLocation) {
                    viewModel.requestCenterOnUser()
                }
                MapControlButton(Icons.Default.Download)
            }

            // Sliding Metrics Panel
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = size.height * panelTranslationY
                    }
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    .padding(20.dp)
            ) {
                Column {
                    // Main Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "TIME",
                            value = formatTime(timeInSeconds.value),
                            modifier = Modifier.weight(1f)
                        )
                        StatItem(
                            label = "DISTANCE (km)",
                            value = String.format("%.1f", distance),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Average Pace
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "AVG PACE (/km)",
                            value = avgPace,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Fixed Action Buttons Section (always visible at bottom)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) {
            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Control Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (isFinished == true) {
                        // Start Button
                        ElevatedButton(
                            onClick = {
                                viewModel.startRun()
                                startTrackingService(context)
                            },
                            modifier = Modifier.size(90.dp),
                            enabled = true,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                            elevation = ButtonDefaults.elevatedButtonElevation(5.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Square,
                                contentDescription = "Location",
                                tint = Color.White
                            )
                        }
                    } else {
                        // Resume/Pause Button
                        ElevatedButton(
                            onClick = {
                                viewModel.pauseOrResumeRun()
                            },
                            modifier = Modifier.size(90.dp),
                            enabled = true,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface),
                            elevation = ButtonDefaults.elevatedButtonElevation(5.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (isRunning) "PAUSE" else "RESUME",
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Finish Button
                        ElevatedButton(
                            onClick = { showFinishConfirmation = true },
                            modifier = Modifier.size(90.dp),
                            enabled = true,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                            elevation = ButtonDefaults.elevatedButtonElevation(5.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "FINISH",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                            )
                        }

                        if (showFinishConfirmation) {
                            AlertDialog(
                                onDismissRequest = { showFinishConfirmation = false },
                                title = { Text("Confirm Finish") },
                                text = { Text("Are you sure you want to finish your run?") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showFinishConfirmation = false
                                            viewModel.finishRun()
                                            stopTrackingService(context)
                                            context.stopService(Intent(context, TrackingService::class.java))

                                            val gpxString = viewModel.exportCurrentRunToGpx()
                                            val userId = supabaseClient.auth.currentUserOrNull()?.id
                                            if (gpxString != null && userId != null) {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    try {
                                                        val publicUrl = viewModel.uploadGpxToSupabase(
                                                            supabaseClient = supabaseClient,
                                                            gpxString = gpxString,
                                                            userId = userId
                                                        )
                                                        if (publicUrl != null) {
                                                            Log.d("RecordScreen", "GPX uploaded successfully: $publicUrl")
                                                            // Insert run metadata into DB
                                                            val startingPoint = viewModel.getStartingPoint()
                                                            val accessToken = context.getString(R.string.mapbox_access_token)
                                                            val locationString = if (startingPoint != null) {
                                                                viewModel.reverseGeocodeStartingPoint(startingPoint, accessToken)
                                                            } else null
                                                            try {
                                                                val avgPaceValue = parsePaceToDouble(viewModel.avgPace.value) ?: 0.0
                                                                viewModel.insertRunToSupabase(
                                                                    supabaseClient = supabaseClient,
                                                                    userId = userId,
                                                                    runStartTimeMillis = viewModel.runStartTimeMillis.value ?: System.currentTimeMillis(),
                                                                    distanceKm = viewModel.distance.value.toDouble(),
                                                                    durationSec = viewModel.timeInSeconds.value.toLong(),
                                                                    avgPace = avgPaceValue,
                                                                    gpxUrl = publicUrl,
                                                                    city = locationString

                                                                )
                                                                Log.d("RecordScreen", "Run metadata inserted into DB!")
                                                                Log.d("RecordScreen", "Resolved location: $locationString")
                                                            } catch (e: Exception) {
                                                                Log.e("RecordScreen", "Error inserting run metadata", e)
                                                            }
                                                        } else {
                                                            Log.e("RecordScreen", "GPX upload failed: publicUrl is null")
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e("RecordScreen", "Error uploading GPX", e)
                                                    }
                                                }
                                            } else {
                                                Log.e("RecordScreen", "GPX string or userId is null; not uploading")
                                            }
                                        }
                                    ) {
                                        Text("Yes")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showFinishConfirmation = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Location Button - toggles metrics panel visibility
                ElevatedButton(
                    onClick = {
                        isPanelVisible = !isPanelVisible
                    },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = if (isPanelVisible)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Location",
                        tint = if (isPanelVisible) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}



@Composable
private fun MapControlButton(
    icon: ImageVector,
    text: String? = null,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun MapScreen() {
    val viewModel: RecordViewModel = getViewModel()
    val pathSegments by viewModel.pathSegments.collectAsState()
    val trackingState by viewModel.trackingState.collectAsState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val primaryColor = Color(0xFF55F78A)

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var lineManager by remember { mutableStateOf<PolylineAnnotationManager?>(null) }

    val initialCameraMoved by viewModel.initialCameraMoved.collectAsState()
    val hasZoomedOnStart by viewModel.hasZoomedOnStart.collectAsState()

    var lastKnownLocation by remember { mutableStateOf<Point?>(null) }
    var wasCameraMovedByUser by remember { mutableStateOf(false) }

    val isDarkTheme = isSystemInDarkTheme()
    val nightStyleUri = "mapbox://styles/mapbox/navigation-night-v1"
    val mapStyle = if (isDarkTheme) nightStyleUri else Style.MAPBOX_STREETS

    LaunchedEffect(Unit) {
        mapViewRef?.gestures?.addOnMoveListener(object : OnMoveListener {
            override fun onMoveBegin(detector: MoveGestureDetector) {
                wasCameraMovedByUser = true
            }

            override fun onMove(detector: MoveGestureDetector): Boolean = false
            override fun onMoveEnd(detector: MoveGestureDetector) {}
        })
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                mapViewRef = this
                val mapboxMap = this.getMapboxMap()

                // 🔧 Set up location provider
                val provider = DefaultLocationProvider(context)
                location.setLocationProvider(provider)

                mapboxMap.loadStyleUri(mapStyle) { style ->
                    val savedCamera = viewModel.getSavedCameraOptions()
                    if (savedCamera != null) {
                        mapboxMap.setCamera(savedCamera)
                        viewModel.markInitialCameraMoved() // Prevent flyTo from overriding restored position
                    }

                    mapboxMap.setProjection(
                        Projection(
                            ProjectionName.MERCATOR
                        )
                    )

                    // 📍 Show user puck
                    location.updateSettings {
                        enabled = true
                        locationPuck = createDefault2DPuck(withBearing = true)
                        puckBearing = PuckBearing.HEADING
                        puckBearingEnabled = true
                    }

                    location.pulsingEnabled = false

                    // 🧵 Line manager
                    lineManager = annotations.createPolylineAnnotationManager()

                    // ✅ GPS tracking using onIndicatorPositionChangedListener
                    // The location provider already handles accuracy filtering internally
                    location.addOnIndicatorPositionChangedListener { point ->

                        lastKnownLocation = point

                        if (!initialCameraMoved && trackingState == TrackingState.IDLE) {
                            mapboxMap.flyTo(
                                CameraOptions.Builder()
                                    .center(point)
                                    .zoom(10.0)
                                    .build(),
                                MapAnimationOptions.mapAnimationOptions {
                                    duration(1500L)
                                }
                            )
                            viewModel.markInitialCameraMoved()
                        }

                        if (trackingState == TrackingState.TRACKING && !hasZoomedOnStart) {
                            mapboxMap.flyTo(
                                CameraOptions.Builder()
                                    .center(point)
                                    .zoom(16.0)
                                    .build(),
                                MapAnimationOptions.mapAnimationOptions {
                                    duration(1500L)
                                }
                            )
                            viewModel.markZoomed()
                        }


                    }
                }
            }
        },
        update = {
            mapViewRef = it
        }
    )








    LaunchedEffect(mapViewRef) {
        mapViewRef?.getMapboxMap()?.addOnCameraChangeListener {
            val cameraState = mapViewRef?.getMapboxMap()?.cameraState
            if (cameraState != null) {
                viewModel.saveCameraPosition(
                    CameraOptions.Builder()
                        .center(cameraState.center)
                        .zoom(cameraState.zoom)
                        .bearing(cameraState.bearing)
                        .pitch(cameraState.pitch)
                        .build()
                )
            }
        }
    }

    // ✅ Re-draw path on pathSegments change
    LaunchedEffect(pathSegments) {
        val manager = lineManager ?: return@LaunchedEffect

        // 🔁 Add a short delay to prevent flicker
        delay(100L)
        manager.deleteAll()

        pathSegments.forEach { segment ->
            if (segment.size >= 2) {
                manager.create(
                    PolylineAnnotationOptions()
                        .withPoints(segment.map { it.point })
                        .withLineColor(primaryColor.toArgb().toUInt().toString())
                        .withLineWidth(4.0)
                )
            }
        }
    }

    // ♻️ Lifecycle binding
    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapViewRef?.onStart()
                Lifecycle.Event.ON_STOP -> mapViewRef?.onStop()
                Lifecycle.Event.ON_DESTROY -> mapViewRef?.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(trackingState) {
        if (trackingState == TrackingState.FINISHED) {
            val allPoints = pathSegments.flatten().map { it.point }
            if (allPoints.size >= 2) {
                val bounds = calculateBounds(allPoints)
                val mapboxMap = mapViewRef?.getMapboxMap() ?: return@LaunchedEffect

                bounds?.let {
                    val cameraOptions = mapboxMap.cameraForCoordinateBounds(
                        it,
                        EdgeInsets(100.0, 100.0, 100.0, 100.0),
                        0.0,
                        0.0
                    )
                    mapboxMap.flyTo(
                        cameraOptions,
                        MapAnimationOptions.mapAnimationOptions {
                            duration(2000L)
                        }
                    )
                }
            }
        }
    }

    //center button
    LaunchedEffect(Unit) {
        viewModel.centerOnUser.collect {
            lastKnownLocation?.let { point ->
                mapViewRef?.getMapboxMap()?.flyTo(
                    CameraOptions.Builder()
                        .center(point)
                        .zoom(16.0)
                        .build(),
                    MapAnimationOptions.mapAnimationOptions {
                        duration(1500L)
                    }
                )
            }
        }
    }
}

@Composable
fun RequestLocationPermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit = {}
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun MapScreenWithPermissions() {
    var permissionGranted by rememberSaveable { mutableStateOf(false) }

    if (!permissionGranted) {
        RequestLocationPermission(
            onPermissionGranted = { permissionGranted = true },
            onPermissionDenied = {
                // Optionally show a UI saying permission is needed
            }
        )
    } else {
        MapScreen()
    }
}

fun calculateBounds(points: List<Point>): CoordinateBounds? {
    if (points.isEmpty()) return null

    var minLat = points.first().latitude()
    var maxLat = points.first().latitude()
    var minLng = points.first().longitude()
    var maxLng = points.first().longitude()

    for (point in points) {
        val lat = point.latitude()
        val lng = point.longitude()
        if (lat < minLat) minLat = lat
        if (lat > maxLat) maxLat = lat
        if (lng < minLng) minLng = lng
        if (lng > maxLng) maxLng = lng
    }

    val southwest = Point.fromLngLat(minLng, minLat)
    val northeast = Point.fromLngLat(maxLng, maxLat)
    return CoordinateBounds(southwest, northeast)
}

private fun startTrackingService(context: Context) {
    val intent = Intent(context, TrackingService::class.java)
    context.startService(intent)
}

private fun stopTrackingService(context: Context) {
    val intent = Intent(context, TrackingService::class.java)
    context.stopService(intent)
}
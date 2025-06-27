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
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.LocationConsumer
import com.mapbox.common.location.*
import com.youcef_bounaas.athlo.Record.presentation.service.TrackingService


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen() {
    val viewModel: RecordViewModel = getViewModel()


    val isRunning by viewModel.isRunning.collectAsState() // bool
    val isFinished by viewModel.isFinished.collectAsState() // bool
    val timeInSeconds = viewModel.timeInSeconds.collectAsState() // 0

    val distance by viewModel.distance.collectAsState() // 0F
    val avgPace by viewModel.avgPace.collectAsState() // 5:30


    val context = LocalContext.current



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
                    else Color(0xFFFF5722)
                )
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isRunning) "RUNNING" else "STOPPED",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // Map Area
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
            // Mock map content
            MapScreenWithPermissions()

            // Map Controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapControlButton(Icons.Default.Layers)
                MapControlButton(Icons.Default.Rotate90DegreesCcw, "3D")
                MapControlButton(Icons.Default.MyLocation){
                    viewModel.requestCenterOnUser()
                }
                MapControlButton(Icons.Default.Download)
            }
        }

        // Stats Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) {
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

            Spacer(modifier = Modifier.height(24.dp))

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
                    if(isFinished == true ) {


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
                        ){
                            Icon(
                                imageVector = Icons.Filled.Square,
                                contentDescription = "Location",
                                tint =  Color.White
                            )
                        }





                    } else
                    {

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
                            onClick = {
                              viewModel.finishRun()
                                stopTrackingService(context)
                                context.stopService(Intent(context, TrackingService::class.java))


                                // Handle finish action
                            },
                            modifier = Modifier.size(90.dp),
                            enabled = true,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                            elevation = ButtonDefaults.elevatedButtonElevation(5.dp),
                            contentPadding = PaddingValues(0.dp)
                        ){
                            Text(
                                text = "FINISH",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,

                                )
                        }
                    }
                }

Spacer(Modifier.width(12.dp))

                // Location Button
                ElevatedButton(
                    onClick = { },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary
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
    val primaryColor = MaterialTheme.colorScheme.primary

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var lineManager by remember { mutableStateOf<PolylineAnnotationManager?>(null) }

    val initialCameraMoved by viewModel.initialCameraMoved.collectAsState()
    val hasZoomedOnStart by viewModel.hasZoomedOnStart.collectAsState()

    var lastKnownLocation by remember { mutableStateOf<Point?>(null) }




    var wasCameraMovedByUser by remember { mutableStateOf(false) }

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

                mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) { style ->
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


                        //foreground tracking
                        /*

                        if (trackingState == TrackingState.TRACKING) {
                            viewModel.addPoint(point)
                        }

                         */


                    }
                }
            }
        },
        update = {
            mapViewRef = it
        }
    )









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
                        .withPoints(segment)
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
            val allPoints = pathSegments.flatten()
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






















package com.youcef_bounaas.athlo.Maps.presentation

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.properties.generated.ProjectionName
import com.mapbox.maps.extension.style.projection.generated.Projection
import com.mapbox.maps.extension.style.projection.generated.setProjection
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.DefaultLocationProvider
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.youcef_bounaas.athlo.Record.presentation.RecordViewModel
import com.youcef_bounaas.athlo.Record.presentation.TrackingState
import com.youcef_bounaas.athlo.Maps.data.MapsViewModel
import org.koin.androidx.compose.getViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.geojson.LineString
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import android.widget.Toast
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener


@SuppressLint("ImplicitSamInstance")
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun MapsScreen() {
    // DEBUG: Show which screen is rendered
    androidx.compose.material3.Text(
        text = "MAPS SCREEN",
        color = androidx.compose.ui.graphics.Color.Red,
        modifier = androidx.compose.ui.Modifier.padding(16.dp)
    )
    val viewModel: RecordViewModel = getViewModel()
    val mapsViewModel: MapsViewModel = getViewModel()
    val trackingState by viewModel.trackingState.collectAsState()
    val context = LocalContext.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var lineManager by remember { mutableStateOf<PolylineAnnotationManager?>(null) }
    val initialCameraMoved by viewModel.initialCameraMoved.collectAsState()
    val hasZoomedOnStart by viewModel.hasZoomedOnStart.collectAsState()
    var lastKnownLocation by remember { mutableStateOf<Point?>(null) }
    var wasCameraMovedByUser by remember { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()
    val nightStyleUri = "mapbox://styles/mapbox/navigation-night-v1"
    val mapStyle = if (isDarkTheme) nightStyleUri else Style.MAPBOX_STREETS
    val accessToken = context.getString(com.youcef_bounaas.athlo.R.string.mapbox_access_token)

    // ViewModel state
    val searchQuery by mapsViewModel.searchQuery.collectAsState()
    val isSearching by mapsViewModel.isSearching.collectAsState()
    val isCreatingRoute by mapsViewModel.isCreatingRoute.collectAsState()
    val routeWaypoints by mapsViewModel.routeWaypoints.collectAsState()
    val snappedRoute by mapsViewModel.snappedRoute.collectAsState()
    val cameraPosition by mapsViewModel.cameraPosition.collectAsState()

    val focusManager = LocalFocusManager.current

    fun performSearch() {
        if (searchQuery.isNotBlank() && !isSearching) {
            mapsViewModel.setSearching(true)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val geocoding = MapboxGeocoding.builder()
                        .accessToken(accessToken)
                        .query(searchQuery)
                        .build()
                    val response = geocoding.executeCall()
                    val results = response.body()?.features()
                    val firstResult = results?.firstOrNull()
                    if (firstResult != null) {
                        val map = mapViewRef?.getMapboxMap()
                        val bbox = firstResult.bbox()
                        if (bbox != null && map != null) {
                            CoroutineScope(Dispatchers.Main).launch {
                                val bounds = com.mapbox.maps.CoordinateBounds(
                                    com.mapbox.geojson.Point.fromLngLat(bbox.west(), bbox.south()),
                                    com.mapbox.geojson.Point.fromLngLat(bbox.east(), bbox.north())
                                )
                                map.flyTo(
                                    map.cameraForCoordinateBounds(
                                        bounds,
                                        com.mapbox.maps.EdgeInsets(100.0, 100.0, 100.0, 100.0),
                                        0.0,
                                        0.0
                                    ),
                                    MapAnimationOptions.mapAnimationOptions {
                                        duration(1500L)
                                    }
                                )
                            }
                        } else if (firstResult.center() != null && map != null) {
                            CoroutineScope(Dispatchers.Main).launch {
                                map.flyTo(
                                    CameraOptions.Builder()
                                        .center(firstResult.center())
                                        .zoom(8.0)
                                        .build(),
                                    MapAnimationOptions.mapAnimationOptions {
                                        duration(1500L)
                                    }
                                )
                            }
                        }
                    }
                } catch (_: Exception) { /* ignore */ }
                mapsViewModel.setSearching(false)
            }
        }
    }

    fun startRouteCreation() {
        mapsViewModel.startRouteCreation()
    }


    fun finishRouteCreation() {
        if (routeWaypoints.size < 2) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = MapboxDirections.builder()
                    .accessToken(accessToken)
                    .routeOptions(
                        RouteOptions.builder()
                            .coordinatesList(routeWaypoints)
                            .profile(DirectionsCriteria.PROFILE_WALKING)
                            .overview(DirectionsCriteria.OVERVIEW_FULL)
                            .build()
                    )
                    .build()
                val response = client.executeCall()
                val route = response.body()?.routes()?.firstOrNull()
                val geometry = route?.geometry()
                if (geometry != null) {
                    val line = LineString.fromPolyline(geometry, 6)
                    mapsViewModel.setSnappedRoute(line.coordinates())
                }
            } catch (_: Exception) { /* ignore */ }
            mapsViewModel.finishRouteCreation()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- MapView ---
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    mapViewRef = this
                    val mapboxMap = this.getMapboxMap()
                    val provider = DefaultLocationProvider(context)
                    location.setLocationProvider(provider)
                    mapboxMap.loadStyleUri(mapStyle) { style ->
                        // Restore saved camera position if available
                        val savedCamera = cameraPosition ?: viewModel.getSavedCameraOptions()
                        if (savedCamera != null) {
                            mapboxMap.setCamera(savedCamera)
                            viewModel.markInitialCameraMoved()
                        }
                        mapboxMap.setProjection(
                            Projection(
                                ProjectionName.MERCATOR
                            )
                        )
                        location.updateSettings {
                            enabled = true
                            locationPuck = createDefault2DPuck(withBearing = true)
                            puckBearing = PuckBearing.HEADING
                            puckBearingEnabled = true
                        }
                        location.pulsingEnabled = false
                        lineManager = annotations.createPolylineAnnotationManager()
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
            update = { mapView ->
                mapViewRef = mapView
                // Add tap listener when in route creation mode
                if (isCreatingRoute) {
                    mapView.getMapboxMap().addOnMapClickListener { point ->
                        mapsViewModel.addRouteWaypoint(point)
                        Toast.makeText(context, "Waypoint added: ${point.longitude()}, ${point.latitude()}", Toast.LENGTH_SHORT).show()
                        true
                    }
                } else {
                    mapView.getMapboxMap().removeOnMapClickListener { true }
                }

                // Save camera position on camera changes
                mapView.getMapboxMap().addOnCameraChangeListener {
                    val cameraState = mapView.getMapboxMap().cameraState
                    if (cameraState != null) {
                        mapsViewModel.saveCameraPosition(
                            CameraOptions.Builder()
                                .center(cameraState.center)
                                .zoom(cameraState.zoom)
                                .bearing(cameraState.bearing)
                                .pitch(cameraState.pitch)
                                .build()
                        )
                    }
                }

                // Draw snapped route if available
                val map = mapView.getMapboxMap()
                val annotationManager = lineManager
                annotationManager?.deleteAll()
                if (snappedRoute.isNotEmpty()) {
                    annotationManager?.create(
                        PolylineAnnotationOptions()
                            .withPoints(snappedRoute)
                            .withLineColor("#FF6200EE")
                            .withLineWidth(5.0)
                    )
                } else if (isCreatingRoute && routeWaypoints.size > 1) {
                    annotationManager?.create(
                        PolylineAnnotationOptions()
                            .withPoints(routeWaypoints)
                            .withLineColor("#FFBB33")
                            .withLineWidth(3.0)
                    )
                }
            }
        )
        // --- Floating Search Field ---
        val searchBgColor = if (isDarkTheme) Color.Black else Color.White
        val searchContentColor = if (isDarkTheme) Color.White else Color.Black
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                color = searchBgColor,
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 8.dp,
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { mapsViewModel.setSearchQuery(it) },
                    placeholder = { Text("Search locations", color = searchContentColor) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = searchBgColor,
                        unfocusedBorderColor = searchContentColor.copy(alpha = 0.3f),
                        focusedBorderColor = searchContentColor,
                        cursorColor = searchContentColor,
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = searchContentColor,
                            modifier = Modifier.clickable(enabled = !isSearching && searchQuery.isNotBlank()) {
                                performSearch()
                                focusManager.clearFocus()
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            performSearch()
                            focusManager.clearFocus()
                        }
                    )
                )
            }
        }
        // --- Floating Create Route Button ---
        FloatingActionButton(
            onClick = {
                if (!isCreatingRoute) {
                    mapsViewModel.startRouteCreation()
                } else if (routeWaypoints.size >= 2) {
                    finishRouteCreation()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 32.dp, end = 24.dp),
            containerColor = if (isDarkTheme) Color.Black else Color.White,
            contentColor = if (isDarkTheme) Color.White else Color.Black,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
                Icon(Icons.Default.Edit, contentDescription = if (!isCreatingRoute) "Create Route" else "Finish Route")
                Text(if (!isCreatingRoute) "Create Route" else if (routeWaypoints.size >= 2) "Finish" else "Add Points", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
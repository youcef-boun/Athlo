package com.youcef_bounaas.athlo.Maps.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mapbox.android.gestures.MoveGestureDetector
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
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.DefaultLocationProvider
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.youcef_bounaas.athlo.Record.presentation.RecordViewModel
import com.youcef_bounaas.athlo.Record.presentation.TrackingState
import org.koin.androidx.compose.getViewModel

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun MapsScreen(

) {
    val viewModel: RecordViewModel = getViewModel()
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
    Column(
        modifier = Modifier
            .fillMaxSize()

    ) {







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







    }
}
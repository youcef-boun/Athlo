package com.youcef_bounaas.athlo.Stats.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.youcef_bounaas.athlo.UserInfo.data.Profile
import com.youcef_bounaas.athlo.ai.presentation.InsightState
import com.youcef_bounaas.athlo.ai.presentation.RunInsightViewModel
import androidx.compose.material.icons.filled.Refresh

import org.koin.androidx.compose.koinViewModel
import android.content.Context
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.layers.properties.generated.ProjectionName
import com.mapbox.maps.extension.style.projection.generated.Projection
import com.mapbox.maps.extension.style.projection.generated.setProjection
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.*
import com.youcef_bounaas.athlo.Record.presentation.calculateBounds
import com.youcef_bounaas.athlo.Stats.data.Run

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.get
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory



data class RunDetails(
    val userName: String,
    val userImage: String,
    val date: String,
    val location: String,
    val title: String,
    val distance: String,
    val movingTime: String,
    val avgPace: String
)

data class LatLng(val latitude: Double, val longitude: Double)

suspend fun fetchAndParseGpx(gpxUrl: String): List<LatLng> = withContext(Dispatchers.IO) {
    val points = mutableListOf<LatLng>()
    val urlStream = URL(gpxUrl).openStream()
    val factory = XmlPullParserFactory.newInstance()
    val parser = factory.newPullParser()
    parser.setInput(urlStream, null)
    var eventType = parser.eventType
    while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG && parser.name == "trkpt") {
            val lat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull()
            val lon = parser.getAttributeValue(null, "lon")?.toDoubleOrNull()
            if (lat != null && lon != null) {
                points.add(LatLng(lat, lon))
            }
        }
        eventType = parser.next()
    }
    urlStream.close()
    points
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsDetailsScreen(
    runId: String?,
    onBackClick: () -> Unit = {},
    supabase: SupabaseClient = get(),
    viewModel: RunInsightViewModel = koinViewModel()
) {
    Log.d("StatsDetailScreen", "[DEBUG] Initializing with runId: $runId")
    if (runId == null) {
        Log.e("StatsDetailScreen", "[ERROR] runId is null")
    }
    Log.d("StatsDetailsScreen", "Received runId: $runId")
    val isDarkMode = isSystemInDarkTheme()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val density = LocalDensity.current

    val bottomSheetDefaultOffsetPx = with(density) { (screenHeight * 0.55f).toPx() }
    val bottomSheetMaxOffsetPx = with(density) { (screenHeight * 0.12f).toPx() }
    var bottomSheetOffset by remember { mutableFloatStateOf(bottomSheetDefaultOffsetPx) }

    val sheetColor = if (isDarkMode) Color.Black else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color(0xFFBBBBBB) else Color(0xFF555555)

    val colorScheme = if (isDarkMode) {
        darkColorScheme(
            background = Color(0xFF181818),
            surface = sheetColor,
            onBackground = textColor,
            onSurface = textColor,
            primary = textColor,
            secondary = secondaryTextColor
        )
    } else {
        lightColorScheme(
            background = Color(0xFFF8F8F8),
            surface = sheetColor,
            onBackground = textColor,
            onSurface = textColor,
            primary = textColor,
            secondary = secondaryTextColor
        )
    }

    var run by remember { mutableStateOf<Run?>(null) }
    var profile by remember { mutableStateOf<Profile?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showInsight by remember { mutableStateOf(false) }

    // Get the current insight state
    val insightState by viewModel.insightState.collectAsState()

    // For map and GPX
    var gpxPoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(runId) {
        Log.d("StatsDetailScreen", "[DEBUG] LaunchedEffect started for runId: $runId")
        loading = true
        error = null
        try {
            if (runId != null) {
                Log.d("StatsDetailScreen", "[DEBUG] Fetching run with id: $runId")
                val fetchedRun = try {
                    supabase.from("runs")
                        .select {
                            filter { eq("id", runId) }
                        }
                        .decodeSingle<Run>()
                } catch (e: Exception) {
                    Log.e("StatsDetailScreen", "[ERROR] Failed to fetch run: ${e.message}", e)
                    throw e
                }
                Log.d("StatsDetailScreen", "[DEBUG] Successfully fetched run: ${fetchedRun.id}")
                run = fetchedRun

                Log.d("StatsDetailsScreen", "Fetched run: $fetchedRun")

                profile = supabase.from("profiles")
                    .select {
                        filter { eq("id", fetchedRun.user_id) }
                    }
                    .decodeSingle<Profile>()

                Log.d("StatsDetailsScreen", "Fetched profile for userId: ${fetchedRun.user_id}")

                Log.d("StatsDetailScreen", "[DEBUG] Run object set: ${fetchedRun.id}")

                // Parse GPX if available
                fetchedRun.gpx_url?.let { gpxUrl ->
                    Log.d("StatsDetailScreen", "[DEBUG] Parsing GPX from URL: $gpxUrl")
                    try {
                        fetchAndParseGpx(gpxUrl)?.let { points ->
                            gpxPoints = points
                            Log.d(
                                "StatsDetailScreen",
                                "[DEBUG] Successfully parsed ${points.size} points from GPX"
                            )
                        } ?: run {
                            Log.w(
                                "StatsDetailScreen",
                                "[WARN] GPX parsing returned null or no points"
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("StatsDetailScreen", "[ERROR] GPX parsing failed: ${e.message}", e)
                    }
                } ?: Log.d("StatsDetailScreen", "[DEBUG] No GPX file available for this run")
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    if (run == null) {
        Log.e("StatsDetailsScreen", "Run is null. Cannot display details.")
    }
    if (profile == null) {
        Log.e("StatsDetailsScreen", "Profile is null. Cannot display details.")
    }

    // Formatting helpers
    fun formatDuration(totalSecs: Long): String {
        val h = totalSecs / 3600
        val m = (totalSecs % 3600) / 60
        val s = totalSecs % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
    }

    fun formatDate(dateStr: String): String {
        // ISO input: "2025-06-30T16:11:00Z"
        return try {
            val parser =
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            val date = parser.parse(dateStr.take(19))
            val fmt =
                java.text.SimpleDateFormat("MMM dd, yyyy h:mm a", java.util.Locale.getDefault())
            if (date != null) fmt.format(date) else dateStr
        } catch (_: Exception) {
            dateStr
        }
    }

    fun formatAvgPace(durationSecs: Long, distanceKm: Double): String {
        if (distanceKm == 0.0) return "--:-- /km"
        val paceSeconds = (durationSecs / distanceKm).toInt()
        val mins = paceSeconds / 60
        val secs = paceSeconds % 60
        return "%d:%02d /km".format(mins, secs)
    }

    fun runTypeFromDate(dateStr: String): String {
        // Parse hour from date string; assumes ISO 8601 input
        return try {
            val parser =
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            val date = parser.parse(dateStr.take(19))
            val cal = java.util.Calendar.getInstance()
            if (date != null) {
                cal.time = date
                val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                val min = cal.get(java.util.Calendar.MINUTE)
                val totalMins = hour * 60 + min
                when {
                    totalMins in (4 * 60) until (11 * 60) -> "Morning Run"
                    totalMins in (11 * 60) until (14 * 60) -> "Lunch Run"
                    totalMins in (14 * 60) until (18 * 60) -> "Afternoon Run"
                    totalMins in (18 * 60) until (21 * 60) -> "Evening Run"
                    else -> "Night Run"
                }
            } else "Run"
        } catch (_: Exception) {
            "Run"
        }
    }

    MaterialTheme(colorScheme = colorScheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Map area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(
                        if (isDarkMode) Color(0xFF181B22) else Color(0xFFF5F5F5)
                    ),
                contentAlignment = Alignment.TopStart
            ) {


                // Mapbox Map View
                var mapView by remember { mutableStateOf<MapView?>(null) }
                var lineManager by remember { mutableStateOf<PolylineAnnotationManager?>(null) }
                val lifecycleOwner = LocalLifecycleOwner.current
                val context = LocalContext.current

                // Map style based on theme
                val isDarkTheme = isSystemInDarkTheme()
                val mapStyle = if (isDarkTheme) Style.DARK else Style.MAPBOX_STREETS

                // Initialize MapView
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            mapView = this
                            // Set Mercator projection
                            getMapboxMap().setProjection(
                                Projection(
                                    ProjectionName.MERCATOR
                                )
                            )

                            getMapboxMap().loadStyleUri(mapStyle) { style ->
                                // Initialize line manager for drawing the route
                                lineManager = annotations.createPolylineAnnotationManager().apply {
                                    // Clear any existing annotations
                                    deleteAll()
                                }
                            }
                        }
                    },
                    update = { view ->
                        // Update map when GPX points change
                        if (gpxPoints.isNotEmpty()) {
                            view.getMapboxMap().getStyle { style ->
                                // Convert GPX points to Mapbox Points
                                val points = gpxPoints.map { point ->
                                    Point.fromLngLat(point.longitude, point.latitude)
                                }

                                // Update the line annotation
                                lineManager?.let { manager ->
                                    manager.deleteAll()

                                    if (points.size >= 2) {
                                        manager.create(
                                            com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions()
                                                .withPoints(points)
                                                .withLineColor("#3bb2d0")
                                                .withLineWidth(4.0)
                                        )

                                        // Calculate bounds and animate camera to show the entire route
                                        val bounds = calculateBounds(points)
                                        bounds?.let { bbox ->
                                            // Calculate camera position that fits the bounds with padding
                                            val cameraPosition =
                                                view.getMapboxMap().cameraForCoordinates(
                                                    points,
                                                    EdgeInsets(
                                                        100.0,
                                                        40.0,
                                                        300.0,
                                                        40.0
                                                    ), // top, left, bottom, right
                                                    0.0, // bearing
                                                    0.0  // pitch
                                                )

                                            // Set camera position directly without animation
                                            view.getMapboxMap().setCamera(cameraPosition)
                                        }
                                    }
                                }
                            }
                        }
                    }
                )

                // Handle lifecycle events
                DisposableEffect(Unit) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_START -> mapView?.onStart()
                            Lifecycle.Event.ON_STOP -> mapView?.onStop()
                            Lifecycle.Event.ON_DESTROY -> mapView?.onDestroy()
                            else -> Unit
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }


                // Top bar with back button OVER the map
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = if (isDarkMode) Color(0xB3000000) else Color(0xB3FFFFFF),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                }
            }

            // Show loading or error
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else if (error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text("Error: $error", color = MaterialTheme.colorScheme.error) }
            } else if (run != null && profile != null) {
                // Generate run details
                val runDetails = RunDetails(
                    userName = "${profile!!.first_name} ${profile!!.last_name}",
                    userImage = profile!!.avatar_url,
                    date = formatDate(run!!.date),
                    location = run!!.city ?: "Unknown Location",
                    title = runTypeFromDate(run!!.date),
                    distance = "%.2f km".format(run!!.distance_km),
                    movingTime = formatDuration(run!!.duration_secs),
                    avgPace = formatAvgPace(run!!.duration_secs, run!!.distance_km)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset {
                            IntOffset(0, bottomSheetOffset.roundToInt())
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                val newOffset = bottomSheetOffset + change.position.y
                                bottomSheetOffset = newOffset.coerceIn(
                                    bottomSheetMaxOffsetPx,
                                    bottomSheetDefaultOffsetPx
                                )
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = sheetColor
                    ),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenHeight)
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        // Drag handle
                        Box(
                            modifier = Modifier.width(40.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = runDetails.userImage,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = runDetails.userName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor
                                )

                                Text(
                                    text = "${runDetails.date} • ${runDetails.location}",
                                    fontSize = 14.sp,
                                    color = secondaryTextColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Run Stats Section - Grouped together
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItemCentered(
                                label = "Distance",
                                value = runDetails.distance,
                                modifier = Modifier.weight(1f),
                                textColor = textColor,
                                labelColor = secondaryTextColor
                            )

                            StatItemCentered(
                                label = "Avg Pace",
                                value = runDetails.avgPace,
                                modifier = Modifier.weight(1f),
                                textColor = textColor,
                                labelColor = secondaryTextColor
                            )

                            StatItemCentered(
                                label = "Moving Time",
                                value = runDetails.movingTime,
                                modifier = Modifier.weight(1f),
                                textColor = textColor,
                                labelColor = secondaryTextColor
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // AI Insight Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AI Insights",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textColor
                                )

                                IconButton(
                                    onClick = {
                                        if (run != null && profile != null) {
                                            viewModel.generateInsight(run!!, profile!!)
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh insight",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            when (val state = insightState) {
                                is InsightState.Loading -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }

                                is InsightState.Success -> {
                                    Text(
                                        text = state.insight,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textColor
                                    )
                                }

                                is InsightState.Error -> {
                                    Column {
                                        Text(
                                            text = "Couldn't generate insights: ${state.message}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                if (run != null && profile != null) {
                                                    viewModel.generateInsight(run!!, profile!!)
                                                }
                                            }
                                        ) {
                                            Text("Try Again")
                                        }
                                    }
                                }

                                is InsightState.Idle -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Click below to generate AI insights about your run!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = secondaryTextColor,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        Button(
                                            onClick = {
                                                if (run != null && profile != null) {
                                                    viewModel.generateInsight(run!!, profile!!)
                                                }
                                            }
                                        ) {
                                            Text("Generate AI Insight")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


}

@Composable
fun StatItemCentered(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    labelColor: Color = Color.Unspecified
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = labelColor,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
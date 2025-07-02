@file:OptIn(ExperimentalMaterial3Api::class)

package com.youcef_bounaas.athlo.Record.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youcef_bounaas.athlo.Stats.data.Run
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt



@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun StatsScreen() {
    val supabaseClient: SupabaseClient = get()
    val viewModel: RecordViewModel = getViewModel()
    val userId = supabaseClient.auth.currentUserOrNull()?.id

    var runs by remember { mutableStateOf<List<Run>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // For GPX previews, cache the parsed list of points for each run
    val gpxCache = remember { mutableStateMapOf<String, List<LatLng>>() }

    LaunchedEffect(userId) {
        loading = true
        if (userId != null) {
            runs = try {
                viewModel.fetchRunsFromSupabase(supabaseClient, userId)
            } catch (e: Exception) {
                emptyList()
            }
        }
        loading = false
    }

    val isDarkMode = isSystemInDarkTheme()

    val colorScheme = if (isDarkMode) {
        darkColorScheme(
            background = Color.Black,
            surface = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White,
            primary = Color.White,
            secondary = Color.Gray
        )
    } else {
        lightColorScheme(
            background = Color.White,
            surface = Color.White,
            onBackground = Color.Black,
            onSurface = Color.Black,
            primary = Color.Black,
            secondary = Color.Gray
        )
    }

    MaterialTheme(colorScheme = colorScheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            TopAppBar(
                title = {
                    Text(
                        text = "My Runs",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                actions = {
                    IconButton(onClick = { /* Handle close if needed */ }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )

            if (loading) {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        Modifier.align(Alignment.Center)
                    )
                }
            } else if (runs.isEmpty()) {
                Box(Modifier.fillMaxSize()) {
                    Text(
                        "No runs yet!",
                        Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(runs) { run ->
                        PopularSpotItemFromRun(run, gpxCache)
                    }
                }
            }
        }
    }
}

// Lightweight LatLng type for GPX preview
data class LatLng(val latitude: Double, val longitude: Double)

// A minimalist GPX parser: extracts all <trkpt lat="..." lon="...">...</trkpt>
fun parseGpxLatLngs(input: InputStream): List<LatLng> {
    val points = mutableListOf<LatLng>()
    val regex = Regex("""<trkpt\s+lat="([0-9\.\-]+)"\s+lon="([0-9\.\-]+)"""")
    input.bufferedReader().useLines { lines ->
        lines.forEach { line ->
            regex.find(line)?.let { match ->
                val lat = match.groupValues[1].toDoubleOrNull()
                val lon = match.groupValues[2].toDoubleOrNull()
                if (lat != null && lon != null) {
                    points.add(LatLng(lat, lon))
                }
            }
        }
    }
    return points
}

@Composable
fun PopularSpotItemFromRun(run: Run, gpxCache: MutableMap<String, List<LatLng>>) {
    var trackPoints by remember(run.gpx_url) { mutableStateOf<List<LatLng>?>(null) }
    var loading by remember(run.gpx_url) { mutableStateOf(false) }

    // GPX download & parse coroutine
    LaunchedEffect(run.gpx_url) {
        if (!gpxCache.containsKey(run.gpx_url)) {
            loading = true
            val parsed = try {
                withContext(Dispatchers.IO) {
                    val url = URL(run.gpx_url)
                    url.openStream().use { input -> parseGpxLatLngs(input) }
                }
            } catch (e: Exception) {
                emptyList()
            }
            gpxCache[run.gpx_url] = parsed
            trackPoints = parsed
            loading = false
        } else {
            trackPoints = gpxCache[run.gpx_url]
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Route preview (mini-map)
            ElevationProfilePreviewFromGpx(
                gpxPoints = trackPoints,
                loading = loading,
                modifier = Modifier.size(60.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = run.city ?: "Unknown Location",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = run.distance_km.roundTo(2).toString() + " KM",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Normal
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = formatDuration(run.duration_secs),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Text(
                        text = formatDate(run.date),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Normal
                    )
                }
            }



        }
    }
}


@Composable
fun ElevationProfilePreviewFromGpx(
    gpxPoints: List<LatLng>?,
    loading: Boolean,
    modifier: Modifier = Modifier
) {
    val isDarkMode = isSystemInDarkTheme()
    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFF5F5F5)
    val profileColor = Color(0xFF55F78A)



    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(30.dp), strokeWidth = 2.dp)
        } else if (gpxPoints != null && gpxPoints.size > 1) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val padding = 8.dp.toPx()

                val lats = gpxPoints.map { it.latitude }
                val lngs = gpxPoints.map { it.longitude }
                val minLat = lats.minOrNull() ?: return@Canvas
                val maxLat = lats.maxOrNull() ?: return@Canvas
                val minLng = lngs.minOrNull() ?: return@Canvas
                val maxLng = lngs.maxOrNull() ?: return@Canvas

                val latRange = maxLat - minLat
                val lngRange = maxLng - minLng

                fun normX(lng: Double): Float =
                    if (lngRange == 0.0) width / 2f else ((lng - minLng) / lngRange * (width - 2 * padding) + padding).toFloat()
                fun normY(lat: Double): Float =
                    if (latRange == 0.0) height / 2f else (height - ((lat - minLat) / latRange * (height - 2 * padding)) - padding).toFloat()

                val path = Path().apply {
                    moveTo(normX(gpxPoints[0].longitude), normY(gpxPoints[0].latitude))
                    for (pt in gpxPoints.drop(1)) {
                        lineTo(normX(pt.longitude), normY(pt.latitude))
                    }
                }

                drawPath(
                    path = path,
                    color = profileColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        } else {
            Text(
                "No\nRoute",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 10.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// Utility: round double to N decimals as string
fun Double.roundTo(n: Int) = "%.${n}f".format(this)

fun formatDuration(totalSecs: Long): String {
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return "%d:%02d".format(mins, secs)
}

fun formatDate(dateString: String): String {
    // ISO date string → "Jun 25, 2025 4:55 PM"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(dateString)
        val fmt = SimpleDateFormat("MMM dd, yyyy h:mm\u00A0a", Locale.getDefault())
        if (date != null) fmt.format(date) else dateString
    } catch (e: Exception) {
        dateString
    }
}
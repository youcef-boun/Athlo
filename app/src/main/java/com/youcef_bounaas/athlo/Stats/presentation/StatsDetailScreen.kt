package com.youcef_bounaas.athlo.Stats.presentation

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.roundToInt

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

@Composable
fun StatsDetailsScreen(
    runDetails: RunDetails = sampleRunDetails,
    onBackClick: () -> Unit = {}
) {
    val isDarkMode = isSystemInDarkTheme()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val density = LocalDensity.current

    // 100% map visible when bottom sheet is at default position
    val bottomSheetDefaultOffsetPx = with(density) { (screenHeight * 0.55f).toPx() }
    val bottomSheetMaxOffsetPx = with(density) { (screenHeight * 0.12f).toPx() }

    var bottomSheetOffset by remember { mutableFloatStateOf(bottomSheetDefaultOffsetPx) }

    // Colors: text is white in dark mode, black in light mode
    // Draggable sheet: white in light, black in dark
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
                // Simulated MapView
                Text(
                    text = "Map View",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Center)
                )

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

            // Draggable bottom sheet
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset {
                        IntOffset(0, bottomSheetOffset.roundToInt())
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val newOffset = bottomSheetOffset + change.position.y
                            bottomSheetOffset = newOffset.coerceIn(bottomSheetMaxOffsetPx, bottomSheetDefaultOffsetPx)
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
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.32f),
                                shape = RoundedCornerShape(2.dp)
                            )
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Profile section
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
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

                    // Title
                    Text(
                        text = runDetails.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Centered Stats
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
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    StatItemCentered(
                        label = "Moving Time",
                        value = runDetails.movingTime,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        textColor = textColor,
                        labelColor = secondaryTextColor
                    )
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

val sampleRunDetails = RunDetails(
    userName = "youcef",
    userImage = "https://pbs.twimg.com/profile_images/1918646624034721792/ub7IwdMh_400x400.jpg",
    date = "May 30, 2025 at 4:11 PM",
    location = "Chelghoum Laid, Mila",
    title = "Afternoon Run",
    distance = "14.04 km",
    movingTime = "1:30:58",
    avgPace = "6:29 /km"
)

@Preview(showBackground = true)
@Composable
fun StatsDetailsScreenPreview() {
    StatsDetailsScreen()
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StatsDetailsScreenDarkPreview() {
    StatsDetailsScreen()
}
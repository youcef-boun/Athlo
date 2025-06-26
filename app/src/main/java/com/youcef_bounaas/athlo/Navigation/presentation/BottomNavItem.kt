package com.youcef_bounaas.athlo.Navigation.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DataThresholding
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem(
        route = NavDestination.Home.route,
        title = "Home",
        icon = Icons.Default.Warehouse
    )

    data object Maps : BottomNavItem(
        route = NavDestination.Maps.route,
        title = "Maps",
        icon = Icons.Default.Map
    )

    data object Record : BottomNavItem(
        route = NavDestination.Record.route,
        title = "Record",
        icon = Icons.Default.TrackChanges
    )
    data object Stats: BottomNavItem(
        route = NavDestination.Stats.route,
        title = "Stats",
        icon = Icons.Default.DataThresholding
    )

    companion object {
        fun getAllItems() = listOf(Home, Maps, Record, Stats)
    }
}
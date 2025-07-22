package com.youcef_bounaas.athlo.Navigation.presentation

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.youcef_bounaas.athlo.ui.theme.AthloGreen

@Composable
fun AppBottomBar(
    navController: NavHostController
) {
    Log.d("AppBottomBar", "THIS IS THE REAL BOTTOM BAR")
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF)
    val selectedIconColor = AthloGreen
    val unselectedIconColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)

    NavigationBar(
        containerColor = backgroundColor,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        BottomNavItem.getAllItems().forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedIconColor,
                    selectedTextColor = selectedIconColor,
                    unselectedIconColor = unselectedIconColor,
                    unselectedTextColor = unselectedIconColor
                )
            )
        }
    }
}
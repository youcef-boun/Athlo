package com.youcef_bounaas.athlo.Navigation.presentation

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.youcef_bounaas.athlo.Authentication.presentation.AuthScreen
import com.youcef_bounaas.athlo.Authentication.presentation.ConfirmEmailScreen
import com.youcef_bounaas.athlo.Authentication.presentation.LoginScreen
import com.youcef_bounaas.athlo.Authentication.presentation.SignupScreen
import com.youcef_bounaas.athlo.Home.presentation.HomeScreen
import com.youcef_bounaas.athlo.Home.presentation.UserInfoScreen
import com.youcef_bounaas.athlo.Maps.presentation.MapsScreen
import com.youcef_bounaas.athlo.Record.presentation.RecordScreen
import com.youcef_bounaas.athlo.Record.presentation.StatsScreen
import com.youcef_bounaas.athlo.Stats.presentation.StatsDetailsScreen
import io.github.jan.supabase.SupabaseClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.youcef_bounaas.athlo.ui.theme.AthloGreen

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    onSignOut: () -> Unit = {},
    supabase: SupabaseClient,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            val isDark = isSystemInDarkTheme()
            val backgroundColor = if (isDark) Color(0xFF21211F) else Color(0xFFFFFFFF)
            val selectedIconColor = AthloGreen
            val unselectedIconColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)
            val selectedIndicatorColor = if (isDark) Color(0xFF000000) else  Color(0xFFF2F2F0)

            // exclude a screen from
            if (currentRoute != NavDestination.Auth.route &&
                currentRoute != NavDestination.SignUp.route &&
                currentRoute != NavDestination.Login.route &&
                currentRoute != NavDestination.UserInfo.route &&
                currentRoute != NavDestination.ConfirmEmail.route &&
                currentRoute != NavDestination.StatsDetails.route
            )
            {
                NavigationBar(
                    containerColor = backgroundColor,
                    tonalElevation = 0.dp
                ) {
                    BottomNavItem.getAllItems().forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = {
                                Text(item.title, fontWeight = FontWeight.Bold) },
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
                                selectedTextColor = unselectedIconColor,
                                unselectedIconColor = unselectedIconColor,
                                unselectedTextColor = unselectedIconColor,
                                indicatorColor = selectedIndicatorColor
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.padding(paddingValues)
        ) {

            // Auth Screens
            composable(NavDestination.Auth.route) {
                AuthScreen(navController)
            }
            composable(NavDestination.SignUp.route) {
                SignupScreen(navController)
            }
            composable(NavDestination.Login.route) {
                LoginScreen(navController)
            }

            composable(NavDestination.UserInfo.route) {
                UserInfoScreen(navController)
            }

            composable(NavDestination.ConfirmEmail.route) {
                ConfirmEmailScreen(navController)
            }

            composable(NavDestination.Home.route) {
                HomeScreen(
                    onSignOut = onSignOut,
                    supabase = supabase
                )
            }

            composable(NavDestination.Maps.route) {
                MapsScreen()
            }

            composable(NavDestination.Record.route) {
                RecordScreen()
            }

            composable(NavDestination.Stats.route) {
                StatsScreen(navController)
            }

            composable(
                route = "${NavDestination.StatsDetails.route}/{runId}",
                arguments = listOf(navArgument("runId") { type = NavType.StringType })
            ) { backStackEntry ->
                val runId = backStackEntry.arguments?.getString("runId")
                StatsDetailsScreen(
                    runId = runId,
                    onBackClick = { navController.popBackStack() }

                )
            }



        }
    }
}
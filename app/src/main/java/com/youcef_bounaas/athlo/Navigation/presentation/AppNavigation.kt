package com.youcef_bounaas.athlo.Navigation.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.youcef_bounaas.athlo.Authentication.presentation.AuthScreen
import com.youcef_bounaas.athlo.Authentication.presentation.ConfirmEmailScreen
import com.youcef_bounaas.athlo.Authentication.presentation.LoginScreen
import com.youcef_bounaas.athlo.Authentication.presentation.SignupScreen
import com.youcef_bounaas.athlo.Home.presentation.HomeScreen
import com.youcef_bounaas.athlo.Home.presentation.UserInfoScreen
import com.youcef_bounaas.athlo.Maps.presentation.MapsScreen
import com.youcef_bounaas.athlo.Record.presentation.RecordScreen
import com.youcef_bounaas.athlo.Record.presentation.StatsScreen

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // exclude a screen from
            if (currentRoute != NavDestination.Auth.route && 
                currentRoute != NavDestination.SignUp.route && 
                currentRoute != NavDestination.Login.route &&
                currentRoute != NavDestination.UserInfo.route &&
                currentRoute != NavDestination.ConfirmEmail.route
                )
                       {
                NavigationBar {
                    BottomNavItem.getAllItems().forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
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
                HomeScreen()
            }

            composable(NavDestination.Maps.route) {
                MapsScreen()
            }

            composable(NavDestination.Record.route) {
                RecordScreen()
            }

            composable(NavDestination.Stats.route) {
                StatsScreen()
            }
        }
    }
}
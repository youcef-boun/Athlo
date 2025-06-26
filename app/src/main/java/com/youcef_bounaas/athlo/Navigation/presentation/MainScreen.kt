package com.youcef_bounaas.athlo.Navigation.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val bottomBarState = remember { BottomBarState() }

    // Hide bottom bar on certain screens
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            when (destination.route) {

                    // NEW: Hide bottom bar on authentication screens
                NavDestination.Auth.route,
                NavDestination.SignUp.route,
                NavDestination.Login.route,
                NavDestination.UserInfo.route
                    -> bottomBarState.hide()

                else -> bottomBarState.show()
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    Scaffold(
        bottomBar = {
            if (bottomBarState.visible) {
                AppBottomBar(navController = navController)
            }
        }
    ) { paddingValues ->
        AppNavigation(
            navController = navController,


          //  startDestination = NavDestination.Record.route,
             startDestination = NavDestination.Auth.route,
           // startDestination = NavDestination.Home.route,

            modifier = Modifier.padding(paddingValues)
        )
    }
}

package com.youcef_bounaas.athlo

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.youcef_bounaas.athlo.Navigation.presentation.AppNavigation
import com.youcef_bounaas.athlo.Navigation.presentation.NavDestination

import com.youcef_bounaas.athlo.ui.theme.AthloTheme
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import org.koin.compose.getKoin
import android.util.Log


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContent {
            val supabase = getKoin().get<SupabaseClient>() // or however you're injecting

            val currentSession = supabase.auth.currentSessionOrNull()

            Log.d("SessionCheck", "Session: $currentSession")

            val startDestination = if (currentSession != null) {
                NavDestination.Home.route
            } else {
                NavDestination.Auth.route
            }



            val navController = rememberNavController()
            AthloTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        navController = navController,
                        startDestination =
                            startDestination
                                //    NavDestination.Record.route
                                   // NavDestination.ConfirmEmail.route

                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AthloTheme {
        val navController = rememberNavController()
        AppNavigation(
            navController = navController,
            startDestination = NavDestination.Auth.route
        )
    }
}
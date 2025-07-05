package com.youcef_bounaas.athlo

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.youcef_bounaas.athlo.Navigation.presentation.AppNavigation
import com.youcef_bounaas.athlo.Navigation.presentation.NavDestination
import com.youcef_bounaas.athlo.ui.theme.AthloTheme
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.getKoin
import android.util.Log
import io.github.jan.supabase.auth.status.SessionStatus

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContent {
            val supabase = getKoin().get<SupabaseClient>()
            val navController = rememberNavController()
            var isUserAuthenticated by remember { mutableStateOf(false) }
            var isCheckingAuth by remember { mutableStateOf(true) }
            val coroutineScope = rememberCoroutineScope()

            // Observe auth state changes
            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    // Initial check
                    val initialSession = try {
                        supabase.auth.currentSessionOrNull()
                    } catch (e: Exception) {
                        Log.e("AuthState", "Error getting initial session", e)
                        null
                    }
                    
                    isUserAuthenticated = initialSession != null
                    isCheckingAuth = false
                    Log.d("AuthState", "Initial session: $initialSession, isAuthenticated: $isUserAuthenticated")
                    
                    // Import the session status classes at the top of the file

                    
                    // Observe session changes
                    supabase.auth.sessionStatus.collect { status ->
                        val newAuthState = when (status) {
                            is SessionStatus.Authenticated -> {
                                Log.d("AuthState", "User is authenticated")
                                true
                            }
                            is SessionStatus.Initializing -> {
                                Log.d("AuthState", "Session initializing")
                                isUserAuthenticated
                            }
                            is SessionStatus.NotAuthenticated -> {
                                Log.d("AuthState", "User is not authenticated")
                                false
                            }
                            is SessionStatus.RefreshFailure -> {
                                Log.d("AuthState", "Session refresh failed")
                                false
                            }
                        }
                        
                        if (isUserAuthenticated != newAuthState) {
                            isUserAuthenticated = newAuthState
                            Log.d("AuthState", "Auth state changed to: $isUserAuthenticated")
                        }
                    }
                }
            }

            AthloTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Show loading indicator while checking auth state
                    if (isCheckingAuth) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val startDestination = if (isUserAuthenticated) {
                            NavDestination.Home.route
                        } else {
                            NavDestination.Auth.route
                        }

                        AppNavigation(
                            navController = navController,
                            startDestination =
                               startDestination,
                           // NavDestination.StatsDetails.route,
                            onSignOut = {
                                isUserAuthenticated = false
                                // Clear the back stack and navigate to Auth screen
                                navController.navigate(NavDestination.Auth.route) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            supabase = supabase
                        )
                    }
                }
            }
        }
    }
}


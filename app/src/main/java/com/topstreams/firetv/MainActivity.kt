package com.topstreams.firetv

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Log the start of main for debugging
        Log.d(TAG, "beginning of main")
        
        // Set fullscreen flags
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Create settings manager
        val settingsManager = SettingsManager(applicationContext)
        
        setContent {
            // Collect theme preference from settings
            val isDarkTheme by settingsManager.isDarkTheme.collectAsState(initial = true)
            
            // Apply theme and render app content
            PorkStreamsTheme(darkTheme = isDarkTheme) {
                AppContainer(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = {
                        lifecycleScope.launch {
                            settingsManager.toggleTheme()
                        }
                    }
                )
            }
        }
    }
}

sealed class AppScreen {
    object GameSelector : AppScreen()
    object TeamList : AppScreen()
    data class StreamView(val streamUrl: String) : AppScreen()
}

@Composable
fun AppContainer(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    // Current screen state
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.GameSelector) }
    
    when (val screen = currentScreen) {
        is AppScreen.GameSelector -> {
            // Show main screen with game selector
            MainScreen(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                useWebViewer = true,
                onGameSelected = { game ->
                    currentScreen = AppScreen.StreamView(game.getStreamUrl())
                },
                onTeamSelected = { team ->
                    currentScreen = AppScreen.StreamView(team.streamUrl)
                },
                onViewAllTeams = {
                    currentScreen = AppScreen.TeamList
                }
            )
        }
        
        is AppScreen.TeamList -> {
            // Show the NBA team list screen
            TeamListScreen(
                onTeamSelected = { team ->
                    currentScreen = AppScreen.StreamView(team.streamUrl)
                },
                onBackPressed = {
                    currentScreen = AppScreen.GameSelector
                }
            )
        }
        
        is AppScreen.StreamView -> {
            // Show WebView with the stream URL
            WebViewScreen(
                streamUrl = screen.streamUrl,
                onBackToSelection = { 
                    currentScreen = AppScreen.GameSelector
                }
            )
        }
    }
}
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
            val useWebViewer by settingsManager.useWebViewer.collectAsState(initial = false)
            
            // Apply theme and render app content
            PorkStreamsTheme(darkTheme = isDarkTheme) {
                AppContainer(
                    isDarkTheme = isDarkTheme,
                    useWebViewer = useWebViewer,
                    onToggleTheme = {
                        lifecycleScope.launch {
                            settingsManager.toggleTheme()
                        }
                    },
                    onToggleWebViewer = {
                        lifecycleScope.launch {
                            settingsManager.toggleWebViewer()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AppContainer(
    isDarkTheme: Boolean,
    useWebViewer: Boolean,
    onToggleTheme: () -> Unit,
    onToggleWebViewer: () -> Unit
) {
    val (selectedGame, setSelectedGame) = remember { mutableStateOf<Game?>(null) }
    
    if (selectedGame == null) {
        // Show main screen with game selector
        MainScreen(
            isDarkTheme = isDarkTheme,
            useWebViewer = useWebViewer,
            onToggleTheme = onToggleTheme,
            onToggleWebViewer = onToggleWebViewer,
            onGameSelected = { game ->
                setSelectedGame(game)
            }
        )
    } else {
        // Show the appropriate player screen based on user preference
        if (useWebViewer) {
            print(selectedGame)
            // Use WebView to load the video on TopStreams
            WebViewScreen(
                streamUrl = "https://topstreams.info/nba/${selectedGame.homeTeam.lowercase()}",
                onBackToSelection = { setSelectedGame(null) }
            )
        } else {
            // Show the regular video player
            VideoPlayerScreen(
                game = selectedGame,
                onBackToSelection = { setSelectedGame(null) }
            )
        }
    }
}
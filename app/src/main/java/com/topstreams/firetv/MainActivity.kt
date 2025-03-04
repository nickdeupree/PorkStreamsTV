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

@Composable
fun AppContainer(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val (selectedGame, setSelectedGame) = remember { mutableStateOf<Game?>(null) }
    
    if (selectedGame == null) {
        // Show main screen with game selector
        MainScreen(
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            useWebViewer = true,
            onGameSelected = { game ->
                setSelectedGame(game)
            }
        )
    } else {
        // Always use WebView to load the video on TopStreams
        WebViewScreen(
            streamUrl = selectedGame.getStreamUrl(),
            onBackToSelection = { setSelectedGame(null) }
        )
    }
}
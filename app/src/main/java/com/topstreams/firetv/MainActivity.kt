package com.topstreams.firetv

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

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
        
        setContent {
            AppContainer()
        }
    }
}

@Composable
fun AppContainer() {
    val (selectedGame, setSelectedGame) = remember { mutableStateOf<Game?>(null) }
    
    if (selectedGame == null) {
        // Show game selector screen
        GameSelectorScreen { game ->
            // When a game is selected, update the state
            setSelectedGame(game)
        }
    } else {
        // Show WebView with the selected game
        WebViewScreen(
            streamUrl = selectedGame.getStreamUrl(),
            onBackToSelection = { setSelectedGame(null) }
        )
    }
}
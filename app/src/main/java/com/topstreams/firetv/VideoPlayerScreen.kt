package com.topstreams.firetv

import android.net.Uri
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Pattern

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    game: Game,
    onBackToSelection: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var streamUrl by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // State to track stream extraction attempts
    var attemptCount by remember { mutableStateOf(0) }
    
    BackHandler {
        onBackToSelection()
    }

    // Extract the streaming URL
    LaunchedEffect(game, attemptCount) {
        isLoading = true
        errorMessage = null
        
        try {
            // Get the stream URL from the TopStreams page
            val directUrl = extractDirectStreamUrl(game.getStreamUrl())
            if (directUrl != null) {
                streamUrl = directUrl
                Log.d("VideoPlayerScreen", "Found stream URL: $directUrl")
            } else {
                if (attemptCount < 3) {
                    // Retry after a delay
                    Log.d("VideoPlayerScreen", "Retrying stream extraction, attempt ${attemptCount + 1}")
                    delay(2000) // Wait 2 seconds before retrying
                    attemptCount++
                } else {
                    errorMessage = "Unable to find stream. Game might not be live yet."
                    Log.e("VideoPlayerScreen", "Failed to extract stream URL after $attemptCount attempts")
                }
            }
        } catch (e: Exception) {
            errorMessage = "Error loading stream: ${e.message}"
            Log.e("VideoPlayerScreen", "Error extracting stream URL", e)
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            isLoading -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading stream...",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
            errorMessage != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage!!,
                        color = Color.White,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            streamUrl != null -> {
                // Setup ExoPlayer with the extracted stream URL
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            val player = ExoPlayer.Builder(ctx).build()
                            this.player = player
                            
                            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                            
                            // Configure the data source factory
                            val dataSourceFactory = DefaultHttpDataSource.Factory()
                                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                                .setAllowCrossProtocolRedirects(true)
                            
                            // Create HLS media source
                            val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(MediaItem.fromUri(Uri.parse(streamUrl)))
                            
                            player.setMediaSource(hlsMediaSource)
                            player.prepare()
                            player.playWhenReady = true
                            player.addListener(object : Player.Listener {
                                override fun onPlaybackStateChanged(state: Int) {
                                    when (state) {
                                        Player.STATE_BUFFERING -> {
                                            Log.d("VideoPlayerScreen", "Buffering video")
                                        }
                                        Player.STATE_READY -> {
                                            Log.d("VideoPlayerScreen", "Video ready to play")
                                        }
                                        Player.STATE_ENDED -> {
                                            Log.d("VideoPlayerScreen", "Video ended")
                                        }
                                        Player.STATE_IDLE -> {
                                            Log.d("VideoPlayerScreen", "Player idle")
                                        }
                                    }
                                }
                                
                                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                                    Log.e("VideoPlayerScreen", "Player error", error)
                                    errorMessage = "Playback error: ${error.message}"
                                }
                            })
                        }
                    },
                    update = { playerView ->
                        // Update logic here if needed
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private suspend fun extractDirectStreamUrl(topstreamsUrl: String): String? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    
    try {
        val request = Request.Builder()
            .url(topstreamsUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .build()
        
        val response = client.newCall(request).execute()
        val html = response.body?.string() ?: return@withContext null

        // Look for HLS stream URLs in the HTML content
        val m3u8Pattern = Pattern.compile(
            "https?:\\/\\/[\\w.-]+\\.akamaized\\.net\\/[\\w\\/.\\-_]+\\.m3u8[\\w\\/.\\-_&=?%]*"
        )
        
        val matcher = m3u8Pattern.matcher(html)
        if (matcher.find()) {
            return@withContext matcher.group()
        }
        
        // Try alternative pattern if the first one fails
        val alternativePattern = Pattern.compile(
            "globalurl\\s*=\\s*['\"]?(https?:\\/\\/[^'\"\\s]+\\.m3u8[^'\"\\s]*)"
        )
        
        val altMatcher = alternativePattern.matcher(html)
        if (altMatcher.find()) {
            return@withContext altMatcher.group(1)
        }
        
        // If we can't find a direct URL, return null
        return@withContext null
    } catch (e: Exception) {
        Log.e("VideoPlayerScreen", "Error extracting stream URL", e)
        return@withContext null
    }
}